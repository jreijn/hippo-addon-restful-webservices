package org.onehippo.forge.webservices.v1.jcr.util;

public abstract class ResponseConstants {

    public static final String STATUS_MESSAGE_OK = "OK";
    public static final String STATUS_MESSAGE_DELETED = "Deleted";
    public static final String STATUS_MESSAGE_CREATED = "Created";
    public static final String STATUS_MESSAGE_BAD_REQUEST = "Request not understood due to errors or malformed syntax";
    public static final String STATUS_MESSAGE_UNAUTHORIZED = "Unauthorized";
    public static final String STATUS_MESSAGE_ACCESS_DENIED = "Access denied";
    public static final String STATUS_MESSAGE_NODE_NOT_FOUND = "Node not found";
    public static final String STATUS_MESSAGE_ERROR_OCCURRED = "Error occurred";

    private ResponseConstants() {
    }
}
