package io.sprock.teamping.client;

import static io.sprock.teamping.TeamPing.pings;

import java.util.Iterator;

import com.google.gson.JsonObject;

public class PingManager {
	public static void clear() {
		Iterator<JsonObject> pingsIter = pings.iterator();
		while (pingsIter.hasNext()) {
			pingsIter.next();
			pingsIter.remove();
		}
	}
}
