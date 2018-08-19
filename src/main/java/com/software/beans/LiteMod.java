package com.software.beans;

import com.software.beans.jsonbean.LiteJsonData;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public class LiteMod extends AbstractMod {
    private static final long serialVersionUID = -6187461238955512669L;

    public void set(LiteJsonData js){
        if(!js.getName().isEmpty()){
            setId(js.getName());
            setVersion(js.getVersion());
            setMode(Mod.MODE.LITE);
            check();
        }else
            throw new RuntimeException(Mod.MODE.NULL_ID_EXCEPTION);
    }

    @Override
    public String toString() {
        return "LiteMod{ " + super.toString();
    }
}
