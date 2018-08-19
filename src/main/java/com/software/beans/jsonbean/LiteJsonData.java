package com.software.beans.jsonbean;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public class LiteJsonData {

    /**
     * name : ChatBubbles
     * mcversion : 1.12.0
     * version : 1.0.1
     * revision : 1001
     * author : MamiyaOtaru
     * description : ChatBubbles displays what people say above their heads
     -100% client side operation
     -May or may not work depending on how the server formats chat.
     */

    private String name;
    private String mcversion;
    private String version;
    private String revision;
    private String author;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
