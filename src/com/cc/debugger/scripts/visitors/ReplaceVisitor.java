package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.Utility;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.attr.AType;
import com.cc.debugger.scripts.attr.ValueAttrubite;
import com.cc.debugger.scripts.node.BlockNode;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.MethodNode;
import com.sun.jdi.*;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.base.reference.BaseStringReference;
import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.dexbacked.util.FixedSizeList;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by CwT on 16/3/1.
 */

/**
 * replace original instructions like 'move-result' with 'const' instructions
 * to simplify
 */
public class ReplaceVisitor extends AbstractVisitor {

    @Override
    public void init(MethodNode node, Tracer tracer) {

    }

    @Override
    public void visit(MethodNode node, Tracer tracer) {
        for (InsnNode insnNode : tracer.getTracer()) {
            if (insnNode.contains(AFlag.REPLACE_WITH_CONSTANT)) {
                MethodLocation location = insnNode.getInstruction();
                ValueAttrubite attr = insnNode.get(AType.VALUE);
                if (attr == null) {
                    throw new RuntimeException("Should have a value!! Error in ReplaceVisitor");
                }
                final String type = attr.getType();
                if (location.getInstruction() == null) {
                    throw new RuntimeException("Something goes wrong in ReplaceVisitor");
                }
                int register = ((OneRegisterInstruction)location.getInstruction()).getRegisterA();
                BuilderInstruction replacement;
                if (type.charAt(0) == '[') {
                    //Array
                    //TODO we still need to add more instructions
                    /**
                     * const/16 v2, 0x10
                     * new-array v0, v2, [B
                     * fill-array-data v0, :label
                     * :label
                     * .array-data 1
                     */
                    BlockNode blockNode = node.getContainBlock(location);
                    if (blockNode == null) {
                        throw new RuntimeException("Something goes wrong in ReplaceVisitor! We didn't find block");
                    }
                    int freeRegister = Utility.getFreeRegister(blockNode, location);
                    BuilderInstruction addinstruction;
                    String subtype = type.substring(1);
                    final List<Value> values = ((ArrayReference)attr.getValue()).getValues();

                    BuilderInstruction new_array = new BuilderInstruction22c(Opcode.NEW_ARRAY, register, freeRegister, new BaseTypeReference() {
                        @Nonnull
                        @Override
                        public String getType() {
                            return type;
                        }
                    });
                    node.getMethodData().addInstruction(location.getIndex(), new_array);
                    BuilderInstruction const_length = new BuilderInstruction31i(Opcode.CONST, freeRegister, values.size());
                    node.getMethodData().addInstruction(new_array.getLocation().getIndex(), const_length);

                    switch (subtype) {
                        case "B":   //byte
                            addinstruction = new BuilderArrayPayload(1,
                                    new FixedSizeList<Number>() {

                                        @Override
                                        public int size() {
                                            return values.size();
                                        }

                                        @Nonnull
                                        @Override
                                        public Number readItem(int index) {
                                            return ((ByteValue)values.get(index)).byteValue();
                                        }
                                    });
                            break;
                        case "S":   //short
                            addinstruction = new BuilderArrayPayload(2,
                                    new FixedSizeList<Number>() {
                                        @Nonnull
                                        @Override
                                        public Number readItem(int index) {
                                            return ((ShortValue)values.get(index)).shortValue();
                                        }

                                        @Override
                                        public int size() {
                                            return values.size();
                                        }
                                    });
                            break;
                        case "I":   //Int
                            addinstruction = new BuilderArrayPayload(4,
                                    new FixedSizeList<Number>() {
                                        @Nonnull
                                        @Override
                                        public Number readItem(int index) {
                                            return ((IntegerValue)values.get(index)).intValue();
                                        }

                                        @Override
                                        public int size() {
                                            return values.size();
                                        }
                                    });
                            break;
                        case "J":   //Long
                            addinstruction = new BuilderArrayPayload(8,
                                    new FixedSizeList<Number>() {
                                        @Nonnull
                                        @Override
                                        public Number readItem(int index) {
                                            return ((LongValue)values.get(index)).longValue();
                                        }

                                        @Override
                                        public int size() {
                                            return values.size();
                                        }
                                    });
                            break;
                        default:
                            throw new RuntimeException("This type of value has not been supported yet:" + type);
                    }
                    node.getMethodData().addInstruction(addinstruction);
                    Label label = addinstruction.getLocation().addNewLabel();
                    replacement = new BuilderInstruction31t(Opcode.FILL_ARRAY_DATA, register, label);
                } else {
                    switch (type) {
                        case "D":
                            double value_double = ((DoubleValue)attr.getValue()).doubleValue();
                            throw new RuntimeException("This type of value has not been supported yet:" + type);
                        case "J":
                            long value_long = ((LongValue)attr.getValue()).longValue();
                            replacement = new BuilderInstruction51l(Opcode.CONST_WIDE, register, value_long);
                            break;
                        case "I":
                            int value_int = ((IntegerValue)attr.getValue()).intValue();
                            replacement = new BuilderInstruction31i(Opcode.CONST, register, value_int);
                            break;
                        case "S":
                            int value_short = ((ShortValue)attr.getValue()).shortValue();
                            replacement = new BuilderInstruction31i(Opcode.CONST, register, value_short);
                            break;
                        case "B":
                            int value_byte = ((ByteValue)attr.getValue()).byteValue();
                            replacement = new BuilderInstruction31i(Opcode.CONST, register, value_byte);
                            break;
                        case "Ljava/lang/String;":
                            final String value_string = ((StringReference)attr.getValue()).value();
                            replacement = new BuilderInstruction21c(Opcode.CONST_STRING, register, new BaseStringReference() {
                                @Nonnull
                                @Override
                                public String getString() {
                                    return value_string;
                                }
                            });
                            break;
                        default:
                            throw new RuntimeException("This type of value has not been supported yet:" + type);
                    }
                }
//                location.setInstruction(null);
                node.getMethodData().replaceInstruction(location.getIndex(), replacement);
            }
        }
    }
}
