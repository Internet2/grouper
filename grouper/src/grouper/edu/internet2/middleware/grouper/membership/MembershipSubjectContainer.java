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
/**
 * @author mchyzer
 * $Id: PrivilegeSubjectContainerImpl.java 8245 2012-04-24 13:45:50Z mchyzer $
 */
package edu.internet2.middleware.grouper.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.subj.SubjectBean;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * in a list of memberships, this is one subject.  this only works when filtering for one owner
 */
public class MembershipSubjectContainer {

  /**
   * consider inheritance in one group
   */
  public void considerAccessPrivilegeInheritance() {
    Set<MembershipSubjectContainer> set = GrouperUtil.toSet(this);
    considerAccessPrivilegeInheritance(set);
  }

  /**
   * if there is a non immediate in all the fields
   * @return true if has non immediate
   */
  public boolean isHasNonImmediate() {
    for (MembershipContainer membershipContainer : GrouperUtil.nonNull(this.membershipContainers).values()) {
      if (membershipContainer.getMembershipAssignType().isNonImmediate()) {
        return true;
      }
    }
    return false;
  }

  /**
   * if update exists for this row
   * @return true if update exists for this row
   */
  public boolean isHasUpdate() {

    return GrouperUtil.nonNull(this.membershipContainers).get(Field.FIELD_NAME_UPDATERS) != null;
      
  }
  
  /**
   * if optout exists for this row
   * @return true if optout exists for this row
   */
  public boolean isHasOptout() {

    return GrouperUtil.nonNull(this.membershipContainers).get(Field.FIELD_NAME_OPTOUTS) != null;
      
  }
  

  /**
   * group owner of this memberships
   */
  private Group groupOwner;
  
  /**
   * stem owner of this memberships
   */
  private Stem stemOwner;
  
  /**
   * stem owner of this memberships
   * @return stem owner
   */
  public Stem getStemOwner() {
    return this.stemOwner;
  }

  /**
   * stem owner of this memberships
   * @param stemOwner1
   */
  public void setStemOwner(Stem stemOwner1) {
    this.stemOwner = stemOwner1;
  }

  /**
   * attribute def owner of membership
   */
  private AttributeDef attributeDefOwner;
  
  /**
   * attribute def owner of membership
   * @return attribute def
   */
  public AttributeDef getAttributeDefOwner() {
    return this.attributeDefOwner;
  }

  /**
   * attribute def owner of membership
   * @param attributeDefOwner1
   */
  public void setAttributeDefOwner(AttributeDef attributeDefOwner1) {
    this.attributeDefOwner = attributeDefOwner1;
  }

  /**
   * group owner of this memberships
   * @return group or null
   */
  public Group getGroupOwner() {
    return this.groupOwner;
  }

  /**
   * group owner of this memberships
   * @param groupOwner1
   */
  public void setGroupOwner(Group groupOwner1) {
    this.groupOwner = groupOwner1;
  }

  /** subject */
  private Subject subject;

