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

package org.onehippo.forge.webservices;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.message.MessageImpl;
import org.junit.Test;
import org.onehippo.forge.webservices.jaxrs.exception.UnauthorizedException;

/**
 * Tests for {@link org.onehippo.forge.webservices.HippoAuthenticationRequestHandler}
 */
public class HippoAuthenticationRequestHandlerTest {

    @Test(expected = UnauthorizedException.class)
    public void testHandleRequest() throws Exception {
        final MessageImpl message = new MessageImpl();
        final HippoAuthenticationRequestHandler hippoAuthenticationRequestHandler = new HippoAuthenticationRequestHandler();
        hippoAuthenticationRequestHandler.handleRequest(message, null);
    }

    @Test(expected = UnauthorizedException.class)
    public void testHandleWithMessageRequest() throws Exception {
        final MessageImpl message = new MessageImpl();
        final AuthorizationPolicy authorizationPolicy = new AuthorizationPolicy();
        authorizationPolicy.setUserName("test");
        authorizationPolicy.setPassword("test");
        message.put(AuthorizationPolicy.class, authorizationPolicy);
        final HippoAuthenticationRequestHandler hippoAuthenticationRequestHandler = new HippoAuthenticationRequestHandler();
        hippoAuthenticationRequestHandler.handleRequest(message, null);
    }

}