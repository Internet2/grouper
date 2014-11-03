/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/

package edu.internet2.middleware.grouperVoot.beans;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * VOOT person bean that gets transformed to json.
 * 
 * @author mchyzer
 * @author <andrea.biancini@gmail.com>
 */
public class VootPerson {
  /** The voot membership role (being "manager", "admin" or "member"). */
  private String voot_membership_role;
  
  /** Person id, e.g. jsmith */
  private String id;
  
  /** Display name, e.g. John Smith */
  private String displayName;
  
  /** Email addresses e.g. jsmith@school.edu, johns@company.com */
  private VootEmail[] emails;
  
  /**
   * Default constructor. 
   */
  public VootPerson() {
    // Do nothing
  }

  /**
   * Contructor that builds a VOOT person from a Grouper subject. 
   * @param subject the groupser subject.
   */
  public VootPerson(Subject subject) {
    this.id = subject.getId();
    this.displayName = subject.getName();

    String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
    if (!StringUtils.isBlank(emailAttributeName)) {
      Set<String> emails = subject.getAttributeValues(emailAttributeName);

      if (GrouperUtil.length(emails) > 0) {
        // maybe first is blank
        if (GrouperUtil.length(emails) != 1 || !StringUtils.isBlank(emails.iterator().next())) {
          int i = 0;
          this.emails = new VootEmail[emails.size()];
          for (String email : emails) {
            this.emails[i] = new VootEmail();
            this.emails[i].setType(VootEmail.MailTypes.OTHER.toString());
            this.emails[i].setValue(email);
          }
        }
      }
    }

  }

  /**
   * Get the voot membership role (being "manager", "admin" or "member").
   * 
   * @return the voot membership role
   */
  public String getVoot_membership_role() {
    return this.voot_membership_role;
  }

  /**
   * Set the voot membership role (being "manager", "admin" or "member").
   * 
   * @param voot_membership_role1 the voot membership role
   */
  public void setVoot_membership_role(String voot_membership_role1) {
    this.voot_membership_role = voot_membership_role1;
  }
  
  /**
   * Get the person id, e.g. jsmith
   * 
   * @return the person id, e.g. jsmith
   */
  public String getId() {
    return this.id;
  }

  /**
   * Set the person id, e.g. jsmith
   * 
   * @param id1 the the person id, e.g. jsmith
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * Get the display name, e.g. John Smith
   * 
   * @return the display name, e.g. John Smith
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * Set the display name, e.g. John Smith
   * 
   * @param displayName1 the display name, e.g. John Smith
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * Get email addresses e.g. jsmith@school.edu, johns@company.com
   * 
   * @return the email addresses.
   */
  public VootEmail[] getEmails() {
    return this.emails;
  }

  /**
   * Set email addresses e.g. jsmith@school.edu, johns@company.com
   * 
   * @param emails1 the email addresses.
   */
  public void setEmails(VootEmail[] emails1) {
    this.emails = emails1;
  }
}
