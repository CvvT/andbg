package com.cc.debugger.cmd;

import com.cc.debugger.Context;

import java.util.ArrayList;

/**
 * Created by CwT on 16/2/23.
 */
public class exit extends abstractCommand {

    public exit() {
        super("", 1, null);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        System.exit(0);
    }
}
