package edu.internet2.middleware.grouper.app.syncToGrouper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import edu.internet2.middleware.grouper.PrivilegeStemSave;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;

@GcPersistableClass(tableName="testgrouper_syncgr_priv_stem", defaultFieldPersist=GcPersist.doPersist)
public class SyncPrivilegeStemToGrouperBean implements GcSqlAssignPrimaryKey {

  /**
   * e.g. stemAdmins, creators, stemAttrReaders, etc
   */
  private String fieldName;
  
  /**
   * e.g. stemAdmins, creators, stemAttrReaders, etc
   * @return
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * e.g. stemAdmins, creators, stemAttrReaders, etc
   * @param theFieldName
   * @return this for chaining
   */
  public SyncPrivilegeStemToGrouperBean assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  public String convertToLabel() {
    String membershipLabel = "privilege stem '" + this.getStemName() 
      + "', '" + this.getSubjectSourceId() + "', '" 
      + (StringUtils.equals("g:gsa", this.getSubjectSourceId()) ? this.getSubjectIdentifier() : this.getSubjectId())
      + "', " + this.fieldName;
    return membershipLabel;
  }

  /**
   * convert for multkey by stem name, source id, and subject id
   * @return
   */
  public MultiKey convertToMultikey() {
    return new MultiKey(this.getStemName(),
        this.getSubjectSourceId(), 
        StringUtils.equals("g:gsa", this.getSubjectSourceId()) 
          ? this.getSubjectIdentifier()
            : this.getSubjectId(), this.fieldName);
  }

  /**
   * 
   * @return
   */
  public PrivilegeStemSave convertToPrivilegeStemSave() {
    return new PrivilegeStemSave()
        .assignImmediateMembershipId(this.getImmediateMembershipId())
        .assignStemName(this.getStemName())
        .assignSubjectSourceId(this.subjectSourceId)
        .assignSubjectId(this.subjectId)
        .assignSubjectIdentifier(this.subjectIdentifier)
        .assignFieldName(this.fieldName);
  }

  public SyncPrivilegeStemToGrouperBean() {
  }

  
  
  @Override
  public boolean equals(Object other) {

    
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof SyncPrivilegeStemToGrouperBean)) {
      return false;
    }
    
    SyncPrivilegeStemToGrouperBean that = (SyncPrivilegeStemToGrouperBean) other;
    EqualsBuilder equalsBuilder = new EqualsBuilder()
      .append(this.stemName, that.stemName)
      .append(this.subjectSourceId, that.subjectSourceId);
    
    if (StringUtils.equals("g:gsa", this.subjectSourceId)) {
      equalsBuilder.append(this.subjectIdentifier, that.subjectIdentifier);
    } else {
      equalsBuilder.append(this.subjectId, that.subjectId);
    }
    equalsBuilder.append(this.fieldName, that.fieldName);
    return  equalsBuilder.isEquals();
    
  }



  /**
   * stem name
   */
  private String stemName;
  
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
   * stem name
   * @return stem name
   */
  public String getStemName() {
    return stemName;
  }

  /**
   * stem name
   * @param theStemName
   * @return
   */
  public SyncPrivilegeStemToGrouperBean assignStemName(String theStemName) {
    this.stemName = theStemName;
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
  public SyncPrivilegeStemToGrouperBean assignSubjectSourceId(String theSourceId) {
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
  public SyncPrivilegeStemToGrouperBean assignSubjectId(String theSubjectId) {
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
  public SyncPrivilegeStemToGrouperBean assignSubjectIdentifier(String theSubjectIdentifier) {
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
  public SyncPrivilegeStemToGrouperBean assignImmediateMembershipIdForInsert(String theImmediateMembershipIdForInsert) {
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

  public SyncPrivilegeStemToGrouperBean assignImmediateMembershipId(String id1) {
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
  
}
