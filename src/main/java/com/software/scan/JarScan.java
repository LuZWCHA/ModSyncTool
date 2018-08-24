package com.software.scan;

import com.software.beans.*;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public class JarScan {
    private LinkedHashMap<String, Class> processes;
    private long sleepTime = 2;

    @SafeVarargs
    public JarScan(Class<? extends AbstractScanProcess>... processes){
        LinkedHashMap<String,Class> ps = new LinkedHashMap<>();
        for (Class<? extends AbstractScanProcess> p :
                processes) {
            ps.put(p.getSimpleName(),p);
        }
        if(processes.length <= 0)
            initDefaultProcesses();
    }

    public JarScan(LinkedHashMap<String, Class> processes) {
        if(processes == null || processes.isEmpty())
            initDefaultProcesses();
        else
            this.processes = processes;
    }

    private void initDefaultProcesses(){
        processes = new LinkedHashMap<>();
        processes.put(TweakModScan.class.getName(), TweakModScan.class);
        //processes.put(SpongeModScan.class.getName(), SpongeModScan.class);
        //processes.put(ForgeModScan.class.getName(), ForgeModScan.class);
        processes.put(ForgeSpongeModScan.class.getSimpleName(),ForgeSpongeModScan.class);
        processes.put(LiteModScan.class.getName(), LiteModScan.class);
        processes.put(BukkitModScan.class.getName(), BukkitModScan.class);
    }

    public void addNewScanProcess(@NotNull ScanProcess process) {
        processes.put(process.getClass().getName(), process.getClass());
    }

    public WrapperMod scan(@NotNull JarFile jarFile) throws InterruptedException, IllegalAccessException, InstantiationException {
        WrapperMod wm = null;

        if (jarFile.getName().endsWith(".litemod")) {
            ScanProcess sp = (ScanProcess) processes.get(LiteModScan.class.getName()).newInstance();
            sp.setSleepTime(sleepTime);
            if (sp.scan(jarFile)) {
                wm = sp.get();
            }
        } else {
            Collection<Class> entries = processes.values();
            for (Class clazz :
                    entries) {
                AbstractScanProcess sp = (AbstractScanProcess) clazz.newInstance();
                if (sp.scan(jarFile)) {
                    wm = sp.get();
                    break;
                }
            }
        }

        return Optional.ofNullable(wm).orElse(WrapperMod.createEmpty());
    }

    public void setSleepTime(long time){
        sleepTime = time;
    }
}
