package com.software.scan;

import com.software.beans.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ForgeSpongeModScan extends SpongeModScan {
    ForgeModScan forgeModScan = new ForgeModScan();

    @Override
    protected JarEntry getSubInfoFile(JarFile jarFile) {
        return null;
    }

    @Override
    protected WrapperMod handle(InputStream ins) throws Exception {
        return super.handle(ins);
    }

    @Override
    protected WrapperMod handle(JarFile jf) throws Exception {
        Enumeration<JarEntry> es = jf.entries();
        WrapperMod wm =WrapperMod.createEmpty();

        WrapperMod forgeModWrapperMod = WrapperMod.createEmpty();

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
                    if (!wm.isEmpty() && wm.get().getMode() == Mod.MODE.SPONGE)
                        break;
                    else if(!wm.isEmpty() && wm.get().getMode() == Mod.MODE.FORGE)
                        forgeModWrapperMod = wm;
                } catch (NoClassDefFoundError | SecurityException | UnsupportedClassVersionError |
                        ClassNotFoundException | VerifyError | IncompatibleClassChangeError e1) {
                    //skip
                }
            }

            Thread.sleep(sleepTime);
        }

        if(wm.isEmpty() && !forgeModWrapperMod.isEmpty())
            wm = forgeModWrapperMod;
        return wm;
    }

    @Override
    public WrapperMod handleClass(Class<?> clazz){
        WrapperMod wp = super.handleClass(clazz);
        if(wp.isEmpty())
            wp = forgeModScan.handleClass(clazz);
        return wp;
    }

    @Override
    public long getId() {
        return 0x0131;
    }
}
