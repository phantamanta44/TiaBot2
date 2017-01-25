package io.github.phantamanta44.tiabot2.jsapi.host;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.data.wrapper.Message;
import io.github.phantamanta44.discord4j.util.concurrent.deferred.Deferreds;
import io.github.phantamanta44.discord4j.util.concurrent.deferred.IUnaryPromise;
import io.github.phantamanta44.discord4j.util.function.Lambdas;
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

	public IUnaryPromise<Message> flushBufferSafe(IEventContext chan) {
		return flushBuffer(chan, 8L);
	}
	
	private IUnaryPromise<Message> flushBuffer(IEventContext chan, long size) {
		if (msgBuffer.isEmpty())
			return Deferreds.call(() -> (Message)null).promise();
		String toSend = msgBuffer.stream()
				.limit(size)
				.reduce((a, b) -> a.concat("\n").concat(b)).orElse("");
		if (toSend.isEmpty())
			return Deferreds.call(() -> (Message)null).promise();
		return chan.send(toSend);
	}
	
}
