package com.cc.debugger.iface;

import com.sun.jdi.event.Event;

/**
 * Created by CwT on 16/2/23.
 */
public interface EventCallback {

    void handleEvent(Event event);
}
