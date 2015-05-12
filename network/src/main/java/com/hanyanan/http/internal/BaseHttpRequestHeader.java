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
    protected final Map<String, String> headers = new LinkedHashMap<>();


    public BaseHttpRequestHeader(BaseHttpRequestHeader header){
        if(null != headers) {
            this.headers.putAll(headers);
        }
    }


    public static final class Builder {
        public static final String SEPARATOR = ";";
        private final Map<String, String> headers = new LinkedHashMap<>();

        /** Add an header line containing a field name, a literal colon, and a value. */
        public Builder appand(String line) {
            int index = line.indexOf(":");
            Preconditions.checkArgument(index >= 0, "Unexpected header: " + line);
            return doAppand(line.substring(0, index).trim(), line.substring(index + 1));
        }

        /** Add a field with the specified value. */
        public Builder appand(String name, String value) {
            Preconditions.checkNotNull(name, "name == null");
            Preconditions.checkNotNull(value, "value == null");

            if (name.length() == 0 || name.indexOf('\0') != -1 || value.indexOf('\0') != -1) {
                throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
            }
            return doAppand(name, value);
        }

        /**
         * Add a field with the specified value without any validation. Only
         * appropriate for headers from the remote peer or cache.
         */
        private Builder doAppand(String attr, String value) {
            String prev = headers.get(attr);
            if(!ValueUtil.isEmpty(prev)) {
                headers.put(attr, prev + SEPARATOR + value);
            }else{
                headers.put(attr, value);
            }

            return this;
        }

        public Builder remove(String attr) {
            Preconditions.checkNotNull(attr);
            headers.remove(attr);
            return this;
        }

        /**
         * Set a field with the specified value. If the field is not found, it is
         * added. If the field is found, the existing values are replaced.
         */
        public Builder put(String line) {
            int index = line.indexOf(":");
            Preconditions.checkArgument(index >= 0, "Unexpected header: " + line);
            return put(line.substring(0, index).trim(), line.substring(index + 1));
        }

        /**
         * Set a field with the specified value. If the field is not found, it is
         * added. If the field is found, the existing values are replaced.
         */
        public Builder put(String attr, String value) {
            Preconditions.checkNotNull(attr, "attr == null");
            Preconditions.checkNotNull(value, "value == null");
            return this;
        }

        /** Equivalent to {@code build().get(name)}, but potentially faster. */
        public String get(String name) {
            for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    return namesAndValues.get(i + 1);
                }
            }
            return null;
        }

        public Headers build() {
            return new Headers(this);
        }
    }
}
