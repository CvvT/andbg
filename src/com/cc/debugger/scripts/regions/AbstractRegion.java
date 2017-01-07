package com.cc.debugger.scripts.regions;

import com.cc.debugger.scripts.attr.AttributeStorage;
import com.cc.debugger.scripts.node.BlockNode;

/**
 * Created by CwT on 16/3/5.
 */
public abstract class AbstractRegion extends AttributeStorage implements IRegion{

    private IRegion parent;
    private BlockNode block;

    public AbstractRegion(BlockNode node, IRegion parent) {
        this.block = node;
        this.parent = parent;
    }

    public IRegion getParent() {
        return parent;
    }

    public void setParent(IRegion parent) {
        this.parent = parent;
    }

    @Override
    public BlockNode getBlock() {
        return block;
    }

    public void setBlock(BlockNode block) {
        this.block = block;
    }
}
