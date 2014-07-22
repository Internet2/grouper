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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;


/**
 *
 */
public class CacheTry {

  /**
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    
    for (int i=0;i<20;i++) {
      SubjectFinder.findById("10021368", true);
    }
    
    for (int i=0;i<20;i++) {
      SubjectFinder.findByIdOrIdentifier("mchyzer", true);
    }
    
    for (int i=0;i<20;i++) {
      GrouperDAOFactory.getFactory().getAttributeAssign().findById("dbfacf21faad4c94b4388b1e8ff54fda", false);
    }
    
  }

}
