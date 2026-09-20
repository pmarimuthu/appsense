package com.maybank.appsense.core.model;

import com.maybank.appsense.core.config.SshConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileBean {

    private String name;
    private String host;
    private int port = SshConstant.DEFAULT_PORT;
    private String username;
    private ServerType serverType = ServerType.JBOSS;
    private List<LogEntryBean> logs = new ArrayList<>();
    private List<String> appPackages = new ArrayList<>();
    private List<String> deploymentMarkers = new ArrayList<>();
    private String hostKeyFingerprint;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public ServerType getServerType() { return serverType; }
    public void setServerType(ServerType serverType) { this.serverType = serverType; }

    public List<LogEntryBean> getLogs() { return logs; }
    public void setLogs(List<LogEntryBean> logs) { this.logs = logs == null ? new ArrayList<>() : logs; }

    public List<String> getAppPackages() { return appPackages; }
    public void setAppPackages(List<String> appPackages) {
        this.appPackages = appPackages == null ? new ArrayList<>() : appPackages;
    }

    public List<String> getDeploymentMarkers() { return deploymentMarkers; }
    public void setDeploymentMarkers(List<String> deploymentMarkers) {
        this.deploymentMarkers = deploymentMarkers == null ? new ArrayList<>() : deploymentMarkers;
    }

    public String getHostKeyFingerprint() { return hostKeyFingerprint; }
    public void setHostKeyFingerprint(String hostKeyFingerprint) { this.hostKeyFingerprint = hostKeyFingerprint; }

    public Optional<LogEntryBean> findDeploymentSource() {
        return logs.stream().filter(LogEntryBean::isDeploymentSource).findFirst();
    }

    public Optional<LogEntryBean> findLog(String label) {
        return logs.stream().filter(entry -> entry.getLabel().equals(label)).findFirst();
    }
}
