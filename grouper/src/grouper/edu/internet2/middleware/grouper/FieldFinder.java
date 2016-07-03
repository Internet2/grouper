/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Find fields.
 * 
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.48 2009-08-11 20:18:08 mchyzer Exp $
 */
public class FieldFinder {

  /** cache name */
  static String cacheName = FieldFinder.class.getName() + ".fieldCache";

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(FieldFinder.class);

  /** default field cache seconds */
  static int defaultFieldCacheSeconds = 1*60;
  
  /** last time refreshed, for testing */
  static long lastTimeRefreshed = -1;
  
  /** 
   * every X minutes, get new elements.  access this with fieldCache() so it is not null
   * store this as a complete map in the cache so that elements dont disappear
   */
  static GrouperCache<Boolean, Map<String,Field>> fieldGrouperCache;

  /**
   * <pre>
   * return the field cache.
   * 
   * Customize with:
   *   &lt;cache  name="edu.internet2.middleware.grouper.FieldFinder.fieldCache"
   *        maxElementsInMemory="10000"
   *        eternal="false"
   *        timeToIdleSeconds="86400"
   *        timeToLiveSeconds="86400"
   *        overflowToDisk="false"
   * /&gt;
   * 
   * </pre>
   * @return cache
   */
  private static GrouperCache<Boolean, Map<String,Field>> fieldGrouperCache() {
    if (fieldGrouperCache == null) {
      fieldGrouperCache = new GrouperCache<Boolean, Map<String,Field>>(
          cacheName, 10000, false, 
          defaultFieldCacheSeconds, defaultFieldCacheSeconds, false);
    }
    return fieldGrouperCache;
  }
  
  /**
   * synchronize on this object
   */
  private static Object fieldGrouperCacheSemaphore = new Object();

  /**
   * init and return the cache
   * @return the cache
   */
  private static Map<String,Field> fieldCache() {
    //synchronize on GrouperStartup to avoid deadlock
    synchronized(fieldGrouperCacheSemaphore) {
      Map<String,Field> theFieldCache = fieldGrouperCache().get(Boolean.TRUE);
      if (theFieldCache == null || theFieldCache.size() == 0) {
        theFieldCache = internal_updateKnownFields();
      }
      return theFieldCache;
      
    }
  }

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
   * find all ids by type
   * @param type
   * @return all ids by type
   */
  public static List<String> findAllIdsByType(FieldType type) {
    try {
      Set<Field> fields = findAllByType(type);
      List<String> fieldIds = GrouperUtil.propertyList(fields, Field.PROPERTY_UUID, String.class);
      return fieldIds;
    } catch (SchemaException se) {
      throw new RuntimeException("Problem finding fields by type: " + type, se);
    }
  }

  /**
   * Get the specified field.
   * <pre class="eg">
   * Field f = FieldFinder.find(field);
   * </pre>
   * @param   name  Name of {@link Field} to return.
   * @return field
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
   * @return field
   * @throws  SchemaException
   */
  public static Field find(String name, boolean exceptionIfNotFound) {
    return find(name, exceptionIfNotFound, true);
  }

  /**
   * Get the specified field.
   * <pre class="eg">
   * Field f = FieldFinder.find(field);
   * </pre>
   * @param   name  Name of {@link Field} to return.
   * @param exceptionIfNotFound true if exception if not found, otherwise null
   * @param includePrivilegeSearch if should also use name as privilege
   * @return field
   * @throws  SchemaException
   */
  public static Field find(String name, boolean exceptionIfNotFound, boolean includePrivilegeSearch) 
    throws  SchemaException {
    Map<String, Field> theFieldCache = fieldCache();
    if ( theFieldCache.containsKey(name) ) {
      return theFieldCache.get(name);
    }
    
    //try by privilege name
    if (includePrivilegeSearch) {
      try {
        Privilege privilege = Privilege.getInstance(name);
        if (privilege != null ) {
          Field field = privilege.getField();
          if (field != null) {
            return field;
          }
        }
      } catch (Exception e) {
        //this is generally ok
        if (LOG.isDebugEnabled()) {
          LOG.debug("Problem finding privilege: " + name, e);
        }
      }
    }
    if (exceptionIfNotFound) {
      //dont refresh more than 2 minutes (or whatever it is set for)
      throw new SchemaException("field not found: " + name + ", expecting one of: "
        + GrouperUtil.stringValue(fieldCache().keySet()));
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
    Map<String, Field> theFieldCache = fieldCache();

    for (Field field : theFieldCache.values()) {
      if (StringUtils.equals(fieldId, field.getUuid())) {
        return field;
      }
    }
    //update cache if not found
    internal_updateKnownFields();
    for (Field field : theFieldCache.values()) {
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
    Set<Field> fields  = new LinkedHashSet(fieldCache().values());
    return fields;
  }
  
    /**
   * 
   * @return all fields
   * @throws GrouperException
   */
  private static Set findAllFromDb() throws GrouperException {
    Set fields = new LinkedHashSet();
    Iterator it = GrouperDAOFactory.getFactory().getField().findAll()
        .iterator();
    while (it.hasNext()) {
      fields.add((Field) it.next());
    }
    return fields;
  }
  
  /** 
   * @param groupType 
   * @return set of fields
   * @throws GrouperDAOException 
   */
  public static Set<Field> findAllByGroupType(GroupType groupType)
      throws  GrouperDAOException {
    Set<Field> fields  = new LinkedHashSet();
    
    Set<Field> allListFields = FieldFinder.findAllByType(FieldType.LIST);
    for (Field listField : allListFields) {
      if (!listField.getUuid().equals(Group.getDefaultList().getUuid())) {
        GroupType currGroupType = GroupTypeFinder.internal_findGroupTypeByField(listField, false);
        if (currGroupType != null && groupType.getUuid().equals(currGroupType.getUuid())) {
          fields.add(listField);
        }
      }
    }

    return fields;
  }
  
  /** 
   * @param groupTypeId
   * @return set of fields
   * @throws GrouperDAOException 
   */
  public static Set<Field> findAllByGroupType(String groupTypeId)
      throws  GrouperDAOException {
    
    @SuppressWarnings("deprecation")
    GroupType groupType = GroupTypeFinder.findByUuid(groupTypeId, true);

    return findAllByGroupType(groupType);
  }

  /**
   * Find all fields of the specified type.
   * <pre class="eg">
   * Set types = FieldFinder.findAllByType(type);
   * </pre>
   * @param type 
   * @return set of fields
   * @throws SchemaException 
   */
  public static Set<Field> findAllByType(FieldType type) 
    throws  SchemaException {
    
    Set<Field> fields  = new LinkedHashSet();
    
    for (Field field : fieldCache().values()) {
      if (StringUtils.equals(type.getType(),field.getTypeString())) {
        fields.add(field);
      }
    }
    return fields;
  }

  /**
   * synchronize on this object
   */
  private static Object internal_updateKnownFieldsSemaphore = new Object();

  /**
   * @return map
   */
  public static Map<String, Field> internal_updateKnownFields() {

    //synchronize on GrouperStartup to avoid deadlock
    synchronized(internal_updateKnownFieldsSemaphore) {
      GrouperStartup.startup();
      Map<String, Field> theFieldCache = new LinkedHashMap<String, Field>();
  
      Field f;
      Set   fieldsInRegistry = findAllFromDb();
      
      // find fields to add to the cache
      Iterator it = fieldsInRegistry.iterator();
      while (it.hasNext()) {
        f = (Field) it.next();
        theFieldCache.put( f.getName(), f );
        }
  
      fieldGrouperCache().put(Boolean.TRUE, theFieldCache);
  
      FieldFinder.lastTimeRefreshed = System.currentTimeMillis();
      
      return theFieldCache;
    }
  } 

  /**
   * clear cache (e.g. if schema export)
   */
  public static void clearCache() {

    //if not there dont worry
    if (fieldGrouperCache == null || fieldGrouperCache.get(Boolean.TRUE) == null ||
        fieldGrouperCache.get(Boolean.TRUE).size() == 0) {
      return;
    }

    fieldGrouperCache().get(Boolean.TRUE).clear();
  }
  
  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    System.out.println(FieldFinder.find("update"));
  }
  
}

