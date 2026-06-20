package edu.internet2.middleware.grouper.app.provisioning;

public class GrouperProvisioningDiagnosticsSettings {

  /**
   * if select all entities during diagnostics
   */
  private boolean diagnosticsEntitiesAllSelect;

  /**
   * if select all entities during diagnostics
   * @return
   */
  public boolean isDiagnosticsEntitiesAllSelect() {
    return diagnosticsEntitiesAllSelect;
  }


  /**
   * if select all entities during diagnostics
   * @param diagnosticsEntitiesAllSelect
   */
  public void setDiagnosticsEntitiesAllSelect(boolean diagnosticsEntitiesAllSelect) {
    this.diagnosticsEntitiesAllSelect = diagnosticsEntitiesAllSelect;
  }

  /**
   * if select all memberships during diagnostics
   */
  private boolean diagnosticsMembershipsAllSelect;



  /**
   * if select all memberships during diagnostics
   * @return
   */
  public boolean isDiagnosticsMembershipsAllSelect() {
    return diagnosticsMembershipsAllSelect;
  }


  /**
   * if select all memberships during diagnostics
   * @param diagnosticsMembershipsAllSelect
   */
  public void setDiagnosticsMembershipsAllSelect(boolean diagnosticsMembershipsAllSelect) {
    this.diagnosticsMembershipsAllSelect = diagnosticsMembershipsAllSelect;
  }

  /**
   * group name to do diagnostics for
   */
  private String diagnosticsGroupName;

  /**
   * group name to do diagnostics for
   * @return group name
   */
  public String getDiagnosticsGroupName() {
    return diagnosticsGroupName;
  }

  /**
   * group name to do diagnostics for
   * @param diagnosticsGroupName
   */
  public void setDiagnosticsGroupName(String diagnosticsGroupName) {
    this.diagnosticsGroupName = diagnosticsGroupName;
  }

  /**
   * if insert group in diagnostics
   */
  private boolean diagnosticsGroupInsert;

  /**
   * if insert group in diagnostics
   * @return if group insert
   */
  public boolean isDiagnosticsGroupInsert() {
    return this.diagnosticsGroupInsert;
  }


  /**
   * if insert group in diagnostics
   * @param diagnosticsGroupInsert1
   */
  public void setDiagnosticsGroupInsert(boolean diagnosticsGroupInsert1) {
    this.diagnosticsGroupInsert = diagnosticsGroupInsert1;
  }
  
  /**
   * if delete group in diagnostics
   */
  private boolean diagnosticsGroupDelete;

  /**
   * if delete group in diagnostics
   * @return if group delete
   */
  public boolean isDiagnosticsGroupDelete() {
    return this.diagnosticsGroupDelete;
  }


  /**
   * if delete group in diagnostics
   * @param diagnosticsGroupDelete1
   */
  public void setDiagnosticsGroupDelete(boolean diagnosticsGroupDelete1) {
    this.diagnosticsGroupDelete = diagnosticsGroupDelete1;
  }

  /**
   * if select all groups during diagnostics
   */
  private boolean diagnosticsGroupsAllSelect;

  /**
   * if select all groups during diagnostics
   * @return
   */
  public boolean isDiagnosticsGroupsAllSelect() {
    return diagnosticsGroupsAllSelect;
  }


  /**
   * if select all groups during diagnostics
   * @param selectAllGroupsDuringDiagnostics
   */
  public void setDiagnosticsGroupsAllSelect(
      boolean selectAllGroupsDuringDiagnostics) {
    this.diagnosticsGroupsAllSelect = selectAllGroupsDuringDiagnostics;
  }
  
  /**
   * if the membership should be added
   */
  private boolean diagnosticsMembershipInsert;
  
  /**
   * @return if the membership should be added
   */
  public boolean isDiagnosticsMembershipInsert() {
    return diagnosticsMembershipInsert;
  }

  /**
   * if the membership should be added
   * @param diagnosticsMembershipInsert
   */
  public void setDiagnosticsMembershipInsert(boolean diagnosticsMembershipInsert) {
    this.diagnosticsMembershipInsert = diagnosticsMembershipInsert;
  }

  /**
   * if the membership should be removed
   */
  private boolean diagnosticsMembershipDelete;
  
  /**
   * @return if the membership should be removed
   */
  public boolean isDiagnosticsMembershipDelete() {
    return diagnosticsMembershipDelete;
  }

