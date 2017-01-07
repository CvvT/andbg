package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.iface.CommandState;
import com.sun.jdi.VirtualMachine;

import java.util.List;

/**
 * Created by CwT on 16/2/22.
 */
public abstract class abstractCommand {

    private String usage;
    public int shell;
    public List<String> aliases;
    public String name;
    protected CommandState listener;

    public boolean ExitOnReturn = false;

    public abstractCommand() {}

    public abstractCommand(String usage, int shell, List<String> aliases, boolean flag) {
        this(usage, shell, aliases);
        this.ExitOnReturn = flag;
    }

    public abstractCommand(String usage, int shell, List<String> aliases){
        String[] name = this.getClass().getName().split("\\.");
        this.usage = usage;
        this.shell = shell;
        this.aliases = aliases;
        this.name = name[name.length-1].replaceAll("_", "-");
    }

    public abstractCommand(String usage, int shell, List<String> aliases, String name){
        this.usage = usage;
        this.shell = shell;
        this.aliases = aliases;
        this.name = name;
    }

    public void setListener(CommandState listener) {
        this.listener = listener;
    }

    public void printUsage(){
        System.out.print(name);
        if (aliases != null) {
            System.out.print('[');
            for (String one : aliases)
                System.out.print(one + "");
            System.out.print(']');
        }
        System.out.println(" " + usage);
    }

    public void done() {
        if (listener != null)
            listener.onEnd();
    }

    public abstract void perform(String[] argv, Context ctx);
}
