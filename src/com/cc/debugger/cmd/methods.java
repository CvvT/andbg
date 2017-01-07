package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;

import java.util.ArrayList;

/**
 * Created by CwT on 16/2/23.
 */
public class methods extends abstractCommand {

    public methods() {
        super("", 0, null, true);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        if (argv.length <= 0) {
            System.err.println("We need more arguments!");
            return;
        }

        ReferenceType type = ctx.getClassbyname(argv[0]);
        if (argv.length > 1) {
            for (Method method : type.allMethods())
                if (method.name().contains(argv[1]))
                    System.out.println(method.name());
        } else {
            for (Method method : type.allMethods()) {
                System.out.println(method.name());
            }
        }
    }
}
