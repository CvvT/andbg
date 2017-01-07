package com.cc.debugger.scripts;

import com.cc.debugger.Context;
import com.cc.debugger.cmd.command;
import com.cc.debugger.scripts.attr.MethodAttribute;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.visitors.ReflectionVisitor;
import com.cc.debugger.test.Testput;
import com.cc.debugger.util;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.util.MethodUtil;
import org.jf.dexlib2.writer.builder.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by CwT on 16/3/16.
 */
public class Simplify {

    private static Simplify instance;

    private String APKpath;
    private Context ctx = null;
    private StepSimplify callback;

    List<MethodEntity> methodsTotrace = new ArrayList<>(1);
    List<MethodEntity> decryptMethods = new ArrayList<>(1);
    Map<MethodEntity, MethodTracer> tracerMap = new HashMap<>(1);
    Map<MethodEntity, MethodTracer> pools = new HashMap<>(1);

    // execution task
    ExecutorService executor = Executors.newFixedThreadPool(3);
    List<Future<Boolean>> tasks = Lists.newArrayList();

    //parse visitors
    Decoder decoders = new Decoder(this);

    //added methods
    Map<MethodEntity, MethodImplementation> addMethods = new HashMap<>(1);
    Map<MethodEntity, String> nameMap = new HashMap<>(1);   //Adjust method name
    Set<String> nameSets = new HashSet<>(1);    //Contain all method names

    public static Simplify getInstance() {
        if (instance == null)
            instance = new Simplify();
        return instance;
    }

