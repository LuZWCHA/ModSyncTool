package com.software.gui.logic;

import javax.xml.stream.events.Attribute;

public enum FileAction {
    DELETE("ENTRY_DELETE"),
    CREATE("ENTRY_CREATE"),
    MODIFY("ENTRY_MODIFY");
    private String value;

    FileAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
