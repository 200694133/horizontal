package com.hanyanan.http.internal;


import static hyn.com.lib.Preconditions.checkNotNull;
import static hyn.com.lib.Preconditions.checkArgument;

import com.google.common.collect.Maps;
import com.hanyanan.http.Headers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hyn.com.lib.Preconditions;
import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/5/9.
 * The header information for a http request. It mark the
 * cache controller/expire time/referer......
 * </pr>
 * Default Request header as follow:
 * Connection: keep-alive
 * Accept-Encoding: compress, gzip
 * Accept-Language: en,zh
 * Accept-Ranges: bytes
 * User-Agent: HANYANAN VERSION 0.0.1
 * Cache-Control: no-cache
 * Accept-Charset:
 * Accept: text/plain, text/html
 * Pragma: no-cache
 */
public class HttpHeader {
    public static final String SEPARATOR = ";";
    protected static final Map<String, Object> DEFAULT_HEADERS = new LinkedHashMap<String, Object>();
    static{
        DEFAULT_HEADERS.put(Headers.Connection.value(), "keep-alive");
        DEFAULT_HEADERS.put(Headers.Accept_Encoding.value(), "gzip, deflate");
        DEFAULT_HEADERS.put(Headers.Accept_Language.value(), "en,zh");
        DEFAULT_HEADERS.put(Headers.Accept_Ranges.value(), "bytes");
        DEFAULT_HEADERS.put(Headers.User_Agent.value(), "HANYANAN VERSION 0.0.1");
        DEFAULT_HEADERS.put(Headers.Cache_Control.value(), "no-cache");
        DEFAULT_HEADERS.put(Headers.Accept_Charset.value(), "utf-8");
        DEFAULT_HEADERS.put(Headers.Accept.value(), "*/*");
        DEFAULT_HEADERS.put(Headers.Pragma.value(), "no-cache");
    }
    private final Map<String, Object> headers = new LinkedHashMap<String, Object>();

    /**
     * The headers with high priority headers, it will override the {@link #headers}, it store the
     * manual operations.
     * */
    private final Map<String, Object> priorHeaders = Maps.newHashMap();
    public HttpHeader(HttpHeader header) {
        if (null != headers) {
            this.headers.putAll(header.headers);
        }
    }

    public HttpHeader(Map<String, List<String>> headers) {
        if(null == headers || headers.size() <= 0) return ;
        Set<Map.Entry<String, List<String>>> entrySet =  headers.entrySet();
        if(null == entrySet || entrySet.size() <= 0) return ;
        //TODO
        for(Map.Entry<String, List<String>> entry : entrySet){
            if(null == entry || entry.getKey() == null || entry.getValue() == null
                    || entry.getValue().size() <= 0) continue;
            StringBuilder value = new StringBuilder();
            for(String val : entry.getValue()){
                if(!ValueUtil.isEmpty(value)) {
                    value.append(SEPARATOR).append(val);
                }else{
                    value.append(value);
                }
            }
        }
    }

    public Object value(String head){
        return headers.get(head);
    }
    /**
     * Add an header line containing a field name, a literal colon, and a value.
     */
    public HttpHeader appand(String line) {
        int index = line.indexOf(":");
        checkArgument(index >= 0, "Unexpected header: " + line);
        return doAppand(line.substring(0, index).trim(), line.substring(index + 1));
    }

    /**
     * Add a field with the specified value.
     */
    public HttpHeader appand(String name, Object value) {
        checkNotNull(name, "name == null");
        checkNotNull(value, "value == null");
        checkNotNull(value.toString(), "value.toString() == null");

        if (name.length() == 0 || name.indexOf('\0') != -1 || value.toString().indexOf('\0') != -1) {
            throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
        }
        return doAppand(name, value);
    }

    /**
     * Add a field with the specified value without any validation. Only
     * appropriate for headers from the remote peer or cache.
     */
    private HttpHeader doAppand(String attr, Object value) {
        Object prev = headers.get(attr);
        if (!ValueUtil.isEmpty(prev.toString())) {
            headers.put(attr, prev.toString() + SEPARATOR + value.toString());
        } else {
            headers.put(attr, value);
        }

        return this;
    }

    public HttpHeader remove(String attr) {
        checkNotNull(attr);
        headers.remove(attr);
        return this;
    }

    /**
     * Set a field with the specified value. If the field is not found, it is
     * added. If the field is found, the existing values are replaced.
     */
    public HttpHeader put(String line) {
        int index = line.indexOf(":");
        checkArgument(index >= 0, "Unexpected header: " + line);
        return setHeadProperty(line.substring(0, index).trim(), line.substring(index + 1));
    }

    /**
     * Set a field with the specified value. If the field is not found, it is
     * added. If the field is found, the existing values are replaced.
     */
    public HttpHeader setHeadProperty(String attr, Object value) {
        checkNotNull(attr, "attr == null");
        checkNotNull(value, "value == null");
        headers.put(attr, value);
        return this;
    }

    public HttpHeader setPriorHeadProperty(String attr, Object value) {
        checkNotNull(attr, "attr == null");
        checkNotNull(value, "value == null");
        this.priorHeaders.put(attr, value);
        return this;
    }
    /**
     * Formate the current head to string mode.
     * @return
     */
    public String string(){
        return null;
    }
}
