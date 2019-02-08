package com.software.scan;

import com.software.beans.Mod;
import com.software.beans.TweakMod;
import com.software.beans.WrapperMod;
import com.software.util.ScanUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 *
 * Created by 陆正威 on 2018/7/13.
 */
public class TweakModScan extends AbstractScanProcess<TweakMod> {

    private Map<String ,String> TweakModStandData;

    public TweakModScan()
    {
        TweakModStandData = new HashMap<>(5);
    }

    @Override
    public long getId() {
        return 0x0012;
    }

    @Override
    protected JarEntry getSubInfoFile(JarFile jarFile) {
        return null;
    }

    @Override
    protected WrapperMod<TweakMod> handle(InputStream ins) throws IOException {
        return WrapperMod.createEmpty();
    }

    @Override
    protected WrapperMod<TweakMod> handle(JarFile jf) throws Exception {
        Manifest manifest = jf.getManifest();
        WrapperMod<TweakMod> wm = WrapperMod.createEmpty();

        Optional.ofNullable(manifest).ifPresent(manifest1 -> {
            resetMap();

            manifest1.getMainAttributes().entrySet().stream()
                    .filter(new Predicate<Map.Entry<Object, Object>>() {
                        @Override
                        public boolean test(Map.Entry<Object, Object> e) {
                            return TweakModStandData.containsKey(e.getKey().toString());
                        }
                    })
                    .forEachOrdered(e -> {
                TweakModStandData.replace(e.getKey().toString(), e.getValue().toString());
            });

            if(!TweakModStandData.get(TweakMod.MFFILE.TweakClass.name()).isEmpty())
            {
                TweakMod tweakMod = new TweakMod();
                String tweakClassName = ScanUtil.getLastWorldOf(TweakModStandData.get(TweakMod.MFFILE.TweakClass.name()),".");
                String tweakModId = tweakClassName.toLowerCase();
                if(tweakModId.contains("tweaker"))
                    tweakModId = tweakModId.replaceAll("tweaker","");
                tweakMod.setId(tweakModId);
                tweakMod.setTweakClass(tweakClassName);
                tweakMod.setVersion(TweakModStandData.get(TweakMod.MFFILE.TweakVersion.name()));
                tweakMod.setMode(Mod.MODE.TWEAK);
                wm.wrap(tweakMod);
            }else if(!TweakModStandData.get(TweakMod.MFFILE.FMLCorePlugin.name()).isEmpty()){
                TweakMod tweakMod = new TweakMod();
                String tweakClassName = ScanUtil.getLastWorldOf(TweakModStandData.get(TweakMod.MFFILE.FMLCorePlugin.name()),".");
                String tweakModId = tweakClassName.toLowerCase();
                if(tweakModId.contains("tweaker"))
                    tweakModId = tweakModId.replaceAll("tweaker","");
                tweakMod.setId(tweakModId);
                tweakMod.setTweakClass(tweakClassName);
                tweakMod.setVersion(TweakModStandData.get(TweakMod.MFFILE.TweakVersion.name()));
                tweakMod.setMode(Mod.MODE.TWEAK);
                wm.wrap(tweakMod);
            }
        });
        return wm;
    }

    private void resetMap(){
        TweakModStandData.put(TweakMod.MFFILE.TweakClass.name(),"");
        TweakModStandData.put(TweakMod.MFFILE.TweakAuthor.name(),"");
        TweakModStandData.put(TweakMod.MFFILE.TweakName.name(),"");
        TweakModStandData.put(TweakMod.MFFILE.TweakVersion.name(),"");
        TweakModStandData.putIfAbsent(TweakMod.MFFILE.FMLCorePlugin.name(),"");
    }
}
