package com.cc.debugger.scripts;

import com.cc.debugger.Context;
import com.cc.debugger.cmd.method_trace;
import com.cc.debugger.cmd.register;
import com.cc.debugger.iface.EventCallback;
import com.cc.debugger.impl.MethodCallback;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.attr.AType;
import com.cc.debugger.scripts.attr.MethodState;
import com.cc.debugger.scripts.attr.ValueAttrubite;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.InsnType;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.test.Testput;
import com.cc.debugger.util;
import com.google.common.collect.ImmutableList;
import com.sun.jdi.Location;
import com.sun.jdi.Value;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.*;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.util.InstructionOffsetMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/2/26.
 */
public class MethodTracer {

    Context ctx = null;
    MethodNode targetMethod = null;
    Tracer tracers = new Tracer();
    InstructionOffsetMap offsetMap = null;
    MethodState mstate = null;

    public MethodTracer(Context ctx) {
        this.ctx = ctx;
        mstate = new MethodState();
    }

    public MethodTracer(Context ctx, Method method) {
        this.ctx = ctx;
        targetMethod = new MethodNode(method);
        offsetMap = new InstructionOffsetMap(ImmutableList.copyOf(targetMethod.getMethodData().getInstructions()));
        mstate = new MethodState();
    }

    public void update(Method method) {
        targetMethod = new MethodNode(method);
        offsetMap = new InstructionOffsetMap(ImmutableList.copyOf(targetMethod.getMethodData().getInstructions()));
    }

    public void addTrace(InsnNode insn) {
        tracers.add(insn);
    }

    public Tracer getTracers() {
        return tracers;
    }

    public MethodState getMstate() {
        return mstate;
    }

    public MethodLocation getInsn(Location location) {
        int index = offsetMap.getInstructionIndexAtCodeOffset((int)location.codeIndex());
        return targetMethod.getMethodLocationbyindex(index);
    }

    public MethodNode getTargetMethod() {
        return targetMethod;
    }

    public void run(String className, final String methodName, final String dec, final StepListener listener) {

//        String className = "com.cc.obfuscationtest.MainActivity";
//        final String methodName = "dowhile";
        className = className.replaceAll("/", ".");
        if (className.charAt(className.length()-1) == ';')
            className = className.substring(1, className.length()-1);
        final String clsName = className;

        ctx.registerEnterMethod(clsName, methodName, dec, new EventCallback() {
            @Override
            public void handleEvent(Event event) {
                MethodEntryEvent mee = (MethodEntryEvent) event;
                util.Logger("Enter method:" + methodName);
                ctx.registerStep(mee.thread(), StepRequest.STEP_OVER, -1, new StepHandler(listener));
                ctx.cancelMethod(event.request(), clsName, methodName, dec, MethodCallback.METHOD_ENTER);
                if (listener != null)
                    listener.enter(mee);
                mee.thread().resume();
            }
        });

        ctx.registerExitMethod(clsName, methodName, dec, new EventCallback() {
            @Override
            public void handleEvent(Event event) {
                MethodExitEvent mee = (MethodExitEvent) event;
                util.Logger("Exit method");
                ctx.cancelStep(mee.thread());
                ctx.cancelMethod(mee.request(), clsName, methodName, dec, MethodCallback.METHOD_EXIT);
                if (listener != null)
                    listener.exit(mee);
                mee.thread().resume();
            }
        });
    }

    public InsnNode getLastReplace() {
        List<InsnNode> tracer = tracers.getTracer();
        int size = tracer.size();
        for (int i = size - 1; i >= 0; i--)
            if (tracer.get(i).contains(AFlag.REPLACE_WITH_CONSTANT))
                return tracer.get(i);
        return null;
    }

    private void test(List<InsnNode> tracer) {
        for (InsnNode node : tracer) {
            System.out.println(node.getInstruction().getIndex() + ": " + node.getInstruction().getCodeAddress()
                + node.getInstruction().getInstruction().getOpcode().name);
        }
    }

    class StepHandler implements EventCallback {

        StepListener listener;

        public StepHandler(StepListener listener) { this.listener = listener; }

        public StepHandler() {}

        @Override
        public void handleEvent(Event event) {
            StepEvent se = (StepEvent)event;
//            long index = se.location().codeIndex();
//            System.out.println("index: " + index);
            if (listener != null) {
                listener.step(se);
            }
            if (se.thread().isSuspended())
                se.thread().resume();
            else {
                util.Logger("[Method Step]thread is not suspend");
            }
        }
    }

    public interface StepListener {
        void enter(MethodEntryEvent event);
        void step(StepEvent event);
        void exit(MethodExitEvent event);
    }

}
