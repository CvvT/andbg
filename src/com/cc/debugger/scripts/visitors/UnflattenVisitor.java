package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.scripts.regions.IRegion;
import com.cc.debugger.scripts.regions.LoopRegion;
import com.cc.debugger.scripts.regions.MulRegion;
import com.cc.debugger.scripts.regions.Region;
import org.jf.dexlib2.builder.MethodLocation;
import sun.jvm.hotspot.opto.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CwT on 16/4/21.
 */
public class UnflattenVisitor extends AbstractVisitor {


    @Override
    public void init(MethodNode node, Tracer tracer) {

    }

    @Override
    public void visit(MethodNode node, Tracer tracer) {
        IRegion root = tracer.getRoot();
        int registerCount = node.getMethodData().getRegisterCount();
        // for now, we just deal with int value for constant propagation
        Map<Integer, Integer> valueMap = new HashMap<>(registerCount);

    }

    private static BlockNode collect(IRegion region, BlockNode lastBlock, List<BlockNode> list) {
        BlockNode curBlock = region.getBlock();
        if (curBlock != null && curBlock != lastBlock) {
            list.add(curBlock);
        } else {
            curBlock = lastBlock;
        }

        if (region.getChildren() != null)
            for (IRegion child : region.getChildren()) {
                curBlock = collect(child, curBlock, list);
            }
        return curBlock;
    }

    public static void generateChain(MethodNode node, LoopRegion loop) {
        List<BlockNode> chain = new ArrayList<>();
        collect(loop, null, chain);

        BlockNode prev = null;
        for (BlockNode block : chain) {
            BlockNode newBlock = BlockSpiltVisitor.startNewBlock(node, -1);
            newBlock.mergeInsn(block);

            for (BlockNode successor : block.getSuccessors()) {
                newBlock.getSuccessors().add(successor);
            }

            if (prev != null) {
                prev.getSuccessors().remove(block);
                prev.getSuccessors().add(newBlock);
            }
            prev = newBlock;
        }


    }

    public static void visit(MethodNode node, IRegion root, Map<Integer, Integer> values) {
        if (root instanceof LoopRegion) {
            if (((LoopRegion) root).contains(AFlag.REMOVE_FOR_FLAT)) {

            }
        }
    }

    private static BlockNode getFirstBlock(IRegion region) {
        if (region.getBlock() != null) {
            return region.getBlock();
        }

        if (region.getChildren() != null)
            for (IRegion child : region.getChildren()) {
                BlockNode ret = getFirstBlock(child);
                if (ret != null)
                    return ret;
            }

        return null;
    }

}
