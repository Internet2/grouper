/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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


import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.Type;
import  org.apache.commons.logging.*;


/**
 * Find group types.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupTypeFinder.java,v 1.8.2.2 2006-04-13 00:35:33 blair Exp $
 */
public class GroupTypeFinder {

  // Private Class Constants
  private static final String ERR_NF  = "unable to find group types: ";
  private static final String ERR_TNF = "invalid group type: ";
  private static final Log    LOG; //     = LogFactory.getLog(GroupTypeFinder.class);    

  // Private Class Variables
  private static Map types = new HashMap();


  static {
    LOG = LogFactory.getLog(GroupTypeFinder.class);
    LOG.debug("finding group types");
    // We need to initialize the known types at this point to try and
    // avoid running into Hibernate exceptions later on when attempting
    // to save objects.
    Iterator iter = _findAll().iterator();
    while (iter.hasNext()) {
      GroupType t = (GroupType) iter.next();
      types.put(t.getName(), t);
      LOG.debug("found group type '" + t.getName() + "': " + t);
    }
  } // static


  // Public Class Methods

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
   */
  public static GroupType find(String name) 
    throws  SchemaException
  {
    // First check to see if type is cached.
    if (types.containsKey(name)) {
      return (GroupType) types.get(name);
    }
    // If not, refresh known types as it may be new and try again. 
    _updateKnownTypes();
    if (types.containsKey(name)) {
      return (GroupType) types.get(name);
    }
    LOG.debug(ERR_TNF + name);
    throw new SchemaException(ERR_TNF + name);
  } // public static GroupType find(name)

  /**
   * Find all public group types.
   * <pre class="eg">
   * Set types = GroupTypeFinder.findAll();
   * </pre>
   * @return  A {@link Set} of {@link GroupType} objects.
   */
  public static Set findAll() {
    _updateKnownTypes();
    Set       values  = new LinkedHashSet();
    Iterator  iter    = types.values().iterator();
    while (iter.hasNext()) {
      GroupType t = (GroupType) iter.next();
      if (!t.getInternal()) {
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
    Iterator  iter  = findAll().iterator();
    while (iter.hasNext()) {
      GroupType t = (GroupType) iter.next();
      if (t.getAssignable()) {
        types.add(t);
      }
    }
    return types;
  } // public static Set findAllAssignable()


  // Private Class Methods //
  private static Set _findAll() {
    Set types = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from GroupType order by name asc");
      qry.setCacheable(GrouperConfig.QRY_GTF_FA); 
      qry.setCacheRegion(GrouperConfig.QCR_GTF_FA);
      types.addAll(qry.list());
      hs.close();  
    }
    catch (HibernateException eH) {
      String err = ERR_NF + eH.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
    LOG.debug("found group types: " + types.size());
    return types;
  } // private Static Set _findAll()

  private static void _updateKnownTypes() {
    // TODO This method irks me still even if it is now more
    //      functionally correct
    Set typesInRegistry = _findAll();
    // Look for types to add
    Iterator addIter = typesInRegistry.iterator();
    while (addIter.hasNext()) {
      GroupType t = (GroupType) addIter.next();
      if (!types.containsKey(t.getName())) {
        types.put(t.getName(), t); // New type.  Add it to the cached list.
      }
    }
    // Look for types to remove
    Set       toDel   = new LinkedHashSet();
    Iterator  delIter = types.values().iterator();
    while (delIter.hasNext()) {
      GroupType t = (GroupType) delIter.next();
      if (!typesInRegistry.contains(t)) {
        toDel.add(t.getName());  
      }
    }
    Iterator  toDelIter = toDel.iterator();
    while (toDelIter.hasNext()) {
      String type = (String) toDelIter.next();
      types.remove(type);  
    }
  } // private static void _updateKnownTypes()

}

