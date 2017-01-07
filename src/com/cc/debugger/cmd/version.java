package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.sun.jdi.VirtualMachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/2/22.
 */
public class version extends abstractCommand {

    public static List<String> aliases;

    static {
        aliases = new ArrayList<>();
        aliases.add("v");
    }

    public version(){
        super("", 0, aliases, true);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        System.out.println(ctx.getVm().description());
    }
}
