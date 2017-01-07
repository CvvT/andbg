package com.cc.debugger.scripts.regions;

import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.InsnNode;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.List;

/**
 * Created by CwT on 16/3/5.
 */
public class Region extends AbstractRegion {

    private InsnNode insn;

    public Region(InsnNode insn, BlockNode node, IRegion parent) {
        super(node, parent);
        this.insn = insn;
    }

    public InsnNode getInsn() {
        return insn;
    }

    @Override
    public List<IRegion> getChildren() {
        return null;
    }

    @Override
    public boolean addChild(IRegion child) {
        return false;
    }

    @Override
    public String baseString() {
        Instruction instruction = insn.getInstruction().getInstruction();
        if (instruction == null)
            return "null";
        return instruction.getOpcode().name();
    }
}
