package com.cc.debugger.absface;

import com.cc.debugger.Context;
import com.cc.debugger.cmd.abstractCommand;

/**
 * Created by CwT on 16/5/4.
 */
public abstract class ToEndCommand extends abstractCommand {

    public abstract void perform();

    @Override
    public void perform(String[] argv, Context ctx) {
        perform();
        if (listener != null)
            listener.onEnd();
    }
}
