package com.cc.debugger.scripts.decode;

import com.cc.debugger.cmd.register;
import com.cc.debugger.scripts.MethodEntity;
import com.cc.debugger.scripts.MethodTracer;
import com.cc.debugger.scripts.Simplify;
import com.cc.debugger.scripts.Utility;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.attr.AType;
import com.cc.debugger.scripts.attr.MethodState;
import com.cc.debugger.scripts.attr.ValueAttrubite;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.InsnType;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.MethodReference;

/**
 * Created by CwT on 16/3/16.
 */
public class DecryptDecode extends AbstractDecode {

    public DecryptDecode(Simplify simplify) {
        super(simplify);
    }

    @Override
    public void parse(MethodTracer tracer, ThreadReference thread, Location location, InsnNode node) {
        MethodLocation insn = node.getInstruction();
        MethodState mstate = tracer.getMstate();
        switch (mstate.getState()) {
            case IDEL:
                if (node.insnType == InsnType.INVOKE) {
                    ReferenceInstruction refinsn = (ReferenceInstruction)insn.getInstruction();
                    if (refinsn == null) {
                        mstate.clear();
                        break;
                    }
                    MethodReference mthref = (MethodReference)refinsn.getReference();
                    MethodEntity entity = new MethodEntity(mthref);
                    if (simplify.getDecryptMethods().contains(entity)) {
                        mstate.setState(MethodState.STATE.MOVE_RESULT); //find out next move-result instruction
                        mstate.setType(mthref.getReturnType());
                        node.add(AFlag.REMOVE);
                    }
                }
                break;
            case MOVE_RESULT:
                if (node.insnType == InsnType.MOVE_RESULT) {
                    OneRegisterInstruction ori = (OneRegisterInstruction)insn.getInstruction();
                    if (ori == null) {
                        mstate.clear();
                        break;
                    }
                    mstate.setRegisterNum(ori.getRegisterA());
                    mstate.setState(MethodState.STATE.WAIT_RESULT);
                    node.add(AFlag.REPLACE_WITH_CONSTANT);
                }
                break;
            case WAIT_RESULT:
                if (mstate.getRegisterNum() >= 0) {
//                            System.out.println("get register:"+mstate.getRegisterNum()+" "+mstate.getType());
                    Value value = new register().getValue(simplify.getCtx(), thread, location,
                            Utility.mapRegister(simplify.getCtx().getVm(), tracer.getTargetMethod(), mstate.getRegisterNum()),
                            mstate.getType());
                    if (value != null) {
                        InsnNode replace = tracer.getLastReplace();
                        if (replace != null) {
                            replace.add(AType.VALUE, new ValueAttrubite(mstate.getType(), value));
                        }
                    }
                    mstate.clear();
                }
                break;
        }
    }
}
