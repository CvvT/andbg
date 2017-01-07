package com.cc.debugger.obfuscator;

import com.cc.debugger.scripts.visitors.AbstractVisitor;
import com.cc.debugger.scripts.visitors.BlockSpiltVisitor;
import com.cc.debugger.scripts.visitors.LiveVarVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/3/19.
 */
public class Obfuscation {

    private List<AbstractVisitor> obfuscators;

    public Obfuscation() {
        obfuscators = new ArrayList<>();
        obfuscators.add(new BlockSpiltVisitor());
        obfuscators.add(new LiveVarVisitor());
    }
}
