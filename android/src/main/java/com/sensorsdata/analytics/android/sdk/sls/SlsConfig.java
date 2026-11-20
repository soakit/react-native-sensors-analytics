package com.sensorsdata.analytics.android.sdk.sls;

/**
 * SLS WebTracking 直传配置(与 ircloud-ydp-web @ircloud/sensors 同一落点)。
 */
public final class SlsConfig {
    private static final String HOST = "cn-hangzhou.log.aliyuncs.com";
    private static final String PROJECT = "dinghuo123";
    private static final String LOGSTORE = "frontend_77ircloud";

    private SlsConfig() {
    }

    public static String trackUrl() {
        return "https://" + PROJECT + "." + HOST + "/logstores/" + LOGSTORE + "/track";
    }
}