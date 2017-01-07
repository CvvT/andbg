package com.cc.debugger.scripts.visitors;

import com.cc.debugger.scripts.BuilderReference;
import com.cc.debugger.scripts.Simplify;
import com.cc.debugger.scripts.Tracer;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.attr.AType;
import com.cc.debugger.scripts.attr.MethodAttribute;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.InsnType;
import com.cc.debugger.scripts.node.MethodNode;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.*;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.util.MethodUtil;
import org.jf.dexlib2.util.TypeUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/3/16.
 */
public class ReflectionVisitor extends AbstractVisitor {

    @Override
    public void init(MethodNode node, Tracer tracer) {

    }

    @Override
    public void visit(MethodNode node, Tracer tracer) {
        for (int i = 0; i < tracer.getTracer().size(); i++) {
            InsnNode oneNode = tracer.getTracer().get(i);
            MethodLocation insn = oneNode.getInstruction();

            if (oneNode.contains(AFlag.REPLACE_REFLECTION)) {
                ReferenceInstruction refinsn = (ReferenceInstruction) insn.getInstruction();
                if (refinsn == null) {
                    return;
                }
                MethodReference mthref = (MethodReference) refinsn.getReference();
                if (mthref.getDefiningClass().equals("Ljava/lang/reflect/Method;")
                        && mthref.getName().equals("invoke")) {
                    FiveRegisterInstruction fri = (FiveRegisterInstruction) insn.getInstruction();
                    MethodAttribute invokeMethod = oneNode.get(AType.METHOD);
                    if (invokeMethod == null)
                        continue;

                    if (AccessFlags.STATIC.isSet(invokeMethod.getModifier())) {
                        System.out.println("invoke static");
                        Simplify.getInstance().addMethod(invokeMethod);
                        List<String> params = new ArrayList<>(1);
                        params.add("[Ljava/lang/Object;");
                        String returnType = "Ljava/lang/Object;";
                        BuilderInstruction35c invoke = new BuilderInstruction35c(
                                Opcode.INVOKE_STATIC,
                                1, fri.getRegisterE(), 0, 0, 0, 0,
                                BuilderReference.makeMethodReference("LDeobfuscator;",
                                        invokeMethod.getMethodName(),
                                        params,
                                        returnType)
                        );
                        node.getMethodData().replaceInstruction(insn.getIndex(), invoke);
                    } else {
                        System.out.println("invoke virtual/direct");
                        Simplify.getInstance().addMethod(invokeMethod);
                        List<String> params = new ArrayList<>(2);
                        params.add("Ljava/lang/Object;");
                        params.add("[Ljava/lang/Object;");
                        String returnType = "Ljava/lang/Object;";
                        BuilderInstruction35c invoke = new BuilderInstruction35c(
                                Opcode.INVOKE_STATIC,
                                2, fri.getRegisterD(), fri.getRegisterE(), 0, 0, 0,
                                BuilderReference.makeMethodReference("LDeobfuscator;",
                                        invokeMethod.getMethodName(),
                                        params,
                                        returnType)
                        );
                        node.getMethodData().replaceInstruction(insn.getIndex(), invoke);
                    }
                }
            }
        }
    }

    public List<Integer> collectParams(Tracer tracer, int end, int arrayReg) {
        // for now, we assume all parameters are added in order as the parameters' types present in the method
        List<Integer> list = new ArrayList<>();
        for (int i = end; i >= 0; i--) {
            InsnNode oneNode = tracer.getTracer().get(i);
            MethodLocation loc = oneNode.getInstruction();
            Instruction insn = loc.getInstruction();
            if (insn == null)
                continue;
            if (insn.getOpcode() == Opcode.NEW_ARRAY && ((TwoRegisterInstruction)insn).getRegisterA() == arrayReg)
                break;
            if (insn.getOpcode() == Opcode.APUT_OBJECT && ((ThreeRegisterInstruction)insn).getRegisterB() == arrayReg) {
                list.add(((ThreeRegisterInstruction)insn).getRegisterA());
            }
        }
        return list;
    }

    public InsnNode findName(Tracer tracer, int end, int targetReg) {
        for (int i = end; i > 0; i--) {
            InsnNode oneNode = tracer.getTracer().get(i);
            MethodLocation loc = oneNode.getInstruction();
            Instruction insn = loc.getInstruction();
            if (insn == null)
                continue;
            if (insn.getOpcode() == Opcode.MOVE_RESULT_OBJECT) {
                int reg = ((OneRegisterInstruction)insn).getRegisterA();
                if (reg == targetReg) {
                    InsnNode ret = tracer.getTracer().get(i-1);
                    if (ret.insnType != InsnType.INVOKE)
                        throw new RuntimeException("invoke must be right behind the move_result_object");
                    return ret;
                }
            }
        }
        return null;
    }

    public static MethodImplementation makeMethodimpl(final MethodAttribute mattr) {
        boolean isStatic = AccessFlags.STATIC.isSet(mattr.getModifier());
        int registerCount = MethodUtil.getParameterRegisterCount(mattr.getParams(), isStatic);
        int totalregister = registerCount + 2;
        // one for index and the other for parameter
        MutableMethodImplementation impl = new MutableMethodImplementation(totalregister);
        int register = 1;   // start from v1, p0 : classObject, p1 : params
        int size = mattr.getParams().size();
        for (int i = 0; i < size; i++) {
            final String type = mattr.getParams().get(i);
            impl.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, i));
            impl.addInstruction(new BuilderInstruction23x(Opcode.AGET_OBJECT, register, totalregister - 1, 0));

