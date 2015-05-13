package com.hanyanan.http;

import java.util.LinkedList;

import hyn.com.lib.Preconditions;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/11.
 */
public class HttpRequestBody {
    private final LinkedList<EntityHolder> resources = new LinkedList<EntityHolder>();

    public void addBody(String param, BinaryResource resource){
        Preconditions.checkNotNull(param);
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(resource.size() > 0, "The Body's size must be greater than 0!");
        resources.add(new EntityHolder(param, null, resource));
    }

    public void addBody(String param, String fileName, BinaryResource resource){
        Preconditions.checkNotNull(param);
        Preconditions.checkNotNull(fileName);
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(resource.size() > 0, "The Body's size must be greater than 0!");
        resources.add(new EntityHolder(param, null, resource));
    }

    /** The entry of one file will upload. */
    private static final class EntityHolder {
        private String param;
        private String fileName;
        BinaryResource resource;
        private EntityHolder(String param, String fileName, BinaryResource binaryResource){
            this.param = param;
            this.fileName = fileName;
            this.resource = binaryResource;
        }
    }
}
