package edu.internet2.middleware.grouper.membership;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * collection of membership paths for a subject in a group, 
 * group privilege, stem privilege, or attribute privilege
 * 
 * @author mchyzer
 *
 */
public class MembershipPathGroup {

  /**
   * true if member has membership in this owner
   * @return true if member has membership in this owner
   */
  public boolean isHasMembership() {
    return GrouperUtil.length(this.membershipPaths) > 0;
  }
  
  /**
   * analyze the membership/privilege of a member in a group by various paths
   * @param group
   * @param member
   * @param field
   * @return the group of paths
   */
  public static MembershipPathGroup analyze(final Group group, final Member member, final Field field) {
    final MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    final GrouperSession GROUPER_SESSION = GrouperSession.staticGrouperSession();
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        membershipPathGroup.analyzeHelper(group, member, field, GROUPER_SESSION.getSubject());
        return null;
      }
    });

    return membershipPathGroup;
  }

  /**
   * analyze the membership/privilege of a member in a group by various paths
   * @param group
   * @param member
   * @param field
   * @param callingSubject
   * @return the group of paths
   */
  public static MembershipPathGroup analyze(final Group group, final Member member, final Field field, Subject callingSubject) {
    MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    membershipPathGroup.analyzeHelper(group, member, field, callingSubject);
    return membershipPathGroup;
  }

  /**
   * analyze the membership/privilege of a member in a group by various paths,  This should be called with a root session
   * @param group
   * @param theMember
   * @param field
   * @param callingSubject is who is executing the call
   */
  private void analyzeHelper(Group ownerGroup, Member theMember, Field field, final Subject callingSubject) {
    this.ownerGroup = ownerGroup;
    this.member = theMember;
    this.field = field;

    if (field.isGroupAccessField()) {
      this.membershipOwnerType = MembershipOwnerType.groupPrivilege;
    } else if (field.isGroupListField()) {
      this.membershipOwnerType = MembershipOwnerType.list;
    } else {
      throw new RuntimeException("Not expecting field: " + field);
    }
    
    this.membershipPaths = new TreeSet<MembershipPath>();
        
    //lets get the groupsets
    Set<GroupSet> groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerGroupAndFieldAndMembershipMember(
        ownerGroup.getId(), field.getId(), this.member);

    //lets get all the groups (not secure for calling user
    final Set<String> groupIds = new HashSet<String>();
    groupIds.add(ownerGroup.getId());
    for (GroupSet groupSet : GrouperUtil.nonNull(groupSets)) {
      
      groupIds.add(groupSet.getOwnerGroupId());
      groupIds.add(groupSet.getMemberGroupId());
      
    }
    
    Set<Group> groups = new GroupFinder().assignGroupIds(groupIds).findGroups();
    
    //lets put these in a map
    Map<String, Group> groupIdToGroupMapUnsecure = new HashMap<String, Group>();
    for (Group group : GrouperUtil.nonNull(groups)) {
      groupIdToGroupMapUnsecure.put(group.getId(), group);
    }
    
    //secure group query, see what the subject can READ
    GrouperSession secureSession = GrouperSession.start(callingSubject, false);
    Set<Group> groupsSecure = (Set<Group>)GrouperSession.callbackGrouperSession(secureSession, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return new GroupFinder().assignGroupIds(groupIds).assignSubject(callingSubject)
            .assignPrivileges(AccessPrivilege.READ_PRIVILEGES).findGroups();
        
      }
    });
    GrouperSession.stopQuietly(secureSession);
    
    Map<String, GroupSet> groupSetIdToGroupSet = new HashMap<String, GroupSet>();
    for (GroupSet groupSet : GrouperUtil.nonNull(groupSets)) {
      groupSetIdToGroupSet.put(groupSet.getId(), groupSet);
    }

    //see which groups have immediate memberships (these are the ones we care about)
    Set<Object[]> membershipGroupMemberSet = new MembershipFinder().assignGroupIds(groupIds).assignMembershipType(MembershipType.IMMEDIATE)
        .assignField(field).addMemberId(member.getId()).findMembershipsMembers();

    Set<Group> groupsWithImmediateMemberships = new HashSet<Group>();

    for (Object[] membershipGroupMember : GrouperUtil.nonNull(membershipGroupMemberSet)) {
      groupsWithImmediateMemberships.add((Group)membershipGroupMember[1]);
    }

    for (GroupSet groupSet : GrouperUtil.nonNull(groupSets)) {

      MembershipType membershipType = null;
      if (StringUtils.equals("immediate", groupSet.getType())) {
        membershipType = MembershipType.IMMEDIATE;
      } else if (StringUtils.equals("effective", groupSet.getType())) {
        membershipType = MembershipType.EFFECTIVE;
      } else if (StringUtils.equals("composite", groupSet.getType())) {
        membershipType = MembershipType.COMPOSITE;
      } else  {
        throw new RuntimeException("Not expecting groupSet type: " + groupSet.getType());
      }
      
      List<MembershipPathNode> membershipPathNodes = new ArrayList<MembershipPathNode>();
      
      GroupSet currentGroupSet = groupSet;
      
      boolean allowed = true;
      
      for (int i=groupSet.getDepth(); i>=0; i--) {
        
        Group memberGroup = groupIdToGroupMapUnsecure.get(currentGroupSet.getMemberGroupId());
        allowed = allowed && groupsSecure.contains(memberGroup);
        MembershipPathNode membershipPathNode = null;
        if (memberGroup.hasComposite()) {

          Composite composite = memberGroup.getComposite(true);
          CompositeType compositeType = composite.getType();
          
          Group leftGroup = composite.getLeftGroup();
          Group rightGroup = composite.getRightGroup();

          membershipPathNode = new MembershipPathNode(field, memberGroup, compositeType, leftGroup, rightGroup);

        } else {
        
          membershipPathNode = new MembershipPathNode(field, memberGroup);
        }
        membershipPathNodes.add(membershipPathNode);
        
        //move up the chain
        currentGroupSet = groupSetIdToGroupSet.get(currentGroupSet.getParentId());

      }

      //if its a composite, we need to deal with the factors
      Group memberGroup = groupIdToGroupMapUnsecure.get(groupSet.getMemberGroupId());

      MembershipPath membershipPath = new MembershipPath(this.member, membershipPathNodes, membershipType);

      membershipPath.setPathAllowed(allowed);

      //is it immediate, or a means to an end?
      if (groupsWithImmediateMemberships.contains(memberGroup)) {
        this.membershipPaths.add(membershipPath);
        
      }
      
      if (memberGroup.hasComposite()) {
        
        //lets analyze each factor
        Composite composite = memberGroup.getComposite(true);
        CompositeType compositeType = composite.getType();
        if (compositeType == CompositeType.UNION) {
          
          MembershipPathGroup membershipPathGroupLeft = MembershipPathGroup.analyze(composite.getLeftGroup(), theMember, field, callingSubject);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupLeft);
          MembershipPathGroup membershipPathGroupRight = MembershipPathGroup.analyze(composite.getRightGroup(), theMember, field, callingSubject);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupRight);
          
        } else if (compositeType == CompositeType.COMPLEMENT) {

          MembershipPathGroup membershipPathGroupLeft = MembershipPathGroup.analyze(composite.getLeftGroup(), theMember, field, callingSubject);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupLeft);
          
        } else if (compositeType == CompositeType.INTERSECTION) {
          MembershipPathGroup membershipPathGroupLeft = MembershipPathGroup.analyze(composite.getLeftGroup(), theMember, field, callingSubject);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupLeft);
          MembershipPathGroup membershipPathGroupRight = MembershipPathGroup.analyze(composite.getRightGroup(), theMember, field, callingSubject);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupRight);
          
        }
      }
      
    }
    
  }

  /**
   * merge in a composite branch into the main path group
   * @param membershipPathGroup
   */
  private void mergeMembershipPathGroup(MembershipPath compositePath, MembershipPathGroup membershipPathGroup) {
    if (membershipPathGroup != null && membershipPathGroup.isHasMembership()) {
      for (MembershipPath membershipPath : membershipPathGroup.getMembershipPaths()) {

        List<MembershipPathNode> membershipPathNodes = new ArrayList<MembershipPathNode>();
        
        //first add the closest
        membershipPathNodes.addAll(membershipPath.getMembershipPathNodes());
        
        //then add the ones all the way to the destination
        membershipPathNodes.addAll(compositePath.getMembershipPathNodes());
        
        MembershipPath newMembershipPath = new MembershipPath(membershipPath.getMember(), membershipPathNodes, 
            membershipPath.getMembershipType() == MembershipType.IMMEDIATE ? MembershipType.EFFECTIVE : membershipPath.getMembershipType());
        
        newMembershipPath.setPathAllowed(membershipPath.isPathAllowed() && compositePath.isPathAllowed());
        
        this.getMembershipPaths().add(newMembershipPath);
      }
    }
  }
  
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {

    //########################## Non composite
    System.out.println("########################## Non composite\n");
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject memberSubject = SubjectFinder.findById("test.subject.0", true);
    Subject sessionSubject = SubjectFinder.findById("test.subject.1", true);
    Subject sessionSubject2 = SubjectFinder.findById("test.subject.2", true);
    Subject sessionSubject3 = SubjectFinder.findById("test.subject.3", true);
    Subject sessionSubject4 = SubjectFinder.findById("test.subject.4", true);
    Member memberMember = MemberFinder.findBySubject(grouperSession, memberSubject, true);
    
    Group endGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallGroup").save();
    
    endGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    endGroup.grantPriv(sessionSubject2, AccessPrivilege.READ, false);
    endGroup.grantPriv(sessionSubject3, AccessPrivilege.ADMIN, false);
    endGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);
    
    endGroup.addMember(memberSubject, false);
    
    //one hop membership
    {
      Group intermediateGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:intermediateGroup").save();
  
      intermediateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      intermediateGroup.grantPriv(sessionSubject3, AccessPrivilege.ADMIN, false);
      intermediateGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);

      endGroup.addMember(intermediateGroup.toSubject(), false);
      intermediateGroup.addMember(memberSubject, false);
    }
    
    //two hop membership
    {
      Group intermediateGroup2a_member = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:intermediateGroup2a_member").save();
      intermediateGroup2a_member.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      Group intermediateGroup2b_owner = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:intermediateGroup2b_owner").save();
      intermediateGroup2b_owner.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
  
      endGroup.addMember(intermediateGroup2b_owner.toSubject(), false);
      intermediateGroup2b_owner.addMember(intermediateGroup2a_member.toSubject(), false);
      intermediateGroup2a_member.addMember(memberSubject, false);
    }

    Group overallComposite = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallComposite").save();
    overallComposite.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    overallComposite.grantPriv(sessionSubject4, AccessPrivilege.READ, false);
    
    {
      Group appGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:appGroup").save();
      appGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      appGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);
  
      Group employeeGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:employeeGroup").save();
      employeeGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      employeeGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);
  
      if (!overallComposite.hasComposite()) {
        overallComposite.addCompositeMember( CompositeType.INTERSECTION, appGroup, employeeGroup );
      }
  
      appGroup.addMember(memberSubject, false);
      employeeGroup.addMember(memberSubject, false);
      
      endGroup.addMember(overallComposite.toSubject(), false);
    }
    
    Group overallCompositeDepth2owner = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallCompositeDepth2owner").save();
    overallCompositeDepth2owner.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

    {
      Group overallCompositeDepth2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:overallCompositeDepth2").save();
      overallCompositeDepth2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      Group appGroupDepth2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:appGroupDepth2").save();
      appGroupDepth2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      Group employeeGroupDepth2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:employeeGroupDepth2").save();
      employeeGroupDepth2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      overallCompositeDepth2owner.addMember(overallCompositeDepth2.toSubject(), false);
      
      if (!overallCompositeDepth2.hasComposite()) {
        overallCompositeDepth2.addCompositeMember( CompositeType.INTERSECTION, appGroupDepth2, employeeGroupDepth2 );
      }
  
      appGroupDepth2.addMember(memberSubject, false);
      employeeGroupDepth2.addMember(memberSubject, false);
      
      endGroup.addMember(overallCompositeDepth2owner.toSubject(), false);
    }
    
    Group overallEffectiveComposite = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallEffectiveComposite").save();
    overallEffectiveComposite.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

    {
      Group appEffectiveGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:appEffectiveGroup").save();
      appEffectiveGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      Group appImmediateGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:appImmediateGroup").save();
      appImmediateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      Group employeeEffectiveGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:employeeEffectiveGroup").save();
      employeeEffectiveGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      Group employeeImmediateGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:employeeImmediateGroup").save();
      employeeImmediateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      if (!overallEffectiveComposite.hasComposite()) {
        overallEffectiveComposite.addCompositeMember( CompositeType.INTERSECTION, appEffectiveGroup, employeeEffectiveGroup );
      }

      appEffectiveGroup.addMember(appImmediateGroup.toSubject(), false);
      employeeEffectiveGroup.addMember(employeeImmediateGroup.toSubject(), false);

      appImmediateGroup.addMember(memberSubject, false);
      employeeImmediateGroup.addMember(memberSubject, false);

      endGroup.addMember(overallEffectiveComposite.toSubject(), false);
    }
    
    MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());
    
    System.out.println(membershipPathGroup.toString());

    //########################## noncomposite as test.subject.1
    System.out.println("\n\n########################## noncomposite as test.subject.1\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());

    System.out.println(membershipPathGroup.toString());


    //########################## noncomposite as test.subject.2
    System.out.println("\n\n########################## noncomposite as test.subject.2\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject2);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());

    System.out.println(membershipPathGroup.toString());


    //########################## noncomposite as test.subject.3
    System.out.println("\n\n########################## noncomposite as test.subject.3\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject3);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());

    System.out.println(membershipPathGroup.toString());


    //########################## noncomposite as test.subject.4
    System.out.println("\n\n########################## noncomposite as test.subject.4\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject4);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());

    System.out.println(membershipPathGroup.toString());


    //########################## composite
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.startRootSession();

    System.out.println("\n\n########################## composite\n");

    membershipPathGroup = MembershipPathGroup.analyze(overallComposite, memberMember, Group.getDefaultList());

    System.out.println(membershipPathGroup.toString());


  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    
    boolean first = true;
    for (MembershipPath membershipPath : GrouperUtil.nonNull(this.membershipPaths)) {

      if (!first) {
        result.append("\n");
      }
      result.append(membershipPath.toString());
      
      first = false;
    }
    return result.toString();
  }  

  /**
   * set of membership paths ordered by shortest path to longest
   */
  private Set<MembershipPath> membershipPaths;

  /**
   * member for this membership
   */
  private Member member;
  
  /**
   * if this is a list or group privilege, this is the owner group
   */
  private Group ownerGroup;

  /**
   * if this is a stem privilege, this is the owner stem
   */
  private Stem ownerStem;

  /**
   * if this is an attributeDef privilege, this is the owner attribute def
   */
  private AttributeDef ownerAttributeDef;

  /**
   * field of the overall membership
   */
  private Field field;
  
  /**
   * what type e.g. list, or stemPrivilege
   */
  private MembershipOwnerType membershipOwnerType;

  /**
   * set of membership paths
   * @return paths
   */
  public Set<MembershipPath> getMembershipPaths() {
    return this.membershipPaths;
  }

  /**
   * set of membership paths
   * @param membershipPaths1
   */
  public void setMembershipPaths(Set<MembershipPath> membershipPaths1) {
    this.membershipPaths = membershipPaths1;
  }

  /**
   * member for this membership
   * @return member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * member for this membership
   * @param member1
   */
  public void setMember(Member member1) {
    this.member = member1;
  }

  /**
   * if this is a list or group privilege, this is the owner group
   * @return group
   */
  public Group getOwnerGroup() {
    return this.ownerGroup;
  }

  /**
   * if this is a list or group privilege, this is the owner group
   * @param ownerGroup1
   */
  public void setOwnerGroup(Group ownerGroup1) {
    this.ownerGroup = ownerGroup1;
  }

  /**
   * if this is a stem privilege, this is the owner stem
   * @return owner stem
   */
  public Stem getOwnerStem() {
    return this.ownerStem;
  }

  /**
   * if this is a stem privilege, this is the owner stem
   * @param ownerStem1
   */
  public void setOwnerStem(Stem ownerStem1) {
    this.ownerStem = ownerStem1;
  }

  /**
   * if this is an attributeDef privilege, this is the owner attribute def
   * @return attribute def
   */
  public AttributeDef getOwnerAttributeDef() {
    return this.ownerAttributeDef;
  }

  /**
   * if this is an attributeDef privilege, this is the owner attribute def
   * @param ownerAttributeDef1
   */
  public void setOwnerAttributeDef(AttributeDef ownerAttributeDef1) {
    this.ownerAttributeDef = ownerAttributeDef1;
  }

  /**
   * field of the overall membership
   * @return field
   */
  public Field getField() {
    return this.field;
  }

  /**
   * 
   * @param field1
   */
  public void setField(Field field1) {
    this.field = field1;
  }

  /**
   * what type e.g. list, or stemPrivilege
   * @return owner type
   */
  public MembershipOwnerType getMembershipOwnerType() {
    return this.membershipOwnerType;
  }

  /**
   * what type e.g. list, or stemPrivilege
   * @param membershipOwnerType1
   */
  public void setMembershipOwnerType(MembershipOwnerType membershipOwnerType1) {
    this.membershipOwnerType = membershipOwnerType1;
  }


}
