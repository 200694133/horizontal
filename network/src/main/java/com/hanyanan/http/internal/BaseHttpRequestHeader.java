package com.hanyanan.http.internal;

import com.hanyanan.http.Headers;

import java.util.LinkedHashMap;
import java.util.Map;

import hyn.com.lib.Preconditions;
import hyn.com.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/5/9.
 * The header information for a http request. It mark the
 * cache controller/expire time/referer......
 */
public class BaseHttpRequestHeader {
    public static final String SEPARATOR = ";";
    private final Map<String, Object> headers = new LinkedHashMap<String, Object>();
    /** request charset */
//    private final String charSet;
    public BaseHttpRequestHeader(BaseHttpRequestHeader header) {
        if (null != headers) {
            this.headers.putAll(headers);
        }
    }

    /**
     * Add an header line containing a field name, a literal colon, and a value.
     */
    public BaseHttpRequestHeader appand(String line) {
        int index = line.indexOf(":");
        Preconditions.checkArgument(index >= 0, "Unexpected header: " + line);
        return doAppand(line.substring(0, index).trim(), line.substring(index + 1));
    }

    /**
     * Add a field with the specified value.
     */
    public BaseHttpRequestHeader appand(String name, Object value) {
        Preconditions.checkNotNull(name, "name == null");
        Preconditions.checkNotNull(value, "value == null");
        Preconditions.checkNotNull(value.toString(), "value.toString() == null");

        if (name.length() == 0 || name.indexOf('\0') != -1 || value.toString().indexOf('\0') != -1) {
            throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
        }
        return doAppand(name, value);
    }

    /**
     * Add a field with the specified value without any validation. Only
     * appropriate for headers from the remote peer or cache.
     */
    private BaseHttpRequestHeader doAppand(String attr, Object value) {
        Object prev = headers.get(attr);
        if (!ValueUtil.isEmpty(prev.toString())) {
            headers.put(attr, prev.toString() + SEPARATOR + value.toString());
        } else {
            headers.put(attr, value);
        }

        return this;
    }

    public BaseHttpRequestHeader remove(String attr) {
        Preconditions.checkNotNull(attr);
        headers.remove(attr);
        return this;
    }

    /**
     * Set a field with the specified value. If the field is not found, it is
     * added. If the field is found, the existing values are replaced.
     */
    public BaseHttpRequestHeader put(String line) {
        int index = line.indexOf(":");
        Preconditions.checkArgument(index >= 0, "Unexpected header: " + line);
        return put(line.substring(0, index).trim(), line.substring(index + 1));
    }

    /**
     * Set a field with the specified value. If the field is not found, it is
     * added. If the field is found, the existing values are replaced.
     */
    public BaseHttpRequestHeader put(String attr, Object value) {
        Preconditions.checkNotNull(attr, "attr == null");
        Preconditions.checkNotNull(value, "value == null");
        headers.put(attr, value);
        return this;
    }
}
