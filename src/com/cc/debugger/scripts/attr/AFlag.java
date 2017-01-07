package com.cc.debugger.scripts.attr;

/**
 * Created by CwT on 16/2/26.
 */
public enum AFlag {
    REMOVE,
    REPLACE_WITH_CONSTANT,  //replace original instruction with move/const instruction
    REPLACE_REFLECTION,
    VALUE,
    ARRAY_PAYLOAD,

    HANDLER,
    EXCEPTION,

    REACHED,

    SYNTHETIC,

    //trace simplify
    REMOVE_FOR_FLAT,
}
