package com.hanyanan.http;

import java.util.LinkedList;

import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/11.
 */
public class HttpRequestBody {









    public static class Builder {
        private LinkedList







        public void addBody(String param, BinaryResource binaryResource){

        }

        public void addBody(String param, String fileName, BinaryResource binaryResource){

        }
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
