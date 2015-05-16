package com.hanyanan.http.internal;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.LinkedList;

import hyn.com.lib.Preconditions;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/11.
 */
public class HttpRequestBody {
    private final LinkedList<EntityHolder> resources = new LinkedList<EntityHolder>();

    public HttpRequestBody add(String param, BinaryResource resource){
        Preconditions.checkNotNull(param);
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(resource.size() > 0, "The Body's size must be greater than 0!");
        resources.add(new EntityHolder(param, null, resource));
        return this;
    }

    public HttpRequestBody add(String param, String fileName, BinaryResource resource){
        Preconditions.checkNotNull(param);
        Preconditions.checkNotNull(fileName);
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(resource.size() > 0, "The Body's size must be greater than 0!");
        resources.add(new EntityHolder(param, null, resource));
        return this;
    }

    /** The entry of one file will upload. */
    private static final class EntityHolder {
        @NotNull private String param;
        @Nullable private String fileName;
        @NotNull BinaryResource resource;
        private EntityHolder(String param, String fileName, BinaryResource binaryResource){
            this.param = param;
            this.fileName = fileName;
            this.resource = binaryResource;
        }
    }



    public static HttpRequestBody create(String mediaType, String charSet, String content){
        //TODO
        return null;
    }

    public static HttpRequestBody create(String content){
        //TODO
        return null;
    }
}
