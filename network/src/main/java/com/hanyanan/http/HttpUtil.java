package com.hanyanan.http;

import com.hanyanan.http.internal.DateUtils;

import java.util.Map;

/**
 * Created by hanyanan on 2015/5/11.
 */
public class HttpUtil {
    public static final byte[] CRLF = { '\r', '\n' };
    public static final String DEFAULT_CHARSET = "utf-8";
    public static final String DEFAULT_ENCODING = "gzip";
    public static final int DEFAULT_TIMEOUT = 5000;//5s
    public static final String DEFAULT_USER_AGENT = "horizontal-version 0.0.1 :)";

    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr) {
        try {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (Exception e) {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    /**
     * Retrieve a charset from headers
     *
     * @param headers An {@link java.util.Map} of headers
     * @param defaultCharset Charset to return if none can be found
     * @return Returns the charset specified in the Content-Type of this header,
     * or the defaultCharset if none can be found.
     */
    public static String parseCharset(Map<String, String> headers, String defaultCharset) {
        String contentType = headers.get(Headers.Content_Type);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return defaultCharset;
    }
}
