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
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GroupTypeFinder.class);

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
   */
  public static Set findAll() {
    internal_updateKnownTypes();
    Set       values  = new LinkedHashSet();
    GroupType t;
    Iterator  iter    = types.values().iterator();
    while (iter.hasNext()) {
      t = (GroupType) iter.next();
      if ( !t .getIsInternal() ) {
        values.add(t); // We only want !internal group types
      }
    }
    return values;
  } // public static Set findAll()

  /**
   * Find all assignable group types.
   * <pre class="eg">
   * Set types = GroupTypeFinder.findAllAssignable();
   * </pre>
   * @return  A {@link Set} of {@link GroupType} objects.
   */
  public static Set findAllAssignable() {
    Set       types = new LinkedHashSet();
    GroupType t;
    Iterator  iter  = findAll().iterator();
    while (iter.hasNext()) {
      t = (GroupType) iter.next();
      if ( t.getIsAssignable() ) {
        types.add(t);
      }
    }
    return types;
  } // public static Set findAllAssignable()


  /**
   * 
   */
  public static void internal_updateKnownTypes() {
    // This method irks me still even if it is now more functionally correct
    Set typesInRegistry = _findAll();
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
  } // protected static void internal_updateKnownTypes()

  /**
   * 
   * @return set
   * @throws GrouperException
   */
  private static Set<GroupType> _findAll() throws  GrouperException {
    try {
      Set<GroupType>       types = new LinkedHashSet<GroupType>();
      Iterator<GroupType>  it    = GrouperDAOFactory.getFactory().getGroupType().findAll().iterator();
      while (it.hasNext()) {
        GroupType type = it.next() ;
        types.add(type);
      }
      LOG.info("found group types: " + types.size() );
      return types;
    }
    catch (GrouperException eGRE) {
      String msg = E.GROUPTYPE_FINDALL + eGRE.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eGRE);
    }
  } // private Static Set _findAll()

  /**
   * clear cache (e.g. if schema export)
   */
  public static void clearCache() {
    types.clear();
  }

} // public class GroupTypeFinder

