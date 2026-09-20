package com.maybank.appsense.core.deploy;

import com.maybank.appsense.core.config.DeployConstant;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.DeploymentMarkerRecord;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.parse.ParseService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeploymentService {

    private final List<Pattern> markerPatterns;
    private final Pattern artifactPattern;
    private final ParseService parseService;
    private DeploymentMarkerRecord lastMarker;

    public DeploymentService(List<String> customMarkers, ParseService parseService) throws ConfigException {
        this.markerPatterns = MarkerPatternUtil.compile(customMarkers);
        this.artifactPattern = MarkerPatternUtil.artifactPattern();
        this.parseService = parseService;
    }

    /**
     * Checks one live line. Repeated markers for the same artifact within the dedup window collapse
     * into the first one.
     */
    public synchronized Optional<DeploymentMarkerRecord> inspect(String rawLine) {
        Optional<DeploymentMarkerRecord> marker = match(rawLine);
        if (marker.isEmpty() || isDuplicate(marker.get())) {
            return Optional.empty();
        }
        lastMarker = marker.get();
        return marker;
    }

    /** Scans lines newest first and returns the latest deployment marker, if any. */
    public Optional<DeploymentMarkerRecord> findLast(List<String> rawLines) {
        for (int index = rawLines.size() - 1; index >= 0; index--) {
            Optional<DeploymentMarkerRecord> marker = match(rawLines.get(index));
            if (marker.isPresent()) {
                return marker;
            }
        }
        return Optional.empty();
    }

    public DeploymentMarkerRecord manualMarker() {
        return new DeploymentMarkerRecord(Instant.now(), MessageUtil.get(MessageKey.INFO_DEPLOY_MANUAL));
    }

    private Optional<DeploymentMarkerRecord> match(String rawLine) {
        for (Pattern pattern : markerPatterns) {
            Matcher matcher = pattern.matcher(rawLine);
            if (matcher.find()) {
                return Optional.of(new DeploymentMarkerRecord(timestampOf(rawLine), artifactName(rawLine, matcher)));
            }
        }
        return Optional.empty();
    }

    private String artifactName(String rawLine, Matcher marker) {
        Matcher artifact = artifactPattern.matcher(rawLine);
        if (artifact.find()) {
            String quoted = artifact.group(DeployConstant.ARTIFACT_GROUP_QUOTED);
            return quoted != null ? quoted : artifact.group(DeployConstant.ARTIFACT_GROUP_PLAIN);
        }
        return marker.group();
    }

    private Instant timestampOf(String rawLine) {
        return parseService.parseHeader(rawLine).map(LogLineRecord::timestamp).orElseGet(Instant::now);
    }

    private boolean isDuplicate(DeploymentMarkerRecord candidate) {
        if (lastMarker == null || !lastMarker.warName().equals(candidate.warName())) {
            return false;
        }
        Duration gap = Duration.between(lastMarker.timestamp(), candidate.timestamp()).abs();
        return gap.getSeconds() < DeployConstant.DEDUP_SECONDS;
    }
}
