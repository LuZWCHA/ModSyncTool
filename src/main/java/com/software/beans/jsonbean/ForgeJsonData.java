package com.software.beans.jsonbean;

import java.util.List;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public class ForgeJsonData {

    /**
     * modid : neat
     * name : Neat
     * description : Minimalistic Functional Unit Plates for the modern Minecrafter
     * VERSION : 1.4-16
     * mcversion : 1.12.2
     * logoFile :
     * url : http://www.vazkii.us
     * updateUrl :
     * authorList : ["Vazkii"]
     * credits :
     * parent :
     * screenshots : []
     * dependencies : []
     */

    private String modid;
    private String name;
    private String description;
    private String version;
    private String mcversion;
    private String logoFile;
    private String url;
    private String updateUrl;
    private String credits;
    private String parent;
    private List<String> authorList;
    private List<String> screenshots;
    private List<String> dependencies;

    public String getModid() {
        return modid;
    }

    public void setModid(String modid) {
        this.modid = modid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMcversion() {
        return mcversion;
    }

    public void setMcversion(String mcversion) {
        this.mcversion = mcversion;
    }

    public String getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(String logoFile) {
        this.logoFile = logoFile;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<String> getAuthorList() {
        return authorList;
    }

    public void setAuthorList(List<String> authorList) {
        this.authorList = authorList;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<String> screenshots) {
        this.screenshots = screenshots;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return "ForgeJsonData{" +
                "modid='" + modid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", VERSION='" + version + '\'' +
                ", mcversion='" + mcversion + '\'' +
                ", logoFile='" + logoFile + '\'' +
                ", url='" + url + '\'' +
                ", updateUrl='" + updateUrl + '\'' +
                ", credits='" + credits + '\'' +
                ", parent='" + parent + '\'' +
                ", authorList=" + authorList +
                ", screenshots=" + screenshots +
                ", dependencies=" + dependencies +
                '}';
    }
}
