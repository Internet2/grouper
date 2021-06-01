package edu.internet2.middleware.grouper.app.grouperTypes;

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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils; 

/**
 * <p>Use this class to find objects type attributes on groups</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * GrouperObjectTypesAttributeValue attributeValue = new GdgTypeGroupFinder().assignGroup(group).assignType("ref").findGdgTypeGroupAssignment();
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to find multiple object types attributes on a group
 * <blockquote>
 * <pre>
 * Set<GrouperObjectTypesAttributeValue> attributeValues = new GdgTypeGroupFinder().assignGroup(group).findGdgTypeGroupAssignments();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GdgTypeGroupFinder {
  
  private Group group;
  
  private String groupId;
  
  private String groupName;
  
  private String type;
  
  private boolean runAsRoot;
  
  /**
   * if null (default) retrieve direct and indirect assignments, if true then only retrieve direct assignments,
   * if false only retrieve indirect assignments
   */
  private Boolean directAssignment;
  
  /**
   * assign group from which to retrieve object types attributes
   * @param stem
   * @return
   */
  public GdgTypeGroupFinder assignGroup(Group group) {
    this.group = group;
    return this;
  }
  
  /**
   * assign group id from which to retrieve object types attributes
   * @param stem
   * @return
   */
  public GdgTypeGroupFinder assignGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  } 
  
  /**
   * assign group name from which to retrieve object types attributes
   * @param stem
   * @return
   */
  public GdgTypeGroupFinder assignGroupName(String groupName) {
    this.groupName = groupName;
    return this;
  }
  
  /**
   * type e.g. ref, basis, app
   * @param type
   * @return
   */
  public GdgTypeGroupFinder assignType(String type) {
    this.type = type;
    return this;
  }
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public GdgTypeGroupFinder assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  /**
   * only find attributes where direct assignment is true (possible values: null (default), true, false)
   * @param directAssignment
   * @return
   */
  public GdgTypeGroupFinder assignDirectAssignment(Boolean directAssignment) {
    this.directAssignment = directAssignment;
    return this;
  }
  
  /**
   * find bean containing object types attributes on a group
   * @return
   */
  public GrouperObjectTypesAttributeValue findGdgTypeGroupAssignment() {
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = this.findGdgTypeGroupAssignments();

    return GrouperUtil.setPopOne(gdgTypeGroupAssignments);
  }
  
  
  /**
   * find bean containing object types attributes on a group
   * @return
   */
  @SuppressWarnings("unchecked")
  public Set<GrouperObjectTypesAttributeValue> findGdgTypeGroupAssignments() {
   
    final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = (Set<GrouperObjectTypesAttributeValue>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

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
          if (!group.canHavePrivilege(SUBJECT_IN_SESSION, AccessPrivilege.READ.getName(), false)) {
            throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
              + "' cannot READ group '" + group.getName() + "'");
          }
        }
        
        if (StringUtils.isNotBlank(type) && !GrouperObjectTypesSettings.getObjectTypeNames().contains(type)) {
          throw new RuntimeException("type must be one of the valid types ["+GrouperUtil.collectionToString(GrouperObjectTypesSettings.getObjectTypeNames()) + "]");
        }
        
        Set<GrouperObjectTypesAttributeValue> result = new HashSet<GrouperObjectTypesAttributeValue>();
        
        if (StringUtils.isNotBlank(type)) {
          GrouperObjectTypesAttributeValue typesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(group, type);
          if (typesAttributeValue != null) {            
            result.add(typesAttributeValue);
          }
        } else {
          List<GrouperObjectTypesAttributeValue> grouperObjectTypesAttributeValues = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(group);
          result.addAll(GrouperUtil.nonNull(grouperObjectTypesAttributeValues));
        }
        
        if (directAssignment == null) {
          return result;
        } else if (directAssignment) {
          return result.stream().filter(attributeValue -> attributeValue.isDirectAssignment()).collect(Collectors.toSet());
        } else {
          return result.stream().filter(attributeValue -> !attributeValue.isDirectAssignment()).collect(Collectors.toSet());
        }
        
      }});
    
    return gdgTypeGroupAssignments;
  }

}
