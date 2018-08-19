package com.software.scan;

import com.software.beans.WrapperMod;
import com.sun.istack.internal.NotNull;

import java.util.jar.JarFile;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public interface ScanProcess<T extends WrapperMod> {
    boolean scan(JarFile jarFile) throws InterruptedException;
    @NotNull T get();
    void setSleepTime(long time);
    long getId();
}
