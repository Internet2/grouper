package edu.internet2.middleware.grouper.app.attestation;

public class GrouperAttestationObjectAttributes {
  
  private String markerAttributeAssignId;

  public GrouperAttestationObjectAttributes(String id, String name, String markerAttributeAssignId) {
    this.id = id;
    this.name = name;
    this.markerAttributeAssignId = markerAttributeAssignId;
  }
  
  private String id;
  private String name;
  
  private boolean isOwnedByGroup;
  private boolean isOwnedByStem;
  
  private String daysUntilRecertify;
  
  private String calculatedDaysLeft;
  
  private String dateCertified;
  
  private String minDateCertified;
  
  private String attestationDirectAssign;
  
  private String hasAttestation;
  
  private String stemScope;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isOwnedByGroup() {
    return isOwnedByGroup;
  }
  
  public void setOwnedByGroup(boolean isOwnedByGroup) {
    this.isOwnedByGroup = isOwnedByGroup;
  }

  public boolean isOwnedByStem() {
    return isOwnedByStem;
  }
  
  public void setOwnedByStem(boolean isOwnedByStem) {
    this.isOwnedByStem = isOwnedByStem;
  }
  
  public String getMarkerAttributeAssignId() {
    return markerAttributeAssignId;
  }

  
  public String getAttestationDirectAssign() {
    return attestationDirectAssign;
  }

  
  public void setAttestationDirectAssign(String attestationDirectAssign) {
    this.attestationDirectAssign = attestationDirectAssign;
  }

  
  public String getHasAttestation() {
    return hasAttestation;
  }

  
  public void setHasAttestation(String hasAttestation) {
    this.hasAttestation = hasAttestation;
  }

  public String getStemScope() {
    return stemScope;
  }

  
  public void setStemScope(String stemScope) {
    this.stemScope = stemScope;
  }

  
  public void setMarkerAttributeAssignId(String markerAttributeAssignId) {
    this.markerAttributeAssignId = markerAttributeAssignId;
  }

  
  public String getCalculatedDaysLeft() {
    return calculatedDaysLeft;
  }

  
  public void setCalculatedDaysLeft(String calculatedDaysLeft) {
    this.calculatedDaysLeft = calculatedDaysLeft;
  }

  
  public String getDateCertified() {
    return dateCertified;
  }

  
  public void setDateCertified(String dateCertified) {
    this.dateCertified = dateCertified;
  }

  
  public String getMinDateCertified() {
    return minDateCertified;
  }

  
  public void setMinDateCertified(String minDateCertified) {
    this.minDateCertified = minDateCertified;
  }

  
  public String getDaysUntilRecertify() {
    return daysUntilRecertify;
  }

  
  public void setDaysUntilRecertify(String daysUntilRecertify) {
    this.daysUntilRecertify = daysUntilRecertify;
  }
  
  
}
