package com.software.beans;


/**
 * Created by 陆正威 on 2018/7/14.
 */
public class BukkitMod extends AbstractMod {

    private String website;
    private static final long serialVersionUID = 6864451204564729559L;

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public enum PLUGINYML{
        main,
        name,
        version,
        authors,
        website
    };

    @Override
    public String toString() {
        return "BukkitMod{" +
                "website='" + website + '\'' + super.toString();
    }
}
