package com.software.gui.beans;

import com.software.gui.Config;
import com.software.gui.utils.FileHelper;

import java.io.Serializable;
import java.util.Objects;

public class ServerInf implements Serializable {
    private static final long serialVersionUID = 2480085107060241178L;

    private long id;
    private String name;
    private String address;
    private String path;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static ServerInf defaultServer(){
        ServerInf serverInf = new ServerInf();
        serverInf.setPath(FileHelper.getJarDir());
        serverInf.setAddress(Config.DEFAULT_SERVER_ADDRESS);
        serverInf.setName(Config.DEFAULT_SERVER_NAME);

        return serverInf;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerInf)) return false;
        ServerInf serverInf = (ServerInf) o;
        return Objects.equals(name, serverInf.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
