/**
 * 
 */
package edu.internet2.middleware.grouperVoot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperVoot.beans.VootGetGroupsResponse;
import edu.internet2.middleware.grouperVoot.beans.VootGetMembersResponse;
import edu.internet2.middleware.grouperVoot.beans.VootGroup;
import edu.internet2.middleware.grouperVoot.beans.VootPerson;
import edu.internet2.middleware.subject.Subject;


/**
 * business logic for voot
 * @author mchyzer
 *
 */
public class VootLogic {

  /**
   * get the members for a group based on a group
   * @param vootGroup
   * @return the response
   */
  public static VootGetMembersResponse getMembers(VootGroup vootGroup) {
    
    //note the name is the id
    String groupName = vootGroup.getId();
    
    //throws exception if the group is not found
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    
    Set<Subject> memberSubjects = new HashSet<Subject>();
    
    {
      Set<Member> members = group.getMembers();
      for (Member member : members) {
        memberSubjects.add(member.getSubject());
      }
    }
    Set<Subject> admins = group.getAdmins();
    Set<Subject> updaters = group.getUpdaters();
    
    //lets keep track of the subjects
    //since subjects have a composite key, then keep track with multikey
    Map<MultiKey, Subject> multiKeyToSubject = new HashMap<MultiKey, Subject>();

    //member, admin, manager
    Map<MultiKey, String> memberToRole = new HashMap<MultiKey, String>();
    
    for (Subject subject : memberSubjects) {
      MultiKey subjectMultiKey = new MultiKey(subject.getSourceId(), subject.getId());
      multiKeyToSubject.put(subjectMultiKey, subject);
      memberToRole.put(subjectMultiKey, "admin");
    }
    for (Subject subject : updaters) {
      MultiKey subjectMultiKey = new MultiKey(subject.getSourceId(), subject.getId());
      multiKeyToSubject.put(subjectMultiKey, subject);
      memberToRole.put(subjectMultiKey, "manager");
    }
    for (Subject subject : admins) {
      MultiKey subjectMultiKey = new MultiKey(subject.getSourceId(), subject.getId());
      multiKeyToSubject.put(subjectMultiKey, subject);
      memberToRole.put(subjectMultiKey, "member");
    }
    
    VootGetMembersResponse vootGetMembersResponse = new VootGetMembersResponse();
    
    VootPerson[] result = new VootPerson[memberToRole.size()];
    vootGetMembersResponse.setEntry(result);
    
    int index = 0;
    
    
    //lets put them all back and make the person subjects
    for (MultiKey multiKey : memberToRole.keySet()) {
      Subject subject = multiKeyToSubject.get(multiKey);
      String role = memberToRole.get(multiKey);
      VootPerson vootPerson = new VootPerson(subject);
      vootPerson.setVoot_membership_role(role);
      result[index] = vootPerson;
      
      index++;
    }
    
    vootGetMembersResponse.assignPaging(result);
    
    return vootGetMembersResponse;
  }
  
  /**
   * get the groups that a person is in
   * @param vootPerson
   * @return the groups
   */
  public static VootGetGroupsResponse getGroups(VootPerson vootPerson) {
    
    Subject subject = SubjectFinder.findById(vootPerson.getId(), true);
    
    return getGroups(subject);
  }
    
  /**
   * get a subject
   * @param subject
   * @return the response
   */
  public static VootGetMembersResponse getSubject(Subject subject) {
    
    VootPerson vootPerson = new VootPerson(GrouperSession.staticGrouperSession().getSubject());
    VootGetMembersResponse vootGetMembersResponse = new VootGetMembersResponse();
    vootGetMembersResponse.setEntry(new VootPerson[]{vootPerson});
    
    vootGetMembersResponse.assignPaging(vootGetMembersResponse.getEntry());
    return vootGetMembersResponse;
  }
  
  /**
   * get the groups that a person is in
   * @param subject
   * @return the groups
   */
  public static VootGetGroupsResponse getGroups(Subject subject) {
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
    
    VootGetGroupsResponse vootGetGroupsResponse = new VootGetGroupsResponse();
    
    if (member == null) {
      vootGetGroupsResponse.assignPaging(null);
      return vootGetGroupsResponse;
    }
    
    //member, admin, manager
    
    Set<Group> groups = member.getGroups();
    
    Set<Group> admins = member.getGroups(FieldFinder.find("admins", true));
    
    Set<Group> updaters = member.getGroups(FieldFinder.find("updaters", true));
    
    Map<Group, String> groupToRole = new TreeMap<Group, String>();
    
    //if you are a member, and not an admin or updater, then you are a member
    for (Group group : GrouperUtil.nonNull(groups)) {
      groupToRole.put(group, "member");
    }

    //if you are an updater and not an admin, then you are a manager
    for (Group group : GrouperUtil.nonNull(updaters)) {
      groupToRole.put(group, "manager");
    }
    
    //if you are an admin, then you are an admin
    for (Group group : GrouperUtil.nonNull(admins)) {
      groupToRole.put(group, "admin");
    }
    
    
    if (groupToRole.size() == 0) {
      vootGetGroupsResponse.assignPaging(null);
      return vootGetGroupsResponse;
    }
    
    VootGroup[] result = new VootGroup[groupToRole.size()];
    vootGetGroupsResponse.setEntry(result);
    
    int index = 0;
    for (Group group : groupToRole.keySet()) {
      
      VootGroup vootGroup = new VootGroup(group);
      vootGroup.setVoot_membership_role(groupToRole.get(group));
      
      result[index] = vootGroup;
      
      index++;
    }
    
    vootGetGroupsResponse.assignPaging(result);
    return vootGetGroupsResponse;
  }
  
  
  /**
   * get the groups that a person is in
   * @return the groups
   */
  public static VootGetGroupsResponse getGroups() {
    
    VootGetGroupsResponse vootGetGroupsResponse = new VootGetGroupsResponse();

    Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure("%", null, null, null);
    
    if (GrouperUtil.length(groups) == 0) {
      vootGetGroupsResponse.assignPaging(null);
      return vootGetGroupsResponse;
    }
    
    VootGroup[] result = new VootGroup[groups.size()];
    vootGetGroupsResponse.setEntry(result);
    
    int index = 0;
    for (Group group : groups) {
      
      VootGroup vootGroup = new VootGroup(group);
      
      result[index] = vootGroup;
      
      index++;
    }
    
    vootGetGroupsResponse.assignPaging(result);
    return vootGetGroupsResponse;
  }
}
