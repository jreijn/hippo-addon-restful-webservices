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

package org.onehippo.forge.webservices.jaxrs.hateoas;

import java.net.URI;
import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;

public class ResourceTest {

    @Test
    public void testGetEntity() throws Exception {
        final Resource<String> stringResource = new Resource<String>("test");
        assertTrue("test".equals(stringResource.getEntity()));
    }

    @Test
    public void testSetEntity() throws Exception {
        final Resource<String> stringResource = new Resource<String>("test");
        stringResource.setEntity("test2");
        assertTrue("test2".equals(stringResource.getEntity()));
    }

    @Test
    public void testAddLink() throws Exception {
        final Resource<String> stringResource = new Resource<String>("test");
        final Link self = new Link("self", new URI("http://localhost/").toString());
        stringResource.addLink(self);
        assertTrue(stringResource.getLinks().size()==1);
    }

    @Test
    public void testSetLinks() throws Exception {
        final Resource<String> stringResource = new Resource<String>("test");
        final Link self = new Link("self", new URI("http://localhost/").toString());
        final ArrayList<Link> links = new ArrayList<Link>();
        links.add(self);
        stringResource.setLinks(links);
        assertTrue(stringResource.getLinks().size() == 1);
    }
}