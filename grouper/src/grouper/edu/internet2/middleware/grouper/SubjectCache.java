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
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Subject cache provider.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectCache.java,v 1.2 2006-02-03 19:38:53 blair Exp $
 *     
*/
class SubjectCache {

  /*
   * TODO Ideally we would use ehcache for caching subjects **but**,
   *      unfortunately, Subjects are not serializable.  Alas.  Hence
   *      this hack using HashMaps.  Which, come to think of it, is how
   *      we were caching subjects in 0.6.
   */

  // Protected Class Constants
  protected static final String ID    = "edu.internet2.middleware.grouper.SubjectCache.Id";
  protected static final String IDFR  = "edu.internet2.middleware.grouper.SubjectCache.Identifier";


  // Private Class Constants
  private static final String ERR_CNF = "subject cache not found: ";
  private static final Log    LOG     = LogFactory.getLog(SubjectCache.class);
  private static final Map    CACHES  = new HashMap();


  static {
    CACHES.put( ID,   new SubjectCache(ID)    );
    CACHES.put( IDFR, new SubjectCache(IDFR)  );
  } // static


  // Private Class Variables
  private static Map caches = new HashMap();


  // Private Instance Variables
  private Map     cache;
  private String  name;


  // Constructors
  private SubjectCache(String name) {
    this.name   = name;
    this.cache  = new HashMap();
  } // private SubjectCache()


  // Public Instance Methods
  public String toString() {
    return new ToStringBuilder(this)
      .append("name"  , this.name   )
      .toString();
  } // public String toString()


  // Hibernate Accessors

  // Protected Class Methods
  protected static SubjectCache getCache(String name) 
    throws  RuntimeException
  {
    if (CACHES.containsKey(name)) {
      return (SubjectCache) CACHES.get(name);
    }
    String err = ERR_CNF + name;
    LOG.fatal(err);
    throw new RuntimeException(err);
  } // protected static SubjectCache getCache(name)


  // Protected Instance Methods
  protected Subject get(String id, String type) {
    String key = this._getKey(id, type);
    if (this.cache.containsKey(key)) {
      return (Subject) this.cache.get(key);
    }
    return null;
  } // protected Subject get(id, type)

  protected void put(Subject subj) {
    String key = this._getKey(subj.getId(), subj.getType().getName());
    if (this.cache.containsKey(key)) {
      Subject cached = (Subject) this.cache.get(key);
      if (SubjectHelper.eq(subj, cached)) {
        // Already cached. Skip.
      }
      else {
        // Already cached but not the same subject.  Presumably this is
        // a subject that cannot be uniquely resolved.  Remove it from
        // the cache.
        this.cache.remove(key);  
      }
    }
    else {
      this.cache.put(key, subj);
    }
  } // protected void put(subj)


  // Private Instance Methods
  private String _getKey(String id, String type) {
    // TODO memoize?
    String delim  = "|";
    String key    = id + delim + type;
    return key;
  } // private String _getKey(id, type)

}