  /**
   * add effective memberships for inheritance of privileges or 
   * GrouperAll for stem
   * @param membershipSubjectContainers
   */
  public static void considerNamingPrivilegeInheritance(final Set<MembershipSubjectContainer> membershipSubjectContainers) {

    Set<Stem> stems = new HashSet<Stem>();
    
    //get the list of groups
    for (MembershipSubjectContainer membershipSubjectContainer : membershipSubjectContainers) {
      Stem stem = membershipSubjectContainer.getStemOwner();
      
      //note not sure why this would be null, these should be stem owned memberships
      if (stem != null) {
        stems.add(stem);
      }
    }
    
    Map<MultiKey, Boolean> stemIdPermissionNameAllowedForGrouperAll = new HashMap<MultiKey, Boolean>();

    for (Stem stem : stems) {

      boolean grouperAllHasStem = stem.hasStem(SubjectFinder.findAllSubject());
      stemIdPermissionNameAllowedForGrouperAll.put(new MultiKey(stem.getId(), NamingPrivilege.STEM.getName()), grouperAllHasStem);
      
      boolean grouperAllHasCreate = grouperAllHasStem || stem.hasCreate(SubjectFinder.findAllSubject());
      stemIdPermissionNameAllowedForGrouperAll.put(new MultiKey(stem.getId(), NamingPrivilege.CREATE.getName()), grouperAllHasCreate);

      boolean grouperAllHasAttrRead = grouperAllHasStem || grouperAllHasCreate || stem.hasStemAttrRead(SubjectFinder.findAllSubject());
      stemIdPermissionNameAllowedForGrouperAll.put(new MultiKey(stem.getId(), NamingPrivilege.STEM_ATTR_READ.getName()), grouperAllHasAttrRead);

      boolean grouperAllHasAttrUpdate = grouperAllHasStem || grouperAllHasCreate || stem.hasStemAttrUpdate(SubjectFinder.findAllSubject());
      stemIdPermissionNameAllowedForGrouperAll.put(new MultiKey(stem.getId(), NamingPrivilege.STEM_ATTR_UPDATE.getName()), grouperAllHasAttrUpdate);
    }
    
    Set<String> stemFieldNames = GrouperUtil.toSet(Field.FIELD_NAME_STEMMERS, Field.FIELD_NAME_CREATORS, 
        Field.FIELD_NAME_STEM_ATTR_READERS, Field.FIELD_NAME_STEM_ATTR_UPDATERS);
    
    Subject rootSubject = SubjectFinder.findRootSubject();
    Subject everyEntitySubject = SubjectFinder.findAllSubject();
    
    for (MembershipSubjectContainer membershipSubjectContainer : membershipSubjectContainers) {
      
      Stem stem = membershipSubjectContainer.getStemOwner();
      
      boolean grouperAllHasStem = stemIdPermissionNameAllowedForGrouperAll.get(new MultiKey(stem.getId(), NamingPrivilege.STEM.getName()));
      boolean grouperAllHasCreate = stemIdPermissionNameAllowedForGrouperAll.get(new MultiKey(stem.getId(), NamingPrivilege.CREATE.getName()));
      boolean grouperAllHasAttrRead = stemIdPermissionNameAllowedForGrouperAll.get(new MultiKey(stem.getId(), NamingPrivilege.STEM_ATTR_READ.getName()));
      boolean grouperAllHasAttrUpdate = stemIdPermissionNameAllowedForGrouperAll.get(new MultiKey(stem.getId(), NamingPrivilege.STEM_ATTR_UPDATE.getName()));
      
      Subject subject = membershipSubjectContainer.getSubject();
      
      //if we are on grouper system
      if (SubjectHelper.eq(subject, rootSubject)) {
        
        for (String fieldName : stemFieldNames) {
          //it is also effective, merge that with whatever was there
          membershipSubjectContainer.addMembership(fieldName, MembershipAssignType.EFFECTIVE);
        }
        
      } else {
        //else
        boolean isEveryEntity = SubjectHelper.eq(everyEntitySubject, membershipSubjectContainer.getSubject());
        
        //see what the subject has
        boolean subjectHasStemEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_STEMMERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_STEMMERS).getMembershipAssignType().isNonImmediate()
            || (isEveryEntity ? false : grouperAllHasStem);

        boolean subjectHasStem = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_STEMMERS) != null 
            || subjectHasStemEffective || grouperAllHasStem;
        
        boolean subjectHasCreateEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_CREATORS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_CREATORS).getMembershipAssignType().isNonImmediate()
            || subjectHasStem || (isEveryEntity ? false : grouperAllHasCreate);
        
        boolean subjectHasCreate = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_CREATORS) != null 
            || subjectHasCreateEffective || grouperAllHasCreate;
        
        boolean subjectHasAttrReadEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_STEM_ATTR_READERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_STEM_ATTR_READERS).getMembershipAssignType().isNonImmediate()
            || subjectHasCreate || subjectHasStem || (isEveryEntity ? false : grouperAllHasAttrRead);
        
        boolean subjectHasAttrUpdateEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_STEM_ATTR_UPDATERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_STEM_ATTR_UPDATERS).getMembershipAssignType().isNonImmediate()
            || subjectHasCreate || subjectHasStem  || (isEveryEntity ? false : grouperAllHasAttrUpdate);

        //if the subject has an effective stem priv, add it in
        if (subjectHasStemEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_STEMMERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasCreateEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_CREATORS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasAttrReadEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_STEM_ATTR_READERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasAttrUpdateEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_STEM_ATTR_UPDATERS, MembershipAssignType.EFFECTIVE);
        }
        
      }
    }
  }

  /**
   * consider a new membership
   * @param fieldName
   * @param membershipAssignType
   */
  public void addMembership(String fieldName, MembershipAssignType newMembershipAssignType) {
    MembershipContainer membershipContainer = this.getMembershipContainers().get(fieldName);

    if(membershipContainer != null) {
      //maybe we need to change or set the type
      newMembershipAssignType = MembershipAssignType.convert(membershipContainer.getMembershipAssignType(),newMembershipAssignType);
      membershipContainer.setMembershipAssignType(newMembershipAssignType);
    } else {
      //create a new one where one didnt exist before
      membershipContainer = new MembershipContainer(fieldName, newMembershipAssignType);
      this.getMembershipContainers().put(fieldName, membershipContainer);
    }
  }
  
  /**
   * member
   */
  private Member member;
  
  /**
   * member
   * @return the member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * member
   * @param member1
   */
  public void setMember(Member member1) {
    this.member = member1;
  }

  /**
   * membership containers for field
   */
  private Map<String, MembershipContainer> membershipContainers;
  
  /**
   * @see edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer#getPrivilegeContainers()
   */
  public Map<String, MembershipContainer> getMembershipContainers() {
    return this.membershipContainers;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer#getSubject()
   */
  public Subject getSubject() {
    return this.subject;
  }

  
  /**
   * @param subject1 the subject to set
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }

  
  /**
   * @param privilegeContainers1 the privilegeContainers to set
   */
  public void setMembershipContainers(Map<String, MembershipContainer> privilegeContainers1) {
    this.membershipContainers = privilegeContainers1;
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    
    if (this.groupOwner != null) {
      result.append("Group: ");
      result.append(this.groupOwner.getName());
      result.append(", ");
    }
    
    if (this.stemOwner != null) {
      result.append("Stem: ");
      result.append(this.stemOwner.getName());
      result.append(", ");
    }
    
    if (this.attributeDefOwner != null) {
      result.append("Attribute def: ");
      result.append(this.attributeDefOwner.getName());
      result.append(", ");
    }
    
    if (this.subject == null) {
      result.append("Subject: null");
    } else {
      result.append(GrouperUtil.subjectToString(this.subject));
    }
    result.append(": ");
    if (GrouperUtil.length(this.membershipContainers) == 0) {
      result.append(" no memberships");
    } else {
      
      Set<String> fieldNameSet = this.membershipContainers.keySet();
      int index = 0;
      for (String fieldName: fieldNameSet) {
        result.append(this.membershipContainers.get(fieldName));
        if (index < fieldNameSet.size()-1) {
          result.append(", ");
        }
        index++;
      }
    }
    return result.toString();
  }
  
  /**
   * add effective memberships for inheritance of privileges or 
   * GrouperAll for attributeDef
   * @param membershipSubjectContainers
   */
  public static void considerAttributeDefPrivilegeInheritance(final Set<MembershipSubjectContainer> membershipSubjectContainers) {

    Set<AttributeDef> attributeDefs = new HashSet<AttributeDef>();
    
    //get the list of attribute defs
    for (MembershipSubjectContainer membershipSubjectContainer : membershipSubjectContainers) {
      AttributeDef attributeDef = membershipSubjectContainer.getAttributeDefOwner();
      
      //note, not sure why this would be null, these should be attribute def owned memberships
      if (attributeDef != null) {
        attributeDefs.add(attributeDef);
      }
    }
    
    Map<MultiKey, Boolean> attributeDefIdPermissionNameAllowedForGrouperAll = new HashMap<MultiKey, Boolean>();

    for (AttributeDef attributeDef : attributeDefs) {
      
      boolean grouperAllHasAdmin = attributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectFinder.findAllSubject());
      attributeDefIdPermissionNameAllowedForGrouperAll.put(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_ADMIN.getName()), grouperAllHasAdmin);
      
      boolean grouperAllHasUpdate = grouperAllHasAdmin || attributeDef.getPrivilegeDelegate().hasAttrUpdate(SubjectFinder.findAllSubject());
      attributeDefIdPermissionNameAllowedForGrouperAll.put(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_UPDATE.getName()), grouperAllHasUpdate);

      boolean grouperAllHasRead = grouperAllHasAdmin || attributeDef.getPrivilegeDelegate().hasAttrRead(SubjectFinder.findAllSubject());
      attributeDefIdPermissionNameAllowedForGrouperAll.put(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_READ.getName()), grouperAllHasRead);

      boolean grouperAllHasOptin = grouperAllHasAdmin || attributeDef.getPrivilegeDelegate().hasAttrOptin(SubjectFinder.findAllSubject());
      attributeDefIdPermissionNameAllowedForGrouperAll.put(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_OPTIN.getName()), grouperAllHasOptin);

      boolean grouperAllHasOptout = grouperAllHasAdmin || attributeDef.getPrivilegeDelegate().hasAttrOptout(SubjectFinder.findAllSubject());
      attributeDefIdPermissionNameAllowedForGrouperAll.put(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_OPTOUT.getName()), grouperAllHasOptout);

      boolean grouperAllHasAttrRead = grouperAllHasAdmin || attributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(SubjectFinder.findAllSubject());
      attributeDefIdPermissionNameAllowedForGrouperAll.put(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_DEF_ATTR_READ.getName()), grouperAllHasAttrRead);

      boolean grouperAllHasAttrUpdate = grouperAllHasAdmin || attributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(SubjectFinder.findAllSubject());
      attributeDefIdPermissionNameAllowedForGrouperAll.put(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE.getName()), grouperAllHasAttrUpdate);

      boolean grouperAllHasView = grouperAllHasAdmin || grouperAllHasUpdate || grouperAllHasRead 
          || grouperAllHasOptin || grouperAllHasOptout || grouperAllHasAttrRead || grouperAllHasAttrUpdate
          || attributeDef.getPrivilegeDelegate().hasAttrView(SubjectFinder.findAllSubject());
      attributeDefIdPermissionNameAllowedForGrouperAll.put(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_VIEW.getName()), grouperAllHasView);

    }
    
    
    Set<String> groupFieldNames = GrouperUtil.toSet(Field.FIELD_NAME_ATTR_ADMINS, Field.FIELD_NAME_ATTR_UPDATERS, 
        Field.FIELD_NAME_ATTR_DEF_ATTR_READERS, Field.FIELD_NAME_ATTR_DEF_ATTR_UPDATERS,
        Field.FIELD_NAME_ATTR_READERS, Field.FIELD_NAME_ATTR_OPTINS, Field.FIELD_NAME_ATTR_OPTOUTS, Field.FIELD_NAME_ATTR_VIEWERS);
    
    Subject rootSubject = SubjectFinder.findRootSubject();
    Subject everyEntitySubject = SubjectFinder.findAllSubject();
    
    for (MembershipSubjectContainer membershipSubjectContainer : membershipSubjectContainers) {
      AttributeDef attributeDef = membershipSubjectContainer.getAttributeDefOwner();
      boolean grouperAllHasAdmin = attributeDefIdPermissionNameAllowedForGrouperAll.get(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_ADMIN.getName()));
      boolean grouperAllHasUpdate = attributeDefIdPermissionNameAllowedForGrouperAll.get(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_UPDATE.getName()));
      boolean grouperAllHasRead = attributeDefIdPermissionNameAllowedForGrouperAll.get(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_READ.getName()));
      boolean grouperAllHasOptin = attributeDefIdPermissionNameAllowedForGrouperAll.get(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_OPTIN.getName()));
      boolean grouperAllHasOptout = attributeDefIdPermissionNameAllowedForGrouperAll.get(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_OPTOUT.getName()));
      boolean grouperAllHasAttrRead = attributeDefIdPermissionNameAllowedForGrouperAll.get(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_DEF_ATTR_READ.getName()));
      boolean grouperAllHasAttrUpdate = attributeDefIdPermissionNameAllowedForGrouperAll.get(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE.getName()));
      boolean grouperAllHasView = attributeDefIdPermissionNameAllowedForGrouperAll.get(new MultiKey(attributeDef.getId(), AttributeDefPrivilege.ATTR_VIEW.getName()));;

      Subject subject = membershipSubjectContainer.getSubject();
      
      //if we are on grouper system
      if (SubjectHelper.eq(subject, rootSubject)) {
        
        for (String fieldName : groupFieldNames) {
          //it is also effective, merge that with whatever was there
          membershipSubjectContainer.addMembership(fieldName, MembershipAssignType.EFFECTIVE);
        }
        
      } else {
        //else
        boolean isEveryEntity = SubjectHelper.eq(everyEntitySubject, membershipSubjectContainer.getSubject());
        
        //see what the subject has
        boolean subjectHasAdminEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_ADMINS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_ADMINS).getMembershipAssignType().isNonImmediate()
            || (isEveryEntity ? false : grouperAllHasAdmin);

        boolean subjectHasAdmin = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_ADMINS) != null 
            || subjectHasAdminEffective || grouperAllHasAdmin;
        
        boolean subjectHasUpdateEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || (isEveryEntity ? false : grouperAllHasUpdate);
        
        boolean subjectHasUpdate = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_UPDATERS) != null 
            || subjectHasUpdateEffective || grouperAllHasUpdate;
        
        boolean subjectHasReadEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_READERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_READERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || (isEveryEntity ? false : grouperAllHasRead);
        
        boolean subjectHasRead = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_READERS) != null 
            || subjectHasReadEffective || grouperAllHasRead;
        
        boolean subjectHasOptinEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_OPTINS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_OPTINS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || subjectHasUpdate || (isEveryEntity ? false : grouperAllHasOptin);
        
        boolean subjectHasOptin = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_OPTINS) != null 
            || subjectHasOptinEffective || grouperAllHasOptin;
        
        boolean subjectHasOptoutEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_OPTOUTS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_OPTOUTS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || subjectHasUpdate || (isEveryEntity ? false : grouperAllHasOptout);
        
        boolean subjectHasOptout = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_OPTOUTS) != null 
            || subjectHasReadEffective || grouperAllHasOptout;

        boolean subjectHasAttrReadEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_DEF_ATTR_READERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_DEF_ATTR_READERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || (isEveryEntity ? false : grouperAllHasAttrRead);

        boolean subjectHasAttrRead = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_DEF_ATTR_READERS) != null 
            || subjectHasAttrReadEffective || grouperAllHasAttrRead;

        boolean subjectHasAttrUpdateEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_DEF_ATTR_UPDATERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_DEF_ATTR_UPDATERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin  || (isEveryEntity ? false : grouperAllHasAttrUpdate);

        boolean subjectHasAttrUpdate = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_DEF_ATTR_UPDATERS) != null 
            || subjectHasAttrUpdateEffective || grouperAllHasAttrUpdate;
        
        boolean subjectHasViewEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_VIEWERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ATTR_VIEWERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || subjectHasUpdate || subjectHasRead || subjectHasOptout || subjectHasOptin
            || subjectHasAttrUpdate || subjectHasAttrRead || (isEveryEntity ? false : grouperAllHasView);


        //if the subject has an effective stem priv, add it in
        if (subjectHasAdminEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ATTR_ADMINS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasUpdateEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ATTR_UPDATERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasReadEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ATTR_READERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasViewEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ATTR_VIEWERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasOptinEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ATTR_OPTINS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasOptoutEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ATTR_OPTOUTS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasAttrReadEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ATTR_DEF_ATTR_READERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasAttrUpdateEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ATTR_DEF_ATTR_UPDATERS, MembershipAssignType.EFFECTIVE);
        }
      }  
    }
  }

  /**
   * add effective memberships for inheritance of privileges or 
   * GrouperAll for group
   * @param membershipSubjectContainers
   */
  public static void considerAccessPrivilegeInheritance(final Set<MembershipSubjectContainer> membershipSubjectContainers) {

    Set<Group> groups = new HashSet<Group>();
    
    //get the list of groups
    for (MembershipSubjectContainer membershipSubjectContainer : membershipSubjectContainers) {
      Group group = membershipSubjectContainer.getGroupOwner();
      //note, not sure why it would be null, these should be group owned memberships
      if (group != null) {
        groups.add(group);
      }
    }
    
    Map<MultiKey, Boolean> groupIdPermissionNameAllowedForGrouperAll = new HashMap<MultiKey, Boolean>();

    for (Group group : groups) {
      
      boolean grouperAllHasAdmin = false;
      groupIdPermissionNameAllowedForGrouperAll.put(new MultiKey(group.getId(), AccessPrivilege.ADMIN.getName()), grouperAllHasAdmin);
      
      boolean grouperAllHasUpdate = false;
      groupIdPermissionNameAllowedForGrouperAll.put(new MultiKey(group.getId(), AccessPrivilege.UPDATE.getName()), grouperAllHasUpdate);

      boolean grouperAllHasRead = grouperAllHasAdmin || group.hasRead(SubjectFinder.findAllSubject());
      groupIdPermissionNameAllowedForGrouperAll.put(new MultiKey(group.getId(), AccessPrivilege.READ.getName()), grouperAllHasRead);

      boolean grouperAllHasOptin = grouperAllHasAdmin || group.hasOptin(SubjectFinder.findAllSubject());
      groupIdPermissionNameAllowedForGrouperAll.put(new MultiKey(group.getId(), AccessPrivilege.OPTIN.getName()), grouperAllHasOptin);

      boolean grouperAllHasOptout = grouperAllHasAdmin || group.hasOptout(SubjectFinder.findAllSubject());
      groupIdPermissionNameAllowedForGrouperAll.put(new MultiKey(group.getId(), AccessPrivilege.OPTOUT.getName()), grouperAllHasOptout);

      boolean grouperAllHasAttrRead = grouperAllHasAdmin || group.hasGroupAttrRead(SubjectFinder.findAllSubject());
      groupIdPermissionNameAllowedForGrouperAll.put(new MultiKey(group.getId(), AccessPrivilege.GROUP_ATTR_READ.getName()), grouperAllHasAttrRead);

      boolean grouperAllHasAttrUpdate = false;
      groupIdPermissionNameAllowedForGrouperAll.put(new MultiKey(group.getId(), AccessPrivilege.GROUP_ATTR_UPDATE.getName()), grouperAllHasAttrUpdate);

      boolean grouperAllHasView = grouperAllHasAdmin || grouperAllHasUpdate || grouperAllHasRead 
          || grouperAllHasOptin || grouperAllHasOptout || grouperAllHasAttrRead || grouperAllHasAttrUpdate
          || group.hasView(SubjectFinder.findAllSubject());
      groupIdPermissionNameAllowedForGrouperAll.put(new MultiKey(group.getId(), AccessPrivilege.VIEW.getName()), grouperAllHasView);

    }
    
    
    Set<String> groupFieldNames = GrouperUtil.toSet(Field.FIELD_NAME_ADMINS, Field.FIELD_NAME_UPDATERS, 
        Field.FIELD_NAME_GROUP_ATTR_READERS, Field.FIELD_NAME_GROUP_ATTR_UPDATERS,
        Field.FIELD_NAME_READERS, Field.FIELD_NAME_OPTINS, Field.FIELD_NAME_OPTOUTS, Field.FIELD_NAME_VIEWERS);
    
    Subject rootSubject = SubjectFinder.findRootSubject();
    Subject everyEntitySubject = SubjectFinder.findAllSubject();
    
    for (MembershipSubjectContainer membershipSubjectContainer : membershipSubjectContainers) {
      Group group = membershipSubjectContainer.getGroupOwner();
      boolean grouperAllHasAdmin = groupIdPermissionNameAllowedForGrouperAll.get(new MultiKey(group.getId(), AccessPrivilege.ADMIN.getName()));
      boolean grouperAllHasUpdate = groupIdPermissionNameAllowedForGrouperAll.get(new MultiKey(group.getId(), AccessPrivilege.UPDATE.getName()));
      boolean grouperAllHasRead = groupIdPermissionNameAllowedForGrouperAll.get(new MultiKey(group.getId(), AccessPrivilege.READ.getName()));
      boolean grouperAllHasOptin = groupIdPermissionNameAllowedForGrouperAll.get(new MultiKey(group.getId(), AccessPrivilege.OPTIN.getName()));
      boolean grouperAllHasOptout = groupIdPermissionNameAllowedForGrouperAll.get(new MultiKey(group.getId(), AccessPrivilege.OPTOUT.getName()));
      boolean grouperAllHasAttrRead = groupIdPermissionNameAllowedForGrouperAll.get(new MultiKey(group.getId(), AccessPrivilege.GROUP_ATTR_READ.getName()));
      boolean grouperAllHasAttrUpdate = groupIdPermissionNameAllowedForGrouperAll.get(new MultiKey(group.getId(), AccessPrivilege.GROUP_ATTR_UPDATE.getName()));
      boolean grouperAllHasView = groupIdPermissionNameAllowedForGrouperAll.get(new MultiKey(group.getId(), AccessPrivilege.VIEW.getName()));;

      Subject subject = membershipSubjectContainer.getSubject();
      
      //if we are on grouper system
      if (SubjectHelper.eq(subject, rootSubject)) {
        
        for (String fieldName : groupFieldNames) {
          //it is also effective, merge that with whatever was there
          membershipSubjectContainer.addMembership(fieldName, MembershipAssignType.EFFECTIVE);
        }
        
      } else {
        //else
        boolean isEveryEntity = SubjectHelper.eq(everyEntitySubject, membershipSubjectContainer.getSubject());
        
        //see what the subject has
        boolean subjectHasAdminEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ADMINS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ADMINS).getMembershipAssignType().isNonImmediate()
            || (isEveryEntity ? false : grouperAllHasAdmin);

        boolean subjectHasAdmin = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_ADMINS) != null 
            || subjectHasAdminEffective || grouperAllHasAdmin;
        
        boolean subjectHasUpdateEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_UPDATERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_UPDATERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || (isEveryEntity ? false : grouperAllHasUpdate);
        
        boolean subjectHasUpdate = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_UPDATERS) != null 
            || subjectHasUpdateEffective || grouperAllHasUpdate;
        
        boolean subjectHasReadEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_READERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_READERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || (isEveryEntity ? false : grouperAllHasRead);
        
        boolean subjectHasRead = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_READERS) != null 
            || subjectHasReadEffective || grouperAllHasRead;
        
        boolean subjectHasOptinEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_OPTINS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_OPTINS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || subjectHasUpdate || (isEveryEntity ? false : grouperAllHasOptin);
        
        boolean subjectHasOptin = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_OPTINS) != null 
            || subjectHasOptinEffective || grouperAllHasOptin;
        
        boolean subjectHasOptoutEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_OPTOUTS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_OPTOUTS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || subjectHasUpdate || (isEveryEntity ? false : grouperAllHasOptout);
        
        boolean subjectHasOptout = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_OPTOUTS) != null 
            || subjectHasReadEffective || grouperAllHasOptout;

        boolean subjectHasAttrReadEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_GROUP_ATTR_READERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_GROUP_ATTR_READERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || (isEveryEntity ? false : grouperAllHasAttrRead);

        boolean subjectHasAttrRead = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_GROUP_ATTR_READERS) != null 
            || subjectHasAttrReadEffective || grouperAllHasAttrRead;

        boolean subjectHasAttrUpdateEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_GROUP_ATTR_UPDATERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_GROUP_ATTR_UPDATERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin  || (isEveryEntity ? false : grouperAllHasAttrUpdate);

        boolean subjectHasAttrUpdate = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_GROUP_ATTR_UPDATERS) != null 
            || subjectHasAttrUpdateEffective || grouperAllHasAttrUpdate;
        
        boolean subjectHasViewEffective = membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_VIEWERS) != null
            && membershipSubjectContainer.getMembershipContainers().get(Field.FIELD_NAME_VIEWERS).getMembershipAssignType().isNonImmediate()
            || subjectHasAdmin || subjectHasUpdate || subjectHasRead || subjectHasOptout || subjectHasOptin
            || subjectHasAttrUpdate || subjectHasAttrRead || (isEveryEntity ? false : grouperAllHasView);


        //if the subject has an effective stem priv, add it in
        if (subjectHasAdminEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_ADMINS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasUpdateEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_UPDATERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasReadEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_READERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasViewEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_VIEWERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasOptinEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_OPTINS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasOptoutEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_OPTOUTS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasAttrReadEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_GROUP_ATTR_READERS, MembershipAssignType.EFFECTIVE);
        }
        if (subjectHasAttrUpdateEffective) {
          membershipSubjectContainer.addMembership(Field.FIELD_NAME_GROUP_ATTR_UPDATERS, MembershipAssignType.EFFECTIVE);
        }
      }  
    }
  }
  
  /**
   * convert memberships into membership subject containers
   * @param membershipResults
   * @param fields
   * @param includeInheritedPrivileges
   * @return the containers per user
   */
  public static Set<MembershipSubjectContainer> convertFromMembershipsOwnersMembers(Set<Object[]> memberships,
      Collection<Field> fields, boolean includeInheritedPrivileges) {

    //lets get the field ids that are only inherited, substitute for the original field
    Set<String> inheritedFieldIdsOnly = new HashSet<String>();
    Field fieldToSubstituteForInheritedField = null;
    if (includeInheritedPrivileges && GrouperUtil.length(fields) > 0) {
      
      if (GrouperUtil.length(fields) > 1) {
        throw new RuntimeException("Not yet implemented using includeInheritedPrivileges with more than one field");
      }
      
      fieldToSubstituteForInheritedField = fields.iterator().next();
      
      Collection<Field> inheritedFields = Field.calculateInheritedPrivileges(fields, includeInheritedPrivileges);

      for (Field inheritedField : GrouperUtil.nonNull(inheritedFields)) {
        if (!fieldToSubstituteForInheritedField.equals(inheritedField)) {
          inheritedFieldIdsOnly.add(inheritedField.getUuid());
        }
      }
      
    }
    
    //this multikey is sourceid, subjectid, ownerid, fieldid, 
    Map<MultiKey, MembershipSubjectContainer> resultsMemberOwnerToMembershipSubjectContainer = new LinkedHashMap<MultiKey, MembershipSubjectContainer>();
    
    if (GrouperUtil.length(memberships) > 0) {

      //lets get all the subjects by member id
      Map<String, Subject> memberIdToSubject = new HashMap<String, Subject>();

      {
        Map<String, SubjectBean> memberIdToSubjectBean = new HashMap<String, SubjectBean>();
        Set<SubjectBean> subjectBeans = new HashSet<SubjectBean>();
        for (Object[] membershipResult : memberships) {
          Member member = (Member)membershipResult[2];
          SubjectBean subjectBean = new SubjectBean(member.getSubjectId(), member.getSubjectSourceId());
          memberIdToSubjectBean.put(member.getUuid(), subjectBean);
          subjectBeans.add(subjectBean);
        }
        Map<SubjectBean, Subject> subjectBeanToSubject = SubjectFinder.findBySubjectBeans(subjectBeans);
    
        for (String memberId : memberIdToSubjectBean.keySet()) {
          SubjectBean subjectBean = memberIdToSubjectBean.get(memberId);
          Subject subject = subjectBeanToSubject.get(subjectBean);
          memberIdToSubject.put(memberId, subject);
        }
      }

      //this multikey is sourceid, subjectid, ownerid -> List of Array[membership, owner, member]
      Map<MultiKey, List<Object[]>> memberOwnerToMembershipResultMap = new HashMap<MultiKey, List<Object[]>>();
      
      //this multikey is sourceid, subjectid, ownerid, fieldid, 
      Map<MultiKey, MembershipAssignType> memberOwnerFieldToMembershipAssignTypeMap = new HashMap<MultiKey, MembershipAssignType>();
      
      //this multikey is sourceid, subjectid, ownerid, fieldid, 
      Map<MultiKey, Membership> memberOwnerFieldToImmediateMembershipMap = new HashMap<MultiKey, Membership>();
      
      //lets get all the members first, and keep the answer
      for (Object[] objectArray: memberships) {
        
        Member member = (Member)objectArray[2];

        GrouperObject owner = (GrouperObject)objectArray[1];
        
        String theOwnerId = owner.getId();

        Membership membership = (Membership)objectArray[0];

        //massage inherited privileges to be the underlying privilege
        if (inheritedFieldIdsOnly.contains(membership.getFieldId() )) {
          membership.setUuid(null);
          membership.setImmediateMembershipId(null);
          membership.setType(MembershipType.EFFECTIVE.getTypeString());
          membership.setFieldId(fieldToSubstituteForInheritedField.getUuid());
        }
        
        MultiKey memberOwnerFieldKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId(), 
            theOwnerId, membership.getFieldId());
        MultiKey memberOwnerKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId(), 
            theOwnerId);
        
        MembershipSubjectContainer membershipSubjectContainer = resultsMemberOwnerToMembershipSubjectContainer.get(memberOwnerKey);
        if (membershipSubjectContainer == null) {
          membershipSubjectContainer = new MembershipSubjectContainer();
          membershipSubjectContainer.setSubject(memberIdToSubject.get(member.getId()));
          membershipSubjectContainer.setMember(member);
          if (objectArray[1] instanceof Group) {
            membershipSubjectContainer.setGroupOwner((Group)objectArray[1]);
          }
          if (objectArray[1] instanceof Stem) {
            membershipSubjectContainer.setStemOwner((Stem)objectArray[1]);
          }
          if (objectArray[1] instanceof AttributeDef) {
            membershipSubjectContainer.setAttributeDefOwner((AttributeDef)objectArray[1]);
          }
          resultsMemberOwnerToMembershipSubjectContainer.put(memberOwnerKey, membershipSubjectContainer);
        }            

        List<Object[]> membershipList = memberOwnerToMembershipResultMap.get(memberOwnerKey);
        
        if (membershipList == null) {

          membershipList = new ArrayList<Object[]>();
          
          memberOwnerToMembershipResultMap.put(memberOwnerKey, membershipList);

        }

        membershipList.add(objectArray);
        
        MembershipAssignType membershipAssignType = memberOwnerFieldToMembershipAssignTypeMap.get(memberOwnerFieldKey);
        membershipAssignType = MembershipAssignType.convertMembership(membershipAssignType, membership);
        memberOwnerFieldToMembershipAssignTypeMap.put(memberOwnerFieldKey, membershipAssignType);
        
        if (membership.isImmediate()) {
          memberOwnerFieldToImmediateMembershipMap.put(memberOwnerFieldKey, membership);
        }
        
        
      }
      
      for (MultiKey memberOwnerKey : resultsMemberOwnerToMembershipSubjectContainer.keySet()) {
        
        MembershipSubjectContainer membershipSubjectContainer = resultsMemberOwnerToMembershipSubjectContainer.get(memberOwnerKey);
        
        membershipSubjectContainer.setMembershipContainers(new TreeMap<String, MembershipContainer>());
        
        //lets get the memberships
        List<Object[]> membershipList = memberOwnerToMembershipResultMap.get(memberOwnerKey);
        
        if (membershipList != null) {
          for (Object[] objectArray : membershipList) {
            
            Membership membership = (Membership)objectArray[0];
            
            Member member = (Member)objectArray[2];
            Field field = FieldFinder.findById(membership.getFieldId(), true);
            
            //multiple memberships could have the same result, just skip if already set
            if (membershipSubjectContainer.getMembershipContainers().get(field.getName()) == null) {
              MembershipContainer membershipContainer = new MembershipContainer();
              membershipContainer.setFieldName(field.getName());
              
              //if the subject, field, groupId match, then correlate the assign type...
              
              MultiKey memberOwnerFieldKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId(), membership.getOwnerId(), membership.getFieldId());
              
              MembershipAssignType membershipAssignType = memberOwnerFieldToMembershipAssignTypeMap.get(memberOwnerFieldKey);
              if (membershipAssignType == null) {
                throw new RuntimeException("Why is result not there???");
              }
              membershipContainer.setMembershipAssignType(membershipAssignType);
              
              //get the immediate membership so we can easily revoke
              Membership immediateMembership = memberOwnerFieldToImmediateMembershipMap.get(memberOwnerFieldKey);
              if (immediateMembership != null) {
                membershipContainer.setImmediateMembership(immediateMembership);
              }
              
              membershipSubjectContainer.getMembershipContainers().put(field.getName(), membershipContainer);
              
            }
          }
        }
      }
    }
    return new LinkedHashSet<MembershipSubjectContainer>(resultsMemberOwnerToMembershipSubjectContainer.values());
  }
  
}
