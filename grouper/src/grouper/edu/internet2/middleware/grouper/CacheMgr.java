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
import  net.sf.ehcache.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper Cache Manager
 * <p />
 * @author  blair christensen.
 * @version $Id: CacheMgr.java,v 1.2.2.1 2006-05-11 17:14:22 blair Exp $
 *     
*/
class CacheMgr {

  // Private Class Constants
  private static final String       ERR_CNF = "cache not found: ";
  private static final CacheManager MGR;
  private static final Log          LOG     = LogFactory.getLog(CacheMgr.class);


  static {
    try {
      MGR = CacheManager.create();
    }
    catch (CacheException eC) {
      String err = GrouperLog.ERR_CMGR + eC.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err, eC);
    }
  } // static

  
  // Private Class Variables
  private static Map caches = new HashMap();


  // Protected Class Methods
  protected static Cache getCache(String name) 
    throws  RuntimeException
  {
    Cache cache = null;
    if (caches.containsKey(name)) {
      cache = (Cache) caches.get(name);
    }
    else {
      if (MGR.cacheExists(name)) {
        cache = MGR.getCache(name);
        caches.put(cache.getName(), cache);
      }
    }
    if (cache == null) {
      String err = ERR_CNF + name;
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
    return cache;
  } // protected static Cache getCache(name)

  protected static Set getCaches() {
    Set       caches  = new LinkedHashSet();
    String[]  names   = MGR.getCacheNames();
    for (int i=0; i<names.length; i++) {
      //caches.add( MGR.getCache(names[i]) );
      Cache cache = MGR.getCache(names[i]);
      if (cache.getMemoryStoreSize() > 0) {
        caches.add(cache);
      }
    }
    return caches;
  } // protected static Set getCaches()

  protected static void resetAllCaches() {
    try {
      Iterator iter = getCaches().iterator();
      while (iter.hasNext()) {
        Cache cache = (Cache) iter.next();
        int   size  = cache.getSize();
        if (size > 0) {
          cache.removeAll();
          LOG.info(GrouperLog.MSG_EC + cache.getName() + ": " + size);
        }
      }
    }
    catch (Exception e) {
      String err = GrouperLog.ERR_CMGR + e.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err, e);
    }
  } // protected static void resetAllCaches()

}

