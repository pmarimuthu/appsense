package com.maybank.appsense.core.tail;

import com.maybank.appsense.core.config.SshConstant;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

final class LineSplitOutputStream extends OutputStream {

    private final Consumer<String> sink;
    private final ByteArrayOutputStream pending = new ByteArrayOutputStream();
    private long offset;
    private boolean skippingPartialLine;

    LineSplitOutputStream(long startOffset, boolean skipFirstPartialLine, Consumer<String> sink) {
        this.offset = startOffset;
        this.skippingPartialLine = skipFirstPartialLine;
        this.sink = sink;
    }

    @Override
    public synchronized void write(int value) {
        consume((byte) value);
    }

    @Override
    public synchronized void write(byte[] data, int start, int length) {
        for (int index = start; index < start + length; index++) {
            consume(data[index]);
        }
    }

    synchronized long getOffset() {
        return offset;
    }

    synchronized void discount(String line) {
        offset -= line.getBytes(SshConstant.REMOTE_CHARSET).length + 1;
    }

    synchronized void restartAtZero() {
        pending.reset();
        offset = 0;
        skippingPartialLine = false;
    }

    private void consume(byte value) {
        if (value != SshConstant.LINE_FEED) {
            pending.write(value);
            return;
        }
        byte[] raw = pending.toByteArray();
        pending.reset();
        offset += raw.length + 1;
        if (skippingPartialLine) {
            skippingPartialLine = false;
            return;
        }
        sink.accept(decode(raw));
    }

    private String decode(byte[] raw) {
        int length = raw.length;
        if (length > 0 && raw[length - 1] == SshConstant.CARRIAGE_RETURN) {
            length--;
        }
        return new String(raw, 0, length, SshConstant.REMOTE_CHARSET);
    }
}
