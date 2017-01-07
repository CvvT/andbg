package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.scripts.samples.DumpDex;

/**
 * Created by CwT on 16/5/3.
 */
public class sample extends abstractCommand {

    public sample() {
        super("<sample name> (dumpdex)", -1, null);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        new DumpDex().run(ctx);
    }
}
