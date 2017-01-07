package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.iface.EventCallback;
import com.sun.jdi.Method;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;

/**
 * Created by CwT on 16/5/16.
 */
public class track extends abstractCommand {

    public track() {
        super("start/stop", -1, null, false);
    }

    @Override
    public void perform(String[] argv, Context ctx) {

        EventRequestManager manager = ctx.getVm().eventRequestManager();
        MethodEntryRequest mer = manager.createMethodEntryRequest();
        mer.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        mer.enable();

        ctx.register(mer, new EventCallback() {
            @Override
            public void handleEvent(Event event) {
                MethodEntryEvent mee = (MethodEntryEvent) event;
                Method method = mee.method();
                System.out.println("--" + method.name());
//                mee.thread().resume();
            }
        });
    }
}
