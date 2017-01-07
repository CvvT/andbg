package com.cc.debugger.scripts.attr;

import com.sun.jdi.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CwT on 16/3/23.
 */
public class ArrayValueAttribute implements IAttribute {

    Map<Integer, ValueAttrubite> values = new HashMap<>(1);

    @Override
    public String getType() {
        return null;
    }

    public Map<Integer, ValueAttrubite> getValue() {
        return values;
    }

    public Value getValue(int reg) {
        if (values.containsKey(reg))
            return values.get(reg).getValue();
        return null;
    }
}
