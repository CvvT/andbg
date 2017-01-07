package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.MethodNode;
import com.cc.debugger.scripts.regions.*;
import com.cc.debugger.test.Testput;
import org.jf.dexlib2.builder.MethodLocation;

import java.util.*;

/**
 * Created by CwT on 16/3/5.
 */
public class RegionMakerVisitor extends AbstractVisitor {

    @Override
    public void init(MethodNode node, Tracer tracer) {

    }

    @Override
    public void visit(MethodNode node, Tracer tracer) {
        IRegion parent = new MulRegion(null, null); // ROOT
        tracer.setRoot(parent);
        IRegion cur;

        BlockNode curBlock = node.getEnterBlock();
        for (InsnNode insn : tracer.getTracer()) {
            if (!curBlock.isContain(insn.getInstruction())) {
                curBlock = node.getNextBlock(curBlock, insn.getInstruction());
                while (parent.getBlock() != null && !parent.getBlock().getCdblocks().get(curBlock.getId()))
                    parent = parent.getParent();

            }
            switch (insn.insnType) {
                case IF:
                    cur = new IfRegion(insn, curBlock, parent);
                    parent.addChild(cur);
                    parent = cur;
                    break;
                default:
                    cur = new Region(insn, curBlock, parent);
                    parent.addChild(cur);
                    break;
            }
        }

        modifyTree(tracer);
    }

    private void modifyTree(Tracer tracer) {
        IRegion root = tracer.getRoot();
        travel(root);
    }

    private void travel(IRegion root) {
        while (findLoop(root));
        if (root.getChildren() != null) {
            for (IRegion region : root.getChildren())
                travel(region);
        }
    }

    private boolean findLoop(IRegion parent) {
        int maxLen = 1, maxCount = 0, index = -1;
        if (parent.getChildren() != null) {
            List<IRegion> children = parent.getChildren();
            int size = children.size();
            for (int i = 0; i < size; i++) {
                for (int j = i + maxLen; j < size; j++) {
                    int count = 1;
                    int len = j - i;
                    int k = i;
                    while ( (k + len + len <= size) && equal(k, k + len, children)) {
                        k += len;
                        count++;
                    }
                    if (count > 1) {
                        if (len > maxLen) {
                            maxLen = len;
                            maxCount = count;
                            index = i;
                        } else if (len == maxLen && count > maxCount) {
                            maxCount = count;
                            index = i;
                        }
                    }
                }
            }

            if (index != -1) {
                List<IRegion> mover = new ArrayList<>();
                LoopRegion loop = new LoopRegion(parent);
                for (int i = 0; i < maxCount; i++) {
                    MulRegion each = new MulRegion(loop);
                    loop.addChild(each);
                    for (int j = 0; j < maxLen; j++) {
                        IRegion child = children.get(index + i * maxLen + j);
                        child.setParent(each);
                        each.addChild(child);
                        mover.add(child);
                    }
                }
                for (IRegion remove : mover) {
                    children.remove(remove);
                }
                children.add(index, loop);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param x
     * @param y y must bigger than x
     * @param list
     * @return
     */
    public static boolean equal(int x, int y, List<IRegion> list) {
        int len = y - x;
        int index = 0;
        for (; index < len && x < list.size() && y < list.size();
             index++, x++, y++) {
            if (!(list.get(x) instanceof Region) || !(list.get(y) instanceof Region))
                return false;
            if (((Region)list.get(x)).getInsn().getInstruction().getIndex()
                    != ((Region)list.get(y)).getInsn().getInstruction().getIndex())
                return false;
        }
        return index == len;
    }

}
