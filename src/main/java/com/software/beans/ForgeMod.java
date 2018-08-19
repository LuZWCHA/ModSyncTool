package com.software.beans;

import com.software.beans.jsonbean.ForgeJsonData;
import com.software.beans.jsonbean.ForgeJsonData2;


/**
 * Created by 陆正威 on 2018/7/11.
 */
public class ForgeMod extends AbstractMod{

    private static final long serialVersionUID = -8541819558796514051L;

    private String name;

    @Override
    public String toString() {
        return "ForgeMod{" +
                "name='" + name + '\''+ super.toString() ;
    }

    public void set(ForgeJsonData js){
        set(js.getModid(), js.getName(), js.getVersion());
    }

    public void set(ForgeJsonData2.ModListBean modListBean){
        set(modListBean.getModid(), modListBean.getName(), modListBean.getVersion());
    }

    private void set(String modid, String name, String version) {
        if(!modid.isEmpty()){
            setName(name);
            setId(modid);
            setVersion(version);
            setMode(Mod.MODE.FORGE);
            check();
        }else
            throw new RuntimeException(Mod.MODE.NULL_ID_EXCEPTION);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
