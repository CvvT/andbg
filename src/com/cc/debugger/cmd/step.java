package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.SuspendState;
import com.cc.debugger.iface.EventCallback;
import com.cc.debugger.util;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.StepRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/4/25.
 */
public class step extends abstractCommand {


    static List<String> aliases = null;

    static {
        aliases = new ArrayList<>();
        aliases.add("s");
    }

    public step() {
        super("[into | out | over(default)]", 1, aliases);
    }

    private void checkStep(final Context ctx) {
        util.Logger("Step number:" + ctx.getVm().eventRequestManager().stepRequests().size());
    }

    private void singleStep(final Context ctx, int depth) {
        if (ctx.getState().isSuspend()) {
            ctx.registerStep(ctx.getState().thread, depth, 1, new EventCallback() {
                @Override
                public void handleEvent(Event event) {
                    StepEvent se = (StepEvent) event;
                    SuspendState state = ctx.getState();
                    state.isSuspend = true;
                    state.thread = se.thread();
                    state.location = se.location();
                    util.Logger("single step complete");
                }
            });
            ctx.getState().resume();
        } else {
            System.err.println("suspend thread before step");
        }
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        if (argv.length > 0) {
            switch (argv[0]) {
                case "into":
                    singleStep(ctx, StepRequest.STEP_INTO);
                    break;
                case "out":
                    singleStep(ctx, StepRequest.STEP_OUT);
                    break;
                case "over":
                    singleStep(ctx, StepRequest.STEP_OVER);
                    break;
                case "check":
                    checkStep(ctx);
            }
        } else {
            singleStep(ctx, StepRequest.STEP_OVER);
        }
    }
}
