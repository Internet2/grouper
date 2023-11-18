/*******************************************************************************
 * Copyright 2019 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.pit.PITGroup;


/**
 * gui wrapper around pit group
 */
public class GuiPITGroup {

  
  /**
   * @param thePITGroup
   */
  public GuiPITGroup(PITGroup thePITGroup) {
    this.pitGroup = thePITGroup;
  }
  
  /**
   * pit group
   */
  private PITGroup pitGroup;
  
  /**
   * pit group
   * @return pit group
   */
  public PITGroup getPITGroup() {
    return this.pitGroup;
  }
  
  /**
   * @return link if available otherwise name
   */
  public String getLinkOrName() {
    Group group = GroupFinder.findByUuid(this.pitGroup.getSourceId(), false);
    if (group == null) {
      // just return the name, there's no link for the pit group
      return this.pitGroup.getName();
    }

    return new GuiGroup(group).getLink();
  }
}