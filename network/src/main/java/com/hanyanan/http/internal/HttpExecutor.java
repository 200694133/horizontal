package com.hanyanan.http.internal;

import com.hanyanan.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import hyn.com.lib.Asyncable;

/**
 * Created by hanyanan on 2015/5/13.
 */
public interface HttpExecutor extends Asyncable<HttpRequest, HttpResponse> {

//    public static final HttpExecutor sHttpExecutor = new HttpExecutor();
//
//    private HttpExecutor(){}
//
//    @Override
//    public HttpResponse run(HttpRequest request) throws Throwable {
//        String url = request.getUrl();
//        URL address_url = null;
//        HttpURLConnection connection;
//        try {
//            address_url = new URL(url);
//            connection = (HttpURLConnection) address_url.openConnection();
//            connection.setDoOutput(true);
//            connection.setDoInput(true);
//            connection.setUseCaches(false);
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Connection", "Keep-Alive");
//            connection.setRequestProperty("Charsert", "UTF-8");
//            connection.setRequestProperty("Cookie", "JSESSIONID=" + paramObj.getKey());
//            connection.setConnectTimeout(5000);
//            connection.setReadTimeout(5000);
//            connection.setInstanceFollowRedirects(true);
//            connection.setIfModifiedSince(0x2304320423L);
//
//            connection.setFixedLengthStreamingMode();
//
//
//
//
//
//
//
//
//
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            //TODO , close connection.
//        }
//
//
//        return null;
//    }
//
//
//    protected void sendBody(HttpRequest request, HttpURLConnection connection){
//        if(!HttpPreconditions.requiresRequestBody(request.getMethod().toString())) return ;
//
//    }



//    /**
//     * Performs the request and returns the response. May return null if this
//     * call was canceled.
//     */
//    Response getResponse(Request request, boolean forWebSocket) throws IOException {
//        // Copy body metadata to the appropriate request headers.
//        RequestBody body = request.body();
//        if (body != null) {
//            Request.Builder requestBuilder = request.newBuilder();
//
//            MediaType contentType = body.contentType();
//            if (contentType != null) {
//                requestBuilder.header("Content-Type", contentType.toString());
//            }
//
//            long contentLength = body.contentLength();
//            if (contentLength != -1) {
//                requestBuilder.header("Content-Length", Long.toString(contentLength));
//                requestBuilder.removeHeader("Transfer-Encoding");
//            } else {
//                requestBuilder.header("Transfer-Encoding", "chunked");
//                requestBuilder.removeHeader("Content-Length");
//            }
//
//            request = requestBuilder.build();
//        }
//
//        // Create the initial HTTP engine. Retries and redirects need new engine for each attempt.
//        engine = new HttpEngine(client, request, false, false, forWebSocket, null, null, null, null);
//
//        int followUpCount = 0;
//        while (true) {
//            if (canceled) {
//                engine.releaseConnection();
//                throw new IOException("Canceled");
//            }
//
//            try {
//                engine.sendRequest();
//                engine.readResponse();
//            } catch (RequestException e) {
//                // The attempt to interpret the request failed. Give up.
//                throw e.getCause();
//            } catch (RouteException e) {
//                // The attempt to connect via a route failed. The request will not have been sent.
//                HttpEngine retryEngine = engine.recover(e);
//                if (retryEngine != null) {
//                    engine = retryEngine;
//                    continue;
//                }
//                // Give up; recovery is not possible.
//                throw e.getLastConnectException();
//            } catch (IOException e) {
//                // An attempt to communicate with a server failed. The request may have been sent.
//                HttpEngine retryEngine = engine.recover(e, null);
//                if (retryEngine != null) {
//                    engine = retryEngine;
//                    continue;
//                }
//
//                // Give up; recovery is not possible.
//                throw e;
//            }
//
//            Response response = engine.getResponse();
//            Request followUp = engine.followUpRequest();
//
//            if (followUp == null) {
//                if (!forWebSocket) {
//                    engine.releaseConnection();
//                }
//                return response;
//            }
//
//            if (++followUpCount > MAX_FOLLOW_UPS) {
//                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
//            }
//
//            if (!engine.sameConnection(followUp.url())) {
//                engine.releaseConnection();
//            }
//
//            Connection connection = engine.close();
//            request = followUp;
//            engine = new HttpEngine(client, request, false, false, forWebSocket, connection, null, null,
//                    response);
//        }
//    }
}
