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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

public class LinkBuilderImpl {

    private UriBuilder ub;
    private Map<String, String> params = new HashMap<String, String>();

/*
    public Link build(Object... values) {
        URI resolvedLinkUri = getResolvedUri(values);
        return new Link();
    }
*/

    private URI getResolvedUri(Object... values) {
        URI uri = ub.build(values);
        return uri;
    }

    public LinkBuilderImpl param(String name, String value) {
        checkNotNull(name);
        checkNotNull(value);
        params.put(name, value);
        return this;
    }

    public LinkBuilderImpl rel(String rel) {
        String exisingRel = params.get(Link.REL);
        String newRel = exisingRel == null ? rel : exisingRel + " " + rel;
        return param(Link.REL, newRel);
    }

    public LinkBuilderImpl title(String title) {
        return param(Link.TITLE, title);
    }

    public LinkBuilderImpl type(String type) {
        return param(Link.TYPE, type);
    }

    public LinkBuilderImpl uri(URI uri) {
        ub = UriBuilder.fromUri(uri);
        return this;
    }

    public LinkBuilderImpl uri(String uri) {
        ub = UriBuilder.fromUri(uri);
        return this;
    }

    public LinkBuilderImpl uriBuilder(UriBuilder builder) {
        this.ub = builder;
        return this;
    }

    private void checkNotNull(String value) {
        if (value == null) {
            throw new IllegalArgumentException(value);
        }
    }

}