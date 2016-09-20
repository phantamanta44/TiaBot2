package io.github.phantamanta44.tiabot2.module.script.host;

import io.github.phantamanta44.discord4j.data.wrapper.Role;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;

public class HostObjectRole extends ScriptableObject {

	private static final long serialVersionUID = 1L;
	private Role dataSrc;
	
	public HostObjectRole() {
		// NO-OP
	}
	
	public static HostObjectRole impl(Role src, Scriptable scope) {
		HostObjectRole inst = (HostObjectRole)Context.getCurrentContext().newObject(scope, "Role");
		inst.dataSrc = src;
		return inst;
	}
	
	@Override
	public String getClassName() {
		return "Role";
	}
	
	@JSGetter
	public String colour() {
		return Integer.toHexString(dataSrc.color().getRGB());
	}
	
	@JSGetter
	public String id() {
		return dataSrc.id();
	}
	
	@JSGetter
	public String name() {
		return dataSrc.name();
	}
	
	@JSGetter
	public int position() {
		return dataSrc.weight();
	}
	
	public Role getDataSrc() {
		return dataSrc;
	}

}
