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

package org.onehippo.forge.webservices.jaxrs.management.model;

import java.util.Calendar;
import java.util.List;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( value = "User", description = "User resource representation" )
public class User {

    @ApiModelProperty( value = "Indicates if the user originates from an external system", required = false )
    private boolean external = false;
    @ApiModelProperty( value = "Indicates if the user account is active", required = true )
    private boolean active = true;
    @ApiModelProperty( value = "Indicates if the user account is a system user account", required = true )
    private boolean system = false;
    private String path;
    @ApiModelProperty( value = "The unique username", required = true )
    private String username;
    @ApiModelProperty( value = "The users first name", required = false )
    private String firstName;
    @ApiModelProperty( value = "The users last name", required = false )
    private String lastName;
    @ApiModelProperty( value = "The users e-mail", required = false )
    private String email;
    @ApiModelProperty( value = "The users password", required = false )
    private String password;
    private Calendar passwordLastModified;
    @ApiModelProperty( value = "The groups of which the user is a member", required = false )
    private List<Group> groups;
    private String href;

    public User() {

    }

    public User(final String username) {
        this.username = username;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(final boolean external) {
        this.external = external;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(final boolean system) {
        this.system = system;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    //@XmlJavaTypeAdapter(Iso8601Adapter.class)
    public Calendar getPasswordLastModified() {
        return passwordLastModified;
    }

    public void setPasswordLastModified(final Calendar passwordLastModified) {
        this.passwordLastModified = passwordLastModified;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(final List<Group> groups) {
        this.groups = groups;
    }

    public String getHref() {
        return href;
    }

    public void setHref(final String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return "User{" +
                "external=" + external +
                ", active=" + active +
                ", system=" + system +
                ", path='" + path + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
