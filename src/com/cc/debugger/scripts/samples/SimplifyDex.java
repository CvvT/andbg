package com.cc.debugger.scripts.samples;

import com.cc.debugger.scripts.Simplify;

/**
 * Created by CwT on 16/5/16.
 */
public class SimplifyDex {

    public static void main(String []argv) {
        Simplify simplify = Simplify.getInstance();
        simplify.addMethodtoTrace("Lcom/cc/obfuscationtest/MainActivity;->dowhile(II)I");
//        simplify.addMethodtoTrace("Lcom/cc/obfuscationtest/Reflection;->reflectfunc()V");
        simplify.addMethodtoTrace("Lcom/cc/obfuscationtest/MainActivity;->helloWorld()Ljava/lang/String;");

        // use the return value in place of the method
        simplify.addDecryptMethod("Lcom/cc/obfuscationtest/Base64;->decode([B)[B");

//        simplify.run("/Users/CwT/Desktop/app-debug.apk");
        // if you don't specify the path to the apk or dex file, we assume that the apk is packed and
        // you want to dump the dex file in memory at runtime
        simplify.run(null);
    }
}
