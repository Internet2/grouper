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
import java.util.UUID;

/** 
 * Generate UUIDs.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperUuid.java,v 1.1.10.1 2009-05-18 16:05:32 mchyzer Exp $
 * @since   1.2.0
 *     
*/
public class GrouperUuid {

  /**
   * @return a uuid 
   * @since   1.2.0
   */
  public static String getUuid() {
    String uuid = UUID.randomUUID().toString();
    StringBuilder result = new StringBuilder(uuid.length());
    for (int i=0;i<uuid.length();i++) {
      char theChar = uuid.charAt(i);
      if (theChar != '-') {
        result.append(theChar);
      }
    }
    return result.toString();
  } 

}

