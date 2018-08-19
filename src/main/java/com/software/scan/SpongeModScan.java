package com.software.scan;

import com.software.asmutil.AnnFinderCV;
import com.software.beans.Mod;
import com.software.beans.SpongeMod;
import com.software.beans.WrapperMod;
import com.sun.xml.internal.ws.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * Created by 陆正威 on 2018/7/14.
 */
public class SpongeModScan extends AbstractScanProcess<SpongeMod> {
    @Override
    public long getId() {
        return 0x0029;
    }

    @Override
    protected JarEntry getSubInfoFile(JarFile jarFile) {
        return null;
    }

    @Override
    protected WrapperMod<SpongeMod> handle(InputStream ins) throws Exception {
        return WrapperMod.createEmpty();
    }

    protected String name2PacketNameClazzName(String name){
        if(name.contains("/"))
            name = name.replace("/",".");
        int index = name.lastIndexOf(".");
        if(index!= -1)
            name = name.substring(0,index);
        return name;
    }

    @Override
    protected WrapperMod<SpongeMod> handle(JarFile jf) throws Exception {
        Enumeration<JarEntry> es = jf.entries();
        WrapperMod<SpongeMod> wm =WrapperMod.createEmpty();
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
                    if (!wm.isEmpty())
                        break;

                } catch (NoClassDefFoundError | SecurityException | UnsupportedClassVersionError |
                        ClassNotFoundException | VerifyError | IncompatibleClassChangeError e1) {
                    //skip
                }
            }

            Thread.sleep(sleepTime);
        }
        return wm;
    }

    public WrapperMod<SpongeMod> handleClass(Class<?> clazz){
        WrapperMod<SpongeMod> w = WrapperMod.createEmpty();
        Plugin an = clazz.getAnnotation(Plugin.class);
        if(an != null) {
            SpongeMod mod = new SpongeMod();
            mod.setId(an.id());
            mod.setMode(Mod.MODE.SPONGE);
            mod.setVersion(an.version());
            w.wrap(mod);
        }

        return w;
    }

    //too slow
    private void ASMScan(InputStream ins) throws IOException, ClassNotFoundException {
        AnnFinderCV annFinderCV = new AnnFinderCV();
        ClassReader classReader = new ClassReader(ins);
        classReader.accept(annFinderCV,1);
        List<AnnotationNode> annotationList = annFinderCV.visibleAnnotations;
        if(null != annotationList){
            for (AnnotationNode node :
                    annotationList) {
                com.sun.xml.internal.ws.org.objectweb.asm.Type t = com.sun.xml.internal.ws.org.objectweb.asm.Type.getType(node.desc);
                if(Plugin.class.getName().equals(t.getClassName())){

                }
            }
        }
    }
}
