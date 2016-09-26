package io.github.phantamanta44.tiabot2.jsapi.host;

import io.github.phantamanta44.discord4j.data.wrapper.Channel;
import io.github.phantamanta44.discord4j.data.wrapper.Guild;
import io.github.phantamanta44.discord4j.data.wrapper.GuildUser;
import io.github.phantamanta44.discord4j.data.wrapper.Role;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;

public class HostObjectGuild extends ScriptableObject {

	private static final long serialVersionUID = 1L;
	private Guild dataSrc;
	
	public HostObjectGuild() {
		// NO-OP
	}
	
	public static HostObjectGuild impl(Guild src, Scriptable scope) {
		HostObjectGuild inst = (HostObjectGuild)Context.getCurrentContext().newObject(scope, "Guild");
		inst.dataSrc = src;
		return inst;
	}
	
	@Override
	public String getClassName() {
		return "Guild";
	}
	
	@JSGetter
	public String name() {
		return dataSrc.name();
	}
	
	@JSGetter
	public String id() {
		return dataSrc.id();
	}
	
	@JSFunction
	public HostObjectChannel getChannel(String id) {
		Channel chan = dataSrc.channel(id);
		if (chan == null)
			return null;
		return HostObjectChannel.impl(chan, ScriptableObject.getTopLevelScope(this));
	}
	
	@JSGetter
	public HostObjectChannel[] channels() {
		Scriptable scope = ScriptableObject.getTopLevelScope(this);
		return dataSrc.channels()
				.map(c -> HostObjectChannel.impl(c, scope))
                .toArray(HostObjectChannel[]::new);
	}
	
	@JSGetter
	public String iconUrl() {
		return dataSrc.icon().getUrl();
	}
	
	@JSGetter
	public HostObjectUser owner() {
		return HostObjectUser.impl(dataSrc.owner().of(dataSrc), ScriptableObject.getTopLevelScope(this));
	}
	
	@JSFunction
	public HostObjectRole getRole(String id) {
		Role role = dataSrc.role(id);
		if (role == null)
			return null;
		return HostObjectRole.impl(role, ScriptableObject.getTopLevelScope(this));
	}
	
	@JSGetter
	public HostObjectRole[] roles() {
		Scriptable scope = ScriptableObject.getTopLevelScope(this);
		return dataSrc.roles()
				.map(r -> HostObjectRole.impl(r, scope))
                .toArray(HostObjectRole[]::new);
	}
	
	@JSFunction
	public HostObjectUser getUser(String id) {
		GuildUser user = dataSrc.user(id);
		if (user == null)
			return null;
		return HostObjectUser.impl(user, ScriptableObject.getTopLevelScope(this));
	}
	
	@JSGetter
	public HostObjectUser[] users() {
		Scriptable scope = ScriptableObject.getTopLevelScope(this);
		return dataSrc.users()
				.map(u -> HostObjectUser.impl(u, scope))
                .toArray(HostObjectUser[]::new);
	}
	
	public Guild getDataSrc() {
		return dataSrc;
	}

}
