package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.utils.EmptyBitSet;

import java.util.BitSet;
import java.util.List;

/**
 * Created by CwT on 16/3/4.
 */
public class DominatorVisitor extends AbstractVisitor {
    @Override
    public void init(MethodNode node, Tracer tracer) {

    }

    @Override
    public void visit(MethodNode node, Tracer tracer) {
        processBlocksTree(node);
    }

    private static void processBlocksTree(MethodNode mth) {
        computeDominators(mth);
        computePDominanceFrontier(mth);
        computeBlockCD(mth);
    }

    //post dominators
    private static void computeDominators(MethodNode mth) {
        List<BlockNode> basicBlocks = mth.getBasicBlocks();
        int nBlocks = basicBlocks.size();
        for (BlockNode block : basicBlocks) {
            block.setPdoms(new BitSet(nBlocks));
            block.getPdoms().set(0, nBlocks);
        }

        for (BlockNode exitBlock : mth.getExitBlocks()) {
            exitBlock.getPdoms().clear();
            exitBlock.getPdoms().set(exitBlock.getId());
        }

        BitSet dset = new BitSet(nBlocks);
        boolean changed;
        do {
            changed = false;
            for (BlockNode block : basicBlocks) {
                if (mth.getExitBlocks().contains(block)) {
                    continue;
                }
                BitSet d = block.getPdoms();
                if (!changed) {
                    dset.clear();
                    dset.or(d);
                }
                for (BlockNode pred : block.getSuccessors()) {
                    d.and(pred.getPdoms());
                }
                d.set(block.getId());
                if (!changed && !d.equals(dset)) {
                    changed = true;
                }
            }
        } while (changed);

        // clear self dominance
        for (BlockNode block : basicBlocks) {
            block.getPdoms().clear(block.getId());
        }

        //calculate immediate post dominators
        for (BlockNode block : basicBlocks) {
            if (mth.getExitBlocks().contains(block))
                continue;
            BlockNode ipdom = null;
            List<BlockNode> preds = block.getSuccessors();
            if (preds.size() == 0) {    // Exit Block
                continue;
            } else if (preds.size() == 1) {
                ipdom = preds.get(0);
            } else {
                BitSet bs = new BitSet((block.getPdoms().length()));
                bs.or(block.getPdoms());
                for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                    BlockNode pdom = basicBlocks.get(i);
                    bs.andNot(pdom.getPdoms());
                }
                if (bs.cardinality() != 1) {
                    throw new RuntimeException("Can't find immediate dominator for block " + block
                            + " in " + bs + " preds:" + preds);
                }
                ipdom = basicBlocks.get(bs.nextSetBit(0));
            }
            block.setIpdom(ipdom);
            ipdom.addPdominatesOn(block);
        }
    }

    private static void computePDominanceFrontier(MethodNode mth) {
        mth.getEnterBlock().setPdomFrontier(EmptyBitSet.EMPTY);
        for (BlockNode block : mth.getBasicBlocks()) {
            computeBlockPDF(mth, block);
        }
    }

    private static void computeBlockPDF(MethodNode mth, BlockNode block) {
        if (block.getPdomFrontier() != null) {
            return;
        }
        for (BlockNode c : block.getDominatesOn()) {
            computeBlockPDF(mth, c);
        }
        List<BlockNode> blocks = mth.getBasicBlocks();
        BitSet pdomFrontiier = null;    //where dominance stops
        for (BlockNode s : block.getPredecessors()) {
            if (s.getIpdom() != block) {
                if (pdomFrontiier == null) {
                    pdomFrontiier = new BitSet(blocks.size());
                }
                pdomFrontiier.set(s.getId());
            }
        }
        for (BlockNode c : block.getDominatesOn()) {
            BitSet frontier = c.getPdomFrontier();
            for (int p = frontier.nextSetBit(0); p >= 0; p = frontier.nextSetBit(p + 1)) {
                BlockNode z = blocks.get(p);
                if (z.getIpdom() != block && z != block) {
                    if (pdomFrontiier == null) {
                        pdomFrontiier = new BitSet(blocks.size());
                    }
                    pdomFrontiier.set(p);
                }
            }
        }
        if (pdomFrontiier == null || pdomFrontiier.cardinality() == 0) {
            pdomFrontiier = EmptyBitSet.EMPTY;
        }
        block.setPdomFrontier(pdomFrontiier);
    }

    /**
     * set control dependency block
     * @param mth
     */
    private static void computeBlockCD(MethodNode mth) {
        List<BlockNode> blocks = mth.getBasicBlocks();
        int size = blocks.size();
        for (BlockNode node : blocks) {
            node.setCdblocks(new BitSet(size));
        }
        for (BlockNode node : blocks) {
            BitSet dfrontier = node.getPdomFrontier();
            for (int i = dfrontier.nextSetBit(0); i >= 0; i = dfrontier.nextSetBit(i + 1)) {
                blocks.get(i).getCdblocks().set(node.getId());
            }
        }
    }
}
