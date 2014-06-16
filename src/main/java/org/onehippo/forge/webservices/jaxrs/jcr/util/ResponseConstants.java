/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.webservices.jaxrs.jcr.util;

public abstract class ResponseConstants {

    public static final String STATUS_MESSAGE_OK = "OK";
    public static final String STATUS_MESSAGE_DELETED = "Deleted";
    public static final String STATUS_MESSAGE_CREATED = "Created";
    public static final String STATUS_MESSAGE_UPDATED = "Updated";
    public static final String STATUS_MESSAGE_BAD_REQUEST = "Request not understood due to errors or malformed syntax";
    public static final String STATUS_MESSAGE_UNAUTHORIZED = "Unauthorized";
    public static final String STATUS_MESSAGE_ACCESS_DENIED = "Access denied";
    public static final String STATUS_MESSAGE_NODE_NOT_FOUND = "Node not found";
    public static final String STATUS_MESSAGE_PROPERTY_NOT_FOUND = "Property not found";
    public static final String STATUS_MESSAGE_ERROR_OCCURRED = "Error occurred";

    private ResponseConstants() {
    }
}
