/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Find fields.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.44 2009-03-15 06:37:21 mchyzer Exp $
 */
public class FieldFinder {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(FieldFinder.class);

  /** 
   * every 10 minutes, get new elements
   */
  private static GrouperCache<String, Field> fieldCache = new GrouperCache<String, Field>(
      FieldFinder.class.getName() + ".fieldCache", 10000, false, 60*10, 60*10, false);

  /**
   * @param name
   * @param type
   * @return the field id
   */
  @Deprecated
  public static String findFieldId(String name, String type) {
    return findFieldId(name, type, true);
  }

  /**
   * @param name
   * @param type
   * @param exceptionIfNull
   * @return the field id
   */
  public static String findFieldId(String name, String type, boolean exceptionIfNull) {

    //if both null then we are all set
    if (StringUtils.isBlank(name) && StringUtils.isBlank(type)) {
      return null;
    }

    //either both blank, or both filled in
    if (StringUtils.isBlank(name) || StringUtils.isBlank(type)) {
      throw new RuntimeException("Name or type cannot be blank: '" + name + "', '" + type + "'");
    }
    
    Field field = FieldFinder.find(name, false);
    if (field != null) {
      if (!StringUtils.equals(field.getTypeString(), type)) {
        throw new RuntimeException("Field with name '" + name + "' should have type: '" + type 
            + "' but instead has type: '" + field.getTypeString() + "'");
      }
      return field.getUuid();
    }
    if (exceptionIfNull) {
      throw new RuntimeException("Problem finding attribute name: " + name);
    }
    return null;
  }

  /**
   * find the field id or null if the name is empty.  Runtime exception if problem
   * @param attrName
   * @return the field uuid
   */
  public static String findFieldIdForAttribute(String attrName, boolean exceptionIfNull) {
    return findFieldId(attrName, "attribute", exceptionIfNull);
  }

  /**
   * Get the specified field.
   * <pre class="eg">
   * Field f = FieldFinder.find(field);
   * </pre>
   * @param   name  Name of {@link Field} to return.
   * @throws  SchemaException
   */
  @Deprecated
  public static Field find(String name) throws  SchemaException {
    return find(name, true);
  }

  /**
   * Get the specified field.
   * <pre class="eg">
   * Field f = FieldFinder.find(field);
   * </pre>
   * @param   name  Name of {@link Field} to return.
   * @param exceptionIfNotFound true if exception if not found, otherwise null
   * @throws  SchemaException
   */
  public static Field find(String name, boolean exceptionIfNotFound) 
    throws  SchemaException {
    if ( fieldCache.containsKey(name) ) {
      return fieldCache.get(name);
    }
    internal_updateKnownFields();
    if ( fieldCache.containsKey(name) ) {
      return fieldCache.get(name);
    }
    if (exceptionIfNotFound) {
      throw new SchemaException("field not found: " + name + ", expecting one of: "
          + GrouperUtil.stringValue(fieldCache.keySet()));
    }
    return null;
  } 

  /**
   * Get the specified field by id.
   * @param   fieldId  fieldId
   * @return the field or null if fieldId is blank.  will throw runtime exception if the field is not found
   * @deprecated use the overload
   */
  @Deprecated
  public static Field findById(String fieldId) {
    return findById(fieldId, true);
  }

  /**
   * Get the specified field by id.
   * @param   fieldId  fieldId
   * @param exceptionIfNull
   * @return the field or null if fieldId is blank.  will throw runtime exception if the field is not found
   */
  public static Field findById(String fieldId, boolean exceptionIfNull) {
    if (StringUtils.isBlank(fieldId)) {
      return null;
    }
    for (Field field : fieldCache.values()) {
      if (StringUtils.equals(fieldId, field.getUuid())) {
        return field;
      }
    }
    internal_updateKnownFields();
    for (Field field : fieldCache.values()) {
      if (StringUtils.equals(fieldId, field.getUuid())) {
        return field;
      }
    }
    if (exceptionIfNull) {
      throw new RuntimeException("Cant find field with id: '" + fieldId + "'");
    }
    return null;
  } 

  /**
   * Find all fields.
   * <pre class="eg">
   * Set fields = FieldFinder.findAll();
   * </pre>
   * @return  {@link Set} of {@link Field} objects.
   * @throws  GrouperException
   */
  public static Set findAll() 
    throws  GrouperException
  {
    Set       fields  = new LinkedHashSet();
    Iterator  it      = GrouperDAOFactory.getFactory().getField().findAll().iterator();
    while (it.hasNext()) {
      fields.add( (Field) it.next() ) ;
    }
    return fields;
  }

  /**
   * Find all fields of the specified type.
   * <pre class="eg">
   * Set types = FieldFinder.findAllByType(type);
   * </pre>
   */
  public static Set findAllByType(FieldType type) 
    throws  SchemaException
  {
    Set       fields  = new LinkedHashSet();
    Iterator  it      = GrouperDAOFactory.getFactory().getField().findAllByType(type).iterator();
    while (it.hasNext()) {
      fields.add( (Field) it.next() ) ;
    }
    return fields;
  }


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // TODO 20070531 split and test.
  public static void internal_updateKnownFields() {

    GrouperStartup.startup();
    Field f;
    Set   fieldsInRegistry = findAll();
    
    // find fields to add to the cache
    Iterator it = fieldsInRegistry.iterator();
    while (it.hasNext()) {
      f = (Field) it.next();
      if ( !fieldCache.containsKey( f.getName() ) ) {
        fieldCache.put( f.getName(), f );
      }
    }

    // find fields to remove from the cache
    Set toDel = new LinkedHashSet();
    for ( String key : fieldCache.keySet() ) {
      if ( !fieldsInRegistry.contains( (Field) fieldCache.get(key) ) ) {
        toDel.add( key );
      }
    }
    // and now remove the fields
    it = toDel.iterator();
    while (it.hasNext()) {
      fieldCache.remove( (String) it.next() );
    }
  } 

  /**
   * clear cache (e.g. if schema export)
   */
  public static void clearCache() {
    fieldCache.clear();
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {
    System.out.println(FieldFinder.find("update"));
  }
  
}

