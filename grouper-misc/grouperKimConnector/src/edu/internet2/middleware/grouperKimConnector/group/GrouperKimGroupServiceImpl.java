/**
 * @author mchyzer
 * $Id: GrouperKimGroupServiceImpl.java,v 1.1 2009-12-13 06:57:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.group;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.group.dto.GroupMembershipInfo;
import org.kuali.rice.kim.service.GroupService;


/**
 *
 */
public class GrouperKimGroupServiceImpl implements GroupService {

  /**
   * @see org.kuali.rice.kim.service.GroupService#getDirectGroupIdsForPrincipal(java.lang.String)
   */
  
  public List<String> getDirectGroupIdsForPrincipal(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getDirectMemberGroupIds(java.lang.String)
   */
  
  public List<String> getDirectMemberGroupIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getDirectMemberPrincipalIds(java.lang.String)
   */
  
  public List<String> getDirectMemberPrincipalIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getDirectParentGroupIds(java.lang.String)
   */
  
  public List<String> getDirectParentGroupIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupAttributes(java.lang.String)
   */
  
  public Map<String, String> getGroupAttributes(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupIdsForPrincipal(java.lang.String)
   */
  
  public List<String> getGroupIdsForPrincipal(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupIdsForPrincipalByNamespace(java.lang.String, java.lang.String)
   */
  
  public List<String> getGroupIdsForPrincipalByNamespace(String arg0, String arg1) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupInfo(java.lang.String)
   */
  
  public GroupInfo getGroupInfo(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupInfoByName(java.lang.String, java.lang.String)
   */
  
  public GroupInfo getGroupInfoByName(String arg0, String arg1) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupInfos(java.util.Collection)
   */
  
  public Map<String, GroupInfo> getGroupInfos(Collection<String> arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupMembers(java.util.List)
   */
  
  public Collection<GroupMembershipInfo> getGroupMembers(List<String> arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupMembersOfGroup(java.lang.String)
   */
  
  public Collection<GroupMembershipInfo> getGroupMembersOfGroup(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupsForPrincipal(java.lang.String)
   */
  
  public List<GroupInfo> getGroupsForPrincipal(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getGroupsForPrincipalByNamespace(java.lang.String, java.lang.String)
   */
  
  public List<GroupInfo> getGroupsForPrincipalByNamespace(String arg0, String arg1) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getMemberGroupIds(java.lang.String)
   */
  
  public List<String> getMemberGroupIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getMemberPrincipalIds(java.lang.String)
   */
  
  public List<String> getMemberPrincipalIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#getParentGroupIds(java.lang.String)
   */
  
  public List<String> getParentGroupIds(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#isDirectMemberOfGroup(java.lang.String, java.lang.String)
   */
  
  public boolean isDirectMemberOfGroup(String arg0, String arg1) {
    return false;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#isGroupActive(java.lang.String)
   */
  
  public boolean isGroupActive(String arg0) {
    return false;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#isGroupMemberOfGroup(java.lang.String, java.lang.String)
   */
  
  public boolean isGroupMemberOfGroup(String arg0, String arg1) {
    return false;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#isMemberOfGroup(java.lang.String, java.lang.String)
   */
  
  public boolean isMemberOfGroup(String arg0, String arg1) {
    return false;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#lookupGroupIds(java.util.Map)
   */
  
  public List<String> lookupGroupIds(Map<String, String> arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.GroupService#lookupGroups(java.util.Map)
   */
  
  public List<? extends Group> lookupGroups(Map<String, String> arg0) {
    return null;
  }

}
