package com.software.api;

public interface MyCache {
    void preInit();
    void save();
    boolean doInit();
    boolean doSave();
}
