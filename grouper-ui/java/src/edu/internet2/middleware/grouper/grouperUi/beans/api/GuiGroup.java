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
/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;


/**
 * Result of one group being retrieved since a user is a member of it.  The number of
 * groups will equal the number of groups the user is a member of (provided the filter matches)
 * 
 * @author mchyzer
 */
public class GuiGroup implements Serializable {

  /** group */
  private Group group;
  
  /** if there is an external config url */
  private String configUrl;
  
  /** see if has a config url */
  private Boolean hasMembershipConfigUrl = null;
  
  /**
   * config url if set
   * @return the config url if set
   */
  public String getMembershipConfigUrl() {
    
    if (this.hasMembershipConfigUrl == null) {
      
      if (this.group == null) {

        //sidestep
        return null;
      }

      //default to false
      this.hasMembershipConfigUrl = false;
      
      if (TagUtils.mediaResourceBoolean("simpleMembershipUpdate.allowExternalUrlProperties", false)) {
        
        final GroupType groupType = GroupTypeFinder.find("grouperGroupMembershipSettings", false);
        
        if (groupType != null && GuiGroup.this.group.hasType(groupType)) {
          this.configUrl = GuiGroup.this.group.getAttributeOrFieldValue("grouperGroupMshipSettingsUrl", false, false);
          this.hasMembershipConfigUrl = !StringUtils.isBlank(this.configUrl);
        }
      }
    }
    
    return this.hasMembershipConfigUrl ? this.configUrl : null;
  }

  /**
   * return the group
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "" + this.group;
  }
  
  /**
   * 
   */
  public GuiGroup() {
    
  }
  
  /**
   * 
   * @param theGroup
   */
  public GuiGroup(Group theGroup) {
    this.group = theGroup;
  }
  
  /**
   * the export subject ids file name
   * @return the export subject ids file name
   */
  public String getExportSubjectIdsFileName() {
    return getExportSubjectIdsFileNameStatic(this.group);
  }
  
  /**
   * static logic
   * @param group
   * @return the file name
   */
  public static String getExportSubjectIdsFileNameStatic(Group group) {
    String groupExtensionFileName = group.getDisplayExtension();
    
    groupExtensionFileName = GrouperUiUtils.stripNonFilenameChars(groupExtensionFileName);
    
    return "groupExportSubjectIds_" + groupExtensionFileName + ".csv";
 
  }
  
  /**
   * the export all file name
   * @return the export subject ids file name
   */
  public String getExportAllFileName() {
    return getExportAllFileNameStatic(this.group);
  }
  
  /**
   * static logic
   * @param group
   * @return the file name
   */
  public static String getExportAllFileNameStatic(Group group) {
    String groupExtensionFileName = group.getDisplayExtension();
    
    groupExtensionFileName = GrouperUiUtils.stripNonFilenameChars(groupExtensionFileName);
    
    return "groupExportAll_" + groupExtensionFileName + ".csv";
 
  }
}
