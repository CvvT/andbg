package com.cc.debugger.scripts.regions;

import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.InsnNode;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/3/5.
 */
public class IfRegion extends Region {

    @NotNull List<IRegion> regions = new ArrayList<>(1);

    public IfRegion(InsnNode insn, BlockNode node, IRegion parent) {
        super(insn, node, parent);
    }

    @Override
    public List<IRegion> getChildren() {
        return regions;
    }

    @Override
    public boolean addChild(IRegion child) {
        return regions.add(child);
    }

}
