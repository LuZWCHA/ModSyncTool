package com.software.beans.jsonbean;

public class VersionJsonData {
    private String version;
    private String versionDescription;

    public static VersionJsonData createEmpty(){
        VersionJsonData empty = new VersionJsonData();
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
