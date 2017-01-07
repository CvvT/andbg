package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.util;

/**
 * Created by CwT on 16/4/10.
 */
public class script extends abstractCommand {

    public static final String CLEAN = "clean";
    public static final String PULL = "pull";
    public static final String PULL_CLASS = "pullclass";
    public static final String PULL_METHOD = "pullmethod";
    public static final String SMALI = "smali";
    public script() {
        super("<smali | pull>", 0, null, true);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        if (argv.length > 0) {
            switch (argv[0]) {
                case CLEAN:
                case PULL:
                case PULL_CLASS:
                case PULL_METHOD:
                    argv[0] += ".sh";
                    util.run(argv);
                    break;
                case SMALI:
                    String[] array = new String[argv.length + 1];
                    System.arraycopy(argv, 1, array, 1, argv.length-1);
                    array[0] = argv[0] + ".sh";
                    array[array.length-1] = util.getString("smalipath");
                    util.run(array);
                    break;
            }
        }
        done();
    }
}
