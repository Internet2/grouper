package edu.internet2.middleware.grouper.membership;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
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
   * analyze group privileges for a group and a member
   * @param group
   * @param member
   * @return the membershipPathGroup
   */
  public static MembershipPathGroup analyzePrivileges(final Group group, final Member member) {
    
    final MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    final GrouperSession GROUPER_SESSION = GrouperSession.staticGrouperSession();
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        membershipPathGroup.analyzePrivilegesHelper(group, member, GROUPER_SESSION.getSubject());
        return null;
      }
    });

    return membershipPathGroup;

    
  }
  
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
        
        membershipPathGroup.analyzeHelper(group, member, field, GROUPER_SESSION.getSubject(), DEFAULT_TIME_TO_LIVE);
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
   * @param timeToLive prevent recursive loops
   * @return the group of paths
   */
  public static MembershipPathGroup analyze(final Group group, final Member member, final Field field, Subject callingSubject,
      int timeToLive) {
    MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    membershipPathGroup.analyzeHelper(group, member, field, callingSubject, timeToLive);
    return membershipPathGroup;
  }

  /**
   * analyze the privileges of a member in a group by various paths
   * @param group
   * @param member
   * @param callingSubject
   * @return the group of paths
   */
  public static MembershipPathGroup analyzePrivileges(final Group group, final Member member, Subject callingSubject) {
    MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    membershipPathGroup.analyzePrivilegesHelper(group, member, callingSubject);
    return membershipPathGroup;
  }

  /**
   * merge in a composite branch into the main path group
   * @param compositePath 
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
        
        newMembershipPath.getFields().addAll(compositePath.getFields());
        // the composite path field is members, just ignore that since if you are a member of the composite, you have the field of the parent path
        
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
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject memberSubject = SubjectFinder.findById("test.subject.0", true);
    Subject sessionSubject = SubjectFinder.findById("test.subject.1", true);
    Subject sessionSubject2 = SubjectFinder.findById("test.subject.2", true);
    Subject sessionSubject3 = SubjectFinder.findById("test.subject.3", true);
    Subject sessionSubject4 = SubjectFinder.findById("test.subject.4", true);
    Member memberMember = MemberFinder.findBySubject(grouperSession, memberSubject, true);
    
    Group endGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallGroup").save();
    
    Group privGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:mpaths:privGroup").save();    
    
    Stem privStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:mpaths").save();

    AttributeDef privAttributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.attr).assignName("test:mpaths:privAttrDef").assignToStem(true).save();

    endGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    endGroup.grantPriv(sessionSubject2, AccessPrivilege.READ, false);
    endGroup.grantPriv(sessionSubject3, AccessPrivilege.ADMIN, false);
    endGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);

    privGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    privGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN, false);
    privGroup.grantPriv(sessionSubject2, AccessPrivilege.ADMIN, false);
    privGroup.grantPriv(sessionSubject3, AccessPrivilege.ADMIN, false);
    privGroup.grantPriv(sessionSubject4, AccessPrivilege.ADMIN, false);

    privGroup.grantPriv(endGroup.toSubject(), AccessPrivilege.READ, false);
    privGroup.grantPriv(endGroup.toSubject(), AccessPrivilege.UPDATE, false);
    privGroup.grantPriv(memberSubject, AccessPrivilege.VIEW, false);

    privStem.grantPriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_ATTR_READ, false);
    privStem.grantPriv(sessionSubject2, NamingPrivilege.STEM, false);
    privStem.grantPriv(sessionSubject3, NamingPrivilege.STEM, false);
    privStem.grantPriv(sessionSubject4, NamingPrivilege.STEM, false);
    
    privAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_OPTIN, false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(sessionSubject2, AttributeDefPrivilege.ATTR_READ, false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(sessionSubject3, AttributeDefPrivilege.ATTR_ADMIN, false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(sessionSubject4, AttributeDefPrivilege.ATTR_READ, false);
    
    privStem.grantPriv(endGroup.toSubject(), NamingPrivilege.STEM, false);
    privStem.grantPriv(endGroup.toSubject(), NamingPrivilege.CREATE, false);
    privStem.grantPriv(memberSubject, NamingPrivilege.STEM_ATTR_READ, false);

    privAttributeDef.getPrivilegeDelegate().grantPriv(endGroup.toSubject(), Privilege.getInstance("attrRead"), false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(endGroup.toSubject(), Privilege.getInstance("attrUpdate"), false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(memberSubject, Privilege.getInstance("attrView"), false);

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

      privGroup.grantPriv(intermediateGroup.toSubject(), AccessPrivilege.ADMIN, false);
      privStem.grantPriv(intermediateGroup.toSubject(), NamingPrivilege.STEM_ATTR_UPDATE, false);
      privAttributeDef.getPrivilegeDelegate().grantPriv(intermediateGroup.toSubject(), Privilege.getInstance("attrAdmin"), false);
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
    overallComposite.grantPriv(endGroup.toSubject(), AccessPrivilege.OPTOUT, false);
    
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
    
    System.out.println("########################## Non composite\n");
    System.out.println("####### member #######");
    System.out.println(membershipPathGroup.toString());

    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privGroup, memberMember);
    
    System.out.println("####### groupPriv #######");
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privStem, memberMember);
    
    System.out.println("####### stemPriv #######");
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privAttributeDef, memberMember);
    
    System.out.println("####### attributeDefPriv #######");
    System.out.println(membershipPathGroup.toString());
    
    //########################## noncomposite as test.subject.1
    System.out.println("\n\n########################## noncomposite as test.subject.1\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());

    System.out.println("####### member #######");
    System.out.println(membershipPathGroup.toString());

    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privGroup, memberMember);
    
    System.out.println("####### groupPriv #######");
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privStem, memberMember);
    
    System.out.println("####### stemPriv #######");
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privAttributeDef, memberMember);
    
    System.out.println("####### attributeDefPriv #######");
    System.out.println(membershipPathGroup.toString());
    

    //########################## noncomposite as test.subject.2
    System.out.println("\n\n########################## noncomposite as test.subject.2\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject2);

    System.out.println("####### member #######");
    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());

    System.out.println(membershipPathGroup.toString());

    System.out.println("####### groupPriv #######");
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privGroup, memberMember);
    
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privStem, memberMember);
    
    System.out.println("####### stemPriv #######");
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privAttributeDef, memberMember);
    
    System.out.println("####### attributeDefPriv #######");
    System.out.println(membershipPathGroup.toString());
    

    //########################## noncomposite as test.subject.3
    System.out.println("\n\n########################## noncomposite as test.subject.3\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject3);

    System.out.println("####### member #######");
    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());

    System.out.println(membershipPathGroup.toString());

    System.out.println("####### groupPriv #######");
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privGroup, memberMember);
    
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privStem, memberMember);
    
    System.out.println("####### stemPriv #######");
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privAttributeDef, memberMember);
    
    System.out.println("####### attributeDefPriv #######");
    System.out.println(membershipPathGroup.toString());
    

    //########################## noncomposite as test.subject.4
    System.out.println("\n\n########################## noncomposite as test.subject.4\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject4);

    System.out.println("####### member #######");
    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());

    System.out.println(membershipPathGroup.toString());

    System.out.println("####### groupPriv #######");
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privGroup, memberMember);
    
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privStem, memberMember);
    
    System.out.println("####### stemPriv #######");
    System.out.println(membershipPathGroup.toString());
    
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privAttributeDef, memberMember);
    
    System.out.println("####### attributeDefPriv #######");
    System.out.println(membershipPathGroup.toString());
    

    //########################## composite
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.startRootSession();

    System.out.println("\n\n########################## composite\n");

    membershipPathGroup = MembershipPathGroup.analyze(overallComposite, memberMember, Group.getDefaultList());

    System.out.println("####### member #######");
    System.out.println(membershipPathGroup.toString());

    membershipPathGroup = MembershipPathGroup.analyzePrivileges(overallComposite, memberMember);
    
    System.out.println("####### groupPriv #######");
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

  /**
   * analyze the privileges of a member in a group by various paths,  This should be called with a root session
   * @param group
   * @param theMember
   * @param callingSubject is who is executing the call
   */
  private void analyzePrivilegesHelper(Group group, Member theMember, final Subject callingSubject) {
    this.ownerGroup = group;
    this.member = theMember;
    this.membershipPaths = new TreeSet<MembershipPath>();

    //loop through all the access privileges, and analyze them
    for (Privilege privilege : AccessPrivilege.ALL_PRIVILEGES) {
      
      Field field = privilege.getField();
      MembershipPathGroup fieldMembershipPathGroup = MembershipPathGroup.analyze(group, theMember, field, callingSubject, DEFAULT_TIME_TO_LIVE);
      
      //merge this field in with the overall
      this.mergeFieldMembershipPathGroup(fieldMembershipPathGroup);
      
    }
    
  }

  /**
   * merge in a field membership path group into the overall
   * @param membershipPathGroup
   */
  private void mergeFieldMembershipPathGroup(MembershipPathGroup membershipPathGroup) {
    if (membershipPathGroup != null && membershipPathGroup.isHasMembership()) {
      
      for (MembershipPath newMembershipPath : membershipPathGroup.getMembershipPaths()) {

        boolean foundExistingPath = false;
        
        //see if one exists in the parent
        for (MembershipPath existingMembershipPath : this.membershipPaths) {
          
          //see if equals except fields
          if (existingMembershipPath.equalsExceptFields(newMembershipPath)) {
            //just add the fields
            if (newMembershipPath.getFields() != null && existingMembershipPath.getFields() != null) {
              existingMembershipPath.getFields().addAll(newMembershipPath.getFields());              
              foundExistingPath = true;
              break;
            }
          }
          
        }
        
        //if didnt add it already, then add it
        if (!foundExistingPath) {
          this.membershipPaths.add(newMembershipPath);
        }
        
      }
    }
  }

  /**
   * analyze the privileges of a member in a stem by various paths,  This should be called with a root session
   * @param stem
   * @param theMember
   * @param callingSubject is who is executing the call
   */
  private void analyzePrivilegesHelper(Stem stem, Member theMember, final Subject callingSubject) {
    this.ownerStem = stem;
    this.member = theMember;
    this.membershipPaths = new TreeSet<MembershipPath>();
  
    //loop through all the access privileges, and analyze them
    for (Privilege privilege : NamingPrivilege.ALL_PRIVILEGES) {
      
      Field field = privilege.getField();
      MembershipPathGroup fieldMembershipPathGroup = MembershipPathGroup.analyze(stem, theMember, field, callingSubject);
      
      //merge this field in with the overall
      this.mergeFieldMembershipPathGroup(fieldMembershipPathGroup);
      
    }
    
  }

  /**
   * analyze the privileges of a member in a attributedef by various paths,  This should be called with a root session
   * @param attributeDef
   * @param theMember
   * @param callingSubject is who is executing the call
   */
  private void analyzePrivilegesHelper(AttributeDef attributeDef, Member theMember, final Subject callingSubject) {
    this.ownerAttributeDef = attributeDef;
    this.member = theMember;
    this.membershipPaths = new TreeSet<MembershipPath>();
  
    //loop through all the access privileges, and analyze them
    for (Privilege privilege : AttributeDefPrivilege.ALL_PRIVILEGES) {
      
      Field field = privilege.getField();
      MembershipPathGroup fieldMembershipPathGroup = MembershipPathGroup.analyze(attributeDef, theMember, field, callingSubject);
      
      //merge this field in with the overall
      this.mergeFieldMembershipPathGroup(fieldMembershipPathGroup);
      
    }
    
  }

  /**
   * analyze the membership/privilege of a member in a group/stem/attributeDef by various paths,  This should be called with a root session
   * @param theOwner is group, stem, attributeDef
   * @param theMember
   * @param field
   * @param callingSubject is who is executing the call
   * @param timeToLive when it is less than 0, stop recursing
   */
  private void analyzeHelper(final GrouperObject theOwner, Member theMember, final Field field, final Subject callingSubject,
      int timeToLive){

    boolean isGroup = false;
    boolean isStem = false;
    boolean isAttributeDef = false;

    if (theOwner instanceof Group) {
      isGroup = true;
      this.ownerGroup = (Group)theOwner;
    } else if (theOwner instanceof Stem) {
      isStem = true;
      this.ownerStem = (Stem)theOwner;
    } else if (theOwner instanceof AttributeDef) {
      isAttributeDef = true;
      this.ownerAttributeDef = (AttributeDef)theOwner;
    } else {
      throw new RuntimeException("Not expecting owner: " + theOwner);
    }
    this.member = theMember;
  
    if (field.isGroupAccessField()) {
      this.membershipOwnerType = MembershipOwnerType.groupPrivilege;
    } else if (field.isGroupListField()) {
      this.membershipOwnerType = MembershipOwnerType.list;
    } else if (field.isStemListField()) {
      this.membershipOwnerType = MembershipOwnerType.stemPrivilege;
    } else if (field.isAttributeDefListField()) {
      this.membershipOwnerType = MembershipOwnerType.attributeDefPrivilege;
    } else {
      throw new RuntimeException("Not expecting field: " + field);
    }
    
    this.membershipPaths = new TreeSet<MembershipPath>();

    if (--timeToLive < 0) {
      //we are done
      return;
    }

    //if cant admin the owner group for group privileges, then cant see anything
    if (isAttributeDef && !this.ownerAttributeDef.getPrivilegeDelegate().canHavePrivilege(callingSubject, AttributeDefPrivilege.ATTR_ADMIN.toString(), false)) {
      return;
    }
    if (isGroup && !this.ownerGroup.canHavePrivilege(callingSubject, AccessPrivilege.ADMIN.toString(), false)) {
      return;
    }
    if (isStem && !this.ownerStem.canHavePrivilege(callingSubject, NamingPrivilege.STEM.toString(), false)) {
      return;
    }
    
    //lets get the groupsets
    Set<GroupSet> groupSets = null;
    
    if (isGroup) {
      groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerGroupAndFieldAndMembershipMember(
        this.ownerGroup.getId(), field.getId(), this.member);
    } else if (isStem) {
      groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerStemAndFieldAndMembershipMember(
          this.ownerStem.getId(), field.getId(), this.member);
    } else if (isAttributeDef) {
      groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAttributeDefAndFieldAndMembershipMember(
          this.ownerAttributeDef.getId(), field.getId(), this.member);
    }
  
    //lets get all the groups (not secure for calling user
    final Set<String> groupIds = new HashSet<String>();
    for (GroupSet groupSet : GrouperUtil.nonNull(groupSets)) {
      if (!StringUtils.isBlank(groupSet.getMemberGroupId())) {
        groupIds.add(groupSet.getMemberGroupId());
      }
    }
    
    Set<Group> groups = GrouperUtil.length(groupIds) == 0 ? new HashSet<Group>() 
        : new GroupFinder().assignGroupIds(groupIds).findGroups();
    
    //lets put these in a map
    Map<String, Group> groupIdToGroupMapUnsecure = new HashMap<String, Group>();
    for (Group group : GrouperUtil.nonNull(groups)) {
      groupIdToGroupMapUnsecure.put(group.getId(), group);
    }
    
    //secure group query, see what the subject can READ
    GrouperSession secureSession = GrouperSession.start(callingSubject, false);
    @SuppressWarnings("unchecked")
    Set<Group> groupsSecure = (Set<Group>)GrouperSession.callbackGrouperSession(secureSession, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        GroupFinder groupFinder = new GroupFinder().assignGroupIds(groupIds).assignSubject(callingSubject);
        //this is read since it is more about the groups in groups as members
        groupFinder.assignPrivileges(AccessPrivilege.READ_PRIVILEGES);
        return groupFinder.findGroups();
      }
    });
    GrouperSession.stopQuietly(secureSession);

    Map<String, GroupSet> groupSetIdToGroupSet = new HashMap<String, GroupSet>();
    for (GroupSet groupSet : GrouperUtil.nonNull(groupSets)) {
      groupSetIdToGroupSet.put(groupSet.getId(), groupSet);
    }
  
    //see which ones have immediate memberships (these are the ones we care about).  get the list for this field and members
    MembershipFinder membershipFinder = new MembershipFinder();
    if (isGroup) {
      membershipFinder.addGroupId(this.ownerGroup.getId());

    } else if (isStem) {
      membershipFinder.addStemId(this.ownerStem.getId());
      
    } else if (isAttributeDef) {
      membershipFinder.addAttributeDefId(this.ownerAttributeDef.getId());
    }
    Set<Object[]> membershipGroupMemberSet = membershipFinder
        .assignMembershipType(MembershipType.IMMEDIATE)
        .addField(field).addMemberId(member.getId()).findMembershipsMembers();

    {  
      //also add in the list ones, since a subject could be a member of a group that has a field on something else. 
      //note, you cant do this above since membership finder works on one field type at a time
      Set<Object[]> listMembershipGroupMemberSet = new MembershipFinder().assignGroupIds(groupIds).assignMembershipType(MembershipType.IMMEDIATE)
          .addField(Group.getDefaultList()).addMemberId(member.getId()).findMembershipsMembers();
      
      membershipGroupMemberSet.addAll(listMembershipGroupMemberSet);
    }
    
    //multikey is the groupId/attributeDefId/stemId and fieldId combination
    Set<MultiKey> ownerIdFieldIdWithImmediateMemberships = new HashSet<MultiKey>();
  
    for (Object[] membershipGroupMember : GrouperUtil.nonNull(membershipGroupMemberSet)) {
      String ownerId = null;
        if (membershipGroupMember[1] instanceof Group) {
          ownerId = ((Group)membershipGroupMember[1]).getId();
        } else if (membershipGroupMember[1] instanceof AttributeDef) {
          ownerId = ((AttributeDef)membershipGroupMember[1]).getId();
        } else if (membershipGroupMember[1] instanceof Stem) {
          ownerId = ((Stem)membershipGroupMember[1]).getId();
        } else {
          throw new RuntimeException("Not expecting owner type: " + membershipGroupMember[1]);
        }
        
      String fieldId = ((Membership)membershipGroupMember[0]).getFieldId();
      ownerIdFieldIdWithImmediateMemberships.add(new MultiKey(ownerId, fieldId));
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

        MembershipPathNode membershipPathNode = null;

        if (!StringUtils.isBlank(currentGroupSet.getMemberGroupId())) {
          //if direct, use that field
          if (isGroup && StringUtils.equals(currentGroupSet.getMemberGroupId(), this.ownerGroup.getId())) {
            membershipPathNode = new MembershipPathNode(field, this.ownerGroup);
          } else {
            //else its a member of a group
            Group memberGroup = groupIdToGroupMapUnsecure.get(currentGroupSet.getMemberGroupId());
            allowed = allowed && groupsSecure.contains(memberGroup);
            if (memberGroup.hasComposite()) {
      
              Composite composite = memberGroup.getComposite(true);
              CompositeType compositeType = composite.getType();
              
              Group leftGroup = composite.getLeftGroup();
              Group rightGroup = composite.getRightGroup();
      
              membershipPathNode = new MembershipPathNode(Group.getDefaultList(), memberGroup, compositeType, leftGroup, rightGroup);
      
            } else {
            
              membershipPathNode = new MembershipPathNode(Group.getDefaultList(), memberGroup);
            }
          }
        } else if (isStem && !StringUtils.isBlank(currentGroupSet.getMemberStemId())
            && StringUtils.equals(currentGroupSet.getMemberStemId(), this.ownerStem.getId())) {
          membershipPathNode = new MembershipPathNode(field, this.ownerStem);

        } else if (isAttributeDef && !StringUtils.isBlank(currentGroupSet.getMemberAttrDefId())
            && StringUtils.equals(currentGroupSet.getMemberAttrDefId(), this.ownerAttributeDef.getId())) {
          membershipPathNode = new MembershipPathNode(field, this.ownerAttributeDef);
            
        } else {
          throw new RuntimeException("Not expecting group set: " + groupSet);
        }
        membershipPathNodes.add(membershipPathNode);
        
        //move up the chain
        currentGroupSet = groupSetIdToGroupSet.get(currentGroupSet.getParentId());
  
      }
  
      //if its a composite, we need to deal with the factors
      Group memberGroup = groupIdToGroupMapUnsecure.get(groupSet.getMemberGroupId());
  
      MembershipPath membershipPath = new MembershipPath(this.member, membershipPathNodes, membershipType);
      
      membershipPath.getFields().add(field);
      
      membershipPath.setPathAllowed(allowed);
  
      //is it immediate, or a means to an end?
      
      MultiKey memberIdFieldId = new MultiKey(groupSet.getMemberId(), 
          groupSet.getMemberFieldId());
      
      if (ownerIdFieldIdWithImmediateMemberships.contains(memberIdFieldId)) {
        this.membershipPaths.add(membershipPath);
        
      }
      
      if (memberGroup != null && memberGroup.hasComposite()) {
        
        //lets analyze each factor
        Composite composite = memberGroup.getComposite(true);
        CompositeType compositeType = composite.getType();
        if (compositeType == CompositeType.UNION) {
          
          MembershipPathGroup membershipPathGroupLeft = MembershipPathGroup.analyze(
              composite.getLeftGroup(), theMember, Group.getDefaultList(), callingSubject, timeToLive);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupLeft);
          MembershipPathGroup membershipPathGroupRight = MembershipPathGroup.analyze(
              composite.getRightGroup(), theMember, Group.getDefaultList(), callingSubject, timeToLive);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupRight);
          
        } else if (compositeType == CompositeType.COMPLEMENT) {
  
          MembershipPathGroup membershipPathGroupLeft = MembershipPathGroup.analyze(
              composite.getLeftGroup(), theMember, Group.getDefaultList(), callingSubject, timeToLive);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupLeft);
          
        } else if (compositeType == CompositeType.INTERSECTION) {
          MembershipPathGroup membershipPathGroupLeft = MembershipPathGroup.analyze(
              composite.getLeftGroup(), theMember, Group.getDefaultList(), callingSubject, timeToLive);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupLeft);
          MembershipPathGroup membershipPathGroupRight = MembershipPathGroup.analyze(
              composite.getRightGroup(), theMember, Group.getDefaultList(), callingSubject, timeToLive);
          mergeMembershipPathGroup(membershipPath, membershipPathGroupRight);
          
        }
      }
      
    }
    
  }

  /**
   * analyze the membership/privilege of a member in a stem by various paths
   * @param stem
   * @param member
   * @param field
   * @return the group of paths
   */
  public static MembershipPathGroup analyze(final Stem stem, final Member member, final Field field) {
    final MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    final GrouperSession GROUPER_SESSION = GrouperSession.staticGrouperSession();
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        membershipPathGroup.analyzeHelper(stem, member, field, GROUPER_SESSION.getSubject(), DEFAULT_TIME_TO_LIVE);
        return null;
      }
    });
  
    return membershipPathGroup;
  }

  /**
   * default time to live
   */
  private static final int DEFAULT_TIME_TO_LIVE = 20;
  
  /**
   * analyze the membership/privilege of a member in a attributeDef by various paths
   * @param attributeDef
   * @param member
   * @param field
   * @return the group of paths
   */
  public static MembershipPathGroup analyze(final AttributeDef attributeDef, final Member member, final Field field) {
    final MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    final GrouperSession GROUPER_SESSION = GrouperSession.staticGrouperSession();
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        membershipPathGroup.analyzeHelper(attributeDef, member, field, GROUPER_SESSION.getSubject(), DEFAULT_TIME_TO_LIVE);
        return null;
      }
    });
  
    return membershipPathGroup;
  }

  /**
   * analyze the membership/privilege of a member in a attributeDef by various paths
   * @param attributeDef
   * @param member
   * @param field
   * @param callingSubject
   * @return the group of paths
   */
  public static MembershipPathGroup analyze(final AttributeDef attributeDef, final Member member, final Field field, Subject callingSubject) {
    MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    membershipPathGroup.analyzeHelper(attributeDef, member, field, callingSubject, DEFAULT_TIME_TO_LIVE);
    return membershipPathGroup;
  }

  /**
   * analyze the membership/privilege of a member in a stem by various paths
   * @param stem
   * @param member
   * @param field
   * @param callingSubject
   * @return the group of paths
   */
  public static MembershipPathGroup analyze(final Stem stem, final Member member, final Field field, Subject callingSubject) {
    MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    membershipPathGroup.analyzeHelper(stem, member, field, callingSubject, DEFAULT_TIME_TO_LIVE);
    return membershipPathGroup;
  }

  /**
   * analyze stem privileges for a stem and a member
   * @param stem
   * @param member
   * @return the membershipPathGroup
   */
  public static MembershipPathGroup analyzePrivileges(final Stem stem, final Member member) {
    
    final MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    final GrouperSession GROUPER_SESSION = GrouperSession.staticGrouperSession();
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        membershipPathGroup.analyzePrivilegesHelper(stem, member, GROUPER_SESSION.getSubject());
        return null;
      }
    });
  
    return membershipPathGroup;
  
    
  }

  /**
   * analyze the privileges of a member in a stem by various paths
   * @param stem
   * @param member
   * @param callingSubject
   * @return the group of paths
   */
  public static MembershipPathGroup analyzePrivileges(final Stem stem, final Member member, Subject callingSubject) {
    MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    membershipPathGroup.analyzePrivilegesHelper(stem, member, callingSubject);
    return membershipPathGroup;
  }

  /**
   * analyze attributeDef privileges for a attributeDef and a member
   * @param attributeDef
   * @param member
   * @return the membershipPathGroup
   */
  public static MembershipPathGroup analyzePrivileges(final AttributeDef attributeDef, final Member member) {
    
    final MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    final GrouperSession GROUPER_SESSION = GrouperSession.staticGrouperSession();
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        membershipPathGroup.analyzePrivilegesHelper(attributeDef, member, GROUPER_SESSION.getSubject());
        return null;
      }
    });
  
    return membershipPathGroup;
  
    
  }

  /**
   * analyze the privileges of a member in a attributeDef by various paths
   * @param attributeDef
   * @param member
   * @param callingSubject
   * @return the group of paths
   */
  public static MembershipPathGroup analyzePrivileges(final AttributeDef attributeDef, final Member member, Subject callingSubject) {
    MembershipPathGroup membershipPathGroup = new MembershipPathGroup();
    membershipPathGroup.analyzePrivilegesHelper(attributeDef, member, callingSubject);
    return membershipPathGroup;
  }


}
