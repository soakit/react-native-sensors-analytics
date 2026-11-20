package com.sensorsdata.analytics.android.sdk.sls;

import com.sensorsdata.analytics.android.sdk.SALog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * 把神策事件 JSON 数组(rawMessage,由 DB queryData 取出的明文)平铺成
 * SLS WebTracking 的 __logs__ 载荷。
 * 口径对齐 ircloud-ydp-web @ircloud/sensors 的 handleSensorsData:
 * 顶层剔除 identities/lib,properties 平铺,追加 eventType='sensors',所有值转字符串。
 */
final class SlsPayload {
    private static final String TAG = "SA.SlsPayload";
    private static final String PIPELINE_TYPE = "sensors";

    private SlsPayload() {
    }

    /**
     * @param rawMessage DB 取出的事件 JSON 数组字符串(每条已注入 _flush_time)
     * @return SLS WebTracking POST body;解析失败返回 null(调用方按 InvalidDataException 丢弃本批)
     */
    static String build(String rawMessage) {
        try {
            JSONArray events = new JSONArray(rawMessage);
            JSONArray logs = new JSONArray();
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.optJSONObject(i);
                if (event == null) {
                    continue;
                }
                JSONObject properties = event.optJSONObject("properties");
                event.remove("properties");
                event.remove("identities");
                event.remove("lib");
                if (properties != null) {
                    properties.remove("identities");
                    Iterator<String> keys = properties.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        event.put(key, properties.opt(key));
                    }
                }
                event.put("eventType", PIPELINE_TYPE);
                logs.put(stringifyValues(event));
            }
            JSONObject body = new JSONObject();
            body.put("__logs__", logs);
            return body.toString();
        } catch (Exception e) {
            SALog.printStackTrace(e);
            return null;
        }
    }

    /** SLS WebTracking 要求所有值为字符串;对象值序列化为 JSON 文本,null 直接丢弃。 */
    private static JSONObject stringifyValues(JSONObject in) {
        JSONObject out = new JSONObject();
        Iterator<String> keys = in.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = in.opt(key);
            if (value == null || JSONObject.NULL.equals(value)) {
                continue;
            }
            out.put(key, value instanceof String ? (String) value : String.valueOf(value));
        }
        return out;
    }
}