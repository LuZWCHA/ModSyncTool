package com.software.api;

public abstract class ISyncPlugin {
    private SyncContext syncContext;

    ISyncPlugin(){
    }

    public abstract void construction(String syncVersion,String javaVersion) throws Throwable;

    public boolean initialize(SyncContext context){
        syncContext = context;
        return true;
    }
    public abstract boolean postInitialize();
    public abstract boolean close();
    public abstract void crash() throws Throwable;

}
