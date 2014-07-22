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
 * @version $Id: U.java,v 1.3 2007-04-18 18:02:04 blair Exp $
 * @since   1.0
 */
public class U {

  // PUBLIC CLASS METHODS //

  /**
   * TODO 20070418 relocate to somewhere more appropriate.
   * <p/>
   * @since   1.2.0
   */
  public static String constructName(String stem, String extn) {
    if ( stem.equals(Stem.ROOT_NAME) ) {
      return extn;
    }
    return stem + Stem.DELIM + extn;
  } 

  /**
   * TODO 20070418 relocate to somewhere more appropriate.
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

