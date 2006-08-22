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
import  java.io.*;
import  java.util.*;
import  net.sf.ehcache.*;

/** 
 * Grouper Cache Manager
 * <p/>
 * @author  blair christensen.
 * @version $Id: CacheMgr.java,v 1.10 2006-08-22 19:48:22 blair Exp $
 */
class CacheMgr {

  // STATIC //
  static {
    try {
      MGR = CacheManager.create();
    }
    catch (CacheException eC) {
      String msg = E.CACHE_INIT + eC.getMessage();
      ErrorLog.fatal(CacheMgr.class, msg);
      throw new GrouperRuntimeException(msg, eC);
    }
  } // static


  // PRIVATE CLASS CONSTANTS //
  private static final CacheManager MGR;

  
  // PRIVATE CLASS VARIABLES //
  private static Map caches = new HashMap();


  // PROTECTED CLASS METHODS //
  protected static Cache getCache(String name) 
    throws  GrouperRuntimeException
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
      String msg = E.CACHE_NOTFOUND + name;
      ErrorLog.fatal(CacheMgr.class, msg);
      throw new GrouperRuntimeException(msg);
    }
    return cache;
  } // protected static Cache getCache(name)

  protected static Set getCaches() {
    Set       caches  = new LinkedHashSet();
    String[]  names   = MGR.getCacheNames();
    for (int i=0; i<names.length; i++) {
      Cache cache = MGR.getCache(names[i]);
      if (cache.getMemoryStoreSize() > 0) {
        caches.add(cache);
      }
    }
    return caches;
  } // protected static Set getCaches()

  protected static void resetAllCaches() 
    throws  GrouperRuntimeException
  {
    try {
      Cache     cache;
      Iterator  iter  = getCaches().iterator();
      while (iter.hasNext()) {
        cache       = (Cache) iter.next();
        int   size  = cache.getSize();
        if (size > 0) {
          cache.removeAll();
          DebugLog.info(CacheMgr.class, M.CACHE_EMPTIED + cache.getName() + ": " + size); 
        }
      }
    }
    catch (IOException eIO) {
      String msg = E.CACHE + eIO.getMessage();
      ErrorLog.fatal(CacheMgr.class, msg);
      throw new GrouperRuntimeException(msg, eIO);
    }
  } // protected static void resetAllCaches()

}

