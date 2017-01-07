package com.cc.debugger.scripts.samples;

import com.cc.debugger.Context;
import com.cc.debugger.absface.ToEndCommand;
import com.cc.debugger.cmd.abstractCommand;
import com.cc.debugger.cmd.command;
import com.cc.debugger.cmd.script;
import com.cc.debugger.iface.CommandState;
import com.cc.debugger.scripts.SequenceExecutor;
import com.cc.debugger.scripts.Utility;
import org.jf.dexlib2.iface.ClassDef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by CwT on 16/5/2.
 */
public class DumpDex {

    private static String TARGET_PACKAGE = "com.cc.test";
    private static String PREFIX_PACKAGE = "Lcom/cc/test";

    /**
     * note that method@abstractCommand.perform may be an asynchronous method, we provide class@SequenceExecutor
     * if you want to execute these commands in sequence(Not all command support this feature). REMEMBER to
     * handle it carefully.
     * @param ctx
     */
    public void run(final Context ctx) {
        // update dex file
        final SequenceExecutor executor = new SequenceExecutor(ctx);
//        executor.add(new command(), new String[]{command.INIT});
        executor.add(new command(), new String[]{command.ACTION_SHOW});
        executor.add(new ToEndCommand() {
            @Override
            public void perform() {
                System.out.print("Please input the correct cookie:");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                try {
                    String cookie = reader.readLine();
                    reader.close();
                    executor.addFirst(new command(), new String[] {command.ACTION_DUMP_DEXFILE, cookie});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null);

        executor.add(new script(), new String[] {script.PULL, TARGET_PACKAGE});
        executor.add(new script(), new String[] {script.SMALI, TARGET_PACKAGE});
        executor.add(new ToEndCommand() {
            @Override
            public void perform() {
                if (!ctx.loadDex("dex/classes.dex")) {
                    System.err.println("load dex file : dex/classes.dex error");
                    return;
                }

                // update class
                for (ClassDef classDef : ctx.getDexfile().getClasses()) {
                    String type = classDef.getType();
                    /**
                     * I think it's bad to update every class, but since we aren't sure which one should be updated,
                     * I simply update the classes whose names start with PREFIX_PACKAGE. It's possible to improve it by having
                     * some insight into the updated dex file first and then check out what kind of classes should
                     * be updated.
                     */
                    if (type.startsWith(PREFIX_PACKAGE)) {
                        System.out.println("Working on " + type);
                        executor.add(new command(), new String[] {command.ACTION_UPDATE_CLASS, Utility.getClassName(type)});
                        executor.add(new script(), new String[] {script.PULL_CLASS, TARGET_PACKAGE, Utility.getClassName(type)});
                    }
                    executor.add(new script(), new String[] {"smali"});
                }

                executor.add(new ToEndCommand() {
                    @Override
                    public void perform() {
                        // reload dex file
                        if (!ctx.loadDex("dex/classes.dex")) {
                            System.err.println("load dex file : dex/classes.dex error");
                        }

                        // update method

                        ctx.close();
                    }
                }, null);

            }
        }, null);

        executor.execute();
    }

    public static void main(String[] argv) {
        Context ctx = new Context();
        if (!ctx.run())
            return;

        new DumpDex().run(ctx);
        ctx.close();
    }
}
