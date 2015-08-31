/**
 * Copyright 2014 Internet2
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
 */
/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.privs;

import net.sf.ehcache.Element;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.subject.Subject;

/**
 * Cache for if wheel member
 * <p/>
 * @version $Id: WheelAccessResolver.java,v 1.26 2009-09-21 06:14:26 mchyzer Exp $
 * @since   2.1.0
 */
public class WheelCache {

  /** 2007-11-02 Gary Brown
   * Provide cache for wheel group members
   * Profiling showed lots of time rechecking memberships */
  private static final String CACHE_IS_WHEEL_MEMBER = WheelCache.class.getName()
      + ".isWheelMember";

  /**
   * flush
   */
  public static void flush() {
    EhcacheController.ehcacheController().getCache(CACHE_IS_WHEEL_MEMBER).flush();
  }

  /**
   * Retrieve boolean from cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @return Cached return value or null.
   * @since 2.1.0
   */
  public static Boolean getFromIsWheelMemberCache(Subject subj) {
    Element el = EhcacheController.ehcacheController().getCache(WheelCache.CACHE_IS_WHEEL_MEMBER)
      .get(new MultiKey(subj.getSourceId(), subj.getId()));
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * Put boolean into cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @param rv 
   * @since 2.1.0
   */
  public static void putInHasPrivilegeCache(Subject subj, Boolean rv) {
    EhcacheController.ehcacheController().getCache(WheelCache.CACHE_IS_WHEEL_MEMBER)
      .put(new Element(new MultiKey(subj.getSourceId(), subj.getId()), rv));
  }

  /**
   * Retrieve boolean from cache for readonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @return Cached return value or null.
   * @since 2.1.0
   */
  public static Boolean getFromIsWheelReadonlyMemberCache(Subject subj) {
    Element el = EhcacheController.ehcacheController().getCache(WheelCache.CACHE_IS_WHEEL_MEMBER)
      .get(new MultiKey("readonlyCache", subj.getSourceId(), subj.getId()));
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * Put boolean into cache for readonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @param rv 
   * @since 2.1.0
   */
  public static void putInReadonlyHasPrivilegeCache(Subject subj, Boolean rv) {
    EhcacheController.ehcacheController().getCache(WheelCache.CACHE_IS_WHEEL_MEMBER)
      .put(new Element(new MultiKey("readonlyCache", subj.getSourceId(), subj.getId()), rv));
  }


  /**
   * Retrieve boolean from cache for viewonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @return Cached return value or null.
   * @since 2.1.0
   */
  public static Boolean getFromIsWheelViewonlyMemberCache(Subject subj) {
    Element el = EhcacheController.ehcacheController().getCache(WheelCache.CACHE_IS_WHEEL_MEMBER)
      .get(new MultiKey("viewonlyCache", subj.getSourceId(), subj.getId()));
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * Put boolean into cache for viewonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @param rv 
   * @since 2.1.0
   */
  public static void putInViewonlyHasPrivilegeCache(Subject subj, Boolean rv) {
    EhcacheController.ehcacheController().getCache(WheelCache.CACHE_IS_WHEEL_MEMBER)
      .put(new Element(new MultiKey("viewonlyCache", subj.getSourceId(), subj.getId()), rv));
  }
}
