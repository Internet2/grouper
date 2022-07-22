package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.ext;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.JsonGenerator;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.JsonToken;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.type.WritableTypeId;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.*;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * Serializer implementation for {@link java.sql.Blob} to write as binary
 * (for JSON and other formats Base64-encoded String, for binary formats as
 * true binary).
 *
 * @see edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.ser.std.ByteArraySerializer
 *
 * @since 2.12
 */
@JacksonStdImpl
@SuppressWarnings("serial")
public class SqlBlobSerializer
extends StdScalarSerializer<Blob>
{
    public SqlBlobSerializer()  {
        super(Blob.class);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Blob value) {
        // Could see if "length == 0" but that might be expensive operation
        return (value == null);
    }

    @Override
    public void serialize(Blob value, JsonGenerator gen, SerializerProvider ctxt)
            throws IOException {
        _writeValue(value, gen, ctxt);
    }

    // Copied from `com.fasterxml.jackson.databind.ser.std.ByteArraySerializer`
    @Override
    public void serializeWithType(Blob value, JsonGenerator gen, SerializerProvider ctxt,
            TypeSerializer typeSer)
        throws IOException
    {
        // most likely scalar
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen,
                typeSer.typeId(value, JsonToken.VALUE_EMBEDDED_OBJECT));
        _writeValue(value, gen, ctxt);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

    protected void _writeValue(Blob value, JsonGenerator gen, SerializerProvider ctxt)
            throws IOException
    {
        InputStream in = null;
        try {
            in = value.getBinaryStream();
        } catch(SQLException e)  {
            ctxt.reportMappingProblem(e,
                    "Failed to access `java.sql.Blob` value to write as binary value");
        }
        gen.writeBinary(ctxt.getConfig().getBase64Variant(), in, -1);
    }

    // Copied from `com.fasterxml.jackson.databind.ser.std.ByteArraySerializer`
    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
        throws JsonMappingException
    {
        // 08-Nov-2020, tatu: Same problem as for `byte[]`... should
        //    make work either as String/base64, or array of numbers,
        //   with a qualifier that can be used to determine it's byte[]
        JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(typeHint);
        if (v2 != null) {
            v2.itemsFormat(JsonFormatTypes.INTEGER);
        }
    }
}
