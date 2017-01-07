package com.cc.debugger.scripts.regions;

import com.cc.debugger.scripts.node.BlockNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/3/5.
 */
public class MulRegion extends AbstractRegion {
    List<IRegion> regions = new ArrayList<>(1);

    public MulRegion(BlockNode node, IRegion parent) {
        super(node, parent);
    }

    public MulRegion(IRegion parent) {
        super(null, parent);
    }

    @Override
    public List<IRegion> getChildren() {
        return regions;
    }

    @Override
    public boolean addChild(IRegion child) {
        return regions.add(child);
    }

    @Override
    public String baseString() {
        return "MULBLOCK";
    }
}
