/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

/**
 * Find group types.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupTypeFinder.java,v 1.35 2009-03-15 06:37:21 mchyzer Exp $
 */
public class GroupTypeFinder {
  
  /** 
   * every 10 minutes, get new elements
   */
  private static GrouperCache<String, GroupType> types = new GrouperCache<String, GroupType>(
      GroupTypeFinder.class.getName() + ".typeCache", 10000, false, 60*10, 60*10, false);
  
  /** 
   * every 10 minutes, get new elements
   */
  private static GrouperCache<String, AttributeDefName> legacyAttributes = new GrouperCache<String, AttributeDefName>(
      GroupTypeFinder.class.getName() + ".legacyAttributes", 10000, false, 60*10, 60*10, false);
  
  /** 
   * every 10 minutes, get new elements
   */
  private static GrouperCache<String, String> fieldIdToTypeId = new GrouperCache<String, String>(
      GroupTypeFinder.class.getName() + ".fieldIdToTypeIdCache", 10000, false, 60*10, 60*10, false);

  /** 
   * Find a {@link GroupType}.
   * <p/>
   * A {@link SchemaException} will be thrown if the type is not found.
   * <pre class="eg">
   * try {
   *   GroupType type = GroupTypeFinder.find(name);
   * }
   * catch (SchemaException eS) {
   *   // type does not exist
   * }
   * </pre>
   * @param   name  Find {@link GroupType} with this name.
   * @param exceptionIfNotFound 
   * @return  {@link GroupType}
   * @throws  SchemaException
   * @deprecated
   */
  public static GroupType find(String name, boolean exceptionIfNotFound) 
    throws  SchemaException {
    // First check to see if type is cached.
    if (types.containsKey(name)) {
      return (GroupType) types.get(name);
    }
    // If not, refresh known types as it may be new and try again. 
    internal_updateKnownTypes();
    if (types.containsKey(name)) {
      return (GroupType) types.get(name);
    }
    if (exceptionIfNotFound) {
      String msg = E.INVALID_GROUP_TYPE + name;
      throw new SchemaException(msg);
    }
    return null;
  }

  /** 
   * Find a {@link GroupType}.
   * <p/>
   * A {@link SchemaException} will be thrown if the type is not found.
   * <pre class="eg">
   * try {
   *   GroupType type = GroupTypeFinder.find(name);
   * }
   * catch (SchemaException eS) {
   *   // type does not exist
   * }
   * </pre>
   * @param   typeUuid  Find {@link GroupType} with this uuid.
   * @param exceptionIfNotFound 
   * @return  {@link GroupType}
   * @throws  SchemaException
   * @deprecated
   */
  public static GroupType findByUuid(String typeUuid, boolean exceptionIfNotFound) 
    throws  SchemaException {
    // First check to see if type is cached.
    for (GroupType groupType : types.values()) {
      if (StringUtils.equals(typeUuid, groupType.getUuid())) {
        return groupType;
      }
    }
    // If not, refresh known types as it may be new and try again. 
    internal_updateKnownTypes();
    for (GroupType groupType : types.values()) {
      if (StringUtils.equals(typeUuid, groupType.getUuid())) {
        return groupType;
      }
    }
    if (exceptionIfNotFound) {
      String msg = E.INVALID_GROUP_UUID + typeUuid;
      throw new SchemaException(msg);
    }
    return null;
  }

  /** 
   * Find a {@link GroupType}.
   * <p/>
   * A {@link SchemaException} will be thrown if the type is not found.
   * <pre class="eg">
   * try {
   *   GroupType type = GroupTypeFinder.find(name);
   * }
   * catch (SchemaException eS) {
   *   // type does not exist
   * }
   * </pre>
   * @param   name  Find {@link GroupType} with this name.
   * @return  {@link GroupType}
   * @throws  SchemaException
   * @Deprecated use the overload
   */
  @Deprecated
  public static GroupType find(String name) throws  SchemaException {
    return find(name, true);
  }

  /**
   * Find all public group types.
   * <pre class="eg">
   * Set types = GroupTypeFinder.findAll();
   * </pre>
   * @return  A {@link Set} of {@link GroupType} objects.
   * @deprecated
   */
  public static Set findAll() {
    internal_updateKnownTypes();
    Set       values  = new LinkedHashSet();
    GroupType t;
    Iterator  iter    = types.values().iterator();
    while (iter.hasNext()) {
      t = (GroupType) iter.next();
      values.add(t);
    }
    return values;
  } // public static Set findAll()

