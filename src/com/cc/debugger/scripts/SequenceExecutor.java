package com.cc.debugger.scripts;

import com.cc.debugger.Context;
import com.cc.debugger.cmd.abstractCommand;
import com.cc.debugger.iface.CommandState;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by CwT on 16/5/3.
 */
public class SequenceExecutor extends abstractCommand {

    private ArrayDeque<ExecutorListener> queue = new ArrayDeque<>();
    private Context ctx;

    public SequenceExecutor(Context ctx) {
        super("", 0, null);
        this.ctx = ctx;
    }

    public void addFirst(final abstractCommand cmd, final String[] argv, final CommandState callback) {
        addFirst(new ExecutorListener() {
            @Override
            public void onExecute() {
                cmd.setListener(callback);
                cmd.perform(argv, ctx);
            }
        });
    }

    public void add(final abstractCommand cmd, final String[] argv, final CommandState callback) {
        add(new ExecutorListener() {
            @Override
            public void onExecute() {
                cmd.setListener(callback);
                cmd.perform(argv, ctx);
            }
        });
    }

    public void addFirst(final abstractCommand cmd, final String[] argv) {
        addFirst(new ExecutorListener() {
            @Override
            public void onExecute() {
                cmd.setListener(new CommandState() {
                    @Override
                    public void onEnd() {
                        execute();
                    }
                });
                cmd.perform(argv, ctx);
            }
        });
    }

    public void add(final abstractCommand cmd, final String[] argv) {
        add(new ExecutorListener() {
            @Override
            public void onExecute() {
                cmd.setListener(new CommandState() {
                    @Override
                    public void onEnd() {
                        execute();
                    }
                });
                cmd.perform(argv, ctx);
            }
        });
    }

    public void add(ExecutorListener listener) {
        queue.add(listener);
    }

    public void addFirst(ExecutorListener listener) {
        queue.addFirst(listener);
    }

    public void execute() {
        ExecutorListener listener = queue.poll();
        if (listener != null) {
            listener.onExecute();
        }
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        execute();
    }

    public interface ExecutorListener {
        void onExecute();
    }
}
