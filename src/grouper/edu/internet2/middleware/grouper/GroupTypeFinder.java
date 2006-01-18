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
 * @version $Id: GroupTypeFinder.java,v 1.6 2006-01-18 20:23:29 blair Exp $
 */
class GroupTypeFinder {

  // Private Class Constants
  private static final String ERR_NF  = "unable to find group types: ";
  private static final String ERR_TNF = "invalid group type: ";
  private static final Log    LOG;    
  private static final Map    TYPES   = new HashMap();

  static {
    LOG = LogFactory.getLog(GroupTypeFinder.class);
    LOG.debug("finding group types");
    Iterator iter = _findAll().iterator();
    while (iter.hasNext()) {
      GroupType t = (GroupType) iter.next();
      TYPES.put(t.getName(), t);
      LOG.debug("found group type '" + t.getName() + "': " + t);
    }
  } // static 


  // Protected Class Methods

  protected static GroupType find(String type) 
    throws SchemaException
  {
    if (TYPES.containsKey(type)) {
      return (GroupType) TYPES.get(type);
    }
    LOG.debug(ERR_TNF + type);
    throw new SchemaException(ERR_TNF + type);
  } // public static GroupType find(type)


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

