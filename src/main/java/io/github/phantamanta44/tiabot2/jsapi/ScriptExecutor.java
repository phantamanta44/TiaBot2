package io.github.phantamanta44.tiabot2.jsapi;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ScriptExecutor {
	
	public static void execute(String fileName, String script, boolean restricted) {
		execute(fileName, script.split("\n"), restricted, null);
	}
	
	public static void execute(String fileName, String script, boolean restricted, Map<String, ScriptableObject> inScope) {
		execute(fileName, script.split("\n"), restricted, inScope);
	}
	
	public static void execute(String fileName, String[] script, boolean restricted) {
		execute(fileName, script, restricted, null);
	}

	public static void execute(String fileName, String[] script, boolean restricted, Map<String, ScriptableObject> inScope) {
		try {
			Context ctx = Context.enter();
			Scriptable scope = ctx.initSafeStandardObjects();
			// TODO Define objects in scope
			// TODO Execute the script
		} catch (RhinoException e) {
			// TODO Handle it
		} catch (Exception e) {
			// TODO Handle it
		} finally {
			Context.exit();
		}
	}
	
}
