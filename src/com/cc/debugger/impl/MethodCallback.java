package com.cc.debugger.impl;

import com.cc.debugger.iface.EventCallback;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CwT on 16/4/26.
 */
public class MethodCallback implements EventCallback {

    String className;
    Map<String, EventCallback> callbacks = new HashMap<>(1);

    final public static int METHOD_ENTER = 1;
    final public static int METHOD_EXIT = -1;

    public MethodCallback(String clsName) {
        this.className = clsName;
    }

    public void addCallback(String mthName, String dec, EventCallback callback) {
        String key = mthName + "->" + dec;
        if (callbacks.containsKey(key)) {
            System.err.println("You have register one callback for this method: " + mthName);
            return;
        }

        callbacks.put(key, callback);
    }

    public boolean isEmpty() {
        return callbacks.isEmpty();
    }

    public void removeCallback(String mthName, String dec) {
        String key = mthName + "->" + dec;
        if (callbacks.containsKey(key)) {
            callbacks.remove(key);
        } else {
            System.err.println("You did not register callback for this method: " + mthName);
        }
    }

    @Override
    public void handleEvent(Event event) {
        if (event instanceof MethodEntryEvent) {
            MethodEntryEvent mee = (MethodEntryEvent) event;
            String key = mee.method().name() + "->" + mee.method().signature();
            if (callbacks.containsKey(key))
                callbacks.get(key).handleEvent(event);
            else
                mee.thread().resume();
        } else if (event instanceof MethodExitEvent) {
            MethodExitEvent mee = (MethodExitEvent) event;
            String key = mee.method().name() + "->" + mee.method().signature();
            if (callbacks.containsKey(key))
                callbacks.get(key).handleEvent(event);
            else
                mee.thread().resume();
        }
    }
}
