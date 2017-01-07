package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.node.MethodNode;

/**
 * Created by CwT on 16/2/28.
 */
public abstract class AbstractVisitor {

    public abstract void init(MethodNode node, Tracer tracer);
    public abstract void visit(MethodNode node, Tracer tracer);
}
