package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.InsnType;
import com.cc.debugger.scripts.node.MethodNode;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.instruction.*;

import java.util.*;

/**
 * Created by CwT on 16/2/29.
 */
public class LiveVarVisitor extends AbstractVisitor {

    Map<BlockNode, BitSet> uses;
    Map<BlockNode, BitSet> defs;

    public static final Set<Opcode> CONTAINS = EnumSet.of(
            Opcode.FILL_ARRAY_DATA
    );

    /**
     * init use-def chains of each block
     * @param node
     */
    @Override
    public void init(MethodNode node, Tracer tracer) {
        System.out.println("Enter LiveVarVisitor");

        uses = new HashMap<>();
        defs = new HashMap<>();

        int registerCount = node.getMethodData().getRegisterCount();
        for (InsnNode insnNode : tracer.getTracer()) {
            if (insnNode.contains(AFlag.REMOVE)) {
                insnNode.getInstruction().setInstruction(null); //mark it to delete later
            }
        }

        for (BlockNode blockNode : node.getBasicBlocks()) {
            BitSet use = new BitSet(registerCount);
            BitSet def = new BitSet(registerCount);
            List<MethodLocation> insns = blockNode.getInstructions();
            for (int i = insns.size()-1; i >= 0; i--) {
                MethodLocation location = insns.get(i);
                Instruction insn = location.getInstruction();
                if (insn == null)
                    continue;
                if (insn.getOpcode().setsRegister() || CONTAINS.contains(insn.getOpcode())) {
                    int number = ((OneRegisterInstruction) insn).getRegisterA();
                    def.set(number);
                    use.clear(number);
                    if (insn.getOpcode().setsWideRegister()) {
                        def.set(number + 1);
                        use.clear(number + 1);
                    }
                }
                setUse(insn, use);
            }
            uses.put(blockNode, use);
            defs.put(blockNode, def);
            blockNode.setInSet(new BitSet(registerCount));
            blockNode.setOutSet(new BitSet(registerCount));
        }
    }

    /**
     * calculate IN and OUT sets of live variable and remove dead code
     * @param node
     */
    @Override
    public void visit(MethodNode node, Tracer tracer) {
        boolean changed;
        do {
            liveAnalyse(node, tracer);
            changed = simplify(node);
            System.out.println("Working on ...");
        } while (changed);
    }

    private void liveAnalyse(MethodNode node, Tracer tracer) {
        System.out.println("Live Analyse ...");
        boolean changed;
        do {
            changed = false;
            for (BlockNode block : node.getBasicBlocks()) {
                for (BlockNode sucsessor : block.getSuccessors()) {
                    block.getOutSet().or(sucsessor.getInSet());
                }
                BitSet inset = (BitSet)block.getOutSet().clone();
                inset.andNot(defs.get(block));
                inset.or(uses.get(block));
                if (!changed) {
                    changed = !inset.equals(block.getInSet());
                }
                block.setInSet(inset);
            }
        } while (changed);
    }

    private boolean simplify(MethodNode root) {
        boolean changed = false;
        for (BlockNode node : root.getBasicBlocks()) {
            List<MethodLocation> insns = node.getInstructions();
            BitSet use = (BitSet)node.getOutSet().clone();
            for (int i = insns.size()-1; i >= 0; i--) {
                MethodLocation location = insns.get(i);
                Instruction insn = location.getInstruction();
                if (insn == null)
                    continue;
                if (insn.getOpcode().setsRegister() || CONTAINS.contains(insn.getOpcode())) {
                    int registernum = ((OneRegisterInstruction)insn).getRegisterA();
                    if (!use.get(registernum)   //if we find it that the var will never be used afterwards
                            || (insn.getOpcode().setsWideRegister() && !use.get(registernum+1))) {
                        if (InsnNode.decode(insn) == InsnType.MOVE_RESULT) {
                            insns.get(i-1).setInstruction(null);
                        }
                        location.setInstruction(null);
                        changed = true;
                        continue;
                    }
                    use.clear(registernum);
                    if (insn.getOpcode().setsWideRegister())
                        use.clear(registernum+1);
                } else if (InsnNode.decode(insn) == InsnType.APUT) {
                    //TODO It only work if the variable is local
//                    int registernum = ((ThreeRegisterInstruction) insn).getRegisterB();
//                    if (!use.get(registernum)) {
//                        location.setInstruction(null);
//                        changed = true;
//                        continue;
//                    }
                }

                LiveVarVisitor.setUse(insn, use);
            }
            node.setInSet(use);
        }
        if (changed)
            root.getMethodData().enablefix();
        return changed;
    }

    private static void setAndClear(int index, BitSet set, BitSet clear) {
        set.set(index);
        clear.clear(index);
    }

