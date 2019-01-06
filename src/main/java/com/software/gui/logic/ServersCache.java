package com.software.gui.logic;

import com.software.gui.controllers.beans.ServerInf;
import com.software.gui.utils.FileHelper;
import io.reactivex.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class ServersCache implements MyCache {
    private List<ServerInf> SERVER_INF_LIST = new ArrayList<>();
    public static String FILE_NAME = "servers";

    @Override
    public void preInit() {
        load();
    }

    @Override
    public void save() {
        sync2Disk();
    }

    @Override
    public boolean doInit() {
        return true;
    }

    @Override
    public boolean doSave() {
        return false;
    }

    private void sync2Disk(){
        FileHelper.write(SERVER_INF_LIST,new File(FILE_NAME).toPath());
    }

    private void load(){
        SERVER_INF_LIST.addAll(FileHelper.read2ArrayList(new File(FILE_NAME).toPath()));
    }

    public void addServerInf(ServerInf serverInf){
        SERVER_INF_LIST.add(serverInf);
    }

    public void deleteServerInf(ServerInf serverInf){
        SERVER_INF_LIST.remove(serverInf);
    }

    public Collection<ServerInf> getList(){
        return SERVER_INF_LIST;
    }

    public ServerInf getServerInfByName(@NonNull String s){
        ServerInf inf = null;
        for (ServerInf i :
                SERVER_INF_LIST) {
            if (s.equals(i.getName())) {
                inf = i;
                break;
            }
        }
        return inf;
    }
}
