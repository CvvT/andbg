package com.cc.debugger.scripts.attr;

/**
 * Created by CwT on 16/2/26.
 */
public class MethodState {

    public enum STATE {
        IDEL,
        MOVE_RESULT,
        WAIT_RESULT,
    }

    private STATE state = STATE.IDEL;
    private int registerNum = -1;    //We need to get value of the register if the num >= 0
    private String type = null;

    public void clear() {
        state = STATE.IDEL;
        registerNum = -1;
        type = null;
    }

    public STATE getState() {
        return state;
    }

    public int getRegisterNum() {
        return registerNum;
    }

    public void setRegisterNum(int registerNum) {
        this.registerNum = registerNum;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
