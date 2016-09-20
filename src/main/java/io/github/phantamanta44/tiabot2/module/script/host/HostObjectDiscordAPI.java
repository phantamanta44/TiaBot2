package io.github.phantamanta44.tiabot2.module.script.host;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;

import java.util.ArrayList;
import java.util.List;

public class HostObjectDiscordAPI extends ScriptableObject {

	private static final long serialVersionUID = 1L;
	private List<String> msgBuffer = new ArrayList<>();
	
	@Override
	public String getClassName() {
		return "DiscordAPI";
	}
	
	@JSFunction
	public void print(Object obj) {
		msgBuffer.add(Context.toString(obj));
	}
	
	public void flushBuffer(IEventContext chan) {
		flushBuffer(chan, 128L);
	}

	public void flushBufferSafe(IEventContext chan) {
		flushBuffer(chan, 8L);
	}
	
	private void flushBuffer(IEventContext chan, long size) {
		if (msgBuffer.isEmpty())
			return;
		String toSend = msgBuffer.stream()
				.limit(size)
				.reduce((a, b) -> a.concat("\n").concat(b)).orElse("");
		if (toSend.isEmpty())
			return;
		chan.send(toSend);
	}
	
}
