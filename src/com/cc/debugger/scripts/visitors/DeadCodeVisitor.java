package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.MethodNode;

import java.util.List;

/**
 * Created by CwT on 16/3/2.
 */
public class DeadCodeVisitor extends AbstractVisitor {
    @Override
    public void init(MethodNode node, Tracer tracer) {

    }

    @Override
    public void visit(MethodNode node, Tracer tracer) {
        BlockNode enter = node.getEnterBlock();
        enter.add(AFlag.REACHED);
        markreached(enter);
    }

    private void markreached(BlockNode node) {
        for (BlockNode block : node.getSuccessors()) {
            if (block.contains(AFlag.REACHED))
                continue;
            block.add(AFlag.REACHED);
            markreached(block);
        }
    }
}
