package com.cc.debugger.scripts.regions;

import com.cc.debugger.scripts.node.BlockNode;

import java.util.List;

/**
 * Created by CwT on 16/3/5.
 */
public class LoopRegion extends MulRegion {

    public LoopRegion(BlockNode node, IRegion parent) {
        super(node, parent);
    }

    public LoopRegion(IRegion parent) {
        super(null, parent);
    }

    @Override
    public String baseString() {
        return "LOOPBLOCK";
    }
}
