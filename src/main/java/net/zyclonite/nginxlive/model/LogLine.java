/*
 * nginxlive
 *
 * Copyright (c) 2015-2016   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginxlive.model;

import io.vertx.core.json.JsonObject;
import net.zyclonite.nginxlive.annotation.Queryable;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static net.zyclonite.nginxlive.util.JsonObjectHelper.putNonNull;

/**
 * Created by zyclonite on 25/03/15.
 *
 * log_format  json  '{"remoteAddr":"$remote_addr","remoteUser":"$remote_user",'
 *                   '"time":"$time_iso8601","requestURI":"$request_uri",'
 *                   '"queryString":"$query_string","requestMethod":"$request_method",'
 *                   '"scheme":"$scheme","serverProtocol":"$server_protocol",'
 *                   '"contentType":"$sent_http_content_type","status":$status,'
 *                   '"bodyBytesSent":$body_bytes_sent,"httpReferer":"$http_referer",'
 *                   '"httpUserAgent":"$http_user_agent","httpXForwardedFor":"$http_x_forwarded_for",'
 *                   '"host":"$host","bytesSent":"$bytes_sent",'
 *                   '"requestTime":"$request_time","serverName":"$server_name",'
 *                   '"serverAddr":"$server_addr","serverPort":"$server_port",'
 *                   '"hostname":"$hostname","requestLength":"$request_length",'
 *                   '"proxyHost":"$proxy_host","proxyPort":"$proxy_port",'
 *                   '"uri":"$uri","nginxVersion":"$nginx_version"}';
 *
 */
public class LogLine {
    private String remoteAddr;
    private String remoteUser;
    @Queryable
    private Date time;
    private String requestURI;
    private String queryString;
    private String requestMethod;
    private String scheme;
    private String serverProtocol;
    private String contentType;
    private Integer status;
    private Long bodyBytesSent;
    private String httpReferer;
    private String httpUserAgent;
    private String httpXForwardedFor;
    @Queryable
    private String host;
    private Long bytesSent;
    private Double requestTime;
    @Queryable
    private String serverName;
    private String serverAddr;
    private Integer serverPort;
    private String hostname;
    private Long requestLength;
    private String proxyHost;
    private Integer proxyPort;
    private String uri;
    private String nginxVersion;
    private String location;

    public LogLine() {

    }

    public LogLine(final JsonObject json) {
        this.remoteAddr = json.getString("remoteAddr");
        this.remoteUser = json.getString("remoteUser");
        this.time = Date.from(ZonedDateTime.parse(json.getJsonObject("time").getString("$date")).toInstant());
        this.requestURI = json.getString("requestURI");
        this.queryString = json.getString("queryString");
        this.requestMethod = json.getString("requestMethod");
        this.scheme = json.getString("scheme");
        this.serverProtocol = json.getString("serverProtocol");
        this.contentType = json.getString("contentType");
        this.status = json.getInteger("status");
        this.bodyBytesSent = json.getLong("bodyBytesSent");
        this.httpReferer = json.getString("httpReferer");
        this.httpUserAgent = json.getString("httpUserAgent");
        this.httpXForwardedFor = json.getString("httpXForwardedFor");
        this.host = json.getString("host");
        this.bytesSent = json.getLong("bytesSent");
        this.requestTime = json.getDouble("requestTime");
        this.serverName = json.getString("serverName");
        this.serverAddr = json.getString("serverAddr");
        this.serverPort = json.getInteger("serverPort");
        this.hostname = json.getString("hostname");
        this.requestLength = json.getLong("requestLength");
        this.proxyHost = json.getString("proxyHost");
        this.proxyPort = json.getInteger("proxyPort");
        this.uri = json.getString("uri");
        this.nginxVersion = json.getString("nginxVersion");
        this.location = json.getString("location");
    }

    public JsonObject toJsonObject() {
        final JsonObject jsonObject = new JsonObject();
        putNonNull(jsonObject, "remoteAddr", remoteAddr);
        putNonNull(jsonObject, "remoteUser", remoteUser);
        putNonNull(jsonObject, "time", new JsonObject()
                .put("$date", ZonedDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT)));
        putNonNull(jsonObject, "requestURI", requestURI);
        putNonNull(jsonObject, "queryString", queryString);
        putNonNull(jsonObject, "requestMethod", requestMethod);
        putNonNull(jsonObject, "scheme", scheme);
        putNonNull(jsonObject, "serverProtocol", serverProtocol);
        putNonNull(jsonObject, "contentType", contentType);
        putNonNull(jsonObject, "status", status);
        putNonNull(jsonObject, "bodyBytesSent", bodyBytesSent);
        putNonNull(jsonObject, "httpReferer", httpReferer);
        putNonNull(jsonObject, "httpUserAgent", httpUserAgent);
        putNonNull(jsonObject, "httpXForwardedFor", httpXForwardedFor);
        putNonNull(jsonObject, "host", host);
        putNonNull(jsonObject, "bytesSent", bytesSent);
        putNonNull(jsonObject, "requestTime", requestTime);
        putNonNull(jsonObject, "serverName", serverName);
        putNonNull(jsonObject, "serverAddr", serverAddr);
        putNonNull(jsonObject, "serverPort", serverPort);
        putNonNull(jsonObject, "hostname", hostname);
        putNonNull(jsonObject, "requestLength", requestLength);
        putNonNull(jsonObject, "proxyHost", proxyHost);
        putNonNull(jsonObject, "proxyPort", proxyPort);
        putNonNull(jsonObject, "uri", uri);
        putNonNull(jsonObject, "nginxVersion", nginxVersion);
        putNonNull(jsonObject, "location", location);
        return jsonObject;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getServerProtocol() {
        return serverProtocol;
    }

    public void setServerProtocol(String serverProtocol) {
        this.serverProtocol = serverProtocol;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getBodyBytesSent() {
        return bodyBytesSent;
    }

    public void setBodyBytesSent(Long bodyBytesSent) {
        this.bodyBytesSent = bodyBytesSent;
    }

    public String getHttpReferer() {
        return httpReferer;
    }

    public void setHttpReferer(String httpReferer) {
        this.httpReferer = httpReferer;
    }

    public String getHttpUserAgent() {
        return httpUserAgent;
    }

    public void setHttpUserAgent(String httpUserAgent) {
        this.httpUserAgent = httpUserAgent;
    }

    public String getHttpXForwardedFor() {
        return httpXForwardedFor;
    }

    public void setHttpXForwardedFor(String httpXForwardedFor) {
        this.httpXForwardedFor = httpXForwardedFor;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public Double getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Double requestTime) {
        this.requestTime = requestTime;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Long getRequestLength() {
        return requestLength;
    }

    public void setRequestLength(Long requestLength) {
        this.requestLength = requestLength;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getNginxVersion() {
        return nginxVersion;
    }

    public void setNginxVersion(String nginxVersion) {
        this.nginxVersion = nginxVersion;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
