package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * <p>Use this class to find provisioning attributes on groups</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
 * GrouperProvisioningAttributeValue attributeValue = provisionableGroupFinder.assignGroup(group).assignTargetName("ldapProvTest")
        .findProvisionableGroupAttributeValue();
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to find multiple provisioning attributes on a group
 * <blockquote>
 * <pre>
 * ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
 * Set<GrouperProvisioningAttributeValue> provisionableStemAttributeValues = provisionableGroupFinder.assignGroupName(group.getName()).findProvisionableGroupAttributeValues();
 * </pre>
 * </blockquote>
 * </p>
 */
public class ProvisionableGroupFinder {
  
  private Group group;
  
  private String groupId;
  
  private String groupName;
  
  private String targetName;
  
  private boolean runAsRoot;
  
  /**
   * if null (default) retrieve direct and indirect assignments, if true then only retrieve direct assignments,
   * if false only retrieve indirect assignments
   */
  private Boolean directAssignment;
  
  public ProvisionableGroupFinder assignGroup(Group group) {
    this.group = group;
    return this;
  }
  
  public ProvisionableGroupFinder assignGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  } 
  
  public ProvisionableGroupFinder assignGroupName(String groupName) {
    this.groupName = groupName;
    return this;
  }
  
  public ProvisionableGroupFinder assignTargetName(String targetName) {
    this.targetName = targetName;
    return this;
  }
  
  public ProvisionableGroupFinder assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  public ProvisionableGroupFinder assignDirectAssignment(Boolean directAssignment) {
    this.directAssignment = directAssignment;
    return this;
  }

  
  public GrouperProvisioningAttributeValue findProvisionableGroupAttributeValue() {
    
    Set<GrouperProvisioningAttributeValue> grouperProvisioningAttributeValues = this.findProvisionableGroupAttributeValues();

    return GrouperUtil.setPopOne(grouperProvisioningAttributeValues);
  }
  
  @SuppressWarnings("unchecked")
  public Set<GrouperProvisioningAttributeValue> findProvisionableGroupAttributeValues() {
    
    Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
    
    Set<GrouperProvisioningAttributeValue> grouperProvisioningAttributeValues = (Set<GrouperProvisioningAttributeValue>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (group == null && !StringUtils.isBlank(groupId)) {
          group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false, new QueryOptions().secondLevelCache(false));
        }
        
        if (group == null && !StringUtils.isBlank(groupName)) {
          group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false, new QueryOptions().secondLevelCache(false));
        }
        
        GrouperUtil.assertion(group != null,  "Group not found");
        
        if (!runAsRoot) {
          if (!PrivilegeHelper.isWheelOrRoot(SUBJECT_IN_SESSION)) {
            throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION)+ "' is not wheel or root user.");
          }
        }
        
        if (StringUtils.isNotBlank(targetName) &&  !GrouperProvisioningSettings.getTargets(true).containsKey(targetName)) {
          throw new RuntimeException("target must be one of the valid targets ["+GrouperUtil.collectionToString(GrouperProvisioningSettings.getTargets(true).keySet()) + "]");
        }
        
        Set<GrouperProvisioningAttributeValue> result = new HashSet<GrouperProvisioningAttributeValue>();
        
        if (StringUtils.isNotBlank(targetName)) {
          GrouperProvisioningAttributeValue provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(group, targetName);
          if (provisioningAttributeValue != null) {            
            result.add(provisioningAttributeValue);
          }
        } else {
          List<GrouperProvisioningAttributeValue> provisioningAttributeValues = GrouperProvisioningService.getProvisioningAttributeValues(group);
          result.addAll(GrouperUtil.nonNull(provisioningAttributeValues));
        }
        
        if (directAssignment == null) {
          return result;
        } else if (directAssignment) {
          return result.stream().filter(attributeValue -> attributeValue.isDirectAssignment()).collect(Collectors.toSet());
        } else {
          return result.stream().filter(attributeValue -> !attributeValue.isDirectAssignment()).collect(Collectors.toSet());
        }
        
      }});
    
    return grouperProvisioningAttributeValues;
    
    
  }
  

}
