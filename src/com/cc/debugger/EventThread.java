package com.cc.debugger;

import com.cc.debugger.iface.EventCallback;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequestManager;

/**
 * Created by CwT on 16/2/23.
 */
public class EventThread extends Thread{

    Context ctx = null;
    private boolean connected = true;
    private boolean vmDied = false;
    private EventSet eventSet;

    public EventThread(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        EventQueue queue = ctx.getVm().eventQueue();
        while (connected) {
            try {
                eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                while (it.hasNext()) {
                    handleEvent(it.nextEvent());
                }
//                eventSet.resume();
            } catch (InterruptedException exc) {
                // Ignore
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            }
        }
    }

    /**
     * Dispatch incoming events
     */
    private void handleEvent(Event event) {
        if (event instanceof VMDeathEvent) {
            vmDeathEvent((VMDeathEvent) event);
        } else if (event instanceof VMDisconnectEvent) {
            vmDisconnectEvent((VMDisconnectEvent) event);
        } else {
            EventCallback callback = ctx.getCallback(event.request());
//            if (event instanceof StepEvent)
//                util.Logger("step event:" + ((StepEvent) event).location().method().name());
            if (callback != null)
                callback.handleEvent(event);
            else {
//                eventSet.resume();
                if (event instanceof StepEvent) {
                    if (((StepEvent) event).thread().isSuspended())
                        ((StepEvent) event).thread().resume();
                    else {
                        util.Logger("[Method Step in handler]thread is not suspend");
                    }
                }
//                event.request().disable();
                util.Logger("event doesn't have callback");
            }
        }
    }

    /***************************************************************************
     * A VMDisconnectedException has happened while dealing with another event.
     * We need to flush the event queue, dealing only with exit events (VMDeath,
     * VMDisconnect) so that we terminate correctly.
     */
    synchronized void handleDisconnectedException() {
        EventQueue queue = ctx.getVm().eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator iter = eventSet.eventIterator();
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();
                    if (event instanceof VMDeathEvent) {
                        vmDeathEvent((VMDeathEvent) event);
                    } else if (event instanceof VMDisconnectEvent) {
                        vmDisconnectEvent((VMDisconnectEvent) event);
                    }
                }
                eventSet.resume(); // Resume the VM
            } catch (InterruptedException exc) {
                // ignore
            }
        }
    }

    public void vmDeathEvent(VMDeathEvent event) {
        vmDied = true;
//        printAllThreadInfos();
        System.out.println("-- The application exited --");
    }

    public void vmDisconnectEvent(VMDisconnectEvent event) {
        connected = false;
        if (!vmDied) {
            System.out.println("-- The application has been disconnected --");
        }
    }
}
