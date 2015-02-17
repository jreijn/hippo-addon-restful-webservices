/*
 * Copyright 2015 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.forge.webservices.jaxrs;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.onehippo.forge.webservices.WebservicesIntegrationTest;
import org.onehippo.forge.webservices.jaxrs.management.model.GroupCollection;
import org.onehippo.forge.webservices.jaxrs.management.model.User;
import org.onehippo.forge.webservices.jaxrs.management.model.UserCollection;

import static org.junit.Assert.assertTrue;

public class UsersIntegrationTest extends WebservicesIntegrationTest {

    @Test
    public void testGetUsers() {
        final UserCollection response = client
                .path("users/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(UserCollection.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.getUsers().size() == 2);
    }

    @Test
    public void testGetAdminUser() {
        final User user = client
                .path("users/admin")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(User.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue("admin".equals(user.getUsername()));
    }

    @Test
    public void testGetAdminUserGroups() {
        @SuppressWarnings("unchecked")
        final GroupCollection groups = client
                .path("users/admin/groups")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(GroupCollection.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(groups.getGroups().size() == 1);
    }

    @Test
    public void testGetMe() {
        final User user = client
                .path("users/me")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(User.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue("admin".equals(user.getUsername()));
    }

    @Test
    public void testAddAndRemoveUser() {
        User user = new User("test");
        user.setExternal(false);
        user.setActive(true);
        user.setSystem(false);
        user.setPassword("test");

        final Response response = client
                .path("users/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(user);
        assertTrue(response.getStatus() == Response.Status.CREATED.getStatusCode());

        client.reset();
        final Response delete = client.path("users/test")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE).delete();
        assertTrue(delete.getStatus() == Response.Status.NO_CONTENT.getStatusCode());
    }

}
