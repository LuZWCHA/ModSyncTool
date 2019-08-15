package com.software.beans;

import java.io.Serializable;
import java.util.Objects;

import static com.software.beans.Mod.MODE.UNKNOWN_VERSION;

//information tran body between server and client
public class TransMod implements Serializable{
    private String id;
    private String version;
    private short mode;
    private boolean fromServer;

    public TransMod(String id, String version, short mode, boolean fromServer) {
        this.id = id;
        this.version = version;
        this.mode = mode;
        this.fromServer = fromServer;
    }

    public TransMod() {
        fromServer = false;
    }

    @Override
    public String toString() {
        return getId() + "-" + getVersion()+ "-" +  getModeDescribe(mode);
    }

    public String getSimpleDescribe(){
        return getId() + "-" + getVersion()+ "-" +  getModeDescribe(mode);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public short getMode() {
        return mode;
    }

    public void setMode(short mode) {
        this.mode = mode;
    }


    /*
    * public static short UNKNOWN_MOD_MODE = 0;
        public static short BUKKIT = 1;
        public static short SPONGE = 2;
        public static short FORGE = 3;
        public static short LITE = 4;
        public static short TWEAK = 5;
        public static short EX =6;
    * */

    public String getModeDescribe(short mode){
        return Mod.NameMap[mode];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransMod)) return false;//Note:all subclasses can be compared even not the same class
        TransMod transMod = (TransMod) o;
        return Objects.equals(getId(), transMod.getId()) &&
                Objects.equals(getVersion(), transMod.getVersion()) &&
                (!getVersion().equals(UNKNOWN_VERSION) &&
                !transMod.getVersion().equals(UNKNOWN_VERSION));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVersion());
    }

    public boolean isFromServer() {
        return fromServer;
    }

    public void setFromServer(boolean fromServer) {
        this.fromServer = fromServer;
    }
}
