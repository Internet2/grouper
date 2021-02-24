package edu.internet2.middleware.grouper.app.syncToGrouper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import edu.internet2.middleware.grouper.MembershipSave;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;

@GcPersistableClass(tableName="testgrouper_syncgr_membership", defaultFieldPersist=GcPersist.doPersist)
public class SyncMembershipToGrouperBean implements GcSqlAssignPrimaryKey {

  public String convertToLabel() {
    String membershipLabel = "membership '" + this.getGroupName() 
      + "', '" + this.getSubjectSourceId() + "', '" 
      + (StringUtils.equals("g:gsa", this.getSubjectSourceId()) ? this.getSubjectIdentifier() : this.getSubjectId())
      + "'";
    return membershipLabel;
  }

  /**
   * convert for multkey by group name, source id, and subject id
   * @return
   */
  public MultiKey convertToMultikey() {
    return new MultiKey(this.getGroupName(),
        this.getSubjectSourceId(), 
        StringUtils.equals("g:gsa", this.getSubjectSourceId()) 
          ? this.getSubjectIdentifier()
            : this.getSubjectId());
  }

  /**
   * 
   * @return
   */
  public MembershipSave convertToMembershipSave() {
    return new MembershipSave()
        .assignImmediateMembershipId(this.getImmediateMembershipId())
        .assignGroupName(this.getGroupName())
        .assignSubjectSourceId(this.subjectSourceId)
        .assignSubjectId(this.subjectId)
        .assignSubjectIdentifier(this.subjectIdentifier)
        .assignImmediateMshipDisabledTime(this.immediateMshipDisabledTime)
        .assignImmediateMshipEnabledTime(this.immediateMshipEnabledTime);
  }

  public SyncMembershipToGrouperBean() {
  }

  
  
  @Override
  public boolean equals(Object other) {

    
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof SyncMembershipToGrouperBean)) {
      return false;
    }
    
    SyncMembershipToGrouperBean that = (SyncMembershipToGrouperBean) other;
    EqualsBuilder equalsBuilder = new EqualsBuilder()
      .append(this.groupName, that.groupName)
      .append(this.subjectSourceId, that.subjectSourceId);
    
    if (StringUtils.equals("g:gsa", this.subjectSourceId)) {
      equalsBuilder.append(this.subjectIdentifier, that.subjectIdentifier);
    } else {
      equalsBuilder.append(this.subjectId, that.subjectId);
    }
    equalsBuilder.append(this.immediateMshipDisabledTime, that.immediateMshipDisabledTime);
    equalsBuilder.append(this.immediateMshipEnabledTime, that.immediateMshipEnabledTime);
    return  equalsBuilder.isEquals();
    
  }



  /**
   * group name
   */
  private String groupName;
  
  /**
   * subject source id
   */
  private String subjectSourceId;

  /**
   * subject id
   */
  private String subjectId;
  
  /**
   * subject identifier
   */
  private String subjectIdentifier;
  
  /**
   * group name
   * @return group name
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * group name
   * @param theGroupName
   * @return
   */
  public SyncMembershipToGrouperBean assignGroupName(String theGroupName) {
    this.groupName = theGroupName;
    return this;
  }
  
  /**
   * subject source id
   * @return
   */
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  /**
   * subject source id
   * @param theSourceId
   * @return this for chaining
   */
  public SyncMembershipToGrouperBean assignSubjectSourceId(String theSourceId) {
    this.subjectSourceId = theSourceId;
    return this;
  }

  /**
   * subject id
   * @return
   */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * 
   * @param theSubjectId
   * @return this for chaining
   */
  public SyncMembershipToGrouperBean assignSubjectId(String theSubjectId) {
    this.subjectId = theSubjectId;
    return this;
  }
  
  /**
   * subject identifier
   * @return
   */
  public String getSubjectIdentifier() {
    return subjectIdentifier;
  }

  /**
   * 
   * @param theSubjectIdentifier
   * @return this for chaining
   */
  public SyncMembershipToGrouperBean assignSubjectIdentifier(String theSubjectIdentifier) {
    this.subjectIdentifier = theSubjectIdentifier;
    return this;
  }
  
  /**
   * 
   * @return
   */
  public String getImmediateMembershipIdForInsert() {
    return immediateMembershipIdForInsert;
  }

  /**
   * if inserting and not wanting an assigned id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private String immediateMembershipIdForInsert;

  /**
   * 
   * @param theImmediateMembershipIdForInsert
   * @return this for chaining
   */
  public SyncMembershipToGrouperBean assignImmediateMembershipIdForInsert(String theImmediateMembershipIdForInsert) {
    this.immediateMembershipIdForInsert = theImmediateMembershipIdForInsert;
    return this;
  }
  
  /**
   * membership id
   */
  @GcPersistableField(primaryKey=true)
  private String immediateMembershipId;

  
  public String getImmediateMembershipId() {
    return immediateMembershipId;
  }

  public SyncMembershipToGrouperBean assignImmediateMembershipId(String id1) {
    this.immediateMembershipId = id1;
    return this;
  }

  public void store() {
    new GcDbAccess().storeToDatabase(this);
  }

  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (StringUtils.isBlank(this.immediateMembershipId)) {
      if (!StringUtils.isBlank(this.immediateMembershipIdForInsert)) {
        this.immediateMembershipId = this.immediateMembershipIdForInsert;
      } else {
        this.immediateMembershipId = GrouperUuid.getUuid();
      }
      return true;
    }
    return false;
  }
  
  /**
   * immediate mship disabled time
   */
  private Long immediateMshipDisabledTime = null;

  /**
   * immediate mship disabled time
   * @return
   */
  public Long getImmediateMshipDisabledTime() {
    return immediateMshipDisabledTime;
  }
  
  /**
   * immediate mship enabled time
   */
  private Long immediateMshipEnabledTime = null;

  /**
   * immediate mship enabled time
   * @return
   */
  public Long getImmediateMshipEnabledTime() {
    return immediateMshipEnabledTime;
  }

  /**
   * immediate mship disabled time
   * @param theTime
   * @return
   */
  public SyncMembershipToGrouperBean assignImmediateMshipDisabledTime(Long theTime) {
    this.immediateMshipDisabledTime = theTime;
    return this;
  }

  /**
   * immediate mship enabled time
   * @param theTime
   * @return this for chaining
   */
  public SyncMembershipToGrouperBean assignImmediateMshipEnabledTime(Long theTime) {
    this.immediateMshipEnabledTime = theTime;
    return this;
  }
  
}
