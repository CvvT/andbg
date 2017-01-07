package com.cc.debugger.cmd;

import com.cc.debugger.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by CwT on 16/5/14.
 */
public class silentcommand extends abstractCommand {

    @Override
    public void perform(String[] argv, Context ctx) {
        String pkgName;
        String cmd;
        int cmdNo = 0;
        int cookie = 0;
        if (argv.length < 2) {
            return;
        }

        if (argv.length > 2) {
            cookie = Integer.parseInt(argv[2]);
        }

        pkgName = argv[0];
        cmd = argv[1];
        switch (cmd) {
//            case command.INIT: cmdNo = 0; break;
//            case command.SHOW: cmdNo = 1; break;
//            case command.DUMP_DEX: cmdNo = 3; break;
            case "update":  cmdNo = 2; break;
            case "script": cmdNo = 4; break;
            default:
                return;
        }

        try {
            String cmdline = "adb shell am broadcast -a com.cc.dumpapk --es package " + pkgName + " --ei cmd " + cmdNo;
            if (cmdNo == 0 || cmdNo == 3) {
                if (cookie == 0)
                    return;
                cmdline += " --ei cookie " + cookie;
            }
            Process process = Runtime.getRuntime().exec(cmdline);
            process.waitFor();

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));

            // read any errors from the attempted command
            String result;
            StringBuilder error = new StringBuilder();
            while ((result = stdError.readLine()) != null) {
                error.append(result);
            }
            System.err.println(error.toString());
            // read the output from the command
            StringBuilder out = new StringBuilder();
            while ((result = stdInput.readLine()) != null) {
                out.append(result);
            }
            System.out.println(out.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
