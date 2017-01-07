package com.cc.debugger.scripts.node;

import com.cc.debugger.scripts.Utility;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.attr.AttributeStorage;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by CwT on 16/2/27.
 */
public class MethodNode extends AttributeStorage{

    private final MutableMethodImplementation methodData;
    private final Method method;

    private List<BlockNode> blocks;
    private BlockNode enterBlock;
    private List<BlockNode> exitBlocks;

    public MethodNode(@Nonnull Method method) {
        this.method = method;
        this.methodData = new MutableMethodImplementation(method.getImplementation());
    }

    public void initBasicBlocks() {
        blocks = new ArrayList<>();
        exitBlocks = new ArrayList<>(1);
    }

    public BlockNode getContainBlock(MethodLocation location) {
        for (BlockNode node : getBasicBlocks()) {
            if (node.isContain(location))
                return node;
        }
        return null;
    }

    public BlockNode getNextBlock(BlockNode cur, MethodLocation insn) {
        for (BlockNode next : cur.getSuccessors()) {
            // If we did not find the instruction in the current block, it means we just jumped into
            // one of successor blocks.
            if (next.getInstructions().get(0).getIndex() == insn.getIndex())
                return next;
        }
        throw new RuntimeException("We did not find the next block!!");
    }

    public int getParamterRegisterCount() {
        return Utility.registerCount(method.getParameterTypes());
    }

    public int getAccessFlags() {
        return method.getAccessFlags();
    }

    public MutableMethodImplementation getMethodData() {
        return methodData;
    }

    public List<BlockNode> getBasicBlocks() {
        return blocks;
    }

    public BlockNode getEnterBlock() {
        return enterBlock;
    }

    public void setEnterBlock(BlockNode enterBlock) {
        this.enterBlock = enterBlock;
    }

    public List<BlockNode> getExitBlocks() {
        return exitBlocks;
    }

    public void addExitBlock(BlockNode exitBlock) {
        this.exitBlocks.add(exitBlock);
    }

    public List<MethodLocation> getInstructions() {
        return methodData.getInstructionList();
    }

    public MethodLocation getMethodLocationbyindex(int index) { return getInstructions().get(index); }

    public List<TryNode> getTryBlocks() { return TryNode.getTryCatches(methodData.getTryBlocks()); }
}
