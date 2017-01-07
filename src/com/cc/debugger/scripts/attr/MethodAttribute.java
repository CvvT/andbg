package com.cc.debugger.scripts.attr;

import java.util.List;

/**
 * Created by CwT on 16/3/23.
 */
public class MethodAttribute implements IAttribute {
    String methodName;
    String className;
    List<String> params;
    String returntype;
    int modifier;

    @Override
    public String getType() {
        return "Ljava/lang/reflect/Method;";
    }

    public int getModifier() {
        return modifier;
    }

    public List<String> getParams() {
        return params;
    }

    public String getReturntype() {
        return returntype;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void setReturntype(String returntype) {
        this.returntype = returntype;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }
}
