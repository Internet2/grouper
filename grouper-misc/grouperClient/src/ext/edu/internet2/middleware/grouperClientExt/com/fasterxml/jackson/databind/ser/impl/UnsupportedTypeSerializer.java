package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.ser.impl;

import java.io.IOException;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.*;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.JavaType;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.SerializerProvider;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Special bogus "serializer" that will throw
 * {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.exc.InvalidDefinitionException} if its {@link #serialize}
 * gets invoked. Most commonly registered as handler for unknown types,
 * as well as for catching unintended usage (like trying to use null
 * as Map/Object key).
 */
public class UnsupportedTypeSerializer
    extends StdSerializer<Object>
{
    private static final long serialVersionUID = 1L;

    protected final JavaType _type;

    protected final String _message;

    public UnsupportedTypeSerializer(JavaType t, String msg) {
        super(Object.class);
        _type = t;
        _message = msg;
    }

    @Override
    public void serialize(Object value, JsonGenerator g, SerializerProvider ctxt) throws IOException {
        ctxt.reportBadDefinition(_type, _message);
    }
}
