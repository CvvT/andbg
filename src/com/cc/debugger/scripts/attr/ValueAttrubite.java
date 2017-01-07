package com.cc.debugger.scripts.attr;

import com.sun.jdi.Value;

/**
 * Created by CwT on 16/2/27.
 */
public class ValueAttrubite implements IAttribute {

    Value value;
    String type;

    public ValueAttrubite(String type, Value value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String getType() {
        return type;
    }

    public Value getValue() {
        return value;
    }
}
