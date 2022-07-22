package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsonFormatVisitors;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.JavaType;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Interface {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.JsonSerializer} implements
 * to allow for visiting type hierarchy.
 */
public interface JsonFormatVisitable
{
    /**
     * Get the representation of the schema to which this serializer will conform.
     * 
     * @param typeHint Type of element (entity like property) being visited
     */
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
        throws JsonMappingException;
}
