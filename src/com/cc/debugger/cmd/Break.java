package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.SuspendState;
import com.cc.debugger.iface.EventCallback;
import com.cc.debugger.util;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import com.sun.tools.jdi.LocalVariableImpl;
import com.sun.tools.jdi.LocationImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/2/23.
 */
public class Break extends abstractCommand{

    static List<String> aliases = null;

    static {
        aliases = new ArrayList<>();
        aliases.add("break");
        aliases.add("b");
    }

    public Break() {
        super("<class> [<method> <descriptor>] [exit/lineNo]", 1, aliases);
    }

    public void break_class(final Context ctx, String className, EventCallback callback) {
        final EventRequestManager manager = ctx.getVm().eventRequestManager();

        MethodEntryRequest mer = manager.createMethodEntryRequest();
        mer.addClassFilter(className);
        mer.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        mer.enable();

        ctx.register(mer, callback);
    }

    private void break_class(final Context ctx, String className) {
        break_class(ctx, className, new EventCallback() {
            @Override
            public void handleEvent(Event event) {
                MethodEntryEvent mee = (MethodEntryEvent)event;
                util.Logger("Breakpoint hit in " + mee.method().signature());
                ctx.cancelRequest(mee.request());
                SuspendState state = ctx.getState();
                state.isSuspend = true;
                state.thread = mee.thread();
                state.location = mee.location();
            }
        });
    }

    public void break_method(final Context ctx, final String className, final String mthName, final String dec, final EventCallback callback) {
        ReferenceType type = ctx.getClassbyname(className);
        if (type == null) {
            prepare_class(ctx, className, new EventCallback() {
                @Override
                public void handleEvent(Event event) {
                    ctx.cancelRequest(event.request());
                    ClassPrepareEvent cpe = (ClassPrepareEvent) event;
                    cmd_break(ctx, className, mthName, dec, "0", callback);
                    cpe.thread().resume();
                }
            });
        } else {
            cmd_break(ctx, className, mthName, dec, "0", callback);
        }
    }

    private void break_method(final Context ctx, final String className, final String mthName, final String dec) {
        ReferenceType type = ctx.getClassbyname(className);
        if (type == null) {
            prepare_class(ctx, className, new EventCallback() {
                @Override
                public void handleEvent(Event event) {
                    ctx.cancelRequest(event.request());
                    ClassPrepareEvent cpe = (ClassPrepareEvent) event;
                    cmd_break(ctx, className, mthName, dec, "0");
                    cpe.thread().resume();
                }
            });
        } else {
            cmd_break(ctx, className, mthName, dec, "0");
        }
    }

    public void break_line(final Context ctx, final String clsName, final String mthName, final String dec, final String line, final EventCallback callback) {
        ReferenceType type = ctx.getClassbyname(clsName);
        if (type == null) {
            prepare_class(ctx, clsName, new EventCallback() {
                @Override
                public void handleEvent(Event event) {
                    ctx.cancelRequest(event.request());
                    cmd_break(ctx, clsName, mthName, dec, line, callback);
                }
            });
        } else {
            cmd_break(ctx, clsName, mthName, dec, line, callback);
        }
    }

    private void break_line(final Context ctx, final String clsName, final String mthName, final String dec, final String line) {
        ReferenceType type = ctx.getClassbyname(clsName);
        if (type == null) {
            prepare_class(ctx, clsName, new EventCallback() {
                @Override
                public void handleEvent(Event event) {
                    ctx.cancelRequest(event.request());
                    cmd_break(ctx, clsName, mthName, dec, line);
                }
            });
        } else {
            cmd_break(ctx, clsName, mthName, dec, line);
        }
    }

    private void prepare_class(Context ctx, String clsName, EventCallback callback) {
        final EventRequestManager manager = ctx.getVm().eventRequestManager();

        ReferenceType type = ctx.getClassbyname(clsName);
        if (type == null) {
            // You need to wait it to be loaded
            ClassPrepareRequest cpr = manager.createClassPrepareRequest();
            cpr.addClassFilter(clsName);
            cpr.addCountFilter(1);
            cpr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            cpr.enable();
            ctx.register(cpr, callback);
        }
    }

    public void cmd_break(Context ctx, String clsName, String mthName, String dec, String line, EventCallback callback) {
        EventRequestManager manager = ctx.getVm().eventRequestManager();
        ReferenceType type = ctx.getClassbyname(clsName);
        List<Method> methods = type.methodsByName(mthName);
        if (methods == null || methods.size() == 0) {
            System.err.println("no method found!");
            return;
        }

        if (methods.size() > 1) {
            if (dec == null) {
                System.err.println("more than one method, please specify target");
                return;
            }
        }
        Method method = null;
        for (Method each : methods) {
            if (each.signature().equals(dec))
                method = each;
        }

        if (method == null) {
            System.err.println("Can not find the method:" + mthName + "->" + dec);
            return;
        }

        Location curlocation = null;
        long index = -1;

        if ("exit".equals(line)) {
            //TODO

            return;
        } else {
            try {
                index = Long.parseLong(line);
                curlocation = method.locationOfCodeIndex(index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (curlocation == null) {
            System.err.println("could not find the location of code index: " + index);
            return;
        }

        BreakpointRequest breakpointRequest = manager.createBreakpointRequest(curlocation);
        breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        breakpointRequest.enable();
        ctx.register(breakpointRequest, callback);
    }

    private void cmd_break(Context ctx, String clsName, String mthName, String dec, String line) {
        cmd_break(ctx, clsName, mthName, dec, line, new onbreak(ctx));
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        if (argv.length <= 0) {
            System.err.println("we need more arguments!!");
            return;
        }

        String className = null, methodName = null, dec = null, line = null;

        className = argv[0].replaceAll("/", ".");
        if (className.charAt(className.length()-1) == ';')
            className = className.substring(1, className.length()-1);

        if (argv.length > 1) {
            methodName = argv[1];
            if (argv.length > 2)
                dec = argv[2];
            if (argv.length > 3) {
                line = argv[3];
            }
        }

        if (methodName == null) {
            break_class(ctx, className);
        } else if (line == null) {
            break_method(ctx, className, methodName, dec);
        } else {
            break_line(ctx, className, methodName, dec, line);
        }
    }

    class onbreak implements EventCallback {

        Context ctx;

        public onbreak(Context ctx) { this.ctx = ctx; }

        @Override
        public void handleEvent(Event event) {
            if (event instanceof BreakpointEvent) {
                BreakpointEvent breakpointEvent = (BreakpointEvent)event;
                SuspendState state = ctx.getState();
                state.thread = breakpointEvent.thread();
                state.isSuspend = true;
                state.location = breakpointEvent.location();
                if (state.location.codeIndex() == 0)
                    util.Logger("Enter method: " + state.location.method().name() + "->" + state.location.method().signature());
                else
                    util.Logger("breakpoint hit in " + state.location.method().name() + "->" + state.location.method().signature());
            }
        }
    }
}