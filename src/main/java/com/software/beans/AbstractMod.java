package com.software.beans;

import com.google.common.base.Strings;
import com.google.common.hash.HashCode;

import javax.annotation.Nonnull;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public abstract class AbstractMod extends TransMod {

    private HashCode fileMD5;
    private String filePath;//磁盘中的绝对路径

    protected AbstractMod(@Nonnull String id,@Nonnull String version, short mode){
        super(id.trim().toLowerCase(), Strings.isNullOrEmpty(version) ? Mod.MODE.UNKNOWN_VERSION : version, mode, false);
        check(version,mode);
    }

    AbstractMod(){
        super();
    }

    private void check(String version,short flag){
        if(Strings.isNullOrEmpty(version))
            setVersion(Mod.MODE.UNKNOWN_VERSION);
        if(flag > 8)
            setMode(Mod.MODE.UNKNOWN_MOD_MODE);
    }

    void check(){
        check(getVersion(),getMode());
    }

    //check the id only
    public boolean isSameAs(@Nonnull AbstractMod mod){
        return mod.getId().equals(getId());
    }

    //check id and version
    public boolean isTrulySame(@Nonnull AbstractMod mod){
        return mod.getId().equals(getId()) && !mod.getVersion().equals(Mod.MODE.UNKNOWN_VERSION) && mod.getVersion().equals(getVersion());
    }

    //check MD5 value
    boolean isFileDataSameAs(@Nonnull AbstractMod mod){
        return mod.fileMD5.equals(fileMD5);
    }

    public void setId(@Nonnull String id) {
        super.setId(id.trim().toLowerCase());
    }

    public void setVersion(String version) {
        super.setVersion(Strings.isNullOrEmpty(version) ? Mod.MODE.UNKNOWN_VERSION:version);
    }

    @Override
    public String toString() {
        return ", id='" + getId() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", mode=" + getMode() +'\''+
                ", filePath" + filePath + '\'' +
                '}';
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public HashCode getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(HashCode fileMD5) {
        this.fileMD5 = fileMD5;
    }
}
