package org.onehippo.forge.webservices.v1.jcr;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModelProperty;

@XmlRootElement(name="search")
public class SearchQuery {
    @ApiModelProperty(required = true)
    private String statement;
    @ApiModelProperty(required = true)
    private String language;
    private int limit = 200;
    private int offset;
    private int size;
    private Set<String> sort;

    public String getStatement() {
        return statement;
    }

    public void setStatement(final String statement) {
        this.statement = statement;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public Set<String> getSort() {
        return sort;
    }

    public void setSort(final Set<String> sort) {
        this.sort = sort;
    }
}
