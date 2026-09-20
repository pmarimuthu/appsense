package com.maybank.appsense.core.tail;

import com.maybank.appsense.core.config.SshConstant;
import com.maybank.appsense.core.exception.AppSenseException;
import com.maybank.appsense.core.exception.ConnectionLostException;
import com.maybank.appsense.core.exception.LogPathException;
import com.maybank.appsense.core.model.CommandResultRecord;
import com.maybank.appsense.core.model.FileStatRecord;
import com.maybank.appsense.core.ssh.RemoteStream;
import com.maybank.appsense.core.ssh.ShellQuoteUtil;
import com.maybank.appsense.core.ssh.SshService;
import java.io.OutputStream;
import java.util.function.Consumer;

public class TailService {

    private final SshService sshService;
    private final RotationDetector rotationDetector;
    private final String path;
    private volatile long offset;
    private volatile boolean skipPartialLine;
    private volatile boolean stopped;
    private volatile boolean baselineStale;
    private volatile FileStatRecord baseline;
    private volatile LineSplitOutputStream lineOut;
    private volatile RemoteStream stream;

    public TailService(SshService sshService, RotationDetector rotationDetector, String path) {
        this.sshService = sshService;
        this.rotationDetector = rotationDetector;
        this.path = path;
    }

    public FileStatRecord stat() throws AppSenseException {
        CommandResultRecord result = sshService.execute(
                SshConstant.STAT_COMMAND.formatted(ShellQuoteUtil.quote(path)));
        String[] parts = result.stdout().trim().split(SshConstant.STAT_FIELD_SEPARATOR);
        if (!result.isSuccess() || parts.length < SshConstant.STAT_FIELD_COUNT) {
            throw new LogPathException(path);
        }
        try {
            return new FileStatRecord(Long.parseLong(parts[SshConstant.STAT_INODE_INDEX]),
                    Long.parseLong(parts[SshConstant.STAT_SIZE_INDEX]));
        } catch (NumberFormatException ex) {
            throw new LogPathException(path, ex);
        }
    }

    /** Sets where the next follow starts. A mid-line start drops the first partial line. */
    public void begin(long startOffset, boolean skipFirstPartialLine, FileStatRecord statAtStart) {
        this.offset = startOffset;
        this.skipPartialLine = skipFirstPartialLine;
        this.baseline = statAtStart;
        this.baselineStale = false;
        this.stopped = false;
        this.lineOut = null;
    }

    /**
     * Re-checks the file after a reconnect. Returns true when it was rotated, in which case the
     * next follow starts at offset zero.
     */
    public boolean resume() throws AppSenseException {
        FileStatRecord current = stat();
        long saved = getOffset();
        boolean rotated = baseline != null && (baselineStale
                ? current.size() < saved
                : rotationDetector.isRotated(baseline, current, saved));
        begin(rotated ? 0 : saved, false, current);
        return rotated;
    }

    /**
     * Follows the file until stopped, delivering each complete line to the sink. Blocks the calling
     * thread. Throws when the connection drops or the remote tail ends on its own.
     */
    public void follow(Consumer<String> lineSink, Runnable rotationListener) throws AppSenseException {
        LineSplitOutputStream out = new LineSplitOutputStream(
                offset, skipPartialLine, line -> route(line, lineSink, rotationListener));
        lineOut = out;
        String command = SshConstant.TAIL_FROM_OFFSET_COMMAND.formatted(offset + 1, ShellQuoteUtil.quote(path));
        try {
            stream = sshService.openStream(command, out, OutputStream.nullOutputStream());
            stream.awaitClosed();
        } finally {
            offset = out.getOffset();
        }
        if (stopped) {
            return;
        }
        if (!sshService.isConnected() || stream.getExitStatus() == null) {
            throw new ConnectionLostException(sshService.getHost(), null);
        }
        throw new LogPathException(path);
    }

    public void stop() {
        stopped = true;
        RemoteStream current = stream;
        if (current != null) {
            current.close();
        }
    }

    public long getOffset() {
        LineSplitOutputStream current = lineOut;
        return current == null ? offset : current.getOffset();
    }

    private void route(String line, Consumer<String> lineSink, Runnable rotationListener) {
        if (!line.startsWith(SshConstant.TAIL_NOTICE_PREFIX)) {
            lineSink.accept(line);
            return;
        }
        LineSplitOutputStream current = lineOut;
        current.discount(line);
        if (SshConstant.ROTATION_NOTICE_MARKERS.stream().anyMatch(line::contains)) {
            baselineStale = true;
            current.restartAtZero();
            rotationListener.run();
        }
    }
}
