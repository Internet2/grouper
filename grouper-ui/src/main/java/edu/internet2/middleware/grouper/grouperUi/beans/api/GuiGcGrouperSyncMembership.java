/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.text.SimpleDateFormat;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * 
 */
public class GuiGcGrouperSyncMembership {

  private GcGrouperSyncMembership gcGrouperSyncMembership;
  
  /**
   * @param gcGrouperSyncMembership
   */
  public GuiGcGrouperSyncMembership(GcGrouperSyncMembership gcGrouperSyncMembership) {
    this.gcGrouperSyncMembership = gcGrouperSyncMembership;
  }

  /**
   * @return gcGrouperSyncMembership
   */
  public GcGrouperSyncMembership getGcGrouperSyncMembership() {
    return gcGrouperSyncMembership;
  }

  /**
   * @param gcGrouperSyncMembership
   */
  public void setGcGrouperSyncMembership(GcGrouperSyncMembership gcGrouperSyncMembership) {
    this.gcGrouperSyncMembership = gcGrouperSyncMembership;
  }
  
  /**
   * start label string yyyy/MM/dd h:mm a
   * @return the start label string yyyy/MM/dd h:mm:ss a
   */
  public String getInTargetStartLabel() {

    if (this.gcGrouperSyncMembership == null) {
      return null;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd h:mm:ss a");

    return simpleDateFormat.format(gcGrouperSyncMembership.getInTargetStart());
  }
  
  /**
   * end label string yyyy/MM/dd h:mm a
   * @return the end label string yyyy/MM/dd h:mm:ss a
   */
  public String getInTargetEndLabel() {

    if (this.gcGrouperSyncMembership == null) {
      return null;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd h:mm:ss a");

    return simpleDateFormat.format(gcGrouperSyncMembership.getInTargetEnd());
  }
  
  /**
   * @return provisioner name
   */
  public String getProvisionerName() {
    if (this.gcGrouperSyncMembership == null) {
      return null;
    }
    
    return gcGrouperSyncMembership.getGrouperSync().getProvisionerName();
  }
  
  /**
   * @return link if available otherwise name
   */
  public String getGroupLinkOrName() {
    Group group = GroupFinder.findByUuid(this.gcGrouperSyncMembership.getGrouperSyncGroup().getGroupId(), false);
    if (group == null) {
      // just return the name, there's no link
      return this.gcGrouperSyncMembership.getGrouperSyncGroup().getGroupName();
    }

    return new GuiGroup(group).getLink();
  }
}
