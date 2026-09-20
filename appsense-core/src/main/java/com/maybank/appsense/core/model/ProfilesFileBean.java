package com.maybank.appsense.core.model;

import com.maybank.appsense.core.config.AppConstant;
import java.util.ArrayList;
import java.util.List;

public class ProfilesFileBean {

    private int version = AppConstant.JSON_SCHEMA_VERSION;
    private List<ProfileBean> profiles = new ArrayList<>();

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public List<ProfileBean> getProfiles() { return profiles; }
    public void setProfiles(List<ProfileBean> profiles) {
        this.profiles = profiles == null ? new ArrayList<>() : profiles;
    }
}
