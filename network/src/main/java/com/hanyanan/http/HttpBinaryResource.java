package com.hanyanan.http;

import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/14.
 */
public interface HttpBinaryResource extends BinaryResource {
    /**
     * Return the recommond name for current resource.
     * The server may be provide a default file name for current resource, most of time it will be return {@code null},
     * So client cannot depende on this value.
     * </pr>
     *  The Content-Disposition identify the default file name value in http headers which come from server.
     *  Content-Disposition: attachment; filename="fname.ext". it will return the fname.ext as the default download file
     *  name.
     *  </pr>
     */
    public String getDisposition();
}
