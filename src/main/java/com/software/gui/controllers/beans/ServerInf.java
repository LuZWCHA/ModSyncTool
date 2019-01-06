package com.software.gui.controllers.beans;

import java.io.Serializable;

public class ServerInf implements Serializable {
    private static final long serialVersionUID = 2480085107060241178L;

    private long id;
    private String name;
    private String address;

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
}
