package com.software.beans.jsonbean;

import java.util.List;

/**
 * Created by 陆正威 on 2018/7/13.
 */
public class ForgeJsonData2 {

    /**
     * modListVersion : 2
     * modList : [{"modid":"lunatriuscore","name":"LunatriusCore","description":"A collection of utilities for Lunatrius' mods.","mcversion":"1.12","VERSION":"1.2.0.40","authorList":["Lunatrius"],"credits":"","dependants":[],"dependencies":["forge@[14.21.1.2387,)"],"parent":"","requiredMods":["forge@[14.21.1.2387,)"],"logoFile":"","screenshots":[],"updateUrl":"http://mc.lunatri.us/","url":"http://mc.lunatri.us/","useDependencyInformation":true}]
     */

    private int modListVersion;
    private List<ModListBean> modList;

    public int getModListVersion() {
        return modListVersion;
    }

    public void setModListVersion(int modListVersion) {
        this.modListVersion = modListVersion;
    }

    public List<ModListBean> getModList() {
        return modList;
    }

    public void setModList(List<ModListBean> modList) {
        this.modList = modList;
    }

    public static class ModListBean {
        /**
         * modid : lunatriuscore
         * name : LunatriusCore
         * description : A collection of utilities for Lunatrius' mods.
         * mcversion : 1.12
         * VERSION : 1.2.0.40
         * authorList : ["Lunatrius"]
         * credits :
         * dependants : []
         * dependencies : ["forge@[14.21.1.2387,)"]
         * parent :
         * requiredMods : ["forge@[14.21.1.2387,)"]
         * logoFile :
         * screenshots : []
         * updateUrl : http://mc.lunatri.us/
         * url : http://mc.lunatri.us/
         * useDependencyInformation : true
         */

        private String modid;
        private String name;
        private String description;
        private String mcversion;
        private String version;
        private String credits;
        private String parent;
        private String logoFile;
        private String updateUrl;
        private String url;
        private boolean useDependencyInformation;
        private List<String> authorList;
        private List<String> dependants;
        private List<String> dependencies;
        private List<String> requiredMods;
        private List<String> screenshots;

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

        public String getMcversion() {
            return mcversion;
        }

        public void setMcversion(String mcversion) {
            this.mcversion = mcversion;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
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

        public String getLogoFile() {
            return logoFile;
        }

        public void setLogoFile(String logoFile) {
            this.logoFile = logoFile;
        }

        public String getUpdateUrl() {
            return updateUrl;
        }

        public void setUpdateUrl(String updateUrl) {
            this.updateUrl = updateUrl;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isUseDependencyInformation() {
            return useDependencyInformation;
        }

        public void setUseDependencyInformation(boolean useDependencyInformation) {
            this.useDependencyInformation = useDependencyInformation;
        }

        public List<String> getAuthorList() {
            return authorList;
        }

        public void setAuthorList(List<String> authorList) {
            this.authorList = authorList;
        }

        public List<?> getDependants() {
            return dependants;
        }

        public void setDependants(List<String> dependants) {
            this.dependants = dependants;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
        }

        public List<String> getRequiredMods() {
            return requiredMods;
        }

        public void setRequiredMods(List<String> requiredMods) {
            this.requiredMods = requiredMods;
        }

        public List<?> getScreenshots() {
            return screenshots;
        }

        public void setScreenshots(List<String> screenshots) {
            this.screenshots = screenshots;
        }
    }
}
