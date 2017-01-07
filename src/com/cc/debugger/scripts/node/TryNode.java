package com.cc.debugger.scripts.node;

import org.jf.dexlib2.builder.BuilderExceptionHandler;
import org.jf.dexlib2.builder.BuilderTryBlock;
import org.jf.dexlib2.builder.Label;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by CwT on 16/2/28.
 */
public class TryNode {
    @Nonnull
    public final List<BuilderExceptionHandler> exceptionHandlers;
    @Nonnull public final Label start;
    // The end location is exclusive, it should point to the codeAddress of the instruction immediately after the last
    // covered instruction.
    @Nonnull public final Label end;

    public TryNode(BuilderTryBlock block) {
        this.start = block.start;
        this.end = block.end;
        this.exceptionHandlers = new ArrayList<>();
        this.exceptionHandlers.add(block.exceptionHandler);
    }

    public static List<TryNode> getTryCatches(List<BuilderTryBlock> tryBlocks) {
        List<TryNode> nodes = new ArrayList<>();
        Map<TryNode, TryNode> maps = new HashMap<>();
        for (BuilderTryBlock tryBlock : tryBlocks) {
            TryNode node = new TryNode(tryBlock);
            if (maps.containsKey(node)) {
                maps.get(node).exceptionHandlers.add(tryBlock.exceptionHandler);
            } else {
                nodes.add(node);
                maps.put(node, node);
            }
        }
        return nodes;
    }

    public List<BuilderExceptionHandler> getExceptionHandlers() {
        return exceptionHandlers;
    }

    @Override
    public int hashCode() {
        return this.start.hashCode() + this.end.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TryNode) {
            TryNode other = (TryNode)o;
            if (other.start.getCodeAddress() == start.getCodeAddress()
                    && other.end.getCodeAddress() == end.getCodeAddress()) {
                return true;
            }
        }
        return false;
    }
}
