/**
 * @author mchyzer
 * $Id: AttributeAssignMemberDelegate.java,v 1.4 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * delegate privilege calls from attribute defs
 */
public class AttributeAssignMemberDelegate extends AttributeAssignBaseDelegate {

  /**
   * reference to the member in question
   */
  private Member member = null;
  
  /**
   * populate attribute assignments to prevent N+1 queries when looping through members and 
   * getting attributes.  This cache will last for 2 minutes
   * @param members is the members to populate
   */
  public static void populateAttributeAssignments(Collection<Member> members) {
    
    if (GrouperUtil.length(members) == 0) {
      return;
    }
    
    //lets go through in batches
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(members, 100);
    
    Map<String, Set<AttributeAssign>> attributeAssignMap = new LinkedHashMap<String, Set<AttributeAssign>>(); 
    for (int i=0;i<numberOfBatches;i++) {
      
      List<Member> membersInBatch = GrouperUtil.batchList(members, 100, i);
      List<String> memberIdsInBatch = new ArrayList<String>();
      for (Member member : membersInBatch) {
        memberIdsInBatch.add(member.getUuid());
      }
      Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssign().findMemberAttributeAssignments(null, null, null, 
          memberIdsInBatch, null, null, false);
      
      //start again for this batch
      attributeAssignMap.clear();
      
      //go through the results and collate for members
      for (AttributeAssign attributeAssign : attributeAssigns) {
        
        Set<AttributeAssign> attributeAssignSet = attributeAssignMap.get(attributeAssign.getOwnerMemberId());
        
        //if first for this member, init it
        if (attributeAssignSet == null) {
          attributeAssignSet = new LinkedHashSet<AttributeAssign>();
          attributeAssignMap.put(attributeAssign.getOwnerMemberId(), attributeAssignSet);
        }
        
        attributeAssignSet.add(attributeAssign);
        
      }
      
      //go through the members, and put in the assignments
      for (Member member : membersInBatch) {
        
        //see if there are attributes for this member
        Set<AttributeAssign> attributeAssignsForMember = attributeAssignMap.get(member.getUuid());
        
        if (attributeAssignsForMember == null) {
          //init so it isnt lazy loaded to get nothing
          attributeAssignsForMember = new LinkedHashSet<AttributeAssign>();
        }
        member.getAttributeDelegate().setAllAttributeAssignsForCache(attributeAssignsForMember);
      }
      
    }
      
      
  }
  
  /**
   * 
   * @param member1
   */
  public AttributeAssignMemberDelegate(Member member1) {
    this.member = member1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#newAttributeAssign(java.lang.String, edu.internet2.middleware.grouper.attr.AttributeDefName, java.lang.String)
   */
  @Override
  AttributeAssign newAttributeAssign(String action, AttributeDefName attributeDefName, String uuid) {
    
    AttributeAssignAction attributeAssignAction = attributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(action, true);
    
    return new AttributeAssign(this.member, attributeAssignAction.getId(), attributeDefName, uuid);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanReadAttributeDef(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  @Override
  public
  void assertCanReadAttributeDef(final AttributeDef attributeDef) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canReadAttribute = new boolean[1];
  
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canReadAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrRead(subject);
        return null;
      }
    });
    
    if (!canReadAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot read attributeDef " + attributeDef.getName());
    }
  
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanUpdateAttributeDefName(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanUpdateAttributeDefName(AttributeDefName attributeDefName) {
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canUpdateAttribute = new boolean[1];
    final boolean[] canAdminMember = new boolean[1];
 
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canUpdateAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrUpdate(subject);
        canAdminMember[0] = PrivilegeHelper.isWheelOrRoot(subject);
        return null;
      }
    });
    
    if (!canUpdateAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update attributeDef " + attributeDef.getName());
    }

    if (!canAdminMember[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " is not wheel or GrouperSystem");
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefNameId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefNameId(
      String attributeDefNameId) {

    Set<AttributeAssign> cachedAttributeAssigns = this.getAllAttributeAssignsForCache();
    if (cachedAttributeAssigns != null) {
      
      Set<AttributeAssign> result = new LinkedHashSet<AttributeAssign>();
      
      for (AttributeAssign attributeAssign : cachedAttributeAssigns) {
        if (StringUtils.equals(this.member.getUuid(), attributeAssign.getOwnerMemberId())
            && StringUtils.equals(attributeDefNameId, attributeAssign.getAttributeDefNameId())) {
          result.add(attributeAssign);
        }
      }
      return result;
      
    }

    return GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByMemberIdAndAttributeDefNameId(this.member.getUuid(), attributeDefNameId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefId(
      String attributeDefId) {
    
    Set<AttributeAssign> cachedAttributeAssigns = this.getAllAttributeAssignsForCache();
    if (cachedAttributeAssigns != null) {
      
      Set<AttributeAssign> result = new LinkedHashSet<AttributeAssign>();
      
      for (AttributeAssign attributeAssign : cachedAttributeAssigns) {
        if (StringUtils.equals(this.member.getUuid(), attributeAssign.getOwnerMemberId())
            && StringUtils.equals(attributeDefId, attributeAssign.getAttributeDefName().getAttributeDefId())) {
          result.add(attributeAssign);
        }
      }
      return result;
      
    }

    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByMemberIdAndAttributeDefId(this.member.getUuid(), attributeDefId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwnerAndAttributeDefId(
      String attributeDefId) {
    
    Set<AttributeAssign> cachedAttributeAssigns = this.getAllAttributeAssignsForCache();
    if (cachedAttributeAssigns != null) {
      
      Set<AttributeDefName> result = new LinkedHashSet<AttributeDefName>();
      
      for (AttributeAssign attributeAssign : cachedAttributeAssigns) {
        if (StringUtils.equals(this.member.getUuid(), attributeAssign.getOwnerMemberId())
            && StringUtils.equals(attributeDefId, attributeAssign.getAttributeDefName().getAttributeDefId())) {
          result.add(attributeAssign.getAttributeDefName());
        }
      }
      return result;
      
    }
    
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefNamesByMemberIdAndAttributeDefId(this.member.getUuid(), attributeDefId);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "member", this.member)
      .toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#getAttributeAssignable()
   */
  @Override
  public AttributeAssignable getAttributeAssignable() {
    return this.member;
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwner()
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwner() {
    
    Set<AttributeAssign> cachedAttributeAssigns = this.getAllAttributeAssignsForCache();
    if (cachedAttributeAssigns != null) {
      
      Set<AttributeAssign> result = new LinkedHashSet<AttributeAssign>();
      
      for (AttributeAssign attributeAssign : cachedAttributeAssigns) {
        if (StringUtils.equals(this.member.getUuid(), attributeAssign.getOwnerMemberId())) {
          result.add(attributeAssign);
        }
      }
      return result;
      
    }
    

    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(this.member.getUuid()), null, null, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwner()
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwner() {
    
    Set<AttributeAssign> cachedAttributeAssigns = this.getAllAttributeAssignsForCache();
    if (cachedAttributeAssigns != null) {
      
      Set<AttributeDefName> result = new TreeSet<AttributeDefName>();
      
      for (AttributeAssign attributeAssign : cachedAttributeAssigns) {
        if (StringUtils.equals(this.member.getUuid(), attributeAssign.getOwnerMemberId())) {
          result.add(attributeAssign.getAttributeDefName());
        }
      }
      return result;
      
    }
    
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findMemberAttributeDefNames(null, null, null, GrouperUtil.toSet(this.member.getUuid()),null, true);
  }

}
