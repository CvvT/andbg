package com.cc.debugger.scripts.regions;

import com.cc.debugger.scripts.node.BlockNode;

import java.util.List;

/**
 * Created by CwT on 16/3/5.
 */
public interface IRegion {

    IRegion getParent();

    void setParent(IRegion parent);

    List<IRegion> getChildren();

    boolean addChild(IRegion child);

    BlockNode getBlock();

    String baseString();
}
