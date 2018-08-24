package com.software.scan;

import com.software.beans.BukkitMod;
import com.software.beans.Mod;
import com.software.beans.WrapperMod;
import com.software.util.ScanUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by 陆正威 on 2018/7/14.
 */
public class BukkitModScan extends AbstractScanProcess<BukkitMod> {
    @Override
    public long getId() {
        return 0x0031;
    }

    @Override
    protected JarEntry getSubInfoFile(JarFile jarFile) {
        return jarFile.getJarEntry("plugin.yml");
    }

    @Override
    protected WrapperMod<BukkitMod> handle(InputStream ins) throws IOException {
        WrapperMod<BukkitMod> wm = WrapperMod.createEmpty();
        Yaml yaml = new Yaml();
        Map<String,String> p = yaml.load(ins);

        if(p.containsKey(BukkitMod.PLUGINYML.main.name())){
            BukkitMod mod = new BukkitMod();
            mod.setId(ScanUtil.getLastWorldOf(p.get(BukkitMod.PLUGINYML.main.name()),"."));
            mod.setMode(Mod.MODE.BUKKIT);
            mod.setVersion(String.valueOf(p.get(BukkitMod.PLUGINYML.version.name())));
            mod.setWebsite(p.get(BukkitMod.PLUGINYML.website.name()));
            wm.wrap(mod);
        }
        return wm;
    }

    @Override
    protected WrapperMod<BukkitMod> handle(JarFile jf) throws IOException {
        return WrapperMod.createEmpty();
    }
}
