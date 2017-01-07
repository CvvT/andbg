package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.node.MethodNode;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31t;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by CwT on 16/3/2.
 */
public class TargetRemoveVisitor extends AbstractVisitor {

    @Override
    public void init(MethodNode node, Tracer tracer) {

    }

    @Override
    public void visit(MethodNode node, Tracer tracer) {
        Set<MethodLocation> sets = new HashSet<>();
        List<MethodLocation> remover = new ArrayList<>();
        for (MethodLocation location : node.getInstructions()) {
            if (location.getInstruction() == null)
                continue;
            switch (location.getInstruction().getOpcode()) {
                case FILL_ARRAY_DATA:
                    MethodLocation loc = ((BuilderInstruction31t) location.getInstruction()).getTarget().getLocation();
                    sets.add(loc);
                    break;
                case ARRAY_PAYLOAD:
                    if (!sets.contains(location))
                        remover.add(location);
                    break;
            }
        }
        node.getMethodData().removeInstructions(remover);
    }
}
