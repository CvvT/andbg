package com.cc.debugger.test;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.scripts.regions.IRegion;
import com.cc.debugger.scripts.regions.IfRegion;
import com.cc.debugger.scripts.regions.Region;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.instruction.Instruction;
import sun.jvm.hotspot.utilities.Bits;

import java.util.BitSet;

/**
 * Created by CwT on 16/3/4.
 */
public class Testput {

    public static String insnString(MethodNode mth) {
        StringBuilder sb = new StringBuilder();
        for (MethodLocation location : mth.getInstructions()) {
            Instruction insn = location.getInstruction();
            if (insn != null)
                sb.append(insn.getOpcode().name + "\n");
        }
        return sb.toString();
    }

    public static String CFGString(MethodNode mth) {
        StringBuilder sb = new StringBuilder();
        for (BlockNode block : mth.getBasicBlocks()) {
            sb.append(block.getId()); sb.append(":\n");
            for (MethodLocation insn : block.getInstructions()) {
                if (insn.getInstruction() == null)
                    continue;
                sb.append(insn.getInstruction().getOpcode().name);
                sb.append("\n");
            }
            sb.append("successor:\n");
            for (BlockNode node : block.getSuccessors()) {
//                if (node.contains(AFlag.HANDLER)) {
//                    for (BlockNode next : node.getSuccessors()) {
//                        sb.append(next.getId());
//                        sb.append(" ");
//                    }
//                }else
                sb.append(node.getId()); sb.append(" ");
            }
            sb.append("\n-------------------\n");
        }
        return sb.toString();
    }

    public static String CDString(MethodNode mth) {
        StringBuilder sb = new StringBuilder();
        for (BlockNode block : mth.getBasicBlocks()) {
            sb.append(block.getId()); sb.append(":\n");
            BitSet set = block.getCdblocks();
            for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
                sb.append(i); sb.append(" ");
            }
            sb.append("\n-------------------\n");
        }
        return sb.toString();
    }

    public static String PDFString(MethodNode mth) {
        StringBuilder sb = new StringBuilder();
        for (BlockNode block : mth.getBasicBlocks()) {
            sb.append(block.getId()); sb.append(":\n");
            BitSet set = block.getPdomFrontier();
            for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
                sb.append(i); sb.append(" ");
            }
            sb.append("\n-------------------\n");
        }
        return sb.toString();
    }

    public static String IDomString(MethodNode mth) {
        StringBuilder sb = new StringBuilder();
        for (BlockNode block : mth.getBasicBlocks()) {
            sb.append(block.getId());
            sb.append(":");
            if (block.getIpdom() != null)
                sb.append(block.getIpdom().getId());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String DomOnString(MethodNode mth) {
        StringBuilder sb = new StringBuilder();
        for (BlockNode block : mth.getBasicBlocks()) {
            sb.append(block.getId()); sb.append(":\n");
            for (BlockNode node : block.getDominatesOn()) {
                sb.append(node.getId());
                sb.append(" ");
            }
            sb.append("\n-------------------\n");
        }
        return sb.toString();
    }

    public static String NodeTree(Tracer tracer) {
        IRegion root = tracer.getRoot();
        StringBuilder sb = new StringBuilder(nodetravel(root, 0));
        return sb.toString();
    }

    public static String nodetravel(IRegion root, int ident) {
        StringBuilder sb = new StringBuilder();
        String indent = "";
        for (int i = 0; i < ident; i++)
            indent += "  ";
        sb.append(indent);
        sb.append(root.baseString());
        sb.append("\n");
        if (root.getChildren() == null)
            return sb.toString();
        for (IRegion node : root.getChildren()) {
            sb.append(indent);
            sb.append(nodetravel(node, ident+1));
        }
        return sb.toString();
    }
}
