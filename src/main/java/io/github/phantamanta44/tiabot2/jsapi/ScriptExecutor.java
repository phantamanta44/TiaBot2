package io.github.phantamanta44.tiabot2.jsapi;

import java.util.Map;

import com.github.fge.lambdas.Throwing;
import org.mozilla.javascript.*;

public class ScriptExecutor {

    private static ThreadLocal<Scriptable> threadScope = new ThreadLocal<>();

    public static Scriptable start(boolean restricted) {
        Context ctx = Context.enter();
        Scriptable scope = restricted ? ctx.initSafeStandardObjects() : ctx.initStandardObjects();
        threadScope.set(scope);
        return scope;
    }

	public static Scriptable execute(String fileName, String script) {
		try {
            Context ctx = Context.getCurrentContext();
			Script compiled = ctx.compileString(script, fileName, 0, null);
            Scriptable scope = threadScope.get();
            compiled.exec(ctx, scope);
            threadScope.remove();
            return scope;
		}  finally {
			Context.exit();
		}
	}
	
}
