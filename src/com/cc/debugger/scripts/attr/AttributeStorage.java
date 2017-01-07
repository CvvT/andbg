package com.cc.debugger.scripts.attr;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by CwT on 16/2/26.
 */
public class AttributeStorage {

    private final Set<AFlag> flags;
    private final Map<AType, IAttribute> attributes;

    public AttributeStorage() {
        flags = EnumSet.noneOf(AFlag.class);
        attributes = new HashMap<>();
    }

    public void add(AFlag flag) {
        flags.add(flag);
    }

    public void add(AType type, IAttribute obj) {
        attributes.put(type, obj);
    }

    public void addAll(AttributeStorage otherList) {
        flags.addAll(otherList.flags);
//        attributes.putAll(otherList.attributes);
    }

    public boolean contains(AFlag flag) {
        return flags.contains(flag);
    }

    public boolean contains(AType type) { return attributes.containsKey(type); }

    public void remove(AFlag flag) {
        flags.remove(flag);
    }

    public void remove(AType type) { attributes.remove(type); }

    @SuppressWarnings("unchecked")
    public <T extends IAttribute> T get(AType type) {
        return (T) attributes.get(type);
    }

    public void clear() {
        flags.clear();
        attributes.clear();
    }

    public boolean isEmpty() {
        return flags.isEmpty() && attributes.isEmpty();
    }
}
