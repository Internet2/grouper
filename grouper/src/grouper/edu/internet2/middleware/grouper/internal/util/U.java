/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.internal.util;
import  edu.internet2.middleware.grouper.Stem;
import  java.util.Properties;

/**
 * Grouper Utility Class.
 * @author  blair christensen.
 * @version $Id: U.java,v 1.2 2007-04-17 17:35:00 blair Exp $
 * @since   1.0
 */
public class U {
  // FIXME 20070417 reconsider everything in this class

  // PUBLIC CLASS METHODS //

  /**
   * FIXME 20070417 relocate to somewhere more appropriate.
   * <p/>
   * @since   1.2.0
   */
  public static String constructName(String stem, String extn) {
    if ( stem.equals(Stem.ROOT_EXT) ) {
      return extn;
    }
    return stem + Stem.ROOT_INT + extn;
  } 

  /**
   * FIXME 20070417 relocate to somewhere more appropriate.
   * <p/>
   * @since   1.2.0
   */ 
  public static boolean getBooleanProperty(Properties props, String key) {
    String val = props.getProperty(key);
    if (val == null) {
      return false;
    }
    return "true".equals(val);
  } 
 
} 

