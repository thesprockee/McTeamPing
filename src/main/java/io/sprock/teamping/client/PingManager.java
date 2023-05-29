package io.sprock.teamping.client;

import com.google.gson.JsonObject;

import static io.sprock.teamping.TeamPing.pings;

import java.util.Iterator;

public class PingManager {
  public static void clear() {
    Iterator<JsonObject> pingsIter = pings.iterator();
    while (pingsIter.hasNext()) {
      pingsIter.next();
      pingsIter.remove();
    }
  }
}
