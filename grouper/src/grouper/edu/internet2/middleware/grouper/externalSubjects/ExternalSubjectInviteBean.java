package edu.internet2.middleware.grouper.externalSubjects;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * bean to hold invite
 *
 */
public class ExternalSubjectInviteBean {

  /**
   * make a bean based on the owner attribute assign
   * @param ownerAttributeAssign
   */
  public ExternalSubjectInviteBean() {
    
  }

  /**
   * make a bean based on the owner attribute assign
   * @param ownerAttributeAssign
   */
  public ExternalSubjectInviteBean(AttributeAssign ownerAttributeAssign) {
    
    //lets get all the attribute assigns and values
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
      .findAssignmentsOnAssignments(GrouperUtil.toSet(ownerAttributeAssign), AttributeAssignType.stem_asgn, true);
    
    Map<String, String> attributeDefNameToValueMap = new HashMap<String, String>();
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      
      AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
      String valueString = attributeAssign.getValueDelegate().retrieveValueString();
      attributeDefNameToValueMap.put(attributeDefName.getName(), valueString);
      
    }
    
    //get all the attributes
    //externalSubjectInviteDateName
    //externalSubjectInviteEmailAddressName
    //externalSubjectInviteEmailName
    //externalSubjectInviteEmailWhenRegisteredName
    //externalSubjectInviteExpireDateName
    //externalSubjectInviteGroupUuidsName
    //externalSubjectInviteMemberIdName
    //externalSubjectInviteUuidName
    this.inviteDate = GrouperUtil.longValue(attributeDefNameToValueMap.get(ExternalSubjectAttrFramework.externalSubjectInviteDateName()), -1);
    this.emailAddress = attributeDefNameToValueMap.get(ExternalSubjectAttrFramework.externalSubjectInviteEmailAddressName());
    this.email = attributeDefNameToValueMap.get(ExternalSubjectAttrFramework.externalSubjectInviteEmailName());
    
    {
      String externalSubjectInviteEmailWhenRegistered = attributeDefNameToValueMap.get(ExternalSubjectAttrFramework.externalSubjectInviteEmailWhenRegisteredName());
      this.emailsWhenRegistered = GrouperUtil.splitTrimToSet(externalSubjectInviteEmailWhenRegistered, ",");
    }
    
    this.expireDate = GrouperUtil.longObjectValue(attributeDefNameToValueMap.get(ExternalSubjectAttrFramework.externalSubjectInviteExpireDateName()), true);
    