  /**
   * Find all assignable group types.
   * <pre class="eg">
   * Set types = GroupTypeFinder.findAllAssignable();
   * </pre>
   * @return  A {@link Set} of {@link GroupType} objects.
   * @deprecated
   */
  public static Set findAllAssignable() {
    return findAll();
  } // public static Set findAllAssignable()


  /**
   * 
   */
  public static void internal_updateKnownTypes() {

    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

      /**
       *
       */
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        Set<GroupType> typesInRegistry = new LinkedHashSet<GroupType>();
        Set<AttributeDefName> legacyAttributesInRegistry = new LinkedHashSet<AttributeDefName>();

        String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
        String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");
        String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");

        Stem stem = GrouperDAOFactory.getFactory().getStem().findByName(stemName, true);
        Set<AttributeDefName> attributes = GrouperDAOFactory.getFactory().getAttributeDefName().findByStem(stem.getUuid());
        for (AttributeDefName attribute : attributes) {
          if (attribute.getExtension().startsWith(groupTypePrefix)) {
            GroupType groupType = GroupType.internal_getGroupType(attribute, true);
            typesInRegistry.add(groupType);
            
            // see if there are fields for this type.  if so, cache them.
            String customListPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.customList.prefix");
            AttributeDefName customList = AttributeDefNameFinder.findByName(stemName + ":" + customListPrefix + groupType.getName(), false);
            if (customList != null) {
              List<String> fieldIds = attribute.getAttributeDef().getAttributeValueDelegate().retrieveValuesString(customList.getName());
              for (String fieldId : fieldIds) {
                fieldIdToTypeId.put(fieldId, groupType.getUuid());
              }
            }
          } else if (attribute.getExtension().startsWith(attributePrefix)) {
            legacyAttributesInRegistry.add(attribute);
          }
        }
        
        // Look for types to add
        GroupType tA;
        Iterator  addIter   = typesInRegistry.iterator();
        while (addIter.hasNext()) {
          tA = (GroupType) addIter.next();
          if (!types.containsKey(tA.getName())) {
            types.put(tA.getName(), tA); // New type.  Add it to the cached list.
          }
        }
        // Look for types to remove
        Set       toDel   = new LinkedHashSet();
        GroupType tD;
        Iterator  delIter = types.values().iterator();
        while (delIter.hasNext()) {
          tD = (GroupType) delIter.next();
          if (!typesInRegistry.contains(tD)) {
            toDel.add(tD.getName());  
          }
        }
        String    type;
        Iterator  toDelIter = toDel.iterator();
        while (toDelIter.hasNext()) {
          type = (String) toDelIter.next();
          types.remove(type);  
        }
        
        // Look for legacy attributes to add
        for (AttributeDefName legacyAttribute : legacyAttributesInRegistry) {
          if (!legacyAttributes.containsKey(legacyAttribute.getLegacyAttributeName(true))) {
            legacyAttributes.put(legacyAttribute.getLegacyAttributeName(true), legacyAttribute);
          }
        }
        
        // Look for legacy attributes to remove
        Set<String> toDel2 = new LinkedHashSet<String>();
        for (AttributeDefName legacyAttribute : legacyAttributes.values()) {
          if (!legacyAttributesInRegistry.contains(legacyAttribute)) {
            toDel2.add(legacyAttribute.getLegacyAttributeName(true));  
          }
        }
        
        for (String legacyAttributeName : toDel2) {
          legacyAttributes.remove(legacyAttributeName);  
        }
        
        return null;
      }
    });
  } // protected static void internal_updateKnownTypes()

  /**
   * clear cache (e.g. if schema export)
   */
  public static void clearCache() {
    types.clear();
    fieldIdToTypeId.clear();
    legacyAttributes.clear();
  }
  
  /**
   * @param field
   * @param exceptionIfNoGroupType
   * @return groupType
   */
  public static GroupType internal_findGroupTypeByField(Field field, boolean exceptionIfNoGroupType) {
    String typeId = fieldIdToTypeId.get(field.getUuid());
    if (typeId == null) {
      internal_updateKnownTypes();
      typeId = fieldIdToTypeId.get(field.getUuid());
    }
    
    if (typeId == null) {
      if (exceptionIfNoGroupType) {
        throw new RuntimeException("Field " + field.getName() + " is not associated with a group type.");
      }
      
      return null;
    }
    
    return findByUuid(typeId, true);
  }
  
  /**
   * @return legacy attributes
   */
  public static Set<AttributeDefName> internal_findAllLegacyAttributes() {
    internal_updateKnownTypes();
    Set<AttributeDefName> values = new LinkedHashSet<AttributeDefName>(legacyAttributes.values());
    return values;
  }

} // public class GroupTypeFinder

