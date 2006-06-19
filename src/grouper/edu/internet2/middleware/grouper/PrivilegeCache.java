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
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  net.sf.ehcache.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;

/** 
 * Privilege cache provider.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrivilegeCache.java,v 1.9 2006-06-19 15:17:40 blair Exp $
 *     
 */
class PrivilegeCache {

  // PROTECTED CLASS CONSTANTS //
  protected static final String ACCESS  = "edu.internet2.middleware.grouper.PrivilegeCache.Access";
  protected static final String NAMING  = "edu.internet2.middleware.grouper.PrivilegeCache.Naming";


  // PRIVATE CLASS CONSTANTS //
  private static final String DELIM = "|";


  // PRIVATE CLASS VARABLES //
  private static Map caches = new HashMap();


  // PRIVATE INSTANCE VARIABLES //
  private Cache   cache;
  private String  name;


  // CONSTRUCTORS //
  private PrivilegeCache(Cache cache) {
    this.name   = cache.getName();
    this.cache  = cache;
  } // private PrivilegeCache()


  // PUBLIC INSTANCE METHODS //
  public String toString() {
    return new ToStringBuilder(this)
      .append("name"  , this.name   )
      .append("cache" , this.cache  )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //
  protected static PrivilegeCache getCache(String name) 
    throws  GrouperRuntimeException
  {
    if (caches.containsKey(name)) {
      return (PrivilegeCache) caches.get(name);
    }
    else {
      Cache           cache = CacheMgr.getCache(name);
      PrivilegeCache  pc    = new PrivilegeCache(cache);
      caches.put(name, pc);
      return pc;
    }
  } // protected static PrivilegeCache getCache(name)


  // PROTECTED INSTANCE METHODS //
  protected Element get(Group g, Subject subj, Privilege p) {
    try {
      Element el= this.cache.get( this._getKey(g, subj, p) );
      return el;
    }
    catch (CacheException eC) { 
      ErrorLog.error(PrivilegeCache.class, E.CACHE + eC.getMessage());
      return null;
    }
  } // protected Element get(g, subj, p)

  protected Element get(Stem ns, Subject subj, Privilege p) {
    try {
      Element el= this.cache.get( this._getKey(ns, subj, p) );
      return el;
    }
    catch (CacheException eC) {
      ErrorLog.error(PrivilegeCache.class, E.CACHE + eC.getMessage());
      return null;
    }
  } // protected Element get(ns, subj, p)

  protected void put(Group g, Subject subj, Privilege p, boolean has) {
    Element el = new Element( this._getKey(g, subj, p), Boolean.toString(has) );
    this.cache.put(el);
  } // protected void put(g, subj, p, has)

  protected void put(Stem ns, Subject subj, Privilege p, boolean has) {
    Element el = new Element( this._getKey(ns, subj, p), Boolean.toString(has) );
    this.cache.put(el); 
  } // protected void put(ns, subj, p, has)

  protected void removeAll() 
    throws  Exception
  {
    int size = this.cache.getSize();
    if (size > 0) {
      this.cache.removeAll();
      DebugLog.info(PrivilegeCache.class, M.CACHE_EMPTIED + this.name + ": " + size);
    }
  } // protected void removeAll() 


  // PRIVATE INSTANCE METHODS //
  private String _getKey(Group g, Subject subj, Privilege p) {
    return this._getKey(g.getUuid(), subj, p);
  } // private String _getKey(g, subj, p)

  private String _getKey(Stem ns, Subject subj, Privilege p) {
    return this._getKey(ns.getUuid(), subj, p);
  } // private String _getKey(ns, subj, p)

  private String _getKey(String uuid, Subject subj, Privilege p) {
    // TODO memoize?
    String key    = uuid + DELIM 
      + subj.getId() + DELIM + subj.getType().getName() + subj.getSource().getId() 
      + DELIM + p.getName();
    return key;
  } // private String _getKey(uuid, subj, p)

}

