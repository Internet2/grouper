package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsontype.impl;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.BeanProperty;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.JavaType;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

/**
 * Type deserializer used with {@link As#EXTERNAL_PROPERTY} inclusion mechanism.
 * Actual implementation may look bit strange since it depends on comprehensive
 * pre-processing done by {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.deser.BeanDeserializer}
 * to basically transform external type id into structure that looks more like
 * "wrapper-array" style inclusion. This intermediate form is chosen to allow
 * supporting all possible JSON structures.
 */
public class AsExternalTypeDeserializer extends AsArrayTypeDeserializer
{
    private static final long serialVersionUID = 1L;

    /**
     * @since 2.8
     */
    public AsExternalTypeDeserializer(JavaType bt, TypeIdResolver idRes,
            String typePropertyName, boolean typeIdVisible, JavaType defaultImpl)
    {
        super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl);
    }

    public AsExternalTypeDeserializer(AsExternalTypeDeserializer src,
            BeanProperty property) {
        super(src, property);
    }

    @Override
    public TypeDeserializer forProperty(BeanProperty prop) {
        if (prop == _property) { // usually if it's null
            return this;
        }
        return new AsExternalTypeDeserializer(this, prop);
    }

    @Override
    public As getTypeInclusion() { return As.EXTERNAL_PROPERTY; }

    // yes, very important distinction...
    @Override
    protected boolean _usesExternalId() {
        return true;
    }
}
