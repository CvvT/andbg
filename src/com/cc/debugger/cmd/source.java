package com.cc.debugger.cmd;

import com.cc.debugger.Context;

/**
 * Created by CwT on 16/2/25.
 */
public class source extends abstractCommand {

    public source() {
        super("<path to the apk or dex file>", 1, null);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        if (argv.length > 0) {
            if (ctx.loadDex(argv[0]))
                System.out.println("attach dex file success!!");
            else
                System.out.println("attach dex file failed");
        }
    }
}
