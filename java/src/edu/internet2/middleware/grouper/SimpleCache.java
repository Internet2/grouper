/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


/** 
 * {@link Subject} Lookup Cache.
 * <p />
 * TODO Ideally this class would just become a passthrough for an
 * ehcache cache<br />
 * TODO Until then, however, do I need a TTL?<br />
 * TODO And a flush?<br />
 * TODO And condense the multiple key levels into one?
 *
 * @author  blair christensen.
 * @version $Id: SimpleCache.java,v 1.1 2005-07-29 01:53:50 blair Exp $
 */
public class SimpleCache {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static Log log = LogFactory.getLog(SimpleCache.class);


  /* 
   * PRIVATE INSTANCE VARIABLES
   */
  private Map cache;


  /*
   * CONSTRUCTORS
   */
  protected SimpleCache() {
    this.cache = new HashMap();
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
   * Get value from cache
   */
  protected Object get(String key, String subkey) 
    throws CacheNotFoundException
  {
    boolean cached  = false;
    Object  o       = null;
    if (this.cache.containsKey(key)) {
      Map m = (Map) this.cache.get(key);
      if (m.containsKey(subkey)) {
        log.debug("Cached: " + key + "/" + subkey + "/" + m.get(subkey));
        cached  = true;
        o       = m.get(subkey);
      }
    }
    if (!cached) {
      log.debug("Not cached: " + key + "/" + subkey);
      throw new CacheNotFoundException("Not cached: " + key + "/" + subkey);
    }
    return o;
  }

  /*
   * Put value in cache
   */
  protected void put(String key, String subkey, Object o) {
    Map m = new HashMap();
    if (this.cache.containsKey(key)) {
      m = (Map) this.cache.get(key);
    }
    m.put( (String) subkey, o );
    this.cache.put( (String) key, m );
    log.debug("Caching: " + key + "/" + subkey + "/" + o);
  }

}

