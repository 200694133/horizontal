package com.hanyanan.http.internal;


/**
 * Created by hanyanan on 2015/5/18.
 */
public interface ObjectResource<T> {

//    public ObjectParser getObjectParser();

    public T result() throws Throwable;
}
