package edu.internet2.middleware.grouperKimConnector.group;

import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

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
    finish this
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
