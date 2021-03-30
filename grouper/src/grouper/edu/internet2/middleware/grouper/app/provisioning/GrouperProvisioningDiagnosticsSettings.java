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
  
  
  
}
