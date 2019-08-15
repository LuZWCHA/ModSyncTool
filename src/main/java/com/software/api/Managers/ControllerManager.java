package com.software.api.Managers;

import com.software.api.SyncController;

import java.util.HashMap;
import java.util.Map;

public enum  ControllerManager {
    INSTANCE;
    Map<String,SyncController> controllers;

    ControllerManager(){
        controllers = new HashMap<>();
    }

    public void register(){

    }

    public void remove(){

    }


}
