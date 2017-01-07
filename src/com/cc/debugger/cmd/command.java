package com.cc.debugger.cmd;

import com.alibaba.fastjson.JSONObject;
import com.cc.debugger.Context;
import com.cc.debugger.iface.EventCallback;
import com.sun.glass.ui.SystemClipboard;
import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by CwT on 16/4/4.
 */
public class command extends abstractCommand {

    static List<String> aliases = null;
    public static String ACTION_NAME_KEY = "action";

    //Action
    public final static String ACTION_DUMP_DEXFILE = "dump";
    public final static String ACTION_INIT = "init";
    public final static String ACTION_SHOW = "show";
    public final static String ACTION_UPDATE_CLASS = "update_cls";
    public final static String ACTION_UPDATE_METHOD = "update_mth";
    public final static String ACTION_INVOKE = "invoke";
    public final static String ACTION_DUMP_MEM = "dump_mem";
    public final static String ACTION_DUMP_HEAP = "dump_heap";

    //Key
    final static String COOKIE = "cookie";
    final static String FILE_PATH = "filepath";
    final static String LUA_SCRIPT = "lua";
    final static String START_DUMP_MEM = "start";
    final static String LENGTH_DUMP_MEM = "length";
    final static String CLASS_NAME = "clsName";
    final static String METHOD_NAME = "mthName";
    final static String SIGNATURE = "signature";

    static {
        aliases = new ArrayList<>();
        aliases.add("cmd");
    }

    public command() {
        super("<cmd> [dump init show update_cls update_mth invoke dump_mem dump_heap]", 0, aliases);
    }

    @Override
    public void perform(final String[] argv, final Context ctx) {
        if (argv.length > 0) {
            final JSONObject object = new JSONObject();
            object.put(ACTION_NAME_KEY, argv[0]);
            switch (argv[0]) {
                case ACTION_INIT:
                case ACTION_DUMP_DEXFILE: if (argv.length > 1) object.put(COOKIE, argv[1]); break;
                case ACTION_DUMP_HEAP:
                case ACTION_SHOW: break;
                case ACTION_UPDATE_CLASS: if (argv.length > 1) object.put(CLASS_NAME, argv[1]); break;
                case ACTION_UPDATE_METHOD: updateMethod(argv, ctx); return;
                case ACTION_DUMP_MEM:
                    if (argv.length > 2) {
                        object.put(START_DUMP_MEM, argv[1]);
                        object.put(LENGTH_DUMP_MEM, argv[2]);
                    }
                    if (argv.length > 3) {
                        object.put(FILE_PATH, argv[3]);
                    }
                    break;
                case ACTION_INVOKE: if (argv.length > 1) object.put(LUA_SCRIPT, argv[1]); break;
                default: return;
            }

            Iterator handler = ctx.getVm().classesByName("com.cc.apptroy.CommandExecutor").iterator();
            if (handler.hasNext()) {

                final EventRequestManager erm = ctx.getVm().eventRequestManager();

                final ClassType ct = (ClassType) handler.next();
                List<Method> methods = ct.methodsByName("execute");
                if (methods == null || methods.size() == 0) {
                    System.err.println("ERROR: can not find the executor");
                    return;
                }
                Method target = null;
                for (Method each : methods) {
                    if (each.signature().equals("()V")) {
                        target = each;
                        break;
                    }
                }

                if (target == null) {
                    System.err.println("ERROR: cannot find the method");
                    return;
                }

                Location cur = target.locationOfCodeIndex(0);
                BreakpointRequest br = erm.createBreakpointRequest(cur);
                br.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
                br.enable();
                ctx.register(br, new EventCallback() {
                    @Override
                    public void handleEvent(Event event) {
                        BreakpointEvent be = (BreakpointEvent) event;
                        ctx.cancelRequest(event.request());
                        com.sun.jdi.Method mth = ct.concreteMethodByName("run", "(Ljava/lang/Strng;)V");
                        List<Value> params = new ArrayList<>();
                        params.add(ctx.getVm().mirrorOf(object.toJSONString()));
                        try {
                            ct.invokeMethod(be.thread(), mth, params, ObjectReference.INVOKE_SINGLE_THREADED);
                        } catch (InvalidTypeException e) {
                            e.printStackTrace();
                        } catch (ClassNotLoadedException e) {
                            e.printStackTrace();
                        } catch (IncompatibleThreadStateException e) {
                            e.printStackTrace();
                        } catch (InvocationException e) {
                            e.printStackTrace();
                            System.err.println(e.exception().toString());
                            printStackTrace(e, be.thread());
                        } finally {
                            be.thread().resume();
                        }
                        System.out.println("cmd " + object.toJSONString() + " executed successfully");
                        // should be called when the command finished
                        done();
                    }
                });
            } else {
                System.err.println("Command Executor isn't running, try to restart the app");
            }
        }
    }

