package com.cc.debugger.scripts;

import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.regions.IRegion;
import com.cc.debugger.scripts.regions.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/3/5.
 */
public class Tracer {
    List<InsnNode> tracer = new ArrayList<>();
    IRegion root;

    public void setRoot(IRegion root) {
        this.root = root;
    }

    public IRegion getRoot() {
        return root;
    }

    public List<InsnNode> getTracer() {
        return tracer;
    }

    public void add(InsnNode node) {
        tracer.add(node);
    }
}