    public static void setUse(Instruction insn, BitSet use) {
        int registerNum;
        switch (insn.getOpcode().format) {
            case Format25x:
                throw new RuntimeException("I don't know how to deal with this invoke-lamda instruction");
            case Format3rc:
            case Format3rmi:
            case Format3rms:
                RegisterRangeInstruction range = (RegisterRangeInstruction)insn;
                registerNum = range.getStartRegister();
                for (int i = 0; i < range.getRegisterCount(); i++) {
//                    setAndClear(registerNum+i, use, def);
                    use.set(registerNum+i);
                }
                break;
            case Format35c:
            case Format35ms:
            case Format35mi:
                FiveRegisterInstruction five = (FiveRegisterInstruction)insn;
                int size = five.getRegisterCount();
                switch (size) {
                    case 5:
//                        setAndClear(five.getRegisterG(), use, def);
                        use.set(five.getRegisterG());
                    case 4:
//                        setAndClear(five.getRegisterF(), use, def);
                        use.set(five.getRegisterF());
                    case 3:
//                        setAndClear(five.getRegisterE(), use, def);
                        use.set(five.getRegisterE());
                    case 2:
//                        setAndClear(five.getRegisterD(), use, def);
                        use.set(five.getRegisterD());
                    case 1:
//                        setAndClear(five.getRegisterC(), use, def);
                        use.set(five.getRegisterC());
                        break;
                    default:
                        throw new RuntimeException("format35c size error!");
                }
                break;
            case Format23x:
                registerNum = ((ThreeRegisterInstruction)insn).getRegisterC();
//                setAndClear(registerNum, use, def);
                use.set(registerNum);
                if (isWiderUseC(insn.getOpcode()))
                    use.set(registerNum+1);
//                    setAndClear(registerNum+1, use, def);
            case Format12x:
            case Format22b:
            case Format22c:
            case Format22cs:
            case Format22s:
            case Format22t:
            case Format22x:
            case Format32x:
                registerNum = ((TwoRegisterInstruction)insn).getRegisterB();
//                setAndClear(registerNum, use, def);
                use.set(registerNum);
                if (isWideUseB(insn.getOpcode()))
                    use.set(registerNum+1);
//                    setAndClear(registerNum + 1, use, def);
            case Format11x:
            case Format21c:
            case Format21t:
            case Format31t:
                if (!insn.getOpcode().setsRegister() || has2addr(insn.getOpcode()) || checkcast(insn.getOpcode())) {
                    registerNum = ((OneRegisterInstruction)insn).getRegisterA();
//                    setAndClear(registerNum, use, def);
                    use.set(registerNum);
                    if (isWideUseA(insn.getOpcode()))
                        use.set(registerNum+1);
//                        setAndClear(registerNum+1, use, def);
                }
        }
    }

    private static boolean checkcast(Opcode opcode) { return opcode == Opcode.CHECK_CAST; }

    private static boolean has2addr(Opcode opcode) {
        return opcode.name.contains("2addr");
    }

    private static boolean isWiderUseC(Opcode opcode) {
        switch (opcode) {
            case ADD_LONG:
            case SUB_LONG:
            case MUL_LONG:
            case DIV_LONG:
            case REM_LONG:
            case AND_LONG:
            case OR_LONG:
            case XOR_LONG:
            case ADD_DOUBLE:
            case SUB_DOUBLE:
            case MUL_DOUBLE:
            case DIV_DOUBLE:
            case REM_DOUBLE:
            case CMPG_DOUBLE:
            case CMPL_DOUBLE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isWideUseB(Opcode opcode) {
        switch (opcode) {
            case NEG_LONG:
            case NEG_DOUBLE:
            case LONG_TO_INT:
            case LONG_TO_FLOAT:
            case LONG_TO_DOUBLE:
            case DOUBLE_TO_FLOAT:
            case DOUBLE_TO_INT:
            case DOUBLE_TO_LONG:
            case ADD_LONG:
            case SUB_LONG:
            case MUL_LONG:
            case DIV_LONG:
            case REM_LONG:
            case AND_LONG:
            case OR_LONG:
            case XOR_LONG:
            case SHL_LONG:
            case SHR_LONG:
            case USHR_LONG:
            case ADD_DOUBLE:
            case SUB_DOUBLE:
            case MUL_DOUBLE:
            case DIV_DOUBLE:
            case REM_DOUBLE:
            case ADD_LONG_2ADDR:
            case SUB_LONG_2ADDR:
            case MUL_LONG_2ADDR:
            case DIV_LONG_2ADDR:
            case REM_LONG_2ADDR:
            case AND_LONG_2ADDR:
            case OR_LONG_2ADDR:
            case XOR_LONG_2ADDR:
            case ADD_DOUBLE_2ADDR:
            case SUB_DOUBLE_2ADDR:
            case MUL_DOUBLE_2ADDR:
            case DIV_DOUBLE_2ADDR:
            case REM_DOUBLE_2ADDR:
            case CMPG_DOUBLE:
            case CMPL_DOUBLE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isWideUseA(Opcode opcode) {
        switch (opcode) {
            case RETURN_WIDE:
            case APUT_WIDE:
            case IPUT_WIDE:
            case SPUT_WIDE:
            case SHL_LONG_2ADDR:
            case SHR_LONG_2ADDR:
            case USHR_LONG_2ADDR:
            case IPUT_WIDE_QUICK:
            case ADD_LONG_2ADDR:
            case SUB_LONG_2ADDR:
            case MUL_LONG_2ADDR:
            case DIV_LONG_2ADDR:
            case REM_LONG_2ADDR:
            case AND_LONG_2ADDR:
            case OR_LONG_2ADDR:
            case XOR_LONG_2ADDR:
            case ADD_DOUBLE_2ADDR:
            case SUB_DOUBLE_2ADDR:
            case MUL_DOUBLE_2ADDR:
            case DIV_DOUBLE_2ADDR:
            case REM_DOUBLE_2ADDR:
                return true;
            default:
                return false;
        }
    }
}
