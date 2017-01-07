package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.iface.EventCallback;
import com.cc.debugger.scripts.node.MethodNode;
import com.google.common.collect.ImmutableList;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.*;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.util.InstructionOffsetMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/2/25.
 */
public class method_trace extends abstractCommand{

    static List<String> aliases = null;

    static {
        aliases = new ArrayList<>();
        aliases.add("mt");
        aliases.add("mtrace");
    }

    public method_trace() {
        super("<className> <methodName>", 0, aliases);
    }

    @Override
    public void perform(String[] argv, final Context ctx) {

        if (argv.length < 2) {
            System.err.println("We need more arguments!!!");
            return;
        }

        String className = argv[0];
        final String methodName = argv[1];

    }
}
