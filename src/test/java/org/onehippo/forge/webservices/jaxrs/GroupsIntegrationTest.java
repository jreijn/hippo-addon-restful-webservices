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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.onehippo.forge.webservices.WebservicesIntegrationTest;
import org.onehippo.forge.webservices.jaxrs.management.model.Group;
import org.onehippo.forge.webservices.jaxrs.management.model.GroupCollection;
import org.onehippo.forge.webservices.jaxrs.management.model.User;
import org.onehippo.forge.webservices.jaxrs.management.model.UserCollection;

import static org.junit.Assert.assertTrue;

public class GroupsIntegrationTest extends WebservicesIntegrationTest {

    @Test
    public void testGetGroups() {
        final GroupCollection response = client
                .path("groups/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(GroupCollection.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.getGroups().size() == 4);
    }

    @Test
    public void testGetAdminGroup() {
        final Group group = client
                .path("groups/admin")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(Group.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue("admin".equals(group.getName()));
    }

    @Test
    public void testGroupNotFound() {
        final int status = client
                .path("groups/blah")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get().getStatus();
        assertTrue(status == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testAddAndRemoveGroups() {
        Group group = new Group("test");
        group.setExternal(false);
        List<String> members = new ArrayList<String>();
        members.add("admin");
        group.setMembers(members);

        final Response response = client
                .path("groups/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(group);
        assertTrue(response.getStatus() == Response.Status.CREATED.getStatusCode());

        client.reset();

        final Group testGroup = client
                .path("groups/test")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(Group.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(testGroup.getMembers().size()==1);

        client.reset();

        final Response delete = client.path("groups/test")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE).delete();
        assertTrue(delete.getStatus() == Response.Status.NO_CONTENT.getStatusCode());
    }

}
