package com.cc.debugger.scripts.decode;

import com.cc.debugger.cmd.register;
import com.cc.debugger.scripts.MethodEntity;
import com.cc.debugger.scripts.MethodTracer;
import com.cc.debugger.scripts.Simplify;
import com.cc.debugger.scripts.attr.AFlag;
import com.cc.debugger.scripts.attr.AType;
import com.cc.debugger.scripts.attr.MethodAttribute;
import com.cc.debugger.scripts.attr.ValueAttrubite;
import com.cc.debugger.scripts.node.InsnNode;
import com.cc.debugger.scripts.node.InsnType;
import com.google.common.collect.ImmutableList;
import com.sun.jdi.*;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.reference.MethodReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by CwT on 16/3/16.
 */
public class ReflectionDecode extends AbstractDecode {

    static Set<ReflectionEntity> sets = new HashSet<>();

    static {
//        sets.add(addEntity("Ljava/lang/Class;", "forName"));
//        sets.add(addEntity("Ljava/lang/Class;", "getDeclaredMethod"));
//        sets.add(addEntity("Ljava/lang/Class;", "getDeclaredField"));
        sets.add(addEntity("Ljava/lang/reflect/Method;", "invoke"));
        sets.add(addEntity("Ljava/lang/reflect/Field;", "get"));
    }

    static ReflectionEntity addEntity(String className, String methodName){
        ReflectionEntity entity = new ReflectionEntity();
        entity.setClassName(className);
        entity.setMethodName(methodName);
        return entity;
    }

    public ReflectionDecode(Simplify simplify) {
        super(simplify);
    }

    @Override
    public void parse(MethodTracer tracer, ThreadReference thread, Location location, InsnNode node) {
        MethodLocation insn = node.getInstruction();

        if (node.insnType == InsnType.INVOKE) {
            ReferenceInstruction refinsn = (ReferenceInstruction) insn.getInstruction();
            if (refinsn == null) {
                return;
            }
            MethodReference mthref = (MethodReference)refinsn.getReference();
            ReflectionEntity entity = new ReflectionEntity(mthref);
            FiveRegisterInstruction fri = (FiveRegisterInstruction)insn.getInstruction();
            if (fri == null) {
                return;
            }
            if (sets.contains(entity)) {
                Value value;
                switch (entity.getMethodName()) {
//                    case "forName":
//                        Value value = new register().getValue(simplify.getCtx(), thread, location, fri.getRegisterC(),
//                                "Ljava/lang/String;");
//                        node.add(AType.VALUE, new ValueAttrubite("Ljava/lang/String;", value));
//                        break;
//                    case "getDeclaredMethod":
//                    case "getDeclaredField":
//                        value = new register().getValue(simplify.getCtx(), thread, location, fri.getRegisterD(),
//                                "Ljava/lang/String;");
//                        node.add(AType.VALUE, new ValueAttrubite("Ljava/lang/String;", value));
//                        break;
                    case "invoke":
                        value = new register().getValue(simplify.getCtx(), thread, location, fri.getRegisterC(),
                                "Ljava/lang/reflect/Method;");
                        ObjectReference objRef = (ObjectReference) value;
                        ClassType ct = (ClassType)objRef.referenceType();
                        Method getName = ct.concreteMethodByName("getName", "()Ljava/lang/String;");
                        Method getDeclaringClass = ct.concreteMethodByName("getDeclaringClass", "()Ljava/lang/Class;");
                        Method getReturnType = ct.concreteMethodByName("getReturnType", "()Ljava/lang/Class;");
                        Method getParameterTypes = ct.concreteMethodByName("getParameterTypes", "()[Ljava/lang/Class;");
                        Method getModifiers = ct.concreteMethodByName("getModifiers", "()I");
                        try {
                            MethodAttribute mattr = new MethodAttribute();
                            StringReference strref = (StringReference)objRef.invokeMethod(thread, getName, new ArrayList<Value>(0), 0);
                            mattr.setMethodName(strref.value());
                            IntegerValue modifiers = (IntegerValue)objRef.invokeMethod(thread, getModifiers, new ArrayList<Value>(0), 0);
                            mattr.setModifier(modifiers.value());
                            ClassObjectReference cor = (ClassObjectReference)objRef.invokeMethod(thread, getDeclaringClass, new ArrayList<Value>(0), 0);
                            mattr.setClassName(cor.reflectedType().signature());
                            cor = (ClassObjectReference)objRef.invokeMethod(thread, getReturnType, new ArrayList<Value>(0), 0);
                            mattr.setReturntype(cor.reflectedType().signature());
                            ArrayReference array = (ArrayReference)objRef.invokeMethod(thread, getParameterTypes, new ArrayList<Value>(0), 0);
                            List<String> params = new ArrayList<>(array.length());
                            for (Value one : array.getValues()) {
                                params.add(((ClassObjectReference)one).reflectedType().signature());
                            }
                            mattr.setParams(params);
                            node.add(AType.METHOD, mattr);
                        } catch (InvalidTypeException e) {
                            e.printStackTrace();
                            break;
                        } catch (ClassNotLoadedException e) {
                            e.printStackTrace();
                            break;
                        } catch (IncompatibleThreadStateException e) {
                            e.printStackTrace();
                            break;
                        } catch (InvocationException e) {
                            e.printStackTrace();
                            break;
                        }
                        node.add(AFlag.REPLACE_REFLECTION);
                        break;
                    case "get":
                        break;
                }
            }
        }
    }

    static class ReflectionEntity {
        private String className;
        private String methodName;

        public ReflectionEntity(MethodReference ref) {
            className = ref.getDefiningClass();
            methodName = ref.getName();
        }

        public ReflectionEntity() {}

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
            result = prime * result + ((className == null) ? 0 : className.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ReflectionEntity other = (ReflectionEntity) obj;
            if (methodName == null) {
                if (other.methodName != null)
                    return false;
            } else if (!methodName.equals(other.methodName))
                return false;
            if (className == null) {
                if (other.className != null)
                    return false;
            } else if (!className.equals(other.className))
                return false;
            return true;
        }

        public String getClassName(){
            return className;
        }

        public String getMethodName(){
            return methodName;
        }

        public void setClassName(String classname){
            this.className = classname;
        }

        public void setMethodName(String methodname){
            this.methodName = methodname;
        }

    }
}
