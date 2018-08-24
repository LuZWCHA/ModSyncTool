package com.software.scan;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.software.beans.ForgeMod;
import com.software.beans.WrapperMod;
import com.software.beans.jsonbean.ForgeJsonData;
import com.software.beans.jsonbean.ForgeJsonData2;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * Created by 陆正威 on 2018/7/11.
 */
public class ForgeModScan extends AbstractScanProcess<ForgeMod>{

    @Override
    protected JarEntry getSubInfoFile(JarFile jarFile) {
        //je 为null会调用handle(JarFile)方法
        return jarFile.getJarEntry("mcmod.info");
    }

    protected WrapperMod<ForgeMod> handle(InputStream ins) throws IOException {
        InputStreamReader isr = new InputStreamReader(ins);
        BufferedReader reader = new BufferedReader(isr);
        reader .mark( ins.available() + 1 );
        WrapperMod<ForgeMod> wm = WrapperMod.createEmpty();
        Gson gson = new Gson();
        List<ForgeJsonData> jdl;
        try {
            Type listType = new TypeToken<ArrayList<ForgeJsonData>>(){}.getType();
            jdl = gson.fromJson(reader, listType);
            Optional.ofNullable(jdl)
                    .ifPresent(new Consumer<List<ForgeJsonData>>() {
                        @Override
                        public void accept(List<ForgeJsonData> forgeJsonDatas) {
                            for (ForgeJsonData jd :
                                    forgeJsonDatas) {
                                if(!jd.getModid().isEmpty()) {
                                    ForgeMod mod = new ForgeMod();
                                    mod.set(jd);
                                    wm.wrap(mod);
                                    break;
                                }
                            }
                        }
                    });

        }catch (JsonIOException | JsonSyntaxException e){
            reader.reset();
            ForgeJsonData2 fj2 = gson.fromJson(reader,ForgeJsonData2.class);
            Optional.ofNullable(fj2)
                    .map(new Function<ForgeJsonData2, List<ForgeJsonData2.ModListBean>>() {
                        @Override
                        public List<ForgeJsonData2.ModListBean> apply(ForgeJsonData2 forgeJsonData2) {
                            return forgeJsonData2.getModList();
                        }
                    }).ifPresent(new Consumer<List<ForgeJsonData2.ModListBean>>() {
                @Override
                public void accept(List<ForgeJsonData2.ModListBean> modListBeen) {
                    for (ForgeJsonData2.ModListBean jd :
                            modListBeen) {
                        if (!jd.getModid().isEmpty()) {
                            ForgeMod mod = new ForgeMod();
                            mod.set(jd);
                            wm.wrap(mod);
                            break;
                        }
                    }
                }
            });
        }
        reader.close();
        isr.close();

        return wm;
    }


    @Override
    protected WrapperMod<ForgeMod> handle(JarFile jf) throws Exception {
        Enumeration<JarEntry> es = jf.entries();
        WrapperMod wm = WrapperMod.createEmpty();
        JarEntry e;
        while(es.hasMoreElements()){
            e = es.nextElement();
            if(!e.isDirectory() && e.getName().endsWith(".class") && !e.getName().contains("$")){
                String name = e.getName();
                File file = new File(jf.getName());
                name = name2PacketNameClazzName(name);
                URL url = file.toURI().toURL();
                try (URLClassLoader classLoader = new URLClassLoader(new URL[]{url})) {
                    Class<?> clazz;
                    clazz = classLoader.loadClass(name);

                    wm = handleClass(clazz);
                    if (!wm.isEmpty()) {
                        break;
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError | UnsupportedClassVersionError e1) {
                    //throw new RuntimeException(e1);
                    //skip
                } catch (SecurityException | IncompatibleClassChangeError|VerifyError e1) {
                    //cannot handle next classes,break to stop forge mod scan
                    break;
                }
            }
            Thread.sleep(sleepTime);
        }
        return wm;
    }

    public long getId() {
        return 0x0018;
    }

    private String name2PacketNameClazzName(String name){
        if(name.contains("/"))
            name = name.replace("/",".");
        int index = name.lastIndexOf(".");
        if(index!= -1)
            name = name.substring(0,index);
        return name;
    }

    public WrapperMod<ForgeMod> handleClass(Class<?> clazz){
        WrapperMod<ForgeMod> w = WrapperMod.createEmpty();
        net.minecraftforge.fml.common.Mod an = clazz.getAnnotation(net.minecraftforge.fml.common.Mod.class);
        if(an != null) {
            ForgeJsonData data = new ForgeJsonData();
            data.setModid(an.modid());
            data.setName(an.name());
            data.setVersion(an.version());
            ForgeMod mod = new ForgeMod();
            mod.set(data);
            w.wrap(mod);
        }
        return w;
    }
}
