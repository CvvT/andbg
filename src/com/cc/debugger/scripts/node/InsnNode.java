package com.cc.debugger.scripts.node;

import com.cc.debugger.scripts.attr.AttributeStorage;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.instruction.Instruction;

/**
 * Created by CwT on 16/2/26.
 */
public class InsnNode extends AttributeStorage {

    MethodLocation instruction;
    public final InsnType insnType;

    public InsnNode(MethodLocation instruction) {
        this.instruction = instruction;
        this.insnType = decode(instruction.getInstruction());
    }

    public MethodLocation getInstruction() {
        return instruction;
    }

    public static InsnType decode(Instruction insn) {
        switch (insn.getOpcode()) {
            case MOVE_RESULT:
            case MOVE_RESULT_OBJECT:
            case MOVE_RESULT_WIDE:
                return InsnType.MOVE_RESULT;
            case CONST:
            case CONST_4:
            case CONST_HIGH16:
            case CONST_WIDE:
            case CONST_WIDE_16:
            case CONST_WIDE_HIGH16:
            case CONST_WIDE_32:
                return InsnType.CONST;
            case MOVE:
            case MOVE_16:
            case MOVE_FROM16:
            case MOVE_WIDE:
            case MOVE_WIDE_16:
            case MOVE_WIDE_FROM16:
            case MOVE_OBJECT:
            case MOVE_OBJECT_16:
            case MOVE_OBJECT_FROM16:
                return InsnType.MOVE;
            case THROW:
                return InsnType.THROW;
            case MOVE_EXCEPTION:
                return InsnType.MOVE_EXCEPTION;
            case RETURN:
            case RETURN_OBJECT:
            case RETURN_VOID:
            case RETURN_WIDE:
                return InsnType.RETURN;
            case INVOKE_STATIC:
            case INVOKE_STATIC_RANGE:
            case INVOKE_DIRECT:
            case INVOKE_DIRECT_RANGE:
            case INVOKE_VIRTUAL:
            case INVOKE_VIRTUAL_RANGE:
            case INVOKE_SUPER:
            case INVOKE_SUPER_RANGE:
            case INVOKE_INTERFACE:
            case INVOKE_INTERFACE_RANGE:
                return InsnType.INVOKE;
            case MONITOR_ENTER:
                return InsnType.MONITOR_ENTER;
            case MONITOR_EXIT:
                return InsnType.MONITOR_EXIT;
            case GOTO:
            case GOTO_16:
            case GOTO_32:
                return InsnType.GOTO;
            case IF_EQ:
            case IF_EQZ:
            case IF_GE:
            case IF_GEZ:
            case IF_GT:
            case IF_GTZ:
            case IF_LE:
            case IF_LEZ:
            case IF_LT:
            case IF_LTZ:
            case IF_NE:
            case IF_NEZ:
                return InsnType.IF;
            case PACKED_SWITCH:
            case SPARSE_SWITCH:
                return InsnType.SWITCH;
            case FILL_ARRAY_DATA:
                return InsnType.FILL_ARRAY;
            case APUT:
            case APUT_OBJECT:
            case APUT_WIDE:
            case APUT_BOOLEAN:
            case APUT_BYTE:
            case APUT_CHAR:
            case APUT_SHORT:
                return InsnType.APUT;
            default:
                return InsnType.NOP;
        }
    }
}
