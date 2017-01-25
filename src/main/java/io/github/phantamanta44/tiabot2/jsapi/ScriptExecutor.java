package io.github.phantamanta44.tiabot2.jsapi;

import org.mozilla.javascript.*;

public class ScriptExecutor {

    private static ThreadLocal<Scriptable> threadScope = new ThreadLocal<>();

    public static Scriptable start(boolean restricted) {
        Context ctx = Context.enter();
        Scriptable scope = restricted ? ctx.initSafeStandardObjects() : ctx.initStandardObjects();
        threadScope.set(scope);
        return scope;
    }

	public static ExecutionResults execute(String fileName, String script) {
		try {
            Context ctx = Context.getCurrentContext();
			Script compiled = ctx.compileString(script, fileName, 0, null);
            Scriptable scope = threadScope.get();
            Object returnValue = compiled.exec(ctx, scope);
            threadScope.remove();
            return new ExecutionResults(returnValue, scope);
		}  finally {
			Context.exit();
		}
	}
	
}
