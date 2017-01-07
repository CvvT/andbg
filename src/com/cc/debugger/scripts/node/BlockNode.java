package com.cc.debugger.scripts.node;

import com.cc.debugger.scripts.attr.AttributeStorage;
import org.jf.dexlib2.builder.MethodLocation;
import sun.jvm.hotspot.opto.Block;

import java.util.*;

/**
 * Created by CwT on 16/2/27.
 */
public class BlockNode extends AttributeStorage {

    private int id;
    private final int startOffset;
    private List<MethodLocation> instructions = new ArrayList<>();

    private List<BlockNode> predecessors = new ArrayList<BlockNode>(1);
    private List<BlockNode> successors = new ArrayList<BlockNode>(1);

    private BitSet inSet;
    private BitSet outSet;
//    private List<BlockNode> cleanSuccessors;

    // all dominators
    private BitSet pdoms;
    // dominance frontier
    private BitSet pdomFrontier;
    private BlockNode ipdom;
    // blocks on which dominates this block
    private List<BlockNode> dominatesOn = Collections.emptyList();
    // control dependency
    private BitSet cdblocks;
    // immediate control dependent block
    private BlockNode icdblock;

    public BlockNode(int id, int offset) {
        this.id = id;
        this.startOffset = offset;
    }

    public void addPdominatesOn(BlockNode block) {
        if (dominatesOn.isEmpty()) {
            dominatesOn = new LinkedList<>();
        }
        dominatesOn.add(block);
    }

    public void clear() {
        predecessors.clear();
        successors.clear();
        instructions.clear();
    }

    /**
     *
     * @param block abandoned block
     */
    public void mergeinto(BlockNode block) {
        if (block == null)
            return;

        if (getPredecessors().contains(block))
            getPredecessors().remove(block);
        if (getSuccessors().contains(block))
            getSuccessors().remove(block);

        for (BlockNode pre : block.getPredecessors()) {
            if (pre != this && !getPredecessors().contains(pre)) {
                getPredecessors().add(pre);
                pre.getSuccessors().add(this);
            }
            pre.getSuccessors().remove(block);
        }
        for (BlockNode successor : block.getSuccessors()) {
            if (successor != this && !getSuccessors().contains(successor)) {
                getSuccessors().add(successor);
                successor.getPredecessors().add(this);
            }
            successor.getPredecessors().remove(block);
        }
        block.clear();
    }

    public void mergeintoWithInsn(BlockNode block) {
        mergeInsn(block);
        mergeinto(block);
    }

    public void mergeInsn(BlockNode block) {
        for (MethodLocation location : block.getInstructions()) {
            if (location.getInstruction() != null)
                getInstructions().add(location);
        }
    }

    public BitSet getCdblocks() {
        return cdblocks;
    }

    public BlockNode getIcdblock() {
        return icdblock;
    }

    public void setCdblocks(BitSet cdblocks) {
        this.cdblocks = cdblocks;
    }

    public void setIcdblock(BlockNode icdblock) {
        this.icdblock = icdblock;
    }

    public BlockNode getIpdom() {
        return ipdom;
    }

    public void setIpdom(BlockNode ipdom) {
        this.ipdom = ipdom;
    }

    public List<BlockNode> getDominatesOn() {
        return dominatesOn;
    }

    public BitSet getPdomFrontier() {
        return pdomFrontier;
    }

    public void setPdomFrontier(BitSet pdomFrontier) {
        this.pdomFrontier = pdomFrontier;
    }

    public BitSet getPdoms() {
        return pdoms;
    }

    public void setPdoms(BitSet pdoms) {
        this.pdoms = pdoms;
    }

    public boolean isDominator(BlockNode block) {
        return pdoms.get(block.getId());
    }

    public boolean isContain(MethodLocation location) {
        return instructions.contains(location);
    }

    public BitSet getInSet() {
        return inSet;
    }

    public BitSet getOutSet() {
        return outSet;
    }

    public void setInSet(BitSet inSet) {
        this.inSet = inSet;
    }

    public void setOutSet(BitSet outSet) {
        this.outSet = outSet;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<BlockNode> getPredecessors() {
        return predecessors;
    }

    public List<BlockNode> getSuccessors() {
        return successors;
    }

    public List<MethodLocation> getInstructions() {
        return instructions;
    }

    public MethodLocation getLastInsn() {
        return instructions.get(instructions.size()-1);
    }
}
