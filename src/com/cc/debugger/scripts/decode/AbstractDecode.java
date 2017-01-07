package com.cc.debugger.scripts.decode;

import com.cc.debugger.scripts.MethodTracer;
import com.cc.debugger.scripts.Simplify;
import com.cc.debugger.scripts.node.InsnNode;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;

/**
 * Created by CwT on 16/3/16.
 */
public abstract class AbstractDecode {

    protected Simplify simplify;

    public AbstractDecode(Simplify simplify) {
        this.simplify = simplify;
    }

    public abstract void parse(MethodTracer tracer, ThreadReference thread, Location location, InsnNode node);
}
