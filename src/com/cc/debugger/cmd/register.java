package com.cc.debugger.cmd;

import com.cc.debugger.Context;
import com.cc.debugger.SuspendState;
import com.sun.jdi.*;
import com.sun.tools.jdi.LocalVariableImpl;
import com.sun.tools.jdi.LocationImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/2/23.
 */
public class register extends abstractCommand {

    public register() {
        super("<register number> <type>", 1, null);
    }

    @Override
    public void perform(String[] argv, Context ctx) {
        if (argv.length > 1) {
            try {
                int registerNum = Integer.parseInt(argv[0]);
                String type = argv[1];

                SuspendState state = ctx.getState();
//                Method method = state.location.method();

                Value value = getValue(ctx, state.thread, state.location, registerNum, type);
                System.out.print(value);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("please input a integer number");
                return;
            } finally {
                done();
            }
        }
    }

    public Value getValue(Context ctx, ThreadReference thread, Location endlocation, int registerNum, String type) {
        try {
            Method method = endlocation.method();
            Constructor<LocalVariableImpl> localVariableConstructor = null;
            localVariableConstructor = LocalVariableImpl.class.getDeclaredConstructor(new Class[] { VirtualMachine.class,
                    Method.class, Integer.TYPE, Location.class, Location.class, String.class, String.class, String.class });
            localVariableConstructor.setAccessible(true);

            Constructor<LocationImpl> locationConstructor = LocationImpl.class.getDeclaredConstructor(new Class[] { VirtualMachine.class,
                    Method.class, Long.TYPE });
            locationConstructor.setAccessible(true);

            Location endLocation = locationConstructor.newInstance(new Object[] { ctx.getVm(), method,
                    Integer.valueOf(Integer.MAX_VALUE) });

            LocalVariable localVariable = (LocalVariable)localVariableConstructor.newInstance(new Object[] { ctx.getVm(), method,
                    registerNum,
                    method.locationOfCodeIndex(0L), endLocation, String.format("v%d", new Object[] { Integer.valueOf(registerNum) }),
                    type, null });

            return thread.frame(0).getValue(localVariable);
//                    System.out.println(value.toString());
//                    List<Value> values = ((ArrayReference)value).getValues();
//                    for (Value one : values) {
//                        System.out.print(((ByteValue)one).value());
//                    }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IncompatibleThreadStateException e) {
            e.printStackTrace();
        }
        return null;
    }
}