    public boolean init(String APKpath) {
        this.APKpath = APKpath;
        ctx = new Context();
        callback = new StepSimplify(this);
        if (!ctx.run())
            return false;

        if (APKpath != null) {
            if (!ctx.loadDex(APKpath))
                return false;
        } else {
            new command().perform(new String[]{"show"}, ctx);
            System.out.println("Please input the correct cookie:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String cookie = reader.readLine();
                new command().perform(new String[]{"dex", cookie}, ctx);
                System.out.println("Please input the path to the dumped dex file:");
                APKpath = reader.readLine();
                while (!ctx.loadDex(APKpath)) {
                    System.out.println("Please input the path to the dumped dex file:");
                    APKpath = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

//        for (MethodEntity each : methodsTotrace) {
//            tracerMap.put(each, new MethodTracer(ctx));
//        }
        for (ClassDef classDef : ctx.getDexfile().getClasses()) {
            if (classDef.getType().startsWith("Ljava")  // filter
                    || classDef.getType().startsWith("Landroid")
                    || classDef.getType().startsWith("Ldalvik"))
                continue;
            for (Method method : classDef.getMethods()) {
                MethodEntity entity = new MethodEntity(method);
                if (methodsTotrace.contains(entity)) {
//                    if (AccessFlags.ABSTRACT.isSet(method.getAccessFlags())
//                            || AccessFlags.NATIVE.isSet(method.getAccessFlags())) {
//                        methodsTotrace.remove(entity);
//                        System.err.println("Native/Abstract methods are not supported now : " + entity.getClassName());
//                        continue;
//                    }
                    tracerMap.put(entity, new MethodTracer(ctx, method));
                }
            }
        }
        return true;
    }

    public void run(String APKpath) {
        init(APKpath);

        for (Map.Entry<MethodEntity, MethodTracer> entry : tracerMap.entrySet()) {
            MethodEntity key = entry.getKey();
            entry.getValue().run(key.getClassName(), key.getMethodName(), key.getDescription(), callback);
        }
        System.out.println("Register Done");
        if (tracerMap.size() == 0) {
            close();
            ctx.close();
        }
    }

    public void close() {
        boolean errorOccurred = false;
        try {
            for (Future<Boolean> task: tasks) {
                while(true) {
                    try {
                        if (!task.get()) {
                            errorOccurred = true;
                        }
                    } catch (InterruptedException ex) {
                        continue;
                    } catch (ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                }
            }
        } finally {
            executor.shutdown();
        }

        // rewrite
        try {
            ctx.rewriteDex(new Context.onWriteListener() {
                @Override
                public MethodImplementation rewriteMethod(Method value) {
                    MethodEntity entity = new MethodEntity(value);
                    if (pools.keySet().contains(entity)) {
                        return pools.get(entity).getTargetMethod().getMethodData();
                    }
                    return null;
                }

                @Override
                public Set<ClassDef> addClasses() {
                    Set<ClassDef> addclasses = new HashSet<>(1);
                    List<String> staticparams = new ArrayList<>(1);
                    staticparams.add("[Ljava/lang/Object;");
                    List<String> instanceparams = new ArrayList<>(2);
                    instanceparams.add("Ljava/lang/Object;");
                    instanceparams.add("[Ljava/lang/Object;");
                    String returnType = "Ljava/lang/Object;";
                    List<BuilderMethod> methods = new ArrayList<>(addMethods.size());
                    for (Map.Entry<MethodEntity, MethodImplementation> entry : addMethods.entrySet()) {
                        MethodEntity entity = entry.getKey();
                        methods.add(BuilderReference.makeMethod(entity.getClassName(),
                                nameMap.get(entity),
                                AccessFlags.STATIC.isSet(entity.getAccessFlag()) ? staticparams : instanceparams,
                                returnType,
                                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                                entry.getValue()
                                ));
                    }
                    BuilderClassDef classDef = new BuilderClassDef(BuilderReference.makeTypeReference("LDeobfuscator;"),
                            AccessFlags.PUBLIC.getValue(),
                            BuilderReference.makeTypeReference("Ljava/lang/Object;"),
                            new BuilderTypeList(ImmutableList.<BuilderTypeReference>of()),
                            BuilderReference.makeStringReference("Deobfuscator.java"),
                            BuilderAnnotationSet.EMPTY,
                            new ArrayList<BuilderField>(0),
                            methods
                    );
                    addclasses.add(classDef);
                    return addclasses;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addtask(final MethodTracer methodTracer, MethodEntity entity) {
        tasks.add(executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
//                System.out.println(Testput.NodeTree(methodTracer.getTracers()));
                new Deobfuscator().perform(methodTracer.getTargetMethod(), methodTracer.getTracers());
                return true;
            }
        }));
        pools.put(entity, methodTracer);
    }

    /**
     * Add new method and adjust the method name to avoid override
     * @param mattr
     */
    public synchronized void addMethod(MethodAttribute mattr) {
        MethodEntity entity = new MethodEntity();
        entity.setClassName("LDeobfuscator;");
        entity.setMethodName(mattr.getMethodName());
        StringBuilder sBuilder = new StringBuilder(mattr.getParams().size()+3);
        sBuilder.append("(");
        for (CharSequence type: mattr.getParams()){
            sBuilder.append(type);
        }
        sBuilder.append(")");
        sBuilder.append(mattr.getReturntype());
        entity.setDescription(sBuilder.toString());
        entity.setAccessFlag(mattr.getModifier());
        if (!addMethods.containsKey(entity)) {
            addMethods.put(entity, ReflectionVisitor.makeMethodimpl(mattr));
            if (nameSets.contains(entity.getMethodName())) {
                String newName = entity.getMethodName() + nameMap.size();
                nameMap.put(entity, newName);   //We don't need to add it to name set
            } else {
                nameMap.put(entity, entity.getMethodName());
                nameSets.add(entity.getMethodName());
            }
        }
        mattr.setMethodName(nameMap.get(entity));
    }

    /**
     *
     * @param description format: classType->MethodName(Parameters' Type)Return Type
     */
    public MethodEntity parseMethod(String description) {
        MethodEntity entity = new MethodEntity();
        String res[] = description.split("->");
        entity.setClassName(res[0]);
        entity.setMethodName(res[1].substring(0, res[1].indexOf('(')));
        entity.setDescription(res[1].substring(res[1].indexOf('(')));
        return entity;
    }

    public void addMethodtoTrace(String description) {
        methodsTotrace.add(parseMethod(description));
    }

    public void addDecryptMethod(String description) {
        decryptMethods.add(parseMethod(description));
    }

    public Context getCtx() {
        return ctx;
    }

    public List<MethodEntity> getDecryptMethods() {
        return decryptMethods;
    }

    public Map<MethodEntity, MethodTracer> getTracerMap() {
        return tracerMap;
    }

    class StepSimplify implements MethodTracer.StepListener {

        private Simplify simplify;

        public StepSimplify(Simplify simplify) {
            this.simplify = simplify;
        }

        @Override
        public void enter(MethodEntryEvent event) {
            MethodEntity entity = new MethodEntity();
            com.sun.jdi.Method method = event.method();
            entity.setClassName(method.declaringType().signature());
            entity.setMethodName(method.name());
            entity.setDescription(method.signature());
            MethodTracer tracer = simplify.getTracerMap().get(entity);

            if (tracer != null) {
                parse(tracer, event.thread(), event.location());
            }
        }

        @Override
        public void step(StepEvent event) {
            MethodEntity entity = new MethodEntity();
            com.sun.jdi.Method method = event.location().method();
            entity.setClassName(method.declaringType().signature());
            entity.setMethodName(method.name());
            entity.setDescription(method.signature());
            MethodTracer tracer = simplify.getTracerMap().get(entity);
            if (tracer != null) {
                parse(tracer, event.thread(), event.location());
            }
        }

        @Override
        public void exit(MethodExitEvent event) {
            MethodEntity entity = new MethodEntity();
            com.sun.jdi.Method method = event.method();
            entity.setClassName(method.declaringType().signature());
            entity.setMethodName(method.name());
            entity.setDescription(method.signature());
            simplify.addtask(simplify.getTracerMap().get(entity), entity);
            simplify.getTracerMap().remove(entity);
            if (simplify.getTracerMap().size() == 0) {
                simplify.close();
                ctx.close();
            }
        }
    }

    private void parse(MethodTracer tracer, ThreadReference thread, Location location) {
        if (location.codeIndex() == -1) // within a native method
            return;
        MethodLocation insn = tracer.getInsn(location);
        InsnNode node = new InsnNode(insn);
        tracer.addTrace(node);
        decoders.perform(tracer, thread, location, node);
//        util.Logger(insn.getInstruction().getOpcode().name);
    }

    public static void main(String []argv) {
        Simplify simplify = Simplify.getInstance();
        simplify.addMethodtoTrace("Lcom/cc/obfuscationtest/MainActivity;->dowhile(II)I");
//        simplify.addMethodtoTrace("Lcom/cc/obfuscationtest/Reflection;->reflectfunc()V");
        simplify.addMethodtoTrace("Lcom/cc/obfuscationtest/MainActivity;->helloWorld()Ljava/lang/String;");

        simplify.addDecryptMethod("Lcom/cc/obfuscationtest/Base64;->decode([B)[B");

        simplify.run("/Users/CwT/Desktop/app-debug.apk");
//        simplify.run(null);
    }
}
