/**
 * Copyright 2014 Internet2
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
 */
package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITMembershipView {


  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

  /** constant for field name for: groupSetActiveDb */
  public static final String FIELD_GROUP_SET_ACTIVE_DB = "groupSetActiveDb";

  /** constant for field name for: groupSetEndTimeDb */
  public static final String FIELD_GROUP_SET_END_TIME_DB = "groupSetEndTimeDb";

  /** constant for field name for: groupSetId */
  public static final String FIELD_GROUP_SET_ID = "groupSetId";

  /** constant for field name for: groupSetParentId */
  public static final String FIELD_GROUP_SET_PARENT_ID = "groupSetParentId";

  /** constant for field name for: groupSetStartTimeDb */
  public static final String FIELD_GROUP_SET_START_TIME_DB = "groupSetStartTimeDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: member */
  public static final String FIELD_MEMBER = "member";

  /** constant for field name for: memberId */
  public static final String FIELD_MEMBER_ID = "memberId";

  /** constant for field name for: membershipActiveDb */
  public static final String FIELD_MEMBERSHIP_ACTIVE_DB = "membershipActiveDb";

  /** constant for field name for: membershipEndTimeDb */
  public static final String FIELD_MEMBERSHIP_END_TIME_DB = "membershipEndTimeDb";

  /** constant for field name for: membershipFieldId */
  public static final String FIELD_MEMBERSHIP_FIELD_ID = "membershipFieldId";

  /** constant for field name for: membershipId */
  public static final String FIELD_MEMBERSHIP_ID = "membershipId";
  
  /** constant for field name for: membershipSourceId */
  public static final String FIELD_MEMBERSHIP_SOURCE_ID = "membershipSourceId";

  /** constant for field name for: membershipStartTimeDb */
  public static final String FIELD_MEMBERSHIP_START_TIME_DB = "membershipStartTimeDb";

  /** constant for field name for: ownerAttrDefId */
  public static final String FIELD_OWNER_ATTR_DEF_ID = "ownerAttrDefId";

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerId */
  public static final String FIELD_OWNER_ID = "ownerId";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";
  
  /** id of this type */
  private String id;
  
  /** id in grouper_pit_memberships */
  private String membershipId;
  
  /** source id in grouper_pit_memberships */
  private String membershipSourceId;
  
  /** id in grouper_pit_group_set */
  private String groupSetId;

  /** ownerId */
  private String ownerId;
  
  /** ownerAttrDefId */
  private String ownerAttrDefId;
  
  /** ownerGroupId */
  private String ownerGroupId;
  
  /** ownerStemId */
  private String ownerStemId;
  
  /** memberId */
  private String memberId;
  
  /** fieldId */
  private String fieldId;
  
  /** membershipFieldId */
  private String membershipFieldId;
  
  /** groupSetParentId */
  private String groupSetParentId;
  
  /** depth */
  private int depth;
  
  /** groupSetActiveDb */
  private String groupSetActiveDb;
  
  /** groupSetStartTimeDb */
  private Long groupSetStartTimeDb;
  
  /** groupSetEndTimeDb */
  private Long groupSetEndTimeDb;
  
  /** membershipActiveDb */
  private String membershipActiveDb;
  
  /** membershipStartTimeDb */
  private Long membershipStartTimeDb;
  
  /** membershipEndTimeDb */
  private Long membershipEndTimeDb;
  
  /** pitMember */
  private PITMember pitMember;
  
  /** pitMembership */
  private PITMembership pitMembership;

  /**
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return ownerId
   */
  public String getOwnerId() {
    return ownerId;
  }

  /**
   * @param ownerId
   */
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * @return ownerAttrDefId
   */
  public String getOwnerAttrDefId() {
    return ownerAttrDefId;
  }

  /**
   * @param ownerAttrDefId
   */
  public void setOwnerAttrDefId(String ownerAttrDefId) {
    this.ownerAttrDefId = ownerAttrDefId;
    if (ownerAttrDefId != null) {
      setOwnerId(ownerAttrDefId);
    }
  }

  /**
   * @return ownerGroupId
   */
  public String getOwnerGroupId() {
    return ownerGroupId;
  }

  /**
   * @param ownerGroupId
   */
  public void setOwnerGroupId(String ownerGroupId) {
    this.ownerGroupId = ownerGroupId;
    if (ownerGroupId != null) {
      setOwnerId(ownerGroupId);
    }
  }

  /**
   * @return ownerStemId
   */
  public String getOwnerStemId() {
    return ownerStemId;
  }

  /**
   * @param ownerStemId
   */
  public void setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
    if (ownerStemId != null) {
      setOwnerId(ownerStemId);
    }
  }

  /**
   * @return memberId
   */
  public String getMemberId() {
    return memberId;
  }

  /**
   * @param memberId
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }
  
  /**
   * @param pitMember
   */
  public void setPITMember(PITMember pitMember) {
    this.pitMember = pitMember;
  }
  
  /**
   * @return pitMember
   */
  public PITMember getPITMember() {
    if (this.pitMember != null) {
      return this.pitMember;
    }
    
    this.pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(this.memberId, true);
    return this.pitMember;
  }
  
  /**
   * @return pitMembership
   */
  public PITMembership getPITMembership() {
    if (this.pitMembership != null) {
      return this.pitMembership;
    }
    
    this.pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(this.membershipId, true);
    return this.pitMembership;
  }

  /**
   * @return fieldId
   */
  public String getFieldId() {
    return fieldId;
  }

  /**
   * @param fieldId
   */
  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }
  
  /**
   * @return membershipId
   */
  public String getMembershipId() {
    return membershipId;
  }

  /**
   * @param membershipId
   */
  public void setMembershipId(String membershipId) {
    this.membershipId = membershipId;
  }

  /**
   * @return groupSetId
   */
  public String getGroupSetId() {
    return groupSetId;
  }

  /**
   * @param groupSetId
   */
  public void setGroupSetId(String groupSetId) {
    this.groupSetId = groupSetId;
  }

  /**
   * @return membershipFieldId
   */
  public String getMembershipFieldId() {
    return membershipFieldId;
  }

  /**
   * @param membershipFieldId
   */
  public void setMembershipFieldId(String membershipFieldId) {
    this.membershipFieldId = membershipFieldId;
  }

  /**
   * @return groupSetParentId
   */
  public String getGroupSetParentId() {
    return groupSetParentId;
  }

  /**
   * @param groupSetParentId
   */
  public void setGroupSetParentId(String groupSetParentId) {
    this.groupSetParentId = groupSetParentId;
  }

  /**
   * @return depth
   */
  public int getDepth() {
    return depth;
  }

  /**
   * @param depth
   */
  public void setDepth(int depth) {
    this.depth = depth;
  }

  /**
   * @return groupSetActiveDb
   */
  public String getGroupSetActiveDb() {
    return groupSetActiveDb;
  }

  /**
   * @param groupSetActiveDb
   */
  public void setGroupSetActiveDb(String groupSetActiveDb) {
    this.groupSetActiveDb = groupSetActiveDb;
  }

  /**
   * @return groupSetStartTimeDb
   */
  public Long getGroupSetStartTimeDb() {
    return groupSetStartTimeDb;
  }

  /**
   * @param groupSetStartTimeDb
   */
  public void setGroupSetStartTimeDb(Long groupSetStartTimeDb) {
    this.groupSetStartTimeDb = groupSetStartTimeDb;
  }

  /**
   * @return groupSetEndTimeDb
   */
  public Long getGroupSetEndTimeDb() {
    return groupSetEndTimeDb;
  }

  /**
   * @param groupSetEndTimeDb
   */
  public void setGroupSetEndTimeDb(Long groupSetEndTimeDb) {
    this.groupSetEndTimeDb = groupSetEndTimeDb;
  }
  
  /**
   * @return membershipActiveDb
   */
  public String getMembershipActiveDb() {
    return membershipActiveDb;
  }

  /**
   * @param membershipActiveDb
   */
  public void setMembershipActiveDb(String membershipActiveDb) {
    this.membershipActiveDb = membershipActiveDb;
  }

  /**
   * @return membershipStartTimeDb
   */
  public Long getMembershipStartTimeDb() {
    return membershipStartTimeDb;
  }

  /**
   * @param membershipStartTimeDb
   */
  public void setMembershipStartTimeDb(Long membershipStartTimeDb) {
    this.membershipStartTimeDb = membershipStartTimeDb;
  }

  /**
   * @return membershipEndTimeDb
   */
  public Long getMembershipEndTimeDb() {
    return membershipEndTimeDb;
  }

  /**
   * @param membershipEndTimeDb
   */
  public void setMembershipEndTimeDb(Long membershipEndTimeDb) {
    this.membershipEndTimeDb = membershipEndTimeDb;
  }

  /**
   * @return true if active
   */
  public boolean isActive() {
    if (membershipActiveDb == null || groupSetActiveDb == null) {
      throw new RuntimeException("membershipActiveDb and groupSetActiveDb should not be null.");
    }
    
    if (membershipActiveDb.equals("T") && groupSetActiveDb.equals("T")) {
      return true;
    }
    
    return false;
  }
  
  /**
   * @return start time
   */
  public Timestamp getStartTime() {
    if (membershipStartTimeDb == null || groupSetStartTimeDb == null) {
      throw new RuntimeException("membershipStartTimeDb and groupSetStartTimeDb should not be null.");
    }
    
    Long startTime = GrouperUtil.getMaxLongValue(membershipStartTimeDb, groupSetStartTimeDb);
    
    return new Timestamp(startTime / 1000);
  }
  
  /**
   * @return end time
   */
  public Timestamp getEndTime() {
    if (membershipEndTimeDb == null && groupSetEndTimeDb == null) {
      return null;
    }
    
    Long endTime = GrouperUtil.getMinLongValue(membershipEndTimeDb, groupSetEndTimeDb);

    return new Timestamp(endTime / 1000);
  }

  
  /**
   * @return the membershipSourceId
   */
  public String getMembershipSourceId() {
    return membershipSourceId;
  }

  
  /**
   * @param membershipSourceId the membershipSourceId to set
   */
  public void setMembershipSourceId(String membershipSourceId) {
    this.membershipSourceId = membershipSourceId;
  }
}
