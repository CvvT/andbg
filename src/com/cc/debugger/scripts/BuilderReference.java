package com.cc.debugger.scripts;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.writer.builder.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/3/21.
 */
public class BuilderReference {

    public static BuilderStringReference makeStringReference(String str){
        if (str == null)
            return null;
        return new BuilderStringReference(str);
    }

    public static BuilderTypeReference makeTypeReference(String str){
        return new BuilderTypeReference(makeStringReference(str));
    }

    public static Reference makeFieldReference(String defineClass, String name, String type){
        return new BuilderFieldReference(makeTypeReference(defineClass),
                new BuilderStringReference(name),
                makeTypeReference(type));
    }

    public static BuilderTypeList makeTypeList(List<String> params){
        List<BuilderTypeReference> list = new ArrayList<>();
        for (String type: params){
            list.add(makeTypeReference(type));
        }
        return new BuilderTypeList(list);
    }

    public static BuilderProtoReference makeProtoReference(List<String> params, String returnType){
        StringBuilder sBuilder = new StringBuilder(makeshorty(returnType));
        for (String str: params){
            sBuilder.append(makeshorty(str));
        }
        return new BuilderProtoReference(makeStringReference(sBuilder.toString()),
                makeTypeList(params),
                makeTypeReference(returnType));
    }

    public static BuilderMethodParameter makeMethodParameter(String type, String name){
        return new BuilderMethodParameter(makeTypeReference(type),
                makeStringReference(name),
                BuilderAnnotationSet.EMPTY);
    }

    public static Reference makeMethodReference(String className, String methodName, List<String> params, String returntype){
        return new BuilderMethodReference(makeTypeReference(className),
                makeStringReference(methodName),
                makeProtoReference(params, returntype));
    }

    public static Method getMethod(DexBackedDexFile dexfile, String className, String methodName){
        for (ClassDef classDef: dexfile.getClasses()){
            if (classDef.getType().equals(className)) {
                System.out.println("Find the class");
                for (Method method: classDef.getMethods()){
                    if (method.getName().equals(methodName)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    public static BuilderMethod makeMethod(String className, String methodName, List<String> params,
                                           String returntype, int accessFlags, MethodImplementation impl){
        List<BuilderMethodParameter> list = new ArrayList<>();
        for (String type: params){
            BuilderMethodParameter param = makeMethodParameter(type, null);
            list.add(param);
        }
        return new BuilderMethod((BuilderMethodReference)makeMethodReference(className, methodName, params, returntype),
                list, accessFlags, BuilderAnnotationSet.EMPTY, impl);
    }

    public static String makeshorty(String type){
        if (type.charAt(0) == 'L') {
            return "L";
        }
        return type;
    }

    public static MethodReference getMethodRef(DexFile dexfile, String className, String methodName) {
        for (ClassDef classDef : dexfile.getClasses()) {
            if (!classDef.getType().equals(className))
                continue;
            for (Method method : classDef.getMethods())
                if (method.getName().equals(methodName))
                    return method;
        }
        return null;
    }
}