    {
      String externalSubjectInviteGroupUuids = attributeDefNameToValueMap.get(ExternalSubjectAttrFramework.externalSubjectInviteGroupUuidsName());
      this.groupIds = GrouperUtil.splitTrimToSet(externalSubjectInviteGroupUuids, ",");
    }
    this.memberId = attributeDefNameToValueMap.get(ExternalSubjectAttrFramework.externalSubjectInviteMemberIdName());
    this.uuid = attributeDefNameToValueMap.get(ExternalSubjectAttrFramework.externalSubjectInviteUuidName());
    
  }

  /**
   * if this invite expired
   * @return true if expired
   */
  public boolean isExpired() {
    return this.getExpireDate() != null && this.getExpireDate() < System.currentTimeMillis();
  }
  
  /**
   * clone this object
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    ExternalSubjectInviteBean externalSubjectInviteBean = new ExternalSubjectInviteBean();
    externalSubjectInviteBean.setEmail(this.email);
    externalSubjectInviteBean.setEmailAddress(this.emailAddress);
    externalSubjectInviteBean.setEmailsWhenRegistered(this.emailsWhenRegistered == null ? null : new HashSet<String>(this.emailsWhenRegistered));
    externalSubjectInviteBean.setExpireDate(this.expireDate);
    externalSubjectInviteBean.setGroupIds(this.groupIds == null ? null : new HashSet<String>(this.groupIds));
    externalSubjectInviteBean.setInviteDate(this.inviteDate);
    externalSubjectInviteBean.setMemberId(this.memberId);
    externalSubjectInviteBean.setUuid(this.uuid);
    return externalSubjectInviteBean;
  }

  /** email address invite beans sent to */
  private String emailAddress;

  /**
   * when the invite was sent out, millis since 1970
   */
  private long inviteDate;
  
  /**
   * when this expires: millis since 1970, or null if doesnt expire
   */
  private Long expireDate;
  
  /**
   * email that was sent to the user (well, the first 2000 chars)
   */
  private String email;

  /**
   * list of emails to send to when the registration happens
   */
  private Set<String> emailsWhenRegistered;

  /**
   * list of group ids to provision to when the user registers
   */
  private Set<String> groupIds;
  
  /**
   * member id who invited the user
   */
  private String memberId;
  
  /**
   * uuid in the email sent to the invitee
   */
  private String uuid;

  /**
   * email address invite beans sent to
   * @return email address
   */
  public String getEmailAddress() {
    return this.emailAddress;
  }

  /**
   * email address invite beans sent to
   * @param emailAddress1
   */
  public void setEmailAddress(String emailAddress1) {
    this.emailAddress = emailAddress1;
  }

  /**
   * when the invite was sent out, millis since 1970
   * @return invite date
   */
  public long getInviteDate() {
    return this.inviteDate;
  }

  /**
   * when the invite was sent out, millis since 1970
   * @param inviteDate1
   */
  public void setInviteDate(long inviteDate1) {
    this.inviteDate = inviteDate1;
  }

  /**
   * when this expires: millis since 1970, or null if doesnt expire
   * @return expire date
   */
  public Long getExpireDate() {
    return this.expireDate;
  }

  /**
   * when this expires: millis since 1970, or null if doesnt expire
   * @param expireDate1
   */
  public void setExpireDate(Long expireDate1) {
    this.expireDate = expireDate1;
  }

  /**
   * email that was sent to the user (well, the first 2000 chars)
   * @return the email
   */
  public String getEmail() {
    return this.email;
  }

  /**
   * email that was sent to the user (well, the first 2000 chars)
   * @param email1
   */
  public void setEmail(String email1) {
    this.email = email1;
  }

  /**
   * list of emails to send to when the registration happens
   * @return list of emails
   */
  public Set<String> getEmailsWhenRegistered() {
    return this.emailsWhenRegistered;
  }

  /**
   * list of emails to send to when the registration happens
   * @param emailsWhenRegistered1
   */
  public void setEmailsWhenRegistered(Set<String> emailsWhenRegistered1) {
    this.emailsWhenRegistered = emailsWhenRegistered1;
  }

  /**
   * list of group ids to provision to when the user registers
   * @return group ids
   */
  public Set<String> getGroupIds() {
    return this.groupIds;
  }

  /**
   * list of group ids to provision to when the user registers
   * @param groupIds1
   */
  public void setGroupIds(Set<String> groupIds1) {
    this.groupIds = groupIds1;
  }

  /**
   * member id who invited the user
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * member id who invited the user
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

  /**
   * uuid in the email sent to the invitee
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid in the email sent to the invitee
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }
  
  /**
   * delete this from the database
   * @return true if deleted, false if not there
   */
  public boolean deleteFromDb() {
    
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByAttributeDefNameAndValueString(
          ExternalSubjectAttrFramework.externalSubjectInviteUuidAttributeDefName().getId(), this.getUuid(), null);
    
    if (GrouperUtil.length(attributeAssigns) == 0) {
      return false;
    }
    for (AttributeAssign attributeAssign : attributeAssigns) {
      
      //get the owner
      AttributeAssign ownerAttributeAssign = attributeAssign.getOwnerAttributeAssign();
      
      //just delete and it should cascade...
      ownerAttributeAssign.delete();
      
    }
    return true;
  }

  /**
   * store this to the DB, must have open root session
   */
  public void storeToDb() {
    //passed validation, lets setup the invites
    //lets see if this user already has an invite... should we re-use the uuid?  
    //no, because then they will expire at different times.  note: we can tie them together with email address...
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
      throw new RuntimeException("Must have an open wheel or root grouper session");
    }
    
    Stem externalInviteStem = StemFinder.findByName(grouperSession, ExternalSubjectAttrFramework.attributeExternalSubjectInviteStemName(), true);

    AttributeAssign attributeAssign = externalInviteStem
      .getAttributeDelegate().addAttribute(ExternalSubjectAttrFramework.externalSubjectInviteAttributeDefName()).getAttributeAssign();

    attributeAssign.getAttributeValueDelegate().assignValue(
      ExternalSubjectAttrFramework.externalSubjectInviteDateName(), "" + System.currentTimeMillis());
  
    attributeAssign.getAttributeValueDelegate().assignValue(
      ExternalSubjectAttrFramework.externalSubjectInviteEmailAddressName(), this.getEmailAddress());
  
    attributeAssign.getAttributeValueDelegate().assignValue(
      ExternalSubjectAttrFramework.externalSubjectInviteUuidName(), this.getUuid());

    attributeAssign.getAttributeValueDelegate().assignValue(
      ExternalSubjectAttrFramework.externalSubjectInviteEmailName(), StringUtils.abbreviate(this.getEmail(), 2000));

    if (this.getExpireDate() != null) {

      attributeAssign.getAttributeValueDelegate().assignValue(
          ExternalSubjectAttrFramework.externalSubjectInviteExpireDateName(), Long.toString(this.getExpireDate()));
      attributeAssign.setDisabledTime(new Timestamp(this.getExpireDate()));
      attributeAssign.saveOrUpdate();
    }
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        ExternalSubjectAttrFramework.externalSubjectInviteMemberIdName(), this.getMemberId());

    if (GrouperUtil.length(this.getEmailsWhenRegistered()) > 0) {

      String emailAddressesWhenRegisteredString = GrouperUtil.join(this.getEmailsWhenRegistered().iterator(), ", ");

      if (GrouperUtil.stringLength(emailAddressesWhenRegisteredString) > 2000) {
        throw new RuntimeException("Too many email addresses when registered: " + emailAddressesWhenRegisteredString);
      }
      
      attributeAssign.getAttributeValueDelegate().assignValue(
          ExternalSubjectAttrFramework.externalSubjectInviteEmailWhenRegisteredName(), 
          emailAddressesWhenRegisteredString);

    }
    
    if (GrouperUtil.length(this.getGroupIds()) > 0) {
  
      String groupIdsToAssignString = null;
      if (GrouperUtil.length(this.getGroupIds()) > 0) {
        groupIdsToAssignString = GrouperUtil.join(this.getGroupIds().iterator(), ", ");
      }
      
      attributeAssign.getAttributeValueDelegate().assignValue(
          ExternalSubjectAttrFramework.externalSubjectInviteGroupUuidsName(), 
          groupIdsToAssignString);
      
    }  

  }

  
  /**
   * find an external subject invite bean by uuid
   * @param uuid
   * @return the beans
   */
  public static ExternalSubjectInviteBean findByUuid(String uuid) {
    
    AttributeDefName attributeDefName = ExternalSubjectAttrFramework
      .externalSubjectInviteUuidAttributeDefName();

    List<ExternalSubjectInviteBean> externalSubjectInviteBeans = findByField(attributeDefName, uuid);
    
    return GrouperUtil.listPopOne(externalSubjectInviteBeans);    
  }
  
  /**
   * 
   * @param emailAddress
   * @return the beans
   */
  public static List<ExternalSubjectInviteBean> findByEmailAddress(String emailAddress) {
    
    AttributeDefName attributeDefName = ExternalSubjectAttrFramework
      .externalSubjectInviteEmailAddressAttributeDefName();

    return findByField(attributeDefName, emailAddress);
    
  }
  
  /**
   * 
   * @param uuid
   * @return the beans
   */
  public static List<ExternalSubjectInviteBean> findByEmailAddressViaUuid(String uuid) {
    
    ExternalSubjectInviteBean externalSubjectInviteBean = findByUuid(uuid);
    
    if (externalSubjectInviteBean == null) {
      return null;
    }
        
    String emailAddress = externalSubjectInviteBean.getEmailAddress();
    if (StringUtils.isBlank(emailAddress)) {
      throw new RuntimeException("Why is there no email address??? " + uuid);
    }
    
    List<ExternalSubjectInviteBean> externalSubjectInviteBeans = findByEmailAddress(emailAddress);

    if (GrouperUtil.length(externalSubjectInviteBeans) < 1) {
      throw new RuntimeException("Why did it find by uuid but not email address??? " + uuid + ", " + emailAddress);
    }
    
    return externalSubjectInviteBeans;
    
  }

  /**
   * 
   * @param attributeDefName 
   * @param value
   * @return the beans
   */
  private static List<ExternalSubjectInviteBean> findByField(AttributeDefName attributeDefName, String value) {
    
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByAttributeDefNameAndValueString(attributeDefName.getId(), value, null);
    
    List<ExternalSubjectInviteBean> externalSubjectInviteBeans 
      = new ArrayList<ExternalSubjectInviteBean>();

    for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {

      //lets get the owner of this
      AttributeAssign attributeAssignOwner = attributeAssign.getOwnerAttributeAssign();

      ExternalSubjectInviteBean externalSubjectInviteBean = new ExternalSubjectInviteBean(attributeAssignOwner);

      //see if expired, note, they should be inactive anyways, but check just in case
      if (externalSubjectInviteBean.getExpireDate() != null 
          && externalSubjectInviteBean.getExpireDate() < System.currentTimeMillis()) {

        continue;

      }

      externalSubjectInviteBeans.add(externalSubjectInviteBean);
    }

    return externalSubjectInviteBeans;

  }
  
  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    
    if (!StringUtils.isBlank(this.emailAddress)) {
      result.append("emailAddress: ").append(this.emailAddress).append(", ");
    }
    if (!StringUtils.isBlank(this.uuid)) {
      result.append("uuid: ").append(this.uuid).append(", ");
    }
    if (GrouperUtil.length(this.groupIds) > 0) {
      result.append("groupIds: ").append(GrouperUtil.join(this.groupIds.iterator(), ',')).append(", ");
    }
    if (GrouperUtil.length(this.emailsWhenRegistered) > 0) {
      result.append("emailsWhenRegistered: ").append(GrouperUtil.join(this.emailsWhenRegistered.iterator(), ',')).append(", ");
    }
    if (this.inviteDate > 0) {
      result.append("inviteDate: ").append(new Timestamp(this.inviteDate).toString()).append(", ");
    }
    if (!StringUtils.isBlank(this.memberId)) {
      result.append("inviterMemberId: ").append(this.memberId).append(", ");
    }
    if (this.expireDate != null && this.expireDate > 0) {
      result.append("expireDate: ").append(new Timestamp(this.expireDate).toString()).append(", ");
    }
    if (!StringUtils.isBlank(this.email)) {
      result.append("email: ").append(StringUtils.abbreviate(this.email, 200)).append(", ");
    }
    return result.toString();
  }
  
}