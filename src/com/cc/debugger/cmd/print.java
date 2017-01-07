package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/4/25.
 */
public class print extends abstractCommand {
    static List<String> aliases = null;

    static {
        aliases = new ArrayList<>();
        aliases.add("p");
    }

    public print() { super("[index]", 1, aliases); }

    private boolean checkSuspend(Context ctx) {
        if (!ctx.getState().isSuspend()) {
            System.err.println("suspend thread first!");
            return false;
        }
        return true;
    }

    @Override
    public void perform(String[] argv, Context ctx) {

        if (argv.length > 0) {
            switch (argv[0]) {
                case "index":
                    if (checkSuspend(ctx))
                        util.Logger("current location:" + ctx.getState().location.codeIndex());
                    break;
                case "state":
                    util.Logger("current thread:" + (ctx.getState().isSuspend() ? "suspend" : "running"));
                    break;
                case "method":
                    if (checkSuspend(ctx))
                        util.Logger("current method:" + ctx.getState().location.method().name());
                    break;
            }
        }
    }
}
