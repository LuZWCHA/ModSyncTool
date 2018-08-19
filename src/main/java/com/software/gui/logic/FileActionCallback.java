package com.software.gui.logic;

import java.io.File;

public interface FileActionCallback {
    void delete(File file);

    void create(File file);

    void modify(File file);
}
