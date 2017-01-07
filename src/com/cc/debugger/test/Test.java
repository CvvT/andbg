package com.cc.debugger.test;

import com.cc.debugger.Context;
import com.cc.debugger.iface.EventCallback;
import com.cc.debugger.scripts.Deobfuscator;
import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.Utility;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.scripts.visitors.AbstractVisitor;
import com.cc.debugger.scripts.visitors.BlockSpiltVisitor;
import com.cc.debugger.util;
import com.google.common.collect.ImmutableList;
import com.sun.javafx.scene.layout.region.Margins;
import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.util.InstructionOffsetMap;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by CwT on 16/2/26.
 */
public class Test {

    public static void main(String[] argv) {
//        final Context ctx;
//        ctx = new Context();

//        if (!ctx.loadDex("/Users/CwT/Desktop/app-debug.apk"))
//            return;
//
//        String className = "Lcom/cc/obfuscationtest/MainActivity;";
//        String methodName = "helloWorld";
//
//        for (ClassDef classDef : ctx.getDexfile().getClasses()) {
////            System.out.println(classDef.getType());
//            if (classDef.getType().equals(className)) {
//                for (Method method : classDef.getMethods()) {
////                    System.out.println(method.getName());
//                    if (method.getName().equals(methodName)) {
//                        MethodNode node = new MethodNode(method);
//                        MethodLocation location = node.getMethodLocationbyindex(4);
//                        Tracer tracer = new Tracer();
//                        new Deobfuscator().perform(node, tracer);
//                        System.out.println(Testput.NodeTree(tracer));
//                        break;
//                    }
//                }
//                break;
//            }
//        }
        String CLSNAME = "L\\S*;";
        String MTHNAME = "[A-Za-z<>]+";
        String SIGNATURE = "\\(\\S*\\)\\S+";
        String PARAMETERS = "\\{([\\S\\s]*)\\}";
        String VAR = "[A-Za-z0-9]+";
        final String regx = "const-string\\s*?(\\S*),\\s*\"(\\S+)\"";

        Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher("const-string tag, \"cwt\"");
        while (matcher.find()) {
            System.out.println(matcher.groupCount() + " ");
            for (int i = 0; i <= matcher.groupCount(); i++)
                System.out.println(matcher.group(i));
//            System.out.println(matcher.group("id"));
            System.out.println("------------------");
        }

        //(//d+,)(//d+)"
//        String a = "\"a,\\\"\", \"b\", \"c\"";
//        Pattern aPattern = Pattern.compile("\\s*?\"([\\s\\S]*?)\"\\s*?(,|$)");
//        Matcher amatcher = aPattern.matcher(a);
//        while (amatcher.find()) {
//            for (int i = 0; i <= amatcher.groupCount(); i++) {
//                System.out.println(amatcher.group(i));
//            }
//            System.out.println("------------------");
//        }

//        int start = "()V".indexOf(")");
//        System.out.println("res: " + ("(1)V".substring(1, start)));
//        String a = "\"a\", \"b\", \"c\"";
//        for (String str : a.split(","))
//            System.out.println(str.trim());
//        new Test().test();
//        util.run("test.sh", new String[]{"com.cc.obfuscationtest", "com.cc.obfuscationtest.MainActivity"});
    }

    public Object test(int a) {
        return a;
    }

    public void test() {
        try {
            Class claz = Class.forName("com.cc.debugger.test.Test");
//            System.out.println(claz.getSuperclass().getName());
//            Class[] interfaces = claz.getInterfaces();
//            for (Class type : interfaces) {
//                System.out.println(type.getName());
//            }
//            System.out.println(int.class.getSimpleName());

            int a = 1;
            System.out.println(((Object)a).getClass().getName());
            for (java.lang.reflect.Method mth : claz.getMethods()) {
                StringBuilder sb = new StringBuilder();
                sb.append("(");
                for (Class param : mth.getParameterTypes())
                    sb.append(Utility.getSignature(param.getName()));
                sb.append(")");
                sb.append(Utility.getSignature(mth.getReturnType().getName()));
                System.out.println(mth.getName() + "->" + sb.toString());
            }
//            java.lang.reflect.Method mth = claz.getDeclaredMethod("test", int.class, int.class);
//            System.out.println(Utility.getSignature(mth.getReturnType().getName()));
//            for (Class param : mth.getParameterTypes()) {
//                System.out.println(param.getName());
//            }
//            System.out.println(mth.getModifiers());
//            System.out.println(mth.getName());
//            System.out.println(mth.getDeclaringClass().getName());
//            System.out.println(test(1).getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int test(int a, int b) {return 0;}
    public boolean test(String clsName, String mth, String signature, Object... objects) {return true;}
}
