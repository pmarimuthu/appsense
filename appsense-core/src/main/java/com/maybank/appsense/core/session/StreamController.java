package com.maybank.appsense.core.session;

import com.maybank.appsense.core.config.AppConstant;
import com.maybank.appsense.core.config.SshConstant;
import com.maybank.appsense.core.deploy.DeploymentService;
import com.maybank.appsense.core.event.EventBus;
import com.maybank.appsense.core.event.GapMarkerEvent;
import com.maybank.appsense.core.event.NoticeEvent;
import com.maybank.appsense.core.exception.AppSenseException;
import com.maybank.appsense.core.exception.ConnectionLostException;
import com.maybank.appsense.core.exception.LogPathException;
import com.maybank.appsense.core.model.CommandResultRecord;
import com.maybank.appsense.core.model.FileStatRecord;
import com.maybank.appsense.core.model.GapMarkerRecord;
import com.maybank.appsense.core.model.LogEntryBean;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.parse.ParseService;
import com.maybank.appsense.core.ssh.ShellQuoteUtil;
import com.maybank.appsense.core.ssh.SshService;
import com.maybank.appsense.core.tail.RotationDetector;
import com.maybank.appsense.core.tail.TailService;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Runs the viewed-log tail and the deployment watcher, each on its own virtual thread. */
final class StreamController {

    private static final Logger log = LoggerFactory.getLogger(StreamController.class);
    private static final Runnable NO_ROTATION_ACTION = () -> { };

    private final SshService sshService;
    private final SessionPipeline pipeline;
    private final EventBus eventBus;
    private final RotationDetector rotationDetector;
    private final Runnable onConnectionLost;
    private ProfileBean profile;
    private LogEntryBean viewedLog;
    private DeploymentService deploymentService;
    private TailService viewTail;
    private TailService watchTail;

    StreamController(SshService sshService, SessionPipeline pipeline, EventBus eventBus,
                     RotationDetector rotationDetector, Runnable onConnectionLost) {
        this.sshService = sshService;
        this.pipeline = pipeline;
        this.eventBus = eventBus;
        this.rotationDetector = rotationDetector;
        this.onConnectionLost = onConnectionLost;
    }

    void start(ProfileBean target, LogEntryBean logEntry) throws AppSenseException {
        profile = target;
        viewedLog = logEntry;
        deploymentService = new DeploymentService(
                profile.getDeploymentMarkers(), new ParseService(profile.getServerType()));
        pipeline.configure(profile.getServerType());
        startViewedLog();
        startWatcher();
    }

    void switchLog(LogEntryBean next) throws AppSenseException {
        viewTail.stop();
        viewedLog = next;
        pipeline.reset();
        startViewedLog();
    }

    void resume() throws AppSenseException {
        if (viewTail.resume()) {
            emitGap();
        }
        if (watchTail != null) {
            watchTail.resume();
        }
        launchViewed();
        launchWatcher();
    }

    void stop() {
        if (viewTail != null) {
            viewTail.stop();
        }
        if (watchTail != null) {
            watchTail.stop();
        }
    }

    void markDeployment() {
        pipeline.offerItem(deploymentService.manualMarker());
    }

    private void startViewedLog() throws AppSenseException {
        viewTail = new TailService(sshService, rotationDetector, viewedLog.getPath());
        FileStatRecord stat = viewTail.stat();
        scanHistoricMarker();
        long start = Math.max(stat.size() - SshConstant.INITIAL_BACKLOG_BYTES, 0);
        viewTail.begin(start, start > 0, stat);
        launchViewed();
    }

    private void startWatcher() throws AppSenseException {
        Optional<LogEntryBean> source = profile.findDeploymentSource();
        if (source.isEmpty()) {
            watchTail = null;
            return;
        }
        watchTail = new TailService(sshService, rotationDetector, source.get().getPath());
        FileStatRecord stat = watchTail.stat();
        watchTail.begin(stat.size(), false, stat);
        launchWatcher();
    }

    private void scanHistoricMarker() throws AppSenseException {
        Optional<LogEntryBean> source = profile.findDeploymentSource();
        if (source.isEmpty()) {
            return;
        }
        String path = source.get().getPath();
        CommandResultRecord result = sshService.execute(SshConstant.TAIL_LAST_LINES_COMMAND.formatted(
                SshConstant.DEPLOY_SCAN_LINES, ShellQuoteUtil.quote(path)));
        if (!result.isSuccess()) {
            throw new LogPathException(path);
        }
        deploymentService.findLast(result.stdout().lines().toList()).ifPresent(pipeline::offerHistoricMarker);
    }

    private void launchViewed() {
        launch(AppConstant.TAIL_THREAD, viewTail, pipeline::offerLine, this::emitGap);
    }

    private void launchWatcher() {
        if (watchTail != null) {
            launch(AppConstant.WATCH_THREAD, watchTail, this::inspectForDeployment, NO_ROTATION_ACTION);
        }
    }

    private void launch(String threadName, TailService tail, Consumer<String> sink, Runnable onRotation) {
        Thread.ofVirtual().name(threadName).start(() -> follow(tail, sink, onRotation));
    }

    private void follow(TailService tail, Consumer<String> sink, Runnable onRotation) {
        try {
            tail.follow(sink, onRotation);
        } catch (ConnectionLostException ex) {
            onConnectionLost.run();
        } catch (AppSenseException ex) {
            log.error("Tail stopped on {}", sshService.getHost(), ex);
            eventBus.publish(NoticeEvent.error(ex));
        }
    }

    private void inspectForDeployment(String rawLine) {
        deploymentService.inspect(rawLine).ifPresent(pipeline::offerItem);
    }

    private void emitGap() {
        GapMarkerRecord gap = new GapMarkerRecord(Instant.now());
        pipeline.offerItem(gap);
        eventBus.publish(new GapMarkerEvent(gap));
    }
}