    private void printStackTrace(InvocationException e, ThreadReference thread) {

        ClassType ct = (ClassType) e.exception().referenceType();
        Method mth = ct.concreteMethodByName("printStackTrace", "()V");
        try {
            e.exception().invokeMethod(thread, mth, new ArrayList<Value>(0), ClassType.INVOKE_SINGLE_THREADED);
        } catch (InvalidTypeException e1) {
            e1.printStackTrace();
        } catch (ClassNotLoadedException e1) {
            e1.printStackTrace();
        } catch (IncompatibleThreadStateException e1) {
            e1.printStackTrace();
        } catch (InvocationException e1) {
            e1.printStackTrace();
        }
    }

    private void updateMethod(final String[] argv, final Context ctx) {
        if (argv.length <= 3)
            return;
        final String className = argv[1];
        final String methodName = argv[2];
        final String descriptor = argv[3];

        new Break().break_method(ctx, className, methodName, descriptor, new EventCallback() {
            @Override
            public void handleEvent(Event event) {
                ctx.cancelRequest(event.request());
                BreakpointEvent be = (BreakpointEvent) event;
                Iterator handler = ctx.getVm().classesByName("com.cc.apptroy.CommandExecutor").iterator();
                if (handler.hasNext()) {
                    final ClassType ct = (ClassType) handler.next();
                    com.sun.jdi.Method mth;
                    List<Value> params = new ArrayList<>();
                    mth = ct.concreteMethodByName("run", "(Ljava/lang/Strng;)V");
                    JSONObject object = new JSONObject();
                    object.put(ACTION_NAME_KEY, ACTION_UPDATE_METHOD);
                    object.put(CLASS_NAME, className);
                    object.put(METHOD_NAME, methodName);
                    object.put(SIGNATURE, descriptor);
                    params.add(ctx.getVm().mirrorOf(object.toJSONString()));

                    try {
                        ct.invokeMethod(be.thread(), mth, params, ObjectReference.INVOKE_SINGLE_THREADED);
                    } catch (InvalidTypeException e) {
                        e.printStackTrace();
                    } catch (ClassNotLoadedException e) {
                        e.printStackTrace();
                    } catch (IncompatibleThreadStateException e) {
                        e.printStackTrace();
                    } catch (InvocationException e) {
                        System.out.println(e.exception().referenceType().name());
                        try {
                            e.exception().invokeMethod(be.thread(),
                                    ((ClassType) e.exception().referenceType()).concreteMethodByName("printStackTrace", "()V"),
                                    new ArrayList<Value>(0),
                                    ObjectReference.INVOKE_SINGLE_THREADED);
                        } catch (InvalidTypeException e1) {
                            e1.printStackTrace();
                        } catch (ClassNotLoadedException e1) {
                            e1.printStackTrace();
                        } catch (IncompatibleThreadStateException e1) {
                            e1.printStackTrace();
                        } catch (InvocationException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    } finally {
                        be.thread().resume();
                    }
                    System.out.println("cmd update method" + " executed successfully");
                    done();
                }
            }
        });
        System.out.println("Please trigger the method");
    }

}
