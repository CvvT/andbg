package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.sun.jdi.ThreadReference;

import java.util.ArrayList;

/**
 * Created by CwT on 16/2/23.
 */
public class resume extends abstractCommand {

    public resume() {
        super("[all]", 1, null);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        if (argv.length > 0) {
            if (argv[0].equals("all"))
                ctx.getVm().resume();
        } else {
            ctx.getState().resume();
        }
    }
}
