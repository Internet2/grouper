/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class CustomUiGrouper extends CustomUiUserQueryBase {

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject1 = SubjectFinder.findById("10021368", true);
    Subject subject2 = SubjectFinder.findById("13228666", true);
    Subject subject3 = SubjectFinder.findById("10002177", true);
    Subject subject4 = SubjectFinder.findById("15251428", true);
    
    Group group = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod", true);
    
    CustomUiGrouper customUiGrouper = new CustomUiGrouper();
    
    for (Subject subject : new Subject[]{subject1, subject2, subject3, subject4}) {
      boolean hasMembership = customUiGrouper.hasGrouperMembership(group, subject);
          
      System.out.println(hasMembership);
      
      boolean hasPrivilege = customUiGrouper.hasDirectGrouperGroupPrivilege(group, subject, "read");
      
      System.out.println(hasPrivilege);

      boolean canPrivilege = customUiGrouper.canHaveGroupPrivilege(group, subject, "read");
      
      System.out.println(canPrivilege);
    }
  }
  
  /**
   * cache of memberships for user
   */
  private Map<MultiKey, Group> membershipGroupNameSourceIdSubjectIdToGroupMap = new HashMap<MultiKey, Group>();
  
  
  /**
   * @return the membershipGroupNameSourceIdSubjectIdToGroupMap
   */
  public Map<MultiKey, Group> getMembershipGroupNameSourceIdSubjectIdToGroupMap() {
    return this.membershipGroupNameSourceIdSubjectIdToGroupMap;
  }


  
  
  /**
   * @return the stemIdAndNameToStem
   */
  public Map<String, Stem> getStemIdAndNameToStem() {
    return this.stemIdAndNameToStem;
  }



  
  /**
   * @return the attributeDefIdAndNameToAttributeDef
   */
  public Map<String, AttributeDef> getAttributeDefIdAndNameToAttributeDef() {
    return this.attributeDefIdAndNameToAttributeDef;
  }



  /**
   * @return the groupIdAndNameToGroup
   */
  public Map<String, Group> getGroupIdAndNameToGroup() {
    return this.groupIdAndNameToGroup;
  }


  /**
   * 
   * @param stemNames
   * @param subject
   */
  public void cacheMembershipsInStem(final String stemNames, final Subject subject) {
    long startedNanos = System.nanoTime();

    try {
      
      String[] stemNamesArray = GrouperUtil.splitTrim(stemNames, ",");

      this.debugMapPut("cacheMemStems", stemNamesArray.length);

      for (int i=0;i<stemNamesArray.length;i++) {
        final String stemName = stemNamesArray[0];
        final String stemExtension = GrouperUtil.extensionFromName(stemName);
        
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
    
          public Object callback(GrouperSession grouperSession)
              throws GrouperSessionException {
            
            Stem stem = StemFinder.findByName(grouperSession, stemName, true);
            Member member = MemberFinder.findBySubject(grouperSession, subject, true);
            
            MembershipResult membershipResult = new MembershipFinder().assignStem(stem).assignStemScope(Scope.SUB).assignEnabled(true)
                .addField(Group.getDefaultList()).addMemberId(member.getId()).findMembershipResult();
  
            int foundCount = 0;
            
            for (MembershipSubjectContainer membershipSubjectContainer : membershipResult.getMembershipSubjectContainers()) {
              
              CustomUiGrouper.this.membershipGroupNameSourceIdSubjectIdToGroupMap.put(
                  new MultiKey(membershipSubjectContainer.getGroupOwner().getName(), subject.getSourceId(), subject.getId()), 
                  membershipSubjectContainer.getGroupOwner());
              foundCount++;
            }
            
            CustomUiGrouper.this.debugMapPut(stemExtension + "_cacheMemStemCount", foundCount);
            return null;
          }
          
        });
      }      
    } catch (RuntimeException re) {
      
      this.debugMapPut("cacheMemStemError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("cacheMemStemMillis", (System.nanoTime()-startedNanos)/1000000);
    }


  }
  
  /**
   * cache group id and name to group
   */
  private Map<String, Group> groupIdAndNameToGroup = new HashMap<String, Group>();
  
  /**
   * 
   * @param groupIdsAndNames 
   */
  public void cacheGroups(List<MultiKey> groupIdsAndNames) {
    if (GrouperUtil.length(groupIdsAndNames) == 0) {
      return;
    }

    long startedNanos = System.nanoTime();

    try {
      for (MultiKey groupIdAndName : GrouperUtil.nonNull(groupIdsAndNames)) {
  
        String groupId = (String)groupIdAndName.getKey(0);
        String groupName = (String)groupIdAndName.getKey(1);
        
        if (StringUtils.isBlank(groupId) && StringUtils.isBlank(groupName)) {
          continue;
        }
        Group group = null;
        if (!StringUtils.isBlank(groupId)) {
          group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false);
        } else if (!StringUtils.isBlank(groupName)) {
          group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
        }
        if (group != null) {
          groupIdAndNameToGroup.put(group.getId(), group);
          groupIdAndNameToGroup.put(group.getName(), group);
        }

      }
      
      this.debugMapPut("groupsFound", groupIdAndNameToGroup.size());

    } catch (RuntimeException re) {
      
      this.debugMapPut("cacheGroupError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("cacheGroupMillis", (System.nanoTime()-startedNanos)/1000000);
    }
  }
  

  /**
   * 
   * @param groupNames
   * @param subject
   */
  public void cacheMembershipsInGroups(final Set<String> groupNames, final Subject subject) {

    long startedNanos = System.nanoTime();

    try {
      
      this.debugMapPut("cacheMemGroupsToCheck", GrouperUtil.length(groupNames));

      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          Member member = MemberFinder.findBySubject(grouperSession, subject, true);
          
          MembershipFinder membershipFinder = new MembershipFinder().assignEnabled(true)
              .addField(Group.getDefaultList()).addMemberId(member.getId());
          
          for (String groupName : groupNames) {
            membershipFinder.addGroup(groupName);
          }
          
          MembershipResult membershipResult = membershipFinder.findMembershipResult();

          int foundCount = 0;

          for (MembershipSubjectContainer membershipSubjectContainer : membershipResult.getMembershipSubjectContainers()) {
            
            CustomUiGrouper.this.membershipGroupNameSourceIdSubjectIdToGroupMap.put(
                new MultiKey(membershipSubjectContainer.getGroupOwner().getName(), subject.getSourceId(), subject.getId()), 
                membershipSubjectContainer.getGroupOwner());
            foundCount++;
            
          }

          CustomUiGrouper.this.debugMapPut("cacheMemGroupsCount", foundCount);

          return null;
        }
        
      });
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("cacheMemGroupsError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("cacheMemGroupsMillis", (System.nanoTime()-startedNanos)/1000000);
    }


  }
    
  /**
   * 
   * @param group
   * @param subject
   * @return true if has membership
   */
  public boolean hasGrouperMembership(final Group group, final Subject subject) {

    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          return group.hasMember(subject);
        }
        
      });
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("hasGrouperMship", GrouperUtil.getFullStackTrace(re));
      throw re;

    }

  }
  
  /**
   * 
   * @param group
   * @param subject
   * @param fieldName 
   * @return true if has membership
   */
  public boolean hasDirectGrouperGroupPrivilege(final Group group, final Subject subject, final String fieldName) {

    long startedNanos = System.nanoTime();

    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return group.hasPrivilege(subject, fieldName);
          
        }
        
      });
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("hasPrivilege", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("hasPrivTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }

  }
  
  /**
   * 
   * @param group
   * @param subject
   * @param fieldName 
   * @return true if has membership
   */
  public boolean canHaveGroupPrivilege(final Group group, final Subject subject, final String fieldName) {
    long startedNanos = System.nanoTime();

    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return group.canHavePrivilege(subject, fieldName, false);
          
        }
        
      });
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("canHavePrivError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("canHavePrivTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }

  }

  /**
   * 
   * @param stem
   * @param subject
   * @param fieldName 
   * @return true if has membership
   */
  public boolean canHaveStemPrivilege(final Stem stem, final Subject subject, final String fieldName) {
    long startedNanos = System.nanoTime();

    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return stem.canHavePrivilege(subject, fieldName, false);
          
        }
        
      });
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("canHavePrivError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("canHavePrivTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }

  }

  /**
   * reference back up to engine
   */
  private CustomUiEngine customUiEngine;
  
  /**
   * cache stem id and name to stem
   */
  private Map<String, Stem> stemIdAndNameToStem = new HashMap<String, Stem>();

  /**
   * cache attributeDef id and name to attributeDef
   */
  private Map<String, AttributeDef> attributeDefIdAndNameToAttributeDef = new HashMap<String, AttributeDef>();
  
  /**
   * 
   */
  public CustomUiGrouper() {
  }

  /**
   * reference back up to engine
   * @return the customUiEngine
   */
  public CustomUiEngine getCustomUiEngine() {
    return this.customUiEngine;
  }

  /**
   * reference back up to engine
   * @param customUiEngine the customUiEngine to set
   */
  public void setCustomUiEngine(CustomUiEngine customUiEngine) {
    this.customUiEngine = customUiEngine;
  }
  /**
   * 
   * @param attributeDef
   * @param subject
   * @param fieldName 
   * @return true if has membership
   */
  public boolean canHaveAttributeDefPrivilege(final AttributeDef attributeDef, final Subject subject, final String fieldName) {
    long startedNanos = System.nanoTime();
  
    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return attributeDef.getPrivilegeDelegate().canHavePrivilege(subject, fieldName, false);
          
        }
        
      });
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("canHavePrivError", GrouperUtil.getFullStackTrace(re));
      throw re;
  
    } finally {
      this.debugMapPut("canHavePrivTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }
  
  }
  /**
   * 
   * @param stem
   * @param subject
   * @param fieldName 
   * @return true if has membership
   */
  public boolean hasDirectGrouperStemPrivilege(final Stem stem, final Subject subject, final String fieldName) {
  
    long startedNanos = System.nanoTime();
  
    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return stem.hasPrivilege(subject, fieldName);
          
        }
        
      });
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("hasPrivilege", GrouperUtil.getFullStackTrace(re));
      throw re;
  
    } finally {
      this.debugMapPut("hasPrivTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }
  
  }
  /**
   * 
   * @param attributeDef
   * @param subject
   * @param fieldName 
   * @return true if has membership
   */
  public boolean hasDirectGrouperAttributeDefPrivilege(final AttributeDef attributeDef, final Subject subject, final String fieldName) {
  
    long startedNanos = System.nanoTime();
  
    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return attributeDef.getPrivilegeDelegate().hasPrivilege(subject, fieldName);
          
        }
        
      });
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("hasPrivilege", GrouperUtil.getFullStackTrace(re));
      throw re;
  
    } finally {
      this.debugMapPut("hasPrivTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }
  
  }


  /**
   * 
   * @param stemIdsAndNames 
   */
  public void cacheStems(List<MultiKey> stemIdsAndNames) {
    if (GrouperUtil.length(stemIdsAndNames) == 0) {
      return;
    }
    long startedNanos = System.nanoTime();
  
    try {
      for (MultiKey stemIdAndName : GrouperUtil.nonNull(stemIdsAndNames)) {
  
        String stemId = (String)stemIdAndName.getKey(0);
        String stemName = (String)stemIdAndName.getKey(1);
        
        if (StringUtils.isBlank(stemId) && StringUtils.isBlank(stemName)) {
          continue;
        }
        Stem stem = null;
        if (!StringUtils.isBlank(stemId)) {
          stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, false);
        } else if (!StringUtils.isBlank(stemName)) {
          stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), stemName, false);
        }
        if (stem != null) {
          stemIdAndNameToStem.put(stem.getId(), stem);
          stemIdAndNameToStem.put(stem.getName(), stem);
        }
  
      }
      
      this.debugMapPut("stemsFound", stemIdAndNameToStem.size());
  
    } catch (RuntimeException re) {
      
      this.debugMapPut("cacheStemError", GrouperUtil.getFullStackTrace(re));
      throw re;
  
    } finally {
      this.debugMapPut("cacheStemMillis", (System.nanoTime()-startedNanos)/1000000);
    }
  }


  /**
   * 
   * @param attributeDefIdsAndNames 
   */
  public void cacheAttributeDefs(List<MultiKey> attributeDefIdsAndNames) {
  
    if (GrouperUtil.length(attributeDefIdsAndNames) == 0) {
      return;
    }

    long startedNanos = System.nanoTime();
  
    try {
      for (MultiKey attributeDefIdAndName : GrouperUtil.nonNull(attributeDefIdsAndNames)) {
  
        String attributeDefId = (String)attributeDefIdAndName.getKey(0);
        String nameOfAttributeDef = (String)attributeDefIdAndName.getKey(1);
        
        if (StringUtils.isBlank(attributeDefId) && StringUtils.isBlank(nameOfAttributeDef)) {
          continue;
        }
        AttributeDef attributeDef = null;
        if (!StringUtils.isBlank(attributeDefId)) {
          attributeDef = AttributeDefFinder.findById(attributeDefId, false);
        } else if (!StringUtils.isBlank(nameOfAttributeDef)) {
          attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
        }
        if (attributeDef != null) {
          attributeDefIdAndNameToAttributeDef.put(attributeDef.getId(), attributeDef);
          attributeDefIdAndNameToAttributeDef.put(attributeDef.getName(), attributeDef);
        }
  
      }
      
      this.debugMapPut("attributeDefsFound", attributeDefIdAndNameToAttributeDef.size());
  
    } catch (RuntimeException re) {
      
      this.debugMapPut("cacheAttributeDefError", GrouperUtil.getFullStackTrace(re));
      throw re;
  
    } finally {
      this.debugMapPut("cacheattributeDefMillis", (System.nanoTime()-startedNanos)/1000000);
    }
  }

}
