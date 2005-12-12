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


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  net.sf.ehcache.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Privilege cache provider.
 * <p />
 * @author  blair christensen.
 * @version $Id: PrivilegeCache.java,v 1.2 2005-12-12 16:07:24 blair Exp $
 *     
*/
class PrivilegeCache {

  // Protected Class Constants
  protected static final String ACCESS  = "edu.internet2.middleware.grouper.PrivilegeCache.Access";
  protected static final String NAMING  = "edu.internet2.middleware.grouper.PrivilegeCache.Naming";


  // Private Class Constants
  private static final String       ERR_CNF = "cache not found: ";
  private static final CacheManager MGR;
  private static final Log          LOG     = LogFactory.getLog(PrivilegeCache.class);


  static {
    try {
      MGR = CacheManager.create();
    }
    catch (CacheException eC) {
      String err = GrouperLog.ERR_CMGR + eC.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // static


  // Private Class Variables
  private static Map caches = new HashMap();


  // Private Instance Variables
  private Cache   cache;
  private String  name;


  // Constructors
  private PrivilegeCache(Cache cache) {
    this.name   = cache.getName();
    this.cache  = cache;
  } // private PrivilegeCache()


  // Public Instance Methods
  public String toString() {
    return new ToStringBuilder(this)
      .append("name"  , this.name   )
      .append("cache" , this.cache  )
      .toString();
  } // public String toString()


  // Hibernate Accessors

  // Protected Class Methods
  protected static PrivilegeCache getCache(String name) {
    if (caches.containsKey(name)) {
      return (PrivilegeCache) caches.get(name);
    }
    else {
      if (MGR.cacheExists(name)) {
        Cache cache = MGR.getCache(name);
        PrivilegeCache pc = new PrivilegeCache(cache);
        caches.put(name, pc);
        return pc;
      }
    }
    String err = ERR_CNF + NAMING;
    LOG.fatal(err);
    throw new RuntimeException(err);
  } // protected static PrivilegeCache getCache(name)


  // Protected Instance Methods
  protected Element get(Group g, Subject subj, Privilege p) {
    try {
      Element el= this.cache.get( this._getKey(g, subj, p) );
      return el;
    }
    catch (CacheException eC) { 
      LOG.error(eC.getMessage());
      return null;
    }
  } // protected Element get(g, subj, p)

  protected Element get(Stem ns, Subject subj, Privilege p) {
    try {
      Element el= this.cache.get( this._getKey(ns, subj, p) );
      return el;
    }
    catch (CacheException eC) {
      LOG.error(eC.getMessage());
      return null;
    }
  } // protected Element get(ns, subj, p)

  protected void put(Group g, Subject subj, Privilege p, boolean has) {
    Element el = new Element(
      this._getKey(g, subj, p), new Boolean(has).toString()
    );
    this.cache.put(el);
  } // protected void put(g, subj, p, has)

  protected void put(Stem ns, Subject subj, Privilege p, boolean has) {
    Element el = new Element(
      this._getKey(ns, subj, p), new Boolean(has).toString()
    );
    this.cache.put(el); 
  } // protected void put(ns, subj, p, has)

  protected void removeAll() 
    throws  Exception
  {
    int size = this.cache.getSize();
    if (size > 0) {
      this.cache.removeAll();
      LOG.info(GrouperLog.MSG_EC + this.name + ": " + size);
    }
  } // protected void removeAll() 

  // Private Instance Methods
  private String _getKey(Group g, Subject subj, Privilege p) {
    return this._getKey(g.getUuid(), subj, p);
  } // private String _getKey(g, subj, p)

  private String _getKey(Stem ns, Subject subj, Privilege p) {
    return this._getKey(ns.getUuid(), subj, p);
  } // private String _getKey(ns, subj, p)

  private String _getKey(String uuid, Subject subj, Privilege p) {
    // TODO memoize?
    String delim  = "|";
    String key    = uuid + delim 
      + subj.getId() + delim + subj.getType().getName() + subj.getSource().getId() 
      + delim + p.getName();
    return key;
  } // private String _getKey(uuid, subj, p)

}

