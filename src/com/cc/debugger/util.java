package com.cc.debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by CwT on 16/2/23.
 */
public class util {

    public static ResourceBundle bundle = null;

    public static ResourceBundle getResourceBundle() {
        if(bundle == null) {
            bundle = ResourceBundle.getBundle("debugger");
        }
        return bundle;
    }

    public static String getString(String key) {
        String value = null;
        try {
            value = getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            System.err.println("java.util.MissingResourceException: Couldn't find value for: " + key);
        }
        if(value == null) {
            value = "Could not find resource: " + key + "  ";
        }
        return value;
    }

    public static void run(String name, String[] argv) {
        String[] array = new String[argv.length + 1];
        array[0] = util.class.getResource("/"+name).getPath();
        System.arraycopy(argv, 0, array, 1, argv.length);
        try {
            Runtime.getRuntime().exec("chmod 777 " + array[0]).waitFor();
            Process process = Runtime.getRuntime().exec(array);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void run(String [] argv) {
        try {
            argv[0] = util.class.getResource("/"+argv[0]).getPath();
            Runtime.getRuntime().exec("chmod 777 " + argv[0]).waitFor();
            Process process = Runtime.getRuntime().exec(argv);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void Logger(String dat) {
        System.out.println(dat);
    }

}
