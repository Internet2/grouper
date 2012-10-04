package edu.internet2.middleware.grouperVoot.beans;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


public class VootPerson {
  
  /**
   * default constructor
   */
  public VootPerson() {
    
  }
  
  /**
   * 
   * @param subject
   */
  public VootPerson(Subject subject) {
    this.id = subject.getId();
    this.displayName = subject.getName();
    
    String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
    if (!StringUtils.isBlank(emailAttributeName)) {
      Set<String> emails = subject.getAttributeValues(emailAttributeName);
      
      if (GrouperUtil.length(emails) > 0) {
        int i=0;
        this.emails = new VootEmail[emails.size()];
        for (String email : emails) {
          this.emails[i] = new VootEmail();
          this.emails[i].setType("email");
          this.emails[i].setValue(email);
        }
      }
    }
    
  }
  
  public static void main(String[] args) {
    
    VootPerson vootPerson = new VootPerson();
    
    vootPerson.setDisplayName("John Smith");
    vootPerson.setId("jsmith");
    VootEmail vootEmail = new VootEmail();
    vootEmail.setType("email");
    vootEmail.setValue("john@smith.edu");
    VootEmail vootEmail2 = new VootEmail();
    vootEmail2.setType("email");
    vootEmail2.setValue("jsmith@university.edu");
    vootPerson.setEmails(new VootEmail[]{vootEmail, vootEmail2});
    
    String json = GrouperUtil.jsonConvertToNoWrap(vootPerson);
    
    System.out.println(json);
    
    vootPerson = (VootPerson)GrouperUtil.jsonConvertFrom(json, VootPerson.class);
    
    System.out.println("ID is: " + vootPerson.getId());
    System.out.println("Email is: " + vootPerson.getEmails()[0].getValue());

  }

  /**
   * manager, admin, member
   */
  private String voot_membership_role;
  
  /**
   * manager, admin, member
   * @return manager, admin, member
   */
  public String getVoot_membership_role() {
    return this.voot_membership_role;
  }

  /**
   * manager, admin, member
   * @param voot_membership_role1
   */
  public void setVoot_membership_role(String voot_membership_role1) {
    this.voot_membership_role = voot_membership_role1;
  }

  /** person id, e.g. jsmith */
  private String id;
  
  /** display name, e.g. John Smith */
  private String displayName;
  
  /** e.g. jsmith@school.edu, johns@company.com */
  private VootEmail[] emails;

  /**
   * person id, e.g. jsmith
   * @return person id, e.g. jsmith
   */
  public String getId() {
    return this.id;
  }

  /**
   * person id, e.g. jsmith
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * display name, e.g. John Smith
   * @return display name, e.g. John Smith
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * display name, e.g. John Smith
   * @param displayName1
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * e.g. jsmith@school.edu, johns@company.com
   * @return emails
   */
  public VootEmail[] getEmails() {
    return this.emails;
  }

  /**
   * e.g. jsmith@school.edu, johns@company.com
   * @param emails1
   */
  public void setEmails(VootEmail[] emails1) {
    this.emails = emails1;
  }

  
  
}
