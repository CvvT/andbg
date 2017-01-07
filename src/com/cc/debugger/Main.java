package com.cc.debugger;

import com.cc.debugger.cmd.abstractCommand;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by CwT on 16/2/21.
 */
public class Main {

    static Map<String, abstractCommand> ACTION_MAP = new HashMap<>();
    static String path = "com.cc.debugger.cmd";
    static Context ctx = null;
    static Set<String> help = new HashSet<>();

    static {
        findCommand();
        help.add("-help");
        help.add("-h");
        help.add("--help");
        help.add("-?");
    }

    private static void findCommand() {
        String[] cmds = util.getString("command").split(" ");
        String path = util.getString("commandpath");
        for (String cmd : cmds) {
            bindAction(path + "." + cmd);
        }
    }

    private static void bindAction(String cls) {
        try {
            Class<?> obj = Class.forName(cls);
            abstractCommand instance = (abstractCommand)obj.newInstance();
            ACTION_MAP.put(instance.name.replaceAll("_", "-"), instance);
            if (instance.aliases != null) {
                for (String aliase : instance.aliases)
                    ACTION_MAP.put(aliase, instance);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) {
        if (ctx == null)
            ctx = new Context();

        for (String arg : argv) {
            if (help.contains(arg)) {
                String[] params = {"help", argv[0]};
                ctx.perform(params);
                return;
            }
        }

        ctx.perform(argv);

        abstractCommand cmd = Main.ACTION_MAP.get(argv[0]);
        if (cmd == null || cmd.ExitOnReturn)
            ctx.close();
    }

}
