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

package org.onehippo.forge.webservices.jaxrs.jcr.model;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"score","link","values" })
public class JcrQueryResultNode {

    private double score;
    private URI link;
    private Map<String, String> values = new LinkedHashMap<String, String>();

    public void addValue(String column, String value) {
        this.values.put(column,value);
    }

    public double getScore() {
        return score;
    }

    public void setScore(final double score) {
        this.score = score;
    }

    public URI getLink() {
        return link;
    }

    public void setLink(final URI link) {
        this.link = link;
    }

    public Map<String, String> getValues() {
        return values;
    }
}
