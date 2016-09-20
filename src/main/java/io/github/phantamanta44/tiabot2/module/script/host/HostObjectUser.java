package io.github.phantamanta44.tiabot2.module.script.host;

import io.github.phantamanta44.discord4j.data.wrapper.GuildUser;
import io.github.phantamanta44.discord4j.data.wrapper.user.UserStatus;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;

public class HostObjectUser extends ScriptableObject {

	private static final long serialVersionUID = 1L;
	private GuildUser dataSrc;
	
	public HostObjectUser() {
		// NO-OP
	}
	
	public static HostObjectUser impl(GuildUser src, Scriptable scope) {
		HostObjectUser inst = (HostObjectUser)Context.getCurrentContext().newObject(scope, "GuildUser");
		inst.dataSrc = src;
		return inst;
	}
	
	@Override
	public String getClassName() {
		return "GuildUser";
	}
	
	@JSGetter
	public String name() {
		return dataSrc.name();
	}
	
	@JSGetter
	public String id() {
		return dataSrc.id();
	}
	
	@JSGetter
	public String tag() {
		return dataSrc.tag();
	}
	
	@JSGetter
	public boolean status() {
		return dataSrc.status() != UserStatus.OFFLINE;
	}
	
	@JSGetter
	public String game() {
		String sub = dataSrc.subtitle().getMessage();
		return sub == null ? "" : sub;
	}
	
	@JSGetter
	public String avatarUrl() {
		return dataSrc.avatar().getUrl();
	}

	@JSGetter
    public HostObjectRole[] roles() {
        Scriptable scope = ScriptableObject.getTopLevelScope(this);
        return dataSrc.roles()
                .map(r -> HostObjectRole.impl(r, scope))
                .toArray(HostObjectRole[]::new);
    }
	
	public GuildUser getDataSrc() {
		return dataSrc;
	}

}
