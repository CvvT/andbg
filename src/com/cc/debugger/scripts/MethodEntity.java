package com.cc.debugger.scripts;

import org.jf.dexlib2.iface.reference.MethodReference;

import java.util.List;

/**
 * Created by CwT on 16/3/16.
 */
public class MethodEntity {
    private String className;
    private String methodName;
    private String description;
    private int accessFlag;

    public MethodEntity(MethodReference ref) {
        className = ref.getDefiningClass();
        methodName = ref.getName();
        List<? extends CharSequence> types = ref.getParameterTypes();
        StringBuilder sBuilder = new StringBuilder(types.size()+3);
        sBuilder.append("(");
        for (CharSequence type: types){
            sBuilder.append(type);
        }
        sBuilder.append(")");
        sBuilder.append(ref.getReturnType());
        description = sBuilder.toString();
    }

    public MethodEntity() {}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
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
        MethodEntity other = (MethodEntity) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
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

    public String getDescription(){
        return description;
    }

    public int getAccessFlag() {
        return accessFlag;
    }

    public void setClassName(String classname){
        this.className = classname;
    }

    public void setMethodName(String methodname){
        this.methodName = methodname;
    }

    public void setDescription(String descriptor){
        this.description = descriptor;
    }

    public void setAccessFlag(int accessFlag) {
        this.accessFlag = accessFlag;
    }
}
