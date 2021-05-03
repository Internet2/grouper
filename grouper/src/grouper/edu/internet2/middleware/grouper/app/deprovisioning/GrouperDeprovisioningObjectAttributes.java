package edu.internet2.middleware.grouper.app.deprovisioning;


public class GrouperDeprovisioningObjectAttributes {
  
  private String markerAttributeAssignId;


  public GrouperDeprovisioningObjectAttributes(String id, String name, String markerAttributeAssignId) {
    this.id = id;
    this.name = name;
    this.markerAttributeAssignId = markerAttributeAssignId;
  }
  
  
  private String id;
  private String name;
  
 
  private boolean isOwnedByGroup;
  private boolean isOwnedByStem;
  
  private String affiliation;
  private String ownerStemId;
  private String allowAddsWhileDeprovisioned;
  private String autoChangeLoader;
  private String autoSelectForRemoval;
  private String deprovision;
  private String emailAddresses;
  private String mailToGroup;
  private String showForRemoval;
  private String stemScope;
  private String directAssign;
  private String emailBody;
  private String sendEmail;


  
  public String getMarkerAttributeAssignId() {
    return markerAttributeAssignId;
  }
  
  public void setMarkerAttributeAssignId(String markerAttributeAssignId) {
    this.markerAttributeAssignId = markerAttributeAssignId;
  }
  
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
  
  public String getAffiliation() {
    return affiliation;
  }
  
  public void setAffiliation(String affiliation) {
    this.affiliation = affiliation;
  }
  
  public String getOwnerStemId() {
    return ownerStemId;
  }
  
  public void setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
  }
  
  public String getAllowAddsWhileDeprovisioned() {
    return allowAddsWhileDeprovisioned;
  }
  
  public void setAllowAddsWhileDeprovisioned(String allowAddsWhileDeprovisioned) {
    this.allowAddsWhileDeprovisioned = allowAddsWhileDeprovisioned;
  }
  
  public String getDirectAssign() {
    return directAssign;
  }
  
  public void setDirectAssign(String directAssign) {
    this.directAssign = directAssign;
  }
  
  public String getEmailBody() {
    return emailBody;
  }
  
  public void setEmailBody(String emailBody) {
    this.emailBody = emailBody;
  }
  
  public String getSendEmail() {
    return sendEmail;
  }
  
  public void setSendEmail(String sendEmail) {
    this.sendEmail = sendEmail;
  }

  
  public String getAutoChangeLoader() {
    return autoChangeLoader;
  }

  
  public void setAutoChangeLoader(String autoChangeLoader) {
    this.autoChangeLoader = autoChangeLoader;
  }

  
  public String getAutoSelectForRemoval() {
    return autoSelectForRemoval;
  }

  
  public void setAutoSelectForRemoval(String autoSelectForRemoval) {
    this.autoSelectForRemoval = autoSelectForRemoval;
  }

  
  public String getDeprovision() {
    return deprovision;
  }

  
  public void setDeprovision(String deprovision) {
    this.deprovision = deprovision;
  }

  
  public String getEmailAddresses() {
    return emailAddresses;
  }

  
  public void setEmailAddresses(String emailAddresses) {
    this.emailAddresses = emailAddresses;
  }

  
  public String getMailToGroup() {
    return mailToGroup;
  }

  
  public void setMailToGroup(String mailToGroup) {
    this.mailToGroup = mailToGroup;
  }

  
  public String getShowForRemoval() {
    return showForRemoval;
  }

  
  public void setShowForRemoval(String showForRemoval) {
    this.showForRemoval = showForRemoval;
  }

  
  public String getStemScope() {
    return stemScope;
  }

  
  public void setStemScope(String stemScope) {
    this.stemScope = stemScope;
  }
  
}
