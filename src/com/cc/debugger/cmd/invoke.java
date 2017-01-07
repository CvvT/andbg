package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.iface.EventCallback;
import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by CwT on 16/5/4.
 */
public class invoke extends abstractCommand {

    public invoke() {
        super("", 1, null);
    }

    @Override
    public void perform(String[] argv, Context ctx) {

    }

    public void run(final Context ctx, final String clsName, final String mthName, final String signature, final Object... argv) {
        Iterator handler = ctx.getVm().classesByName("com.cc.apptroy.CommandExecutor").iterator();
        if (handler.hasNext()) {
            final EventRequestManager erm = ctx.getVm().eventRequestManager();
            MethodEntryRequest mer = erm.createMethodEntryRequest();
            mer.addClassFilter("com.cc.apptroy.CommandExecutor");
            mer.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            mer.enable();

            final ClassType ct = (ClassType) handler.next();

            ctx.register(mer, new EventCallback() {
                @Override
                public void handleEvent(Event event) {
                    MethodEntryEvent mee = (MethodEntryEvent) event;
                    ctx.cancelRequest(mee.request());
                    com.sun.jdi.Method mth = null;
                    List<Value> params = new ArrayList<>();
                    mth = ct.concreteMethodByName("staticinvoke",
                            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Z");
                    params.add(ctx.getVm().mirrorOf(clsName));
                    params.add(ctx.getVm().mirrorOf(mthName));
                    params.add(ctx.getVm().mirrorOf(signature));
                    for (Object obj : argv) {
                        params.add(convertobj2val(ctx, obj));
                    }
                    try {
                        ct.invokeMethod(mee.thread(), mth, params, ClassType.INVOKE_SINGLE_THREADED);
                    } catch (InvalidTypeException e) {
                        e.printStackTrace();
                    } catch (ClassNotLoadedException e) {
                        e.printStackTrace();
                    } catch (IncompatibleThreadStateException e) {
                        e.printStackTrace();
                    } catch (InvocationException e) {
                        e.printStackTrace();
                    } finally {
                        mee.thread().resume();
                    }
                    if (listener != null)
                        listener.onEnd();
                }
            });
        } else {
            System.err.println("Command Executor isn't running, try to restart the app");
        }
    }

    private Value convertobj2val(Context ctx, Object obj) {
        if (obj instanceof Boolean)
            return ctx.getVm().mirrorOf((boolean)obj);
        else if (obj instanceof Byte)
            return ctx.getVm().mirrorOf((byte)obj);
        else if (obj instanceof Short)
            return ctx.getVm().mirrorOf((short)obj);
        else if (obj instanceof Character)
            return ctx.getVm().mirrorOf((char)obj);
        else if (obj instanceof Integer)
            return ctx.getVm().mirrorOf((int)obj);
        else if (obj instanceof Long)
            return ctx.getVm().mirrorOf((long)obj);
        else if (obj instanceof Float)
            return ctx.getVm().mirrorOf((float)obj);
        else if (obj instanceof Double)
            return ctx.getVm().mirrorOf((double)obj);
        else if (obj instanceof String)
            return ctx.getVm().mirrorOf((String)obj);

        System.err.println("unsupported case:" + obj.getClass().getName());
        return ctx.getVm().mirrorOfVoid();
    }
}
