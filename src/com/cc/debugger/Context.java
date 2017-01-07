package com.cc.debugger;

import com.cc.debugger.cmd.abstractCommand;
import com.cc.debugger.iface.EventCallback;
import com.cc.debugger.impl.MethodCallback;
import com.google.common.collect.Iterators;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.*;
import com.sun.tools.jdi.SocketAttachingConnector;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.rewriter.*;
import org.jf.dexlib2.writer.pool.DexPool;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

/**
 * Created by CwT on 16/2/22.
 */
public class Context {

    public boolean shell = false;
    private VirtualMachine vm = null;
    List<ReferenceType> classes = null;
    Map<String, ReferenceType> classesMap = null;
    Map<EventRequest, EventCallback> callbacks = new HashMap<>();
    Map<ThreadReference, StepRequest> stepMap = new HashMap<>(1);
    Map<String, MethodCallback> enterMethods = new HashMap<>();
    Map<String, MethodCallback> exitMethods = new HashMap<>();

    //Virtual Machine Ability
    boolean canGetBytecode = false;

    /**
     * Source attach
     */
    DexBackedDexFile dexfile = null;
    static int API = 16;

    private SuspendState state = new SuspendState();

    public void connect(){
        List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
        SocketAttachingConnector sac = null;
        for (Connector connector : connectors) {
            if (connector instanceof SocketAttachingConnector)
                sac = (SocketAttachingConnector)connector;
        }

        try {
            if (sac != null) {
                Map<String, Connector.Argument> arguments = sac.defaultArguments();
                Connector.Argument host = arguments.get("hostname");
                Connector.Argument port = arguments.get("port");
                Connector.Argument timeout = arguments.get("timeout");
                host.setValue("localhost");
                port.setValue("8700");
                timeout.setValue("10000");
                vm = sac.attach(arguments);
                canGetBytecode = vm.canGetBytecodes();
            }
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadDex(String absfilename) {
        try {
            dexfile = DexFileFactory.loadDexFile(absfilename, API);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void register(EventRequest request, EventCallback callback) {
        callbacks.put(request, callback);
        if (request instanceof StepRequest) {
            System.err.println("Please register step request through registerStep method");
        }
    }

    public void registerEnterMethod(String clsName, String mthName, String dec, EventCallback callback) {
        if (enterMethods.containsKey(clsName)) {
            enterMethods.get(clsName).addCallback(mthName, dec, callback);
        } else {
            MethodCallback methodCallback = new MethodCallback(clsName);
            methodCallback.addCallback(mthName, dec, callback);
            enterMethods.put(clsName, methodCallback);

            EventRequestManager manager = getVm().eventRequestManager();
            MethodEntryRequest mer = manager.createMethodEntryRequest();
            mer.addClassFilter(clsName);
            mer.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            mer.enable();

            register(mer, methodCallback);
        }
    }

    public void registerExitMethod(String clsName, String mthName, String dec, EventCallback callback) {
        if (exitMethods.containsKey(clsName)) {
            exitMethods.get(clsName).addCallback(mthName, dec, callback);
        } else {
            MethodCallback methodCallback = new MethodCallback(clsName);
            methodCallback.addCallback(mthName, dec, callback);
            exitMethods.put(clsName, methodCallback);

            EventRequestManager manager = getVm().eventRequestManager();
            MethodExitRequest mexr = manager.createMethodExitRequest();
            mexr.addClassFilter(clsName);
            mexr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            mexr.enable();

            register(mexr, methodCallback);
        }
    }

    public void registerStep(ThreadReference thread, int depth, int count, EventCallback callback) {

        StepRequest step;
        if (stepMap.containsKey(thread)) {
            step = stepMap.get(thread);
            cancelRequest(step);
        }
        step = getVm().eventRequestManager().createStepRequest(thread, StepRequest.STEP_MIN, depth);
        step.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        if (count > 0)
            step.addCountFilter(count);
        step.enable();
        stepMap.put(thread, step);
        callbacks.put(step, callback);
    }

    public void cancelMethod(EventRequest request, String clsName, String mthName, String dec, int which) {
        Map<String, MethodCallback> list = null;
        if (which == MethodCallback.METHOD_ENTER) {
            list = enterMethods;
        } else if (which == MethodCallback.METHOD_EXIT) {
            list = exitMethods;
        }

        if (list != null) {
            if (!list.containsKey(clsName)) {
                System.err.println("You did not register for this class: " + clsName);
                return;
            }

            MethodCallback callback = list.get(clsName);
            callback.removeCallback(mthName, dec);

            if (callback.isEmpty())
                cancelRequest(request);
        }
    }

    public void cancelStep(ThreadReference thread) {
        if (stepMap.containsKey(thread)) {
            stepMap.get(thread).disable();
            cancelRequest(stepMap.get(thread));
            stepMap.remove(thread);
        }
    }

    public void cancelRequest(EventRequest request) {
        callbacks.remove(request);
        vm.eventRequestManager().deleteEventRequest(request);
    }

    public void check() {
        for ( StepRequest one : vm.eventRequestManager().stepRequests()) {
            util.Logger("[-]check step request:" + one.isEnabled());
        }
    }
    public EventCallback getCallback(EventRequest request) {
        return callbacks.get(request);
    }

    public boolean can_perform(abstractCommand cmd) {
        if (this.shell)
            return cmd.shell >= 0;
        return cmd.shell <= 0;
    }

    public void exit() {
        if (vm == null) {
            System.err.println("you should connect to vm first!");
            return;
        }
        vm.exit(1);
    }

    public void close() { System.exit(0);}

    public DexBackedDexFile getDexfile() { return dexfile; }

    public VirtualMachine getVm() { return vm; }

    public ReferenceType getClassbyname(String name) {
        if (classesMap != null && classesMap.containsKey(name))
            return classesMap.get(name);
        getClasses();
        return classesMap.get(name);
    }

    public List<ReferenceType> getClasses() {
        classes = vm.allClasses();
        if (classesMap == null)
            classesMap = new HashMap<>();
        for (ReferenceType type : classes) {
            if (!classesMap.containsKey(type.name()))
                classesMap.put(type.name(), type);
        }
        return classes;
    }

    public SuspendState getState() { return state; }

    public boolean perform(String[] argv) {
        abstractCommand cmd = Main.ACTION_MAP.get(argv[0]);

        if (cmd == null) {
            System.err.println("command " + argv[0] + " is not supported!");
            return false;
        }

        if (!can_perform(cmd)) {
            if (this.shell)
                System.err.println("command " + argv[0] + " is not available in the shell.");
            else
                System.err.println("command " + argv[0] + " is only available in the shell.");
            return false;
        }

        if (vm == null) {
            connect();
            new EventThread(this).start();
        }

        if (vm == null) {
            System.err.println("can not connect to target!");
            return false;
        }

        String[] parameters = new String[argv.length-1];
        System.arraycopy(argv, 1, parameters, 0, parameters.length);
        cmd.perform(parameters, this);

        return true;
    }

    public boolean run() {
        if (vm == null) {
            connect();
            new EventThread(this).start();
        }

        if (vm == null) {
            System.err.println("can not connect to target!");
            return false;
        }

        return true;
    }

    public void rewriteDex(final onWriteListener listener) throws IOException {
        final DexRewriter rewriter = new DexRewriter(new RewriterModule() {
            @Nonnull
            @Override
            public Rewriter<Method> getMethodRewriter(@Nonnull Rewriters rewriters) {
                return new MethodRewriter(rewriters) {
                    @Nonnull
                    @Override
                    public Method rewrite(@Nonnull Method value) {
                        if (listener != null) {
                            final MethodImplementation implementation = listener.rewriteMethod(value);
                            if (implementation != null) {
                                return new RewrittenMethod(value){
                                    public MethodImplementation getImplementation() {
                                        return implementation;
                                    }
                                };
                            }
                        }
                        return new RewrittenMethod(value);
                    }
                };
            }

//            @Nonnull
//            public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters){
//                return new ClassDefRewriter(rewriters){
//                    @Nonnull
//                    @Override
//                    public ClassDef rewrite(@Nonnull ClassDef classDef) {
//                        return new RewrittenClassDef(classDef){
//                            @Nonnull
//                            @Override
//                            public Iterable<? extends Method> getVirtualMethods(){
//                                if (listener != null) {
//                                    final List<Method> addmethods = listener.addMethods(classDef);
//                                    if (addmethods != null && addmethods.size() > 0) {
//                                        Iterable<Method> tmp = new Iterable<Method>() {
//                                            @Override
//                                            public Iterator<Method> iterator() {
//                                                Iterable<? extends Method> iterator = classDef.getVirtualMethods();
//                                                return Iterators.concat(iterator.iterator(), addmethods.iterator());
//                                            }
//                                        };
//                                        return RewriterUtils.rewriteIterable(rewriters.getMethodRewriter(), tmp);
//                                    }
//                                }
//                                return RewriterUtils.rewriteIterable(rewriters.getMethodRewriter(), classDef.getVirtualMethods());
//                            }
//                        };
//                    }
//                };
//            }
        }) {
            @Nonnull
            @Override
            public DexFile rewriteDexFile(@Nonnull DexFile dexFile) {
                return new RewrittenDexFile(dexFile) {
                    @Nonnull
                    @Override
                    public Set<? extends ClassDef> getClasses() {
                        Set<? extends ClassDef> classes = dexFile.getClasses();
                        if (listener != null) {
                            Set<ClassDef> addclasses = listener.addClasses();
                            addclasses.addAll(classes);
                            return RewriterUtils.rewriteSet(getClassDefRewriter(), addclasses);
                        }
                        return RewriterUtils.rewriteSet(getClassDefRewriter(), dexfile.getClasses());
                    }
                };
            }
        };

        DexFile rewritefile = rewriter.rewriteDexFile(dexfile);
        DexPool.writeTo("out.dex", rewritefile);
    }

    protected abstract class RewrittenDexFile implements DexFile {
        @Nonnull protected final DexFile dexFile;

        public RewrittenDexFile(@Nonnull DexFile dexFile) {
            this.dexFile = dexFile;
        }
    }

    public interface onWriteListener {
        MethodImplementation rewriteMethod(Method value);
        //        List<Method> addMethods(ClassDef classdef);
        Set<ClassDef> addClasses();
    }

}
