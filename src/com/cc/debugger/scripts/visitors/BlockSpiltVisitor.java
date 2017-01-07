package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.node.*;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderExceptionHandler;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.MethodLocation;

import java.util.*;

/**
 * Created by CwT on 16/2/28.
 */
public class BlockSpiltVisitor extends AbstractVisitor {


    private static final Set<InsnType> SEPARATE_INSNS = EnumSet.of(
            InsnType.RETURN,
            InsnType.IF,
            InsnType.SWITCH,
            InsnType.MONITOR_ENTER,
            InsnType.MONITOR_EXIT,
            InsnType.THROW
    );

    @Override
    public void init(MethodNode node, Tracer tracer) {
        System.out.println("Enter BlockSpiltVisitor");
        node.initBasicBlocks();
    }

    @Override
    public void visit(MethodNode mth, Tracer tracer) {
        MethodLocation prevInsn = null;
        Map<Integer, BlockNode> blocksMap = new HashMap<>();
        BlockNode curBlock = startNewBlock(mth, 0);
        mth.setEnterBlock(curBlock);

//        List<MethodLocation> instructions = mth.getInstructions();

        for (MethodLocation location : mth.getInstructions()){
            if (location.getInstruction() == null)
                continue;

            if (prevInsn != null) {
                InsnType type = InsnNode.decode(prevInsn.getInstruction());
                if (type == InsnType.GOTO
                        || type == InsnType.THROW
                        || SEPARATE_INSNS.contains(type)) {
                    BlockNode block = startNewBlock(mth, location.getCodeAddress());
                    if (type == InsnType.MONITOR_ENTER
                            || type == InsnType.MONITOR_EXIT
                            || type == InsnType.IF) {
                        connect(curBlock, block);
                    }
                    curBlock = block;
                } else {
                    if (location.getLabels().size() > 0) {
                        BlockNode block = startNewBlock(mth, location.getCodeAddress());
                        connect(curBlock, block);
                        curBlock = block;
                    }
                }
            }
            InsnType type = InsnNode.decode(location.getInstruction());
            if (type == InsnType.RETURN || type == InsnType.THROW)
                mth.addExitBlock(curBlock);
//            if (location.getInstruction().getOpcode() == Opcode.ARRAY_PAYLOAD)
//                curBlock.add(AFlag.ARRAY_PAYLOAD);
            blocksMap.put(location.getCodeAddress(), curBlock);
            curBlock.getInstructions().add(location);
            prevInsn = location;
        }

        for (BlockNode block : mth.getBasicBlocks()) {
            MethodLocation lastOne = block.getLastInsn();
            switch (InsnNode.decode(lastOne.getInstruction())) {
                case GOTO:
                case IF:
                    MethodLocation location = ((BuilderOffsetInstruction)lastOne.getInstruction()).getTarget().getLocation();
                    BlockNode target = getBlock(location.getCodeAddress(), blocksMap);
                    connect(block, target);
                    break;
            }
        }

        for (TryNode tryBlock : mth.getTryBlocks()) {
            BlockNode block = startNewBlock(mth, -1);
            block.add(AFlag.HANDLER);
            for (BuilderExceptionHandler handler : tryBlock.getExceptionHandlers()) {
                BlockNode handlerNode = blocksMap.get(handler.getHandlerCodeAddress());
                handlerNode.add(AFlag.EXCEPTION);
                connect(block, handlerNode);
            }

            int start = tryBlock.start.getCodeAddress();
            int end = tryBlock.end.getCodeAddress();
//            System.out.println(tryBlock.end.getLocation().getInstruction().getOpcode().name);
//            BlockNode node = blocksMap.get(start);
//            connect(block, node);
            while (start < end) {
                BlockNode node = blocksMap.get(start);
                connect(node, block);
                MethodLocation lastInsn = node.getLastInsn();
                start = lastInsn.getCodeAddress() + lastInsn.getInstruction().getCodeUnits();
            }
        }
    }

    public static BlockNode startNewBlock(MethodNode mth, int offset) {
        BlockNode block = new BlockNode(mth.getBasicBlocks().size(), offset);
        mth.getBasicBlocks().add(block);
        return block;
    }

    private static BlockNode getBlock(int offset, Map<Integer, BlockNode> blocksMap) {
        BlockNode block = blocksMap.get(offset);
        if (block == null) {
            throw new RuntimeException("Missing block: " + offset);
        }
        return block;
    }

    static void connect(BlockNode from, BlockNode to) {
        if (!from.getSuccessors().contains(to)) {
            from.getSuccessors().add(to);
        }
        if (!to.getPredecessors().contains(from)) {
            to.getPredecessors().add(from);
        }
    }
}
