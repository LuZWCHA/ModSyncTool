package com.software.beans;

/**
 * Created by 陆正威 on 2018/7/13.
 */
public class TweakMod extends AbstractMod {

    private static final long serialVersionUID = 3833315015281697635L;
    private String tweakClass;
    public enum MFFILE{
        TweakClass,
        TweakAuthor,
        TweakName,
        TweakVersion
    };

    public String getTweakClass() {
        return tweakClass;
    }

    public void setTweakClass(String tweakClass) {
        this.tweakClass = tweakClass;
    }

    @Override
    public String toString() {
        return "TweakMod{" +
                "tweakClass='" + tweakClass + '\'' + super.toString() ;
    }
}
