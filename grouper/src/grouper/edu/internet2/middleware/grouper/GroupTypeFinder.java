/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * @version $Id: GroupTypeFinder.java,v 1.7 2006-01-25 18:55:33 blair Exp $
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
    // I was running into problems when caching these in a HashMap.
    // Given that I am now caching query results, I probably don't need
    // the second cache.
    //
    // But I need to somewhat cache types to avoid Hibernate errors
    // that appear while saving objects.  *sigh*
    if (types.containsKey(name)) { 
      return (GroupType) types.get(name);
    } 
    // Type not cached.  Try to find it as it may be new.
    Iterator iter = _findAll().iterator();
    while (iter.hasNext()) {
      GroupType t = (GroupType) iter.next();
      if (t.getName().equals(name)) {
        // Add it to the map of known types
        types.put(name, t);
        return t;
      }
    }
    LOG.debug(ERR_TNF + name);
    throw new SchemaException(ERR_TNF + name);
  } // public static GroupType find(name)


  // Private Class Methods
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
  } // public Static Set findAll()

}

