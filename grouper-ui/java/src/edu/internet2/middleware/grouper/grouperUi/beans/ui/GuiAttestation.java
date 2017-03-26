/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;

/**
 * @author vsachdeva
 *
 */
public class GuiAttestation {
  
  private AttributeAssignable attributeAssignable;
  
  private Boolean grouperAttestationSendEmail = true;
  
  private String grouperAttestationEmailAddresses; // list of comma separated emails
  
  private String grouperAttestationDaysUntilRecertify;
  
  private String grouperAttestationLastEmailedDate;
  
  private String grouperAttestationDaysBeforeToRemind;
  
  private String grouperAttestationStemScope;
  
  private String grouperAttestationDateCertified;
  
  private Boolean grouperAttestationDirectAssignment = false;
 
  private Mode mode;
  
  private Type type;
  
  public enum Mode {
    EDIT, ADD
  }
  
  public enum Type {
    DIRECT, INDIRECT
  }
  
  public GuiAttestation(AttributeAssignable attributeAssignable, Type type) {
    this.mode = Mode.ADD;
    this.attributeAssignable = attributeAssignable;
    this.type = type;
  }
  
  
  public GuiAttestation(AttributeAssignable attributeAssignable, Boolean grouperAttestationSendEmail,
      String grouperAttestationEmailAddresses,
      String grouperAttestationDaysUntilRecertify,
      String grouperAttestationLastEmailedDate,
      String grouperAttestationDaysBeforeToRemind, String grouperAttestationStemScope,
      String grouperAttestationDateCertified, Boolean grouperAttestationDirectAssignment, Type type) {
    
    super();
    this.mode = Mode.EDIT;
    this.attributeAssignable = attributeAssignable;
    this.grouperAttestationSendEmail = grouperAttestationSendEmail;
    this.grouperAttestationEmailAddresses = grouperAttestationEmailAddresses;
    this.grouperAttestationDaysUntilRecertify = grouperAttestationDaysUntilRecertify;
    this.grouperAttestationLastEmailedDate = grouperAttestationLastEmailedDate;
    this.grouperAttestationDaysBeforeToRemind = grouperAttestationDaysBeforeToRemind;
    this.grouperAttestationStemScope = grouperAttestationStemScope;
    this.grouperAttestationDateCertified = grouperAttestationDateCertified;
    this.grouperAttestationDirectAssignment = grouperAttestationDirectAssignment;
    this.type = type;
  }

  
  public AttributeAssignable getAttributeAssignable() {
    return attributeAssignable;
  }

  public Boolean getGrouperAttestationSendEmail() {
    return grouperAttestationSendEmail;
  }
  
  public void setGrouperAttestationSendEmail(Boolean grouperAttestationSendEmail) {
    this.grouperAttestationSendEmail = grouperAttestationSendEmail;
  }

  public Boolean getGrouperAttestationDirectAssignment() {
    return grouperAttestationDirectAssignment;
  }

  public String getGrouperAttestationEmailAddresses() {
    return grouperAttestationEmailAddresses;
  }
  
  public void setGrouperAttestationEmailAddresses(String grouperAttestationEmailAddresses) {
    this.grouperAttestationEmailAddresses = grouperAttestationEmailAddresses;
  }


  public String getGrouperAttestationDaysUntilRecertify() {
    return grouperAttestationDaysUntilRecertify;
  }

  public void setGrouperAttestationDaysUntilRecertify(String grouperAttestationDaysUntilRecertify) {
    this.grouperAttestationDaysUntilRecertify = grouperAttestationDaysUntilRecertify;
  }

  public String getGrouperAttestationLastEmailedDate() {
    return grouperAttestationLastEmailedDate;
  }
  
  
  public String getGrouperAttestationDaysBeforeToRemind() {
    return grouperAttestationDaysBeforeToRemind;
  }
  
  
  public void setGrouperAttestationDaysBeforeToRemind(String grouperAttestationDaysBeforeToRemind) {
    this.grouperAttestationDaysBeforeToRemind = grouperAttestationDaysBeforeToRemind;
  }


  public String getGrouperAttestationStemScope() {
    return grouperAttestationStemScope;
  }
  
  public String getGrouperAttestationDateCertified() {
    return grouperAttestationDateCertified;
  }

  public Mode getMode() {
    return mode;
  }

  public Type getType() {
    return type;
  }
  
}
