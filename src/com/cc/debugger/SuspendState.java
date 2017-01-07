package com.cc.debugger;

import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;

/**
 * Created by CwT on 16/2/23.
 */
public class SuspendState {

    public boolean isSuspend = false;
    public ThreadReference thread = null;
    public Location location = null;

    public void resume() {
        if (thread != null && thread.isSuspended()) {
            thread.resume();
            isSuspend = false;
        }
    }

    public boolean isSuspend() {
        return thread.isSuspended();
    }
}