            //cast object to primitive type
            if (TypeUtils.isPrimitiveType(type)) {
                impl.addInstruction(new BuilderInstruction21c(Opcode.CHECK_CAST, register, new BaseTypeReference() {
                    @Nonnull
                    @Override
                    public String getType() {
                        return getfullName(type);
                    }
                }));
                impl.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, register, 0, 0, 0, 0,
                        BuilderReference.makeMethodReference(getfullName(type), getValueMethod(type), new ArrayList<String>(0), type)
                ));

                if (!TypeUtils.isWideType(type)) {
                    impl.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT, register));
                } else {
                    impl.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_WIDE, register));
                }
            } else {
                impl.addInstruction(new BuilderInstruction21c(Opcode.CHECK_CAST, register, new BaseTypeReference() {
                    @Nonnull
                    @Override
                    public String getType() {
                        return getfullName(type);
                    }
                }));
            }

            if (type.charAt(0) == 'J' || type.charAt(0) == 'D') {
                register++;
            }
            register++;

        }
        if (isStatic) {
            if (registerCount <= 5) {
                impl.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_STATIC,
                        registerCount,
                        registerCount > 0 ? 1 : 0,
                        registerCount > 1 ? 2 : 0,
                        registerCount > 2 ? 3 : 0,
                        registerCount > 3 ? 4 : 0,
                        registerCount > 4 ? 5 : 0,
                        BuilderReference.makeMethodReference(mattr.getClassName(), mattr.getMethodName(),
                                mattr.getParams(), mattr.getReturntype())));
            } else {
                impl.addInstruction(new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, 1, registerCount,
                        BuilderReference.makeMethodReference(mattr.getClassName(), mattr.getMethodName(),
                                mattr.getParams(), mattr.getReturntype())));
            }
        } else {
            impl.addInstruction(new BuilderInstruction21c(Opcode.CHECK_CAST, totalregister - 2, new BaseTypeReference() {
                @Nonnull
                @Override
                public String getType() {
                    return mattr.getClassName();
                }
            }));
            if (registerCount <= 5) {
                impl.addInstruction(new BuilderInstruction35c(
                        MethodUtil.isDirect(mattr.getModifier()) ? Opcode.INVOKE_DIRECT : Opcode.INVOKE_VIRTUAL,
                        registerCount,
                        registerCount > 0 ? totalregister - 2 : 0,
                        registerCount > 1 ? 1 : 0,
                        registerCount > 2 ? 2 : 0,
                        registerCount > 3 ? 3 : 0,
                        registerCount > 4 ? 4 : 0,
                        BuilderReference.makeMethodReference(mattr.getClassName(), mattr.getMethodName(),
                                mattr.getParams(), mattr.getReturntype())));
            } else {
                impl.addInstruction(new BuilderInstruction12x(Opcode.MOVE_OBJECT, 0, totalregister - 2));
                impl.addInstruction(new BuilderInstruction3rc(
                        MethodUtil.isDirect(mattr.getModifier()) ? Opcode.INVOKE_DIRECT_RANGE : Opcode.INVOKE_VIRTUAL_RANGE,
                        1, registerCount,
                        BuilderReference.makeMethodReference(mattr.getClassName(), mattr.getMethodName(),
                                mattr.getParams(), mattr.getReturntype())));
            }
        }

        if ("V".equals(mattr.getReturntype())) {
            impl.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            return impl;
        }
        // move-result or move-result-object ??
        // cast primitive type to object
        if (TypeUtils.isPrimitiveType(mattr.getReturntype())) {
            List<String> params = new ArrayList<>(1);
            params.add(mattr.getReturntype());
            if (!TypeUtils.isWideType(mattr.getReturntype())) {
                impl.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT, 0));
                impl.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_STATIC,
                        1, 0, 0, 0, 0, 0,
                        BuilderReference.makeMethodReference(getfullName(mattr.getReturntype()),
                                "valueOf",
                                params,
                                getfullName(mattr.getReturntype())
                                )));
            } else {
                impl.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_WIDE, 0));
                impl.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_STATIC,
                        2, 0, 1, 0, 0, 0,
                        BuilderReference.makeMethodReference(getfullName(mattr.getReturntype()),
                                "valueOf",
                                params,
                                getfullName(mattr.getReturntype())
                        )));
            }
            impl.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
        } else {
            impl.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
        }
        impl.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 0));
        return impl;
    }

    /**
     * for primitive type
     * @param type
     * @return
     */
    public static String getValueMethod(String type) {
        switch (type) {
            case "Z":
                return "booleanValue";
            case "B":
                return "byteValue";
            case "S":
                return "shortValue";
            case "C":
                return "charValue";
            case "I":
                return "intValue";
            case "J":
                return "longValue";
            case "F":
                return "floatValue";
            case "D":
                return "doubleValue";
            default:
                throw new RuntimeException("Only accept primitive type");
        }
    }

    public static String getfullName(String type) {
        switch (type) {
            case "Z":
                return "Ljava/lang/Boolean;";
            case "B":
                return "Ljava/lang/Byte;";
            case "S":
                return "Ljava/lang/Short;";
            case "C":
                return "Ljava/lang/Character;";
            case "I":
                return "Ljava/lang/Integer;";
            case "J":
                return "Ljava/lang/Long;";
            case "F":
                return "Ljava/lang/Float;";
            case "D":
                return "Ljava/lang/Double;";
            default:
                return type;
        }
    }
}
