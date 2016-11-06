/*
 * nginxlive
 *
 * Copyright (c) 2015-2016   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginxlive.util;

/**
 * Created by zyclonite on 29/10/15.
 */
public class Constants {
    public static final String DEFAULT_CONFIG = "config.json";
    public static final String DEFAULT_MMDB = "./GeoIP2-City.mmdb";
    public static final int DEFAULT_HTTP_PORT = 5141;
    public static final int DEFAULT_SYSLOG_PORT = 5140;
    public static final String DEFAULT_TIMEZONE = "UTC";
    public static final String MONGO_SERVICE = "mongo";
    public static final String MONGO_ACCESSLOG = "access";
    public static final String MMDB_SERVICE = "mmdb";
    public static final String PUBLIC_ACCESSLOG = "public.access";
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_ERROR = 2;
}