  /**
   * if the membership should be removed
   * @param diagnosticsMembershipDelete
   */
  public void setDiagnosticsMembershipDelete(boolean diagnosticsMembershipDelete) {
    this.diagnosticsMembershipDelete = diagnosticsMembershipDelete;
  }
  
  /**
   * if delete entity in diagnostics
   */
  private boolean diagnosticsEntityDelete;

  /**
   * if delete entity in diagnostics
   * @return if entity delete
   */
  public boolean isDiagnosticsEntityDelete() {
    return this.diagnosticsEntityDelete;
  }


  /**
   * if delete entity in diagnostics
   * @param diagnosticsEntityDelete1
   */
  public void setDiagnosticsEntityDelete(boolean diagnosticsEntityDelete1) {
    this.diagnosticsEntityDelete = diagnosticsEntityDelete1;
  }
  
  /**
   * if insert entity in diagnostics
   */
  private boolean diagnosticsEntityInsert;

  /**
   * if insert entity in diagnostics
   * @return if entity insert
   */
  public boolean isDiagnosticsEntityInsert() {
    return this.diagnosticsEntityInsert;
  }


  /**
   * if insert entity in diagnostics
   * @param diagnosticsEntityInsert1
   */
  public void setDiagnosticsEntityInsert(boolean diagnosticsEntityInsert1) {
    this.diagnosticsEntityInsert = diagnosticsEntityInsert1;
  }
  
  /**
   * subject id or identifier to do diagnostics for
   */
  private String diagnosticsSubjectIdOrIdentifier;

  /**
   * subject id or identifier to do diagnostics for
   * @return subject id or identifier
   */
  public String getDiagnosticsSubjectIdOrIdentifier() {
    return diagnosticsSubjectIdOrIdentifier;
  }

  /**
   * subject id or identifier to do diagnostics for
   * @param diagnosticsSubjectIdOrIdentifier
   */
  public void setDiagnosticsSubjectIdOrIdentifier(String diagnosticsSubjectIdOrIdentifier) {
    this.diagnosticsSubjectIdOrIdentifier = diagnosticsSubjectIdOrIdentifier;
  }

  /**
   * SCIM only: if update entity in target (PATCH) during diagnostics
   */
  private boolean diagnosticsEntityUpdate;

  /**
   * SCIM only: if update entity in target (PATCH) during diagnostics
   * @return if entity update
   */
  public boolean isDiagnosticsEntityUpdate() {
    return this.diagnosticsEntityUpdate;
  }

  /**
   * SCIM only: if update entity in target (PATCH) during diagnostics
   * @param diagnosticsEntityUpdate1
   */
  public void setDiagnosticsEntityUpdate(boolean diagnosticsEntityUpdate1) {
    this.diagnosticsEntityUpdate = diagnosticsEntityUpdate1;
  }

  /**
   * SCIM only: override scimEmailFilterStrategy for this diagnostics run
   */
  private String diagnosticsScimEmailFilterStrategy;

  /**
   * SCIM only: override scimEmailFilterStrategy for this diagnostics run
   * @return strategy
   */
  public String getDiagnosticsScimEmailFilterStrategy() {
    return diagnosticsScimEmailFilterStrategy;
  }

  /**
   * SCIM only: override scimEmailFilterStrategy for this diagnostics run
   * @param diagnosticsScimEmailFilterStrategy
   */
  public void setDiagnosticsScimEmailFilterStrategy(String diagnosticsScimEmailFilterStrategy) {
    this.diagnosticsScimEmailFilterStrategy = diagnosticsScimEmailFilterStrategy;
  }

  /**
   * SCIM only: override scimNamePatchStrategy for this diagnostics run
   */
  private String diagnosticsScimNamePatchStrategy;

  /**
   * SCIM only: override scimNamePatchStrategy for this diagnostics run
   * @return strategy
   */
  public String getDiagnosticsScimNamePatchStrategy() {
    return diagnosticsScimNamePatchStrategy;
  }

  /**
   * SCIM only: override scimNamePatchStrategy for this diagnostics run
   * @param diagnosticsScimNamePatchStrategy
   */
  public void setDiagnosticsScimNamePatchStrategy(String diagnosticsScimNamePatchStrategy) {
    this.diagnosticsScimNamePatchStrategy = diagnosticsScimNamePatchStrategy;
  }

