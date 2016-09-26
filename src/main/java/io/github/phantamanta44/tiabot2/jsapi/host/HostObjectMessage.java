package io.github.phantamanta44.tiabot2.jsapi.host;

import io.github.phantamanta44.discord4j.data.wrapper.Message;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;

public class HostObjectMessage extends ScriptableObject {

	private static final long serialVersionUID = 1L;
	private Message dataSrc;
	
	public HostObjectMessage() {
		// NO-OP
	}
	
	public static HostObjectMessage impl(Message src, Scriptable scope) {
		HostObjectMessage inst = (HostObjectMessage)Context.getCurrentContext().newObject(scope, "Message");
		inst.dataSrc = src;
		return inst;
	}
	
	@Override
	public String getClassName() {
		return "Message";
	}
	
	@JSGetter
	public String body() {
		return dataSrc.body();
	}
	
	@JSGetter
	public HostObjectUser author() {
		return HostObjectUser.impl(dataSrc.author().of(dataSrc.guild()), ScriptableObject.getTopLevelScope(this));
	}
	
	@JSGetter
	public String id() {
		return dataSrc.id();
	}
	
	@JSGetter
	public HostObjectChannel channel() {
		return HostObjectChannel.impl(dataSrc.channel(), ScriptableObject.getTopLevelScope(this));
	}
	
	@JSGetter
	public long timestamp() {
		return dataSrc.timestamp();
	}
	
	public Message getDataSrc() {
		return dataSrc;
	}

}
