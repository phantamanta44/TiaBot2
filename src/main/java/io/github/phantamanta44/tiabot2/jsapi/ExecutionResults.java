package io.github.phantamanta44.tiabot2.jsapi;

import org.mozilla.javascript.Scriptable;

public class ExecutionResults {

    private final Object returnValue;
    private final Scriptable scope;

    public ExecutionResults(Object returnValue, Scriptable scope) {
        this.returnValue = returnValue;
        this.scope = scope;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public Scriptable getScope() {
        return scope;
    }

}
