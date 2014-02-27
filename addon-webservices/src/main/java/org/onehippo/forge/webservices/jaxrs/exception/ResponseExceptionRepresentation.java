package org.onehippo.forge.webservices.jaxrs.exception;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Representation of exception status.
 *
 * @author Jeroen Reijn
 */
@XmlType(propOrder = {"statusCode", "message"})
@XmlRootElement(name = "status")
public class ResponseExceptionRepresentation {
    private int statusCode;
    private String message;

    @XmlElement(name = "status-code")
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @XmlElement(name = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
