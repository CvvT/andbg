package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.InsnType;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.scripts.regions.*;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by CwT on 16/4/22.
 */
public class MarkFlattenVisitor extends AbstractVisitor {

    int size;
    MethodNode method;
    List<MethodLocation> remover = new ArrayList<>(1);

    @Override
    public void init(MethodNode node, Tracer tracer) {
        System.out.println("Enter UnflattenVisitor");
        size = node.getInstructions().size();
        method = node;
    }

    @Override
    public void visit(MethodNode node, Tracer tracer) {
        IRegion root = tracer.getRoot();
        BitSet live = new BitSet(node.getMethodData().getRegisterCount());
        visitLoop(root, live, 0);
    }

    private void visitLoop(IRegion root, BitSet set, int index) {
        if (root instanceof LoopRegion)
            simplify((LoopRegion)root, set, index);
        else {
            if (root.getChildren() != null) {
                List<IRegion> children = root.getChildren();
                for (int i = children.size() - 1; i >= 0; i--) {
                    visitLoop(children.get(i), set, i);
//                    visitAndset(children.get(i), set);
                }
            }

            if (root instanceof Region)
                visitAndset(root, set);
        }
    }

    private void visitAndset(IRegion root, BitSet live) {
        //clear flag
        ((AbstractRegion)root).remove(AFlag.REMOVE_FOR_FLAT);
        if (root.getChildren() != null) {
            List<IRegion> children = root.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                visitAndset(children.get(i), live);
            }
        }

        if (root instanceof Region) {
            InsnNode node = ((Region) root).getInsn();
            Instruction insn = node.getInstruction().getInstruction();
            if (insn != null) {
                if (insn.getOpcode().setsRegister()) {
                    int registernum = ((OneRegisterInstruction) insn).getRegisterA();
                    live.clear(registernum);
                    if (insn.getOpcode().setsWideRegister())
                        live.clear(registernum + 1);
                }
                LiveVarVisitor.setUse(insn, live);
            }
        }
    }

    private boolean removedRegion(IRegion root) {
        if (root.getChildren() != null) {
            for (IRegion child : root.getChildren()) {
                if (!removedRegion(child))
                    return false;
            }
            return true;    // if it's IfRegion, just count its children
        }

        if (root instanceof Region)
            if (!((Region) root).getInsn().contains(AFlag.REMOVE_FOR_FLAT))
                return false;

        return true;
    }

    private void visit(IRegion root, BitSet live) {

        if (root.getChildren() != null) {
            List<IRegion> children = root.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                visit(children.get(i), live);
            }
        }

        if (root instanceof IfRegion) {
            if ( removedRegion(root)) {
                ((IfRegion) root).add(AFlag.REMOVE_FOR_FLAT);
                return;
            }
        }

        if (root instanceof Region) {
            InsnNode node = ((Region)root).getInsn();
            Instruction insn = node.getInstruction().getInstruction();
            if (node.contains(AFlag.REMOVE_FOR_FLAT))
                return;
            if (insn != null) {
                if (node.insnType == InsnType.GOTO) {
                    ((Region) root).add(AFlag.REMOVE_FOR_FLAT);
                    return;
                }

                if (insn.getOpcode().setsRegister()) {
                    int registernum = ((OneRegisterInstruction)insn).getRegisterA();
                    if (!live.get(registernum)   //if we find it that the var will never be used afterwards
                            || (insn.getOpcode().setsWideRegister() && !live.get(registernum+1))) {
                        ((Region) root).add(AFlag.REMOVE_FOR_FLAT);
                    } else {
                        live.clear(registernum);
                        if (insn.getOpcode().setsWideRegister())
                            live.clear(registernum + 1);
                        LiveVarVisitor.setUse(insn, live);
                    }
                } else {
                    LiveVarVisitor.setUse(insn, live);
                }
            }
        }
    }

    private void simplify(LoopRegion loop, BitSet live, int index) {
        List<IRegion> children = loop.getChildren();
        BitSet copy = (BitSet)live.clone();
        for (int i = children.size() - 1; i >= 0; i--) {
            //remove top level insns
            for (IRegion child : children.get(i).getChildren()) {
                if (child instanceof Region)
                    ((Region) child).getInsn().add(AFlag.REMOVE_FOR_FLAT);
            }
            visit(children.get(i), copy);
        }

        if (check(loop, new BitSet(size))) {
            //can simplify
            System.out.println("do simplify...");
            int startIndex = getFirstBlock(loop);
            BlockNode newBlock = BlockSpiltVisitor.startNewBlock(method, -1);
            MulRegion mulRegion = new MulRegion(newBlock, loop.getParent());
            remover.clear();
            doSimplify(loop, newBlock, mulRegion);
            for (IRegion region : mulRegion.getChildren()) {
                if (region instanceof Region)
                    newBlock.getInstructions().add(((Region) region).getInsn().getInstruction());
            }
            method.getMethodData().moveInstructions(startIndex, newBlock.getInstructions());
            method.getMethodData().removeInstructions(remover);
            remover.clear();
            loop.getParent().getChildren().remove(loop);
            loop.getParent().getChildren().add(index, mulRegion);
        } else {
            //can not, simplify children
            for (int i = children.size() - 1; i >= 0; i--) {
                visitLoop(children.get(i), live, i);
            }
        }
    }

    private void doSimplify(IRegion region, BlockNode newBlock, MulRegion newRegion) {
        if (region instanceof Region) {
            MethodLocation location = ((Region) region).getInsn().getInstruction();
            if (!((Region) region).contains(AFlag.REMOVE_FOR_FLAT)) {
                Instruction insn = location.getInstruction();
                if (insn != null) {
//                    System.out.println(insn.getOpcode().name);
                    newRegion.addChild(region);
//                    newBlock.getInstructions().add(((Region) region).getInsn().getInstruction());
                }
            } else {
                if (!remover.contains(location))
                    remover.add(location);
            }
        }

        newBlock.mergeinto(region.getBlock());

        if (region.getChildren() != null) {
            for (IRegion child : region.getChildren()) {
                doSimplify(child, newBlock, newRegion);
            }
        }
    }

    private boolean check(IRegion root, BitSet set) {
        if (((AbstractRegion)root).contains(AFlag.REMOVE_FOR_FLAT)) {
            if (root.getChildren() != null) {
                for (IRegion region : root.getChildren()) {
                    if (!check(region, set))
                        return false;
                }
            }
            return true;
        }
        if (root instanceof Region) {
            int index  = ((Region)root).getInsn().getInstruction().getIndex();
            if (set.get(index)) //set before
                return false;
            set.set(index);
        }
        return true;
    }

    private int getFirstBlock(IRegion region) {
        if (region.getBlock() != null) {
            List<MethodLocation> list = region.getBlock().getInstructions();
            if (list.size() > 0)
                return list.get(0).getIndex();
        }

        if (region.getChildren() != null)
            for (IRegion child : region.getChildren()) {
                int index = getFirstBlock(child);
                if (index >= 0)
                    return index;
            }

        return -1;
    }
}
