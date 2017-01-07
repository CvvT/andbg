package com.cc.debugger.scripts;

import com.cc.debugger.scripts.decode.AbstractDecode;
import com.cc.debugger.scripts.decode.DecryptDecode;
import com.cc.debugger.scripts.decode.ReflectionDecode;
import com.cc.debugger.scripts.node.InsnNode;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/3/16.
 */
public class Decoder {
    private List<AbstractDecode> decodes = new ArrayList<>(2);

    public Decoder(Simplify simplify) {
        decodes.add(new DecryptDecode(simplify));
        decodes.add(new ReflectionDecode(simplify));
    }

    public void perform(MethodTracer tracer, ThreadReference thread, Location location, InsnNode node) {
        for (AbstractDecode decode : decodes) {
            decode.parse(tracer, thread, location, node);
        }
    }
}
