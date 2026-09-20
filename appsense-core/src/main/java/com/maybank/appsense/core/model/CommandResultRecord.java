package com.maybank.appsense.core.model;

public record CommandResultRecord(int exitStatus, String stdout, String stderr) {

    public boolean isSuccess() {
        return exitStatus == 0;
    }
}
