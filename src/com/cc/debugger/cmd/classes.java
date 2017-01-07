package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.sun.jdi.ReferenceType;

import java.util.ArrayList;

/**
 * Created by CwT on 16/2/23.
 */
public class classes extends abstractCommand{

    public classes() {
        super("[<partial class name>]", 0, null, true);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        for (ReferenceType type : ctx.getClasses()) {
            String name = type.name();

            if (argv.length > 0) {
                if (name.contains(argv[0]))
                    System.out.println(name);
            } else {
                System.out.println(name);
            }
        }
    }
}
