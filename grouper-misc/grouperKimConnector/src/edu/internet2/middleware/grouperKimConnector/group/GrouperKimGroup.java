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
package edu.internet2.middleware.grouperKimConnector.group;

import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDetail;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;

/**
* Grouper implementation of the rice group interface
*/
public class GrouperKimGroup implements Group {

  /**
  * groupId is the Grouper UUID
  */
  private String groupId;

  /**
  * group description is the description of the group in grouper
  */
  private String groupDescription;

  /**
  * system (not friendly) name of the group in grouper
  */
  private String groupName;

  /**
  * always going to return the id for the default type
  */
  private String kimTypeId;

  /**
  * <pre>
  * folder in the rice folder where the group is. so if the rice folder is:
  * penn:community:apps:kualiRice
  * and the group is:
  * penn:community:apps:kualiRice:KR_WHATEVER:admins
  * then the nameSpaceCode is KR_WHATEVER
  * </pre> 
  */
  private String nameSpaceCode;

  /**
  * map of name value pairs about group
  */
  private AttributeSet attributeSet;

  /**
  * default constructor
  */
  public GrouperKimGroup() {
  }

  /**
  * 
  * @param wsGroup
  */
  public GrouperKimGroup(WsGroup wsGroup) {
    this.groupId = wsGroup.getUuid();
    this.groupName = wsGroup.getExtension();
    this.groupDescription = wsGroup.getDescription();
    this.kimTypeId = GrouperKimUtils.grouperDefaultGroupTypeId();
    this.nameSpaceCode = GrouperKimUtils.calculateNamespaceCode(wsGroup.getName());
    WsGroupDetail detail = wsGroup.getDetail();
    
    //if there is a detail and attributes, then set the attributeSet
    if (detail != null) {
      int attributeLength = GrouperClientUtils.length(detail.getAttributeNames());
      if (attributeLength > 0) {
        this.attributeSet = new AttributeSet();
        for (int i=0;i<attributeLength;i++) {
          this.attributeSet.put(detail.getAttributeNames()[i], detail.getAttributeValues()[i]);
        }
      }
    }
  }

  /**
  * map of name value pairs about group
  * @see org.kuali.rice.kim.bo.Group#getAttributes()
  */
  public AttributeSet getAttributes() {
    return this.attributeSet;
  }

  /**
  * group description is the description of the group in grouper
  * @see org.kuali.rice.kim.bo.Group#getGroupDescription()
  */
  public String getGroupDescription() {
    return this.groupDescription;
  }

  /**
  * groupId is the Grouper UUID
  * @see org.kuali.rice.kim.bo.Group#getGroupId()
  */
  public String getGroupId() {
    return this.groupId;
  }

  /**
  * system (not friendly) name of the group in grouper
  * @see org.kuali.rice.kim.bo.Group#getGroupName()
  */
  public String getGroupName() {
    return this.groupName;
  }

  /**
  * @see org.kuali.rice.kim.bo.Group#getKimTypeId()
  */
  public String getKimTypeId() {
    return this.kimTypeId;
  }

  /**
  * <pre>
  * folder in the rice folder where the group is. so if the rice folder is:
  * penn:community:apps:kualiRice
  * and the group is:
  * penn:community:apps:kualiRice:KR_WHATEVER:admins
  * then the nameSpaceCode is KR_WHATEVER
  * </pre> 
  * @see org.kuali.rice.kim.bo.Group#getNamespaceCode()
  */
  public String getNamespaceCode() {
    return this.nameSpaceCode;
  }

  /**
  * @see org.kuali.rice.kim.bo.Group#isActive()
  */
  public boolean isActive() {
    return true;
  }

  /**
  * @see org.kuali.rice.kns.bo.BusinessObject#prepareForWorkflow()
  */
  public void prepareForWorkflow() {
  }

  /**
  * @see org.kuali.rice.kns.bo.BusinessObject#refresh()
  */
  public void refresh() {
  }
}
