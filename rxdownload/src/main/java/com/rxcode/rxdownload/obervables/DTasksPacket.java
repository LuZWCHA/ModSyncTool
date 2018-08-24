package com.rxcode.rxdownload.obervables;

import com.rxcode.rxdownload.api.RxCarrier;

public class DTasksPacket implements RxCarrier {
    @Override
    public int getType() {
        return 0x0012;
    }


}
