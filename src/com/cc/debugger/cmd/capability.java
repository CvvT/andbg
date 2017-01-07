package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.sun.jdi.VirtualMachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/4/4.
 */
public class capability extends abstractCommand{

    static List<String> aliases = null;

    static {
        aliases = new ArrayList<>();
        aliases.add("cap");
    }

    public capability() {
        super("", 0, aliases, true);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        VirtualMachine vm = ctx.getVm();
        System.out.println("Can add method:" + vm.canAddMethod());
        System.out.println("Can force early return:" + vm.canForceEarlyReturn());
        System.out.println("Can get bytecodes:" + vm.canGetBytecodes());
        System.out.println("Can redefine classes:" + vm.canRedefineClasses());
        System.out.println("Can get method return values:" + vm.canGetMethodReturnValues());
        System.out.println("Can be modified:" + vm.canBeModified());
    }
}
