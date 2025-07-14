package com.wearconnectivity;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONToWritableMap {
    
  /**
   * Parse JSONObject to ReadableMap
   *
   * @param obj The JSONObject to be parsed
   * @return readableMap from the JSONObject
   */
  public static ReadableMap fromJSONObject(JSONObject obj) throws JSONException {
    WritableMap result = Arguments.createMap();
    Iterator<String> keys = obj.keys();

    while (keys.hasNext()) {
      String key = keys.next();
      Object val = obj.get(key);
      if (val instanceof JSONObject) {
        result.putMap(key, fromJSONObject((JSONObject) val));
      } else if (val instanceof JSONArray) {
        result.putArray(key, fromJSONArray((JSONArray) val));
      } else if (val instanceof String) {
        result.putString(key, (String) val);
      } else if (val instanceof Boolean) {
        result.putBoolean(key, (Boolean) val);
      } else if (val instanceof Integer) {
        result.putInt(key, (Integer) val);
      } else if (val instanceof Double) {
        result.putDouble(key, (Double) val);
      } else if (val instanceof Long) {
        result.putInt(key, ((Long) val).intValue());
      } else if (obj.isNull(key)) {
        result.putNull(key);
      } else {
        // Unknown value type. Will throw
        throw new JSONException("Unexpected value when parsing JSON object. key: " + key);
      }
    }

    return result;
  }
}
