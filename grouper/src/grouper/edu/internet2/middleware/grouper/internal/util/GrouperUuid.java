/**
 * Copyright 2012 Internet2
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
import java.util.UUID;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/** 
 * Generate UUIDs.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperUuid.java,v 1.2 2009-08-11 20:18:09 mchyzer Exp $
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
    
    //config option to do that
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("uuid.use.dashes", false)) {
      return uuid;
    }

    char[] result = new char[32];
    int resultIndex = 0;
    for (int i=0;i<uuid.length();i++) {
      char theChar = uuid.charAt(i);
      if (theChar != '-') {
        if (resultIndex >= result.length) {
          throw new RuntimeException("Why is resultIndex greater than result.length ???? " 
              + resultIndex + " , " + result.length + ", " + uuid);
        }
        result[resultIndex++] = theChar;
      }
    }
    return new String(result);
  } 

}

