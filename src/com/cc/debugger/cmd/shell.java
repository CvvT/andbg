package com.cc.debugger.cmd;

import com.cc.debugger.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by CwT on 16/2/23.
 */
public class shell extends abstractCommand {

    BufferedReader reader = null;

    public shell() {
        super("", 0, null, true);
    }

    private void input() {
        System.out.print(">> ");
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        ctx.shell = true;
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        }

        while (true) {
            try {
                input();
                String line = reader.readLine();
                if (line.equals("")) {
                    continue;
                }
                ctx.perform(line.split(" "));
                System.out.println("execution finish");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
