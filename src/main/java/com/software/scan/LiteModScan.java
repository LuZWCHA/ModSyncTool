package com.software.scan;

import com.software.beans.LiteMod;
import com.software.beans.Mod;
import com.software.beans.WrapperMod;
import com.software.beans.jsonbean.LiteJsonData;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public class LiteModScan extends AbstractScanProcess<LiteMod> {

    @Override
    protected JarEntry getSubInfoFile(JarFile jarFile) {
        return jarFile.getJarEntry("litemod.json");
    }

    public WrapperMod<LiteMod> handle(InputStream ins) throws Exception {
        WrapperMod<LiteMod> wm = WrapperMod.createEmpty();
        InputStreamReader isr = new InputStreamReader(ins);
        BufferedReader reader = new BufferedReader(isr);
        LiteMod mod = new LiteMod();
        Gson gson = new Gson();
        LiteJsonData ljd = gson.fromJson(reader, LiteJsonData.class);

         if(!ljd.getName().isEmpty()){
             mod.set(ljd);
             wm.wrap(mod);
         }
        reader.close();
        isr.close();

         return wm;
    }

    @Override
    protected  WrapperMod<LiteMod> handle(JarFile jf) throws IOException {
        return WrapperMod.createEmpty();
    }

    @Override
    public long getId() {
        return 0x0013;
    }
}
