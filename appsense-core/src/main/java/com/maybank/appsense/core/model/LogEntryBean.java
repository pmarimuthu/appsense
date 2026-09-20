package com.maybank.appsense.core.model;

public class LogEntryBean {

    private String label;
    private String path;
    private boolean deploymentSource;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public boolean isDeploymentSource() { return deploymentSource; }
    public void setDeploymentSource(boolean deploymentSource) { this.deploymentSource = deploymentSource; }
}
