package edu.internet2.middleware.grouperDuo;

/**
 * Class representing the user object from Duo's Admin API.
 *
 * See https://duo.com/docs/adminapi#users
 */
public class GrouperDuoAdministrator {

  private String adminId;

  private String email;

  private Long lastLogin;

  private String name;

  private boolean isPasswordChangeRequired;

  private String phone;

  private boolean isRestrictedByAdminUnits;

  private String role;

  private boolean isActive;

  public String getAdminId() {
    return adminId;
  }

  public void setAdminId(String adminId) {
    this.adminId = adminId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Long getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(Long lastLogin) {
    this.lastLogin = lastLogin;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isPasswordChangeRequired() {
    return isPasswordChangeRequired;
  }

  public void setPasswordChangeRequired(boolean passwordChangeRequired) {
    isPasswordChangeRequired = passwordChangeRequired;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public boolean isRestrictedByAdminUnits() {
    return isRestrictedByAdminUnits;
  }

  public void setRestrictedByAdminUnits(boolean restrictedByAdminUnits) {
    isRestrictedByAdminUnits = restrictedByAdminUnits;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  @Override
  public String toString() {
    return String.format("[%s] %s <%s> - ", this.adminId, this.name, this.email, this.role);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (!(o instanceof GrouperDuoAdministrator))
      return false;

    return ((GrouperDuoAdministrator) o).getAdminId().equals(this.getAdminId());
  }

}
