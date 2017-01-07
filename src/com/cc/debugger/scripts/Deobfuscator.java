package com.cc.debugger.scripts;

import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.scripts.visitors.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/3/1.
 */
public class Deobfuscator {

    private List<AbstractVisitor> visitors;

    /**
     * add visitors in order
     */
    public Deobfuscator() {
        visitors = new ArrayList<>();
        visitors.add(new BlockSpiltVisitor());
        visitors.add(new LiveVarVisitor());
        visitors.add(new ReplaceVisitor());
        visitors.add(new TargetRemoveVisitor());

        visitors.add(new DominatorVisitor());
        visitors.add(new RegionMakerVisitor());
        visitors.add(new MarkFlattenVisitor());
//        visitors.add(new TraceLiveVisitor());
        visitors.add(new LiveVarVisitor());

        visitors.add(new ReflectionVisitor());
        visitors.add(new LiveVarVisitor());

    }

    public void perform(MethodNode node, Tracer tracer) {
        for (AbstractVisitor visitor : visitors) {
            visitor.init(node, tracer);
            visitor.visit(node, tracer);
        }
    }
}
