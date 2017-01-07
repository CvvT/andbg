package com.cc.debugger.scripts;

import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.scripts.visitors.LiveVarVisitor;
import com.sun.jdi.VirtualMachine;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by CwT on 16/2/29.
 */
public class Utility {

    public static int registerCount(List<? extends CharSequence> list) {
        int count = 0;
        for (CharSequence one : list) {
            count++;
            if (isWideType(one.toString()))
                count++;
        }
        return count;
    }

    public static boolean isWideType(@Nonnull String type) {
        char c = type.charAt(0);
        return c == 'J' || c == 'D';
    }

    public static int mapRegister(VirtualMachine vm, MethodNode node, int registernum) {
        if (vm.version().equals("1.5.0")) {
            return mapRegisterForDalvik(node, registernum);
        }
        return mapRegisterForArt(node, registernum);
    }

    private static int mapRegisterForArt(MethodNode node, final int register)
    {
        int totalRegister = node.getMethodData().getRegisterCount();
        int parameterRegisters = node.getParamterRegisterCount();

        if (Modifier.isStatic(node.getAccessFlags())) {
            return register;
        }
        if (register >= totalRegister - parameterRegisters) {
            return register - (totalRegister - parameterRegisters);
        }
        return register + parameterRegisters;
    }

    private static int mapRegisterForDalvik(MethodNode node, final int register)
    {
        if (Modifier.isStatic(node.getAccessFlags())) {
            return register;
        }
        int totalRegisters = node.getMethodData().getRegisterCount();
        int parameterRegisters = node.getParamterRegisterCount();
        if (register == totalRegisters - parameterRegisters) {
            return 0;
        }
        if (register == 0) {
            return 1000;
        }
        return register;
    }

    public static int getFreeRegister(BlockNode node, MethodLocation index) {
        List<MethodLocation> insns = node.getInstructions();
        BitSet use = (BitSet)node.getOutSet().clone();
        for (int i = insns.size()-1; i >= 0; i--) {
            MethodLocation location = insns.get(i);
            if (location.getIndex() < index.getIndex())
                break;  //we can stop here
            Instruction insn = location.getInstruction();
            if (insn == null)
                continue;
            if (insn.getOpcode().setsRegister()) {
                int registernum = ((OneRegisterInstruction)insn).getRegisterA();
                if (!use.get(registernum)   //if we find it that the var will never be used afterwards
                        || (insn.getOpcode().setsWideRegister() && !use.get(registernum+1))) {
                    location.setInstruction(null);
                    continue;
                }
                use.clear(registernum);
                if (insn.getOpcode().setsWideRegister())
                    use.clear(registernum+1);
                LiveVarVisitor.setUse(insn, use);
            } else {
                LiveVarVisitor.setUse(insn, use);
            }
        }
        return use.nextClearBit(0);
    }

    public static String getSignature(String name){
        if (name.length() <= 2) //for I and [I, and it can not contain "."
            return name;
        name = name.replaceAll("\\.", "/");
        if (name.charAt(0) == '[')
            return name;
        switch (name){
            case "void":
                return "V";
            case "boolean":
                return "Z";
            case "float":
                return "F";
            case "short":
                return "S";
            case "char":
                return "C";
            case "double":
                return "D";
            case "long":
                return "J";
            case "byte":
                return "B";
            case "int":
                return "I";
        }

        return "L" + name + ";";
    }

    public static String getClassName(String signature) {
        String clsName = signature.replaceAll("/", ".");
        if (clsName.charAt(0) == 'L') {
            return clsName.substring(1, clsName.length() - 1);
        }
        return clsName;
    }

    public static List<String> listDesc(String desc) {
        List<String> list = new ArrayList<String>(5);
        char[] chars = desc.toCharArray();
        int i = 0;
        while (i < chars.length) {
            switch (chars[i]) {
                case 'V':
                case 'Z':
                case 'C':
                case 'B':
                case 'S':
                case 'I':
                case 'F':
                case 'J':
                case 'D':
                    list.add(Character.toString(chars[i]));
                    i++;
                    break;
                case '[': {
                    int count = 1;
                    while (chars[i + count] == '[') {
                        count++;
                    }
                    if (chars[i + count] == 'L') {
                        count++;
                        while (chars[i + count] != ';') {
                            count++;
                        }
                    }
                    count++;
                    list.add(new String(chars, i, count));
                    i += count + 1;
                    break;
                }
                case 'L': {
                    int count = 1;
                    while (chars[i + count] != ';') {
                        ++count;
                    }
                    count++;
                    list.add(new String(chars, i, count));
                    i += count + 1;
                    break;
                }
                default:
            }
        }
        return list;
    }
}
