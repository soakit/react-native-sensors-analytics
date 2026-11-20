package com.sensorsdata.analytics.android.sdk.sls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class SlsPayloadTest {

    @Test
    public void build_flattensPropertiesAndAddsPipelineType() throws Exception {
        String rawMessage = "[{\"type\":\"track\",\"event\":\"appLaunch\",\"distinct_id\":\"u1\","
                + "\"time\":1737100000000,\"_flush_time\":1737100001000,"
                + "\"properties\":{\"$lib\":\"Android\",\"$lib_version\":\"6.5.1\","
                + "\"doWhat\":\"launch\",\"tenantId\":34535819}}]";
        String body = SlsPayload.build(rawMessage);
        assertNotNull(body);
        JSONArray logs = new JSONObject(body).getJSONArray("__logs__");
        assertEquals(1, logs.length());
        JSONObject log = logs.getJSONObject(0);
        // 顶层字段保留
        assertEquals("appLaunch", log.getString("event"));
        assertEquals("u1", log.getString("distinct_id"));
        assertEquals("1737100000000", log.getString("time"));
        // properties 平铺 + 数值转字符串(SLS WebTracking 要求值为字符串)
        assertEquals("launch", log.getString("doWhat"));
        assertEquals("34535819", log.getString("tenantId"));
        assertEquals("Android", log.getString("$lib"));
        // 管道标记与 DB 注入字段
        assertEquals("sensors", log.getString("eventType"));
        assertTrue(log.has("_flush_time"));
        assertFalse(log.has("properties"));
    }

    @Test
    public void build_stripsIdentitiesAndLib() throws Exception {
        String rawMessage = "[{\"event\":\"e1\","
                + "\"identities\":{\"$identity_1\":\"x\"},"
                + "\"lib\":{\"$lib\":\"Android\"},"
                + "\"properties\":{\"identities\":\"{\\\"a\\\":1}\",\"doWhat\":\"t\"}}]";
        String body = SlsPayload.build(rawMessage);
        assertNotNull(body);
        JSONObject log = new JSONObject(body).getJSONArray("__logs__").getJSONObject(0);
        assertFalse(log.has("identities"));
        assertFalse(log.has("lib"));
        assertEquals("t", log.getString("doWhat"));
    }

    @Test
    public void build_returnsNullOnBrokenJson() {
        assertNull(SlsPayload.build("{not-json"));
    }
}