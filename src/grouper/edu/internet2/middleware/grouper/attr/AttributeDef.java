/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAttributeDefDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeDefActionDelegate;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * definition of an attribute
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AttributeDef extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, Owner {

  /** default action */
  public static final String ACTION_DEFAULT = "assign";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(AttributeDef.class);

  /** name of the groups attribute def table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_DEF = "grouper_attribute_def";

  /** constant for column for: assign_to_attribute_def */
  public static final String COLUMN_ASSIGN_TO_ATTRIBUTE_DEF  = "assign_to_attribute_def";
  
  /** constant for column for: assign_to_attribute_def_assn */
  public static final String COLUMN_ASSIGN_TO_ATTRIBUTE_DEF_ASSN  = "assign_to_attribute_def_assn";
  
  /** constant for column for: assign_to_eff_membership */
  public static final String COLUMN_ASSIGN_TO_EFF_MEMBERSHIP  = "assign_to_eff_membership";
  
  /** constant for column for: assign_to_eff_membership_assn */
  public static final String COLUMN_ASSIGN_TO_EFF_MEMBERSHIP_ASSN  = "assign_to_eff_membership_assn";
  
  /** constant for column for: assign_to_group */
  public static final String COLUMN_ASSIGN_TO_GROUP  = "assign_to_group";
  
  /** constant for column for: assign_to_group_assn */
  public static final String COLUMN_ASSIGN_TO_GROUP_ASSN  = "assign_to_group_assn";
  
  /** constant for column for: assign_to_imm_membership */
  public static final String COLUMN_ASSIGN_TO_IMM_MEMBERSHIP  = "assign_to_imm_membership";
  
  /** constant for column for: assign_to_imm_membership_assn */
  public static final String COLUMN_ASSIGN_TO_IMM_MEMBERSHIP_ASSN  = "assign_to_imm_membership_assn";
  
  /** constant for column for: assign_to_member */
  public static final String COLUMN_ASSIGN_TO_MEMBER  = "assign_to_member";
  
  /** constant for column for: assign_to_member_assn */
  public static final String COLUMN_ASSIGN_TO_MEMBER_ASSN  = "assign_to_member_assn";
  
  /** constant for column for: assign_to_stem */
  public static final String COLUMN_ASSIGN_TO_STEM  = "assign_to_stem";
  
  /** constant for column for: assign_to_stem_assn */
  public static final String COLUMN_ASSIGN_TO_STEM_ASSN = "assign_to_stem_assn";


  
  /** if the attribute def is public */
  public static final String COLUMN_ATTRIBUTE_DEF_PUBLIC = "attribute_def_public";

  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_TYPE = "attribute_def_type";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_CREATOR_ID = "creator_id";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_DESCRIPTION = "description";

  /** column */
  public static final String COLUMN_EXTENSION = "extension";

  /** column */
  public static final String COLUMN_NAME = "name";

  /** column */
  public static final String COLUMN_MULTI_ASSIGNABLE = "multi_assignable";

  /** column */
  public static final String COLUMN_MULTI_VALUED = "multi_valued";

  /** column */
  public static final String COLUMN_STEM_ID = "stem_id";

  /** column */
  public static final String COLUMN_VALUE_TYPE = "value_type";

  /** column */
  public static final String COLUMN_ID = "id";

  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  
  /** constant for field name for: assignToAttributeDef */
  public static final String ASSIGN_TO_ATTRIBUTE_DEF  = "assignToAttributeDef";
  
  /** constant for field name for: assignToAttributeDefAssn */
  public static final String ASSIGN_TO_ATTRIBUTE_DEF_ASSN  = "assignToAttributeDefAssn";
  
  /** constant for field name for: assignToEffMembership */
  public static final String ASSIGN_TO_EFF_MEMBERSHIP  = "assignToEffMembership";
  
  /** constant for field name for: assignToEffMembershipAssn */
  public static final String ASSIGN_TO_EFF_MEMBERSHIP_ASSN  = "assignToEffMembershipAssn";
  
  /** constant for field name for: assignToGroup */
  public static final String ASSIGN_TO_GROUP  = "assignToGroup";
  
  /** constant for field name for: assignToGroupAssn */
  public static final String ASSIGN_TO_GROUP_ASSN  = "assignToGroupAssn";
  
  /** constant for field name for: assignToImmMembership */
  public static final String ASSIGN_TO_IMM_MEMBERSHIP  = "assignToImmMembership";
  
  /** constant for field name for: assignToImmMembershipAssn */
  public static final String ASSIGN_TO_IMM_MEMBERSHIP_ASSN  = "assignToImmMembershipAssn";
  
  /** constant for field name for: assignToMember */
  public static final String ASSIGN_TO_MEMBER  = "assignToMember";
  
  /** constant for field name for: assignToMemberAssn */
  public static final String ASSIGN_TO_MEMBER_ASSN  = "assignToMemberAssn";
  
  /** constant for field name for: assignToStem */
  public static final String ASSIGN_TO_STEM  = "assignToStem";
  
  /** constant for field name for: assignToStemAssn */
  public static final String ASSIGN_TO_STEM_ASSN = "assignToStemAssn";

  
  /** constant for field name for: attributeDefPublic */
  public static final String FIELD_ATTRIBUTE_DEF_PUBLIC = "attributeDefPublic";

  /** constant for field name for: attributeDefType */
  public static final String FIELD_ATTRIBUTE_DEF_TYPE = "attributeDefType";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: extension */
  public static final String FIELD_EXTENSION = "extension";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: multiAssignable */
  public static final String FIELD_MULTI_ASSIGNABLE = "multiAssignable";

  /** constant for field name for: multiValued */
  public static final String FIELD_MULTI_VALUED = "multiValued";

  /** constant for field name for: stemId */
  public static final String FIELD_STEM_ID = "stemId";

  /** constant for field name for: valueType */
  public static final String FIELD_VALUE_TYPE = "valueType";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_PUBLIC, FIELD_ATTRIBUTE_DEF_TYPE, FIELD_CONTEXT_ID, 
      FIELD_CREATE_TIME, FIELD_DESCRIPTION, FIELD_EXTENSION, 
      FIELD_ID, FIELD_MODIFY_TIME, FIELD_MULTI_ASSIGNABLE, 
      FIELD_MULTI_VALUED, FIELD_STEM_ID, FIELD_VALUE_TYPE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_PUBLIC, FIELD_ATTRIBUTE_DEF_TYPE, FIELD_CONTEXT_ID, 
      FIELD_CREATE_TIME, FIELD_DESCRIPTION, FIELD_EXTENSION, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_MODIFY_TIME, 
      FIELD_MULTI_ASSIGNABLE, FIELD_MULTI_VALUED, FIELD_STEM_ID, FIELD_VALUE_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeDef clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignAttributeDefDelegate attributeAssignAttributeDefDelegate;
  
  /**
   * 
   * @return the delegate
   */
  public AttributeAssignAttributeDefDelegate getAttributeDelegate() {
    if (this.attributeAssignAttributeDefDelegate == null) {
      this.attributeAssignAttributeDefDelegate = new AttributeAssignAttributeDefDelegate(this);
    }
    return this.attributeAssignAttributeDefDelegate;
  }

  /** delegate */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeDefActionDelegate attributeDefActionDelegate;
  
  /**
   * delegate the action list management to this class
   * @return the delegate
   */
  public AttributeDefActionDelegate getAttributeDefActionDelegate() {
    this.attributeDefActionDelegate = new AttributeDefActionDelegate(this);
    return this.attributeDefActionDelegate;
  }

  /** id of this attribute def */
  private String id;

  /** context id of the transaction */
  private String contextId;

  /** stem that this attribute is in */
  private String stemId;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * memberId of who created this
   */
  private String creatorId;
  
  
  /**
   * @return the creatorId
   */
  public String getCreatorId() {
    return this.creatorId;
  }

  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    this.creatorId = GrouperSession.staticGrouperSession(true).getMemberUuid();
  }

  /**
   * @param creatorId1 the creatorId to set
   */
  public void setCreatorId(String creatorId1) {
    this.creatorId = creatorId1;
  }

  /**
   * store this group (update) to database
   */
  public void store() {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            //make sure subject is allowed to do this
            Subject subject = GrouperSession.staticGrouperSession().getSubject();
            if (!AttributeDef.this.getPrivilegeDelegate().canAttrAdmin(subject)) {
              throw new InsufficientPrivilegeException(GrouperUtil
                  .subjectToString(subject)
                  + " is not attrAdmin on attributeDef: " + AttributeDef.this.getName());
            }
            
            GrouperDAOFactory.getFactory().getAttributeDef().saveOrUpdate(AttributeDef.this);
            return null;
          }
        });
  }
  
  /** delegate privilege calls to another class to separate logic */
  private AttributeDefPrivilegeDelegate attributeDefPrivilegeDelegate = null;

  /**
   * privilege delegate to handle security on this attribute def
   * @return the delegate
   */
  public AttributeDefPrivilegeDelegate getPrivilegeDelegate() {
    if (this.attributeDefPrivilegeDelegate == null) {
      this.attributeDefPrivilegeDelegate = new AttributeDefPrivilegeDelegate(this);
    }
    return this.attributeDefPrivilegeDelegate;
  }
  
  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   */
  private String description;

  /**
   * extension of attribute
   */
  private String extension;

  /**
   * name of attribute
   */
  private String name;

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   */
  private boolean attributeDefPublic = false;
  
  /**
   * type of this attribute (e.g. attribute or privilege)
   */
  private AttributeDefType attributeDefType;
  
  /** if can assign to group/role */
  private boolean assignToGroup;
  
  /** if can assign to stem */
  private boolean assignToStem;
  
  /** if can assign to member */
  private boolean assignToMember;
  
  /** if can assign to immediate membership */
  private boolean assignToImmMembership;
  
  /** if can assign to effective membership */
  private boolean assignToEffMembership;

  /** if can assign to attribute def */
  private boolean assignToAttributeDef;

  /** if can assign to assignment of group/role */
  private boolean assignToGroupAssn;
  
  /** if can assign to assignment of stem */
  private boolean assignToStemAssn;
  
  /** if can assign to assignment of member */
  private boolean assignToMemberAssn;
  
  /** if can assign to assignment of immediate membership */
  private boolean assignToImmMembershipAssn;
  
  /** if can assign to assignment of effective membership */
  private boolean assignToEffMembershipAssn;

  /** if can assign to assignment of attribute def */
  private boolean assignToAttributeDefAssn;

  /**
   * if can assign to group/role
   * @return the assignToGroup
   */
  public boolean isAssignToGroup() {
    return this.assignToGroup;
  }
  
  /**
   * if can assign to group/role
   * @param assignToGroup the assignToGroup to set
   */
  public void setAssignToGroup(boolean assignToGroup) {
    this.assignToGroup = assignToGroup;
  }

  /**
   * if can assign to stem
   * @return the assignToStem
   */
  public boolean isAssignToStem() {
    return this.assignToStem;
  }
  
  /**
   * if can assign to stem
   * @param assignToStem the assignToStem to set
   */
  public void setAssignToStem(boolean assignToStem) {
    this.assignToStem = assignToStem;
  }

  /**
   * if can assign to member
   * @return the assignToMember
   */
  public boolean isAssignToMember() {
    return this.assignToMember;
  }
  
  /**
   * if can assign to member
   * @param assignToMember the assignToMember to set
   */
  public void setAssignToMember(boolean assignToMember) {
    this.assignToMember = assignToMember;
  }

  /**
   * if can assign to immediate membership
   * @return the assignToImmMembership
   */
  public boolean isAssignToImmMembership() {
    return this.assignToImmMembership;
  }

  /**
   * if can assign to immediate membership
   * @param assignToImmMembership the assignToImmMembership to set
   */
  public void setAssignToImmMembership(boolean assignToImmMembership) {
    this.assignToImmMembership = assignToImmMembership;
  }

  /**
   * if can assign to effective membership
   * @return the assignToEffMembership
   */
  public boolean isAssignToEffMembership() {
    return this.assignToEffMembership;
  }

  /**
   * if can assign to effective membership
   * @param assignToEffMembership the assignToEffMembership to set
   */
  public void setAssignToEffMembership(boolean assignToEffMembership) {
    this.assignToEffMembership = assignToEffMembership;
  }

  /**
   * if can assign to attribute def
   * @return the assignToAttributeDef
   */
  public boolean isAssignToAttributeDef() {
    return this.assignToAttributeDef;
  }

  /**
   * if can assign to attribute def
   * @param assignToAttributeDef the assignToAttributeDef to set
   */
  public void setAssignToAttributeDef(boolean assignToAttributeDef) {
    this.assignToAttributeDef = assignToAttributeDef;
  }

  /**
   * if can assign to assignment of group/role
   * @return the assignToGroupAssn
   */
  public boolean isAssignToGroupAssn() {
    return this.assignToGroupAssn;
  }
  
  /**
   * if can assign to assignment of group/role
   * @param assignToGroupAssn the assignToGroupAssn to set
   */
  public void setAssignToGroupAssn(boolean assignToGroupAssn) {
    this.assignToGroupAssn = assignToGroupAssn;
  }


  
  /**
   * allowed to assign to a stem assignment
   * @return the assignToStemAssn
   */
  public boolean isAssignToStemAssn() {
    return this.assignToStemAssn;
  }


  
  /**
   * allowed to assign to a stem assignment
   * @param assignToStemAssn the assignToStemAssn to set
   */
  public void setAssignToStemAssn(boolean assignToStemAssn) {
    this.assignToStemAssn = assignToStemAssn;
  }


  
  /**
   * allowed to assign to a member assignment
   * @return the assignToMemberAssn
   */
  public boolean isAssignToMemberAssn() {
    return this.assignToMemberAssn;
  }


  
  /**
   * allowed to assign to a member assignment
   * @param assignToMemberAssn the assignToMemberAssn to set
   */
  public void setAssignToMemberAssn(boolean assignToMemberAssn) {
    this.assignToMemberAssn = assignToMemberAssn;
  }


  
  /**
   * allowed to assign to an immediate membership assignment
   * @return the assignToImmMembershipAssn
   */
  public boolean isAssignToImmMembershipAssn() {
    return this.assignToImmMembershipAssn;
  }


  
  /**
   * allowed to assign to an immediate membership assignment
   * @param assignToImmMembershipAssn the assignToImmMembershipAssn to set
   */
  public void setAssignToImmMembershipAssn(boolean assignToImmMembershipAssn) {
    this.assignToImmMembershipAssn = assignToImmMembershipAssn;
  }


  
  /**
   * allowed to assign to an effective membership assignment
   * @return the assignToEffMembershipAssn
   */
  public boolean isAssignToEffMembershipAssn() {
    return this.assignToEffMembershipAssn;
  }


  
  /**
   * allowed to assign to an effective membership assignment
   * @param assignToEffMembershipAssn the assignToEffMembershipAssn to set
   */
  public void setAssignToEffMembershipAssn(boolean assignToEffMembershipAssn) {
    this.assignToEffMembershipAssn = assignToEffMembershipAssn;
  }


  
  /**
   * allowed to assign to an attribute definition assignment
   * @return the assignToAttributeDefAssn
   */
  public boolean isAssignToAttributeDefAssn() {
    return this.assignToAttributeDefAssn;
  }


  
  /**
   * allowed to assign to an attribute definition assignment
   * @param assignToAttributeDefAssn the assignToAttributeDefAssn to set
   */
  public void setAssignToAttributeDefAssn(boolean assignToAttributeDefAssn) {
    this.assignToAttributeDefAssn = assignToAttributeDefAssn;
  }

  /**
   * if can assign to group/role
   * @return the assignToGroup
   */
  public String getAssignToGroupDb() {
    return this.assignToGroup ? "T" : "F";
  }
  
  /**
   * if can assign to group/role
   * @param assignToGroup1 the assignToGroup to set
   */
  public void setAssignToGroupDb(String assignToGroup1) {
    this.assignToGroup = GrouperUtil.booleanValue(assignToGroup1);
  }

  /**
   * if can assign to stem
   * @return the assignToStem
   */
  public String getAssignToStemDb() {
    return this.assignToStem ? "T" : "F";
  }
  
  /**
   * if can assign to stem
   * @param assignToStem1 the assignToStem to set
   */
  public void setAssignToStemDb(String assignToStem1) {
    this.assignToStem = GrouperUtil.booleanValue(assignToStem1);
  }

  /**
   * if can assign to member
   * @return the assignToMember
   */
  public String getAssignToMemberDb() {
    return this.assignToMember ? "T" : "F";
  }
  
  /**
   * if can assign to member
   * @param assignToMember1 the assignToMember to set
   */
  public void setAssignToMemberDb(String assignToMember1) {
    this.assignToMember = GrouperUtil.booleanValue(assignToMember1);
  }

  /**
   * if can assign to immediate membership
   * @return the assignToImmMembership
   */
  public String getAssignToImmMembershipDb() {
    return this.assignToImmMembership ? "T" : "F";
  }

  /**
   * if can assign to immediate membership
   * @param assignToImmMembership1 the assignToImmMembership to set
   */
  public void setAssignToImmMembershipDb(String assignToImmMembership1) {
    this.assignToImmMembership = GrouperUtil.booleanValue(assignToImmMembership1);
  }

  /**
   * if can assign to effective membership
   * @return the assignToEffMembership
   */
  public String getAssignToEffMembershipDb() {
    return this.assignToEffMembership ? "T" : "F";
  }

  /**
   * if can assign to effective membership
   * @param assignToEffMembership1 the assignToEffMembership to set
   */
  public void setAssignToEffMembershipDb(String assignToEffMembership1) {
    this.assignToEffMembership = GrouperUtil.booleanValue(assignToEffMembership1);
  }

  /**
   * if can assign to attribute def
   * @return the assignToAttributeDef
   */
  public String getAssignToAttributeDefDb() {
    return this.assignToAttributeDef ? "T" : "F";
  }

  /**
   * if can assign to attribute def
   * @param assignToAttributeDef1 the assignToAttributeDef to set
   */
  public void setAssignToAttributeDefDb(String assignToAttributeDef1) {
    this.assignToAttributeDef = GrouperUtil.booleanValue(assignToAttributeDef1);
  }

  /**
   * if can assign to assignment of group/role
   * @return the assignToGroupAssn
   */
  public String getAssignToGroupAssnDb() {
    return this.assignToGroupAssn ? "T" : "F";
  }
  
  /**
   * if can assign to assignment of group/role
   * @param assignToGroupAssn1 the assignToGroupAssn to set
   */
  public void setAssignToGroupAssnDb(String assignToGroupAssn1) {
    this.assignToGroupAssn = GrouperUtil.booleanValue(assignToGroupAssn1);
  }


  
  /**
   * allowed to assign to a stem assignment
   * @return the assignToStemAssn
   */
  public String getAssignToStemAssnDb() {
    return this.assignToStemAssn ? "T" : "F";
  }


  
  /**
   * allowed to assign to a stem assignment
   * @param assignToStemAssn1 the assignToStemAssn to set
   */
  public void setAssignToStemAssnDb(String assignToStemAssn1) {
    this.assignToStemAssn = GrouperUtil.booleanValue(assignToStemAssn1);
  }


  
  /**
   * allowed to assign to a member assignment
   * @return the assignToMemberAssn
   */
  public String getAssignToMemberAssnDb() {
    return this.assignToMemberAssn ? "T" : "F";
  }


  
  /**
   * allowed to assign to a member assignment
   * @param assignToMemberAssn1 the assignToMemberAssn to set
   */
  public void setAssignToMemberAssnDb(String assignToMemberAssn1) {
    this.assignToMemberAssn = GrouperUtil.booleanValue(assignToMemberAssn1);
  }


  
  /**
   * allowed to assign to an immediate membership assignment
   * @return the assignToImmMembershipAssn
   */
  public String getAssignToImmMembershipAssnDb() {
    return this.assignToImmMembershipAssn ? "T" : "F";
  }


  
  /**
   * allowed to assign to an immediate membership assignment
   * @param assignToImmMembershipAssn1 the assignToImmMembershipAssn to set
   */
  public void setAssignToImmMembershipAssnDb(String assignToImmMembershipAssn1) {
    this.assignToImmMembershipAssn = GrouperUtil.booleanValue(assignToImmMembershipAssn1);
  }


  
  /**
   * allowed to assign to an effective membership assignment
   * @return the assignToEffMembershipAssn
   */
  public String getAssignToEffMembershipAssnDb() {
    return this.assignToEffMembershipAssn ? "T" : "F";
  }


  
  /**
   * allowed to assign to an effective membership assignment
   * @param assignToEffMembershipAssnDb1 the assignToEffMembershipAssn to set
   */
  public void setAssignToEffMembershipAssnDb(String assignToEffMembershipAssnDb1) {
    this.assignToEffMembershipAssn = GrouperUtil.booleanValue(assignToEffMembershipAssnDb1);
  }


  
  /**
   * allowed to assign to an attribute definition assignment
   * @return the assignToAttributeDefAssn
   */
  public String getAssignToAttributeDefAssnDb() {
    return this.assignToAttributeDefAssn ? "T" : "F";
  }


  
  /**
   * allowed to assign to an attribute definition assignment
   * @param assignToAttributeDefAssnDb the assignToAttributeDefAssn to set
   */
  public void setAssignToAttributeDefAssnDb(String assignToAttributeDefAssnDb) {
    this.assignToAttributeDefAssn = GrouperUtil.booleanValue(assignToAttributeDefAssnDb);
  }


  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @return if public
   */
  public boolean isAttributeDefPublic() {
    return this.attributeDefPublic;
  }

  /**
   * hibernate mapped method for if this attribute def is public
   * @return true if public, false if not (default false)
   */
  public String getAttributeDefPublicDb() {
    return this.attributeDefPublic ? "T" : "F";
  }

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @param theAttributeDefPublicDb
   */
  public void setAttributeDefPublicDb(String theAttributeDefPublicDb) {
    this.attributeDefPublic = GrouperUtil.booleanValue(theAttributeDefPublicDb, false);
  }
  
  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @param attributeDefPublic1
   */
  public void setAttributeDefPublic(boolean attributeDefPublic1) {
    this.attributeDefPublic = attributeDefPublic1;
  }

  /**
   * type of this attribute (e.g. attribute or privilege)
   * @return attribute def type
   */
  public AttributeDefType getAttributeDefType() {
    return this.attributeDefType;
  }

  /**
   * type of this attribute (e.g. attribute or privilege)
   * @param attributeDefType
   */
  public void setAttributeDefType(AttributeDefType attributeDefType) {
    this.attributeDefType = attributeDefType;
  }

  /**
   * type of this attribute (e.g. attribute or privilege)
   * @return the attribute def type
   */
  public String getAttributeDefTypeDb() {
    return this.attributeDefType == null ? null : this.attributeDefType.name();
  }

  /**
   * type of this attribute (e.g. attr or priv or limit)
   * @param theAttributeDefType
   */
  public void setAttributeDefTypeDb(String theAttributeDefType) {
    this.attributeDefType = AttributeDefType.valueOfIgnoreCase(theAttributeDefType, false);
  }

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @return if public
   */
  public boolean isAttributeDefIsPublic() {
    return attributeDefPublic;
  }

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @param attributeDefIsPublic1
   */
  public void setAttributeDefIsPublic(boolean attributeDefIsPublic1) {
    this.attributeDefPublic = attributeDefIsPublic1;
  }

  /**
   * stem that this attribute is in
   * @return the stem id
   */
  public String getStemId() {
    return stemId;
  }

  /**
   * stem that this attribute is in
   * @param stemId1
   */
  public void setStemId(String stemId1) {
    this.stemId = stemId1;
  }

  /**
   * if this attribute can be assigned to the same action to the same object more than once
   */
  private boolean multiAssignable;
  
  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   */
  private boolean multiValued;

  /**
   * type of the value,  int, double, string, marker
   */
  private AttributeDefValueType valueType = AttributeDefValueType.marker;
  
  /**
   * type of the value,  int, double, string, marker
   * @return the type
   */
  public AttributeDefValueType getValueType() {
    return valueType;
  }

  /**
   * type of the value,  int, double, string, marker
   * @param valueType1
   */
  public void setValueType(AttributeDefValueType valueType1) {
    this.valueType = valueType1;
  }

  /**
   * type of the value,  int, double, string, marker
   * @return the type
   */
  public String getValueTypeDb() {
    return this.valueType == null ? null : this.valueType.toString();
  }

  /**
   * type of the value,  int, double, string, marker
   * @param valueType1
   */
  public void setValueTypeDb(String valueType1) {
    this.valueType = AttributeDefValueType.valueOfIgnoreCase(valueType1, false);
  }

  /**
   * if this attribute can be assigned to the same action to the same object more than once
   * @return if multiassignable
   */
  public boolean isMultiAssignable() {
    return multiAssignable;
  }
  
  /**
   * if this attribute can be assigned to the same action to the same object more than once
   * convert to string for hibernate
   * @return the string value
   */
  public String getMultiAssignableDb() {
    return this.multiAssignable ? "T" : "F";
  }

  /**
   * if this attribute can be assigned to the same action to the same object more than once
   * convert to string for hibernate
   * @param multiAssignableDb
   */
  public void setMultiAssignableDb(String multiAssignableDb) {
    this.multiAssignable = GrouperUtil.booleanValue(multiAssignableDb, false);
  }
  
  /**
   * if this attribute can be assigned to the same action to the same object more than once
   * @param multiAssignable1
   */
  public void setMultiAssignable(boolean multiAssignable1) {
    this.multiAssignable = multiAssignable1;
  }

  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * @return boolean
   */
  public boolean isMultiValued() {
    return multiValued;
  }

  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * convert to String for hibernate
   * @return if multivalued
   */
  public String getMultiValuedDb() {
    return this.multiValued ? "T" : "F";
  }

  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * convert to String for hibernate
   * @param multiValuedDb
   */
  public void setMultiValuedDb(String multiValuedDb) {
    this.multiValued = GrouperUtil.booleanValue(multiValuedDb, false);
  }
  
  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * @param multiValued1
   */
  public void setMultiValued(boolean multiValued1) {
    this.multiValued = multiValued1;
  }

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * id of this attribute def
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * id of this attribute def
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }
  
  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * extension of attribute expireTime
   * @return extension
   */
  public String getExtension() {
    return extension;
  }

  /**
   * extension of attribute expireTime
   * @return extension
   */
  public String getExtensionDb() {
    return extension;
  }

  /**
   * extension of attribute expireTime
   * @param extension1
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * extension of attribute expireTime
   * @param extension1
   */
  public void setExtensionDb(String extension1) {
    this.extension = extension1;
  }

  /**
   * 
   * @return the name for hibernate
   */
  public String getNameDb() {
    return name;
  }

  /**
   * 
   * @param name1
   */
  public void setNameDb(String name1) {
    this.name = name1;
  }
  

  /**
   * Get group name.
   * @return  Group name.
   * @throws  GrouperException
   */
  public String getName() throws GrouperException  {

    if (StringUtils.isBlank(this.name)) {
      LOG.error( "attributeDef is blank");
      throw new GrouperException("attributeDef is blank");
    }
    return this.name;
  } // public String getName()

  /**
   * Set attributeDef <i>name</i>.  This should not be called
   * @param   value   Set <i>extension</i> to this value.
   */
  public void setName(String value) {
    throw new InsufficientPrivilegeException("group name is system maintained: " + this.name + ", " + value);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AttributeDef)) {
      return false;
    }
    return StringUtils.equals(this.id, ((AttributeDef)obj).id);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return StringUtils.defaultString(this.id).hashCode();
  }


  /**
   * @see edu.internet2.middleware.grouper.misc.Owner#getUuid()
   */
  public String getUuid() {
    return this.getId();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "name", this.name)
      .append( "uuid", this.getId() )
      .toString();
  }

}
