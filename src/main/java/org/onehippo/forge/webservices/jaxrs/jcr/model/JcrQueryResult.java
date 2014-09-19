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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Query result representation.
 */
@ApiModel(value = "Representation of a QueryResult")
@XmlRootElement(name="results")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"took", "hits", "nodes"})
public class JcrQueryResult {

    @ApiModelProperty(notes = "The nodes found by the query")
    private List<JcrQueryResultNode> nodes;
    @ApiModelProperty(notes = "The amount of nodes found")
    private long hits;
    @ApiModelProperty(notes = "The time it took to execute the query")
    private long took;

    /**
     * Returns the time it took to perform the query (milliseconds)
     * @return the time in milliseconds
     */
    public long getTook() {
        return took;
    }

    public void setTook(final long took) {
        this.took = took;
    }

    /**
     * Total number of found results
     * @return number of found results
     */
    public long getHits() {
        return hits;
    }

    public void setHits(final long hits) {
        this.hits = hits;
    }

    public List<JcrQueryResultNode> getNodes() {
        return nodes;
    }

    public void setNodes(final List<JcrQueryResultNode> nodes) {
        this.nodes = nodes;
    }

}
