package com.software.api;

import com.software.api.Managers.ControllerManager;
import com.software.api.Managers.CacheManager;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SyncContext {
    private ControllerManager controllerManager;
    private CacheManager cacheManager;

    private SyncContext(){

    }

    public SyncContext(@NonNull ControllerManager controllerManager,@NonNull CacheManager cacheManager){
        this.cacheManager = cacheManager;
        this.controllerManager = controllerManager;
    }

    public ControllerManager getControllerManager(){
        return controllerManager;
    }
    public CacheManager getCacheManager(){
        return cacheManager;
    }
}
