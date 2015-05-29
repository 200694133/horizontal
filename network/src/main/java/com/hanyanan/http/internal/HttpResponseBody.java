package com.hanyanan.http.internal;

import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/25.
 */
public class HttpResponseBody {
    private final BinaryResource resource;

    HttpResponseBody(BinaryResource resource){
        this.resource = resource;
    }

    public BinaryResource getResource(){
        return resource;
    }
}
