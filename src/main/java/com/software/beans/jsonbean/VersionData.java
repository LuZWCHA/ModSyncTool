package com.software.beans.jsonbean;

public class VersionData {
    private String version;
    private String versionDescription;



    public static VersionData createEmpty(){
        VersionData empty = new VersionData();
        empty.version = null;
        return empty;
    }

    public boolean isEmpty(){
        return version == null || version.isEmpty();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionDescription() {
        return versionDescription;
    }

    public void setVersionDescription(String versionDescription) {
        this.versionDescription = versionDescription;
    }
}