  /**
   * SCIM only: override scimEmailPatchStrategy for this diagnostics run
   */
  private String diagnosticsScimEmailPatchStrategy;

  /**
   * SCIM only: override scimEmailPatchStrategy for this diagnostics run
   * @return strategy
   */
  public String getDiagnosticsScimEmailPatchStrategy() {
    return diagnosticsScimEmailPatchStrategy;
  }

  /**
   * SCIM only: override scimEmailPatchStrategy for this diagnostics run
   * @param diagnosticsScimEmailPatchStrategy
   */
  public void setDiagnosticsScimEmailPatchStrategy(String diagnosticsScimEmailPatchStrategy) {
    this.diagnosticsScimEmailPatchStrategy = diagnosticsScimEmailPatchStrategy;
  }

  /**
   * SCIM only: test entity givenName to set/update in target
   */
  private String diagnosticsScimGivenName;

  /**
   * SCIM only: test entity givenName to set/update in target
   * @return givenName
   */
  public String getDiagnosticsScimGivenName() {
    return diagnosticsScimGivenName;
  }

  /**
   * SCIM only: test entity givenName to set/update in target
   * @param diagnosticsScimGivenName
   */
  public void setDiagnosticsScimGivenName(String diagnosticsScimGivenName) {
    this.diagnosticsScimGivenName = diagnosticsScimGivenName;
  }

  /**
   * SCIM only: test entity familyName to set/update in target
   */
  private String diagnosticsScimFamilyName;

  /**
   * SCIM only: test entity familyName to set/update in target
   * @return familyName
   */
  public String getDiagnosticsScimFamilyName() {
    return diagnosticsScimFamilyName;
  }

  /**
   * SCIM only: test entity familyName to set/update in target
   * @param diagnosticsScimFamilyName
   */
  public void setDiagnosticsScimFamilyName(String diagnosticsScimFamilyName) {
    this.diagnosticsScimFamilyName = diagnosticsScimFamilyName;
  }

  /**
   * SCIM only: test entity middleName to set/update in target
   */
  private String diagnosticsScimMiddleName;

  /**
   * SCIM only: test entity middleName to set/update in target
   * @return middleName
   */
  public String getDiagnosticsScimMiddleName() {
    return diagnosticsScimMiddleName;
  }

  /**
   * SCIM only: test entity middleName to set/update in target
   * @param diagnosticsScimMiddleName
   */
  public void setDiagnosticsScimMiddleName(String diagnosticsScimMiddleName) {
    this.diagnosticsScimMiddleName = diagnosticsScimMiddleName;
  }

  /**
   * SCIM only: test entity formatted name to set/update in target
   */
  private String diagnosticsScimFormattedName;

  /**
   * SCIM only: test entity formatted name to set/update in target
   * @return formatted name
   */
  public String getDiagnosticsScimFormattedName() {
    return diagnosticsScimFormattedName;
  }

  /**
   * SCIM only: test entity formatted name to set/update in target
   * @param diagnosticsScimFormattedName
   */
  public void setDiagnosticsScimFormattedName(String diagnosticsScimFormattedName) {
    this.diagnosticsScimFormattedName = diagnosticsScimFormattedName;
  }

  /**
   * SCIM only: test entity email value to set/update in target
   */
  private String diagnosticsScimEmailValue;

  /**
   * SCIM only: test entity email value to set/update in target
   * @return email value
   */
  public String getDiagnosticsScimEmailValue() {
    return diagnosticsScimEmailValue;
  }

  /**
   * SCIM only: test entity email value to set/update in target
   * @param diagnosticsScimEmailValue
   */
  public void setDiagnosticsScimEmailValue(String diagnosticsScimEmailValue) {
    this.diagnosticsScimEmailValue = diagnosticsScimEmailValue;
  }

  /**
   * SCIM only: test entity email type to set/update in target
   */
  private String diagnosticsScimEmailType;

  /**
   * SCIM only: test entity email type to set/update in target
   * @return email type
   */
  public String getDiagnosticsScimEmailType() {
    return diagnosticsScimEmailType;
  }

  /**
   * SCIM only: test entity email type to set/update in target
   * @param diagnosticsScimEmailType
   */
  public void setDiagnosticsScimEmailType(String diagnosticsScimEmailType) {
    this.diagnosticsScimEmailType = diagnosticsScimEmailType;
  }

}
