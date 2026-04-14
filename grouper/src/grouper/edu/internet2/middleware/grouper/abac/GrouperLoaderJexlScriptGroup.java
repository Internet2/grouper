package edu.internet2.middleware.grouper.abac;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;

public class GrouperLoaderJexlScriptGroup {

  private Group group;
  
  public Group getGroup() {
    if (group == null && !StringUtils.isBlank(groupId)) {
      group = GroupFinder.findByUuid(this.getGroupId(), true);
      this.setGroup(group);
    }
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  private AttributeAssign attributeAssign;
  
  public AttributeAssign getAttributeAssign() {
    return attributeAssign;
  }

  public void setAttributeAssign(AttributeAssign attributeAssign) {
    this.attributeAssign = attributeAssign;
  }

  private String groupId;

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  
  private String attributeAssignId;

  public String getAttributeAssignId() {
    return attributeAssignId;
  }

  public void setAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
  }
  
  private String script;

  public String getScript() {
    return script;
  }

  /**
   * comma-separated list of subject source IDs for this group, or null for global defaults
   */
  public String getSubjectSourceIds() {
    return subjectSourceIds;
  }

  public void setSubjectSourceIds(String subjectSourceIds) {
    this.subjectSourceIds = subjectSourceIds;
  }

  public void setScript(String script) {
    this.script = script;
  }

  private String subjectSourceIds;
  
  private Set<String> scriptContainsGroupNames = new HashSet<String>();

  public Set<String> getScriptContainsGroupNames() {
    return scriptContainsGroupNames;
  }

}
