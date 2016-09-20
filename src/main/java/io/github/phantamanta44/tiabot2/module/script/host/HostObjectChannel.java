package io.github.phantamanta44.tiabot2.module.script.host;

import io.github.phantamanta44.discord4j.data.wrapper.Channel;
import io.github.phantamanta44.discord4j.data.wrapper.Message;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;

public class HostObjectChannel extends ScriptableObject {

	private static final long serialVersionUID = 1L;
	private Channel dataSrc;
	
	public HostObjectChannel() {
		// NO-OP
	}
	
	public static HostObjectChannel impl(Channel src, Scriptable scope) {
		HostObjectChannel inst = (HostObjectChannel)Context.getCurrentContext().newObject(scope, "Channel");
		inst.dataSrc = src;
		return inst;
	}
	
	@Override
	public String getClassName() {
		return "Channel";
	}
	
	@JSGetter
	public String id() {
		return dataSrc.id();
	}
	
	@JSFunction
	public HostObjectMessage getMessage(String id) {
		Message msg = dataSrc.messages().withId(id).findAny().orElse(null);
		if (msg == null)
			return null;
		return HostObjectMessage.impl(msg, ScriptableObject.getTopLevelScope(this));
	}
	
	@JSGetter
	public HostObjectMessage[] messages() {
		Scriptable scope = ScriptableObject.getTopLevelScope(this);
		return dataSrc.messages()
				.map(m -> HostObjectMessage.impl(m, scope))
				.toArray(HostObjectMessage[]::new);
	}
	
	@JSGetter
	public String name() {
		return dataSrc.name();
	}
	
	@JSGetter
	public int position() {
		return dataSrc.position();
	}
	
	@JSGetter
	public String topic() {
		return dataSrc.topic();
	}
	
	@JSGetter
	public String tag() {
		return dataSrc.tag();
	}

	public Channel getDataSrc() {
		return dataSrc;
	}

}
