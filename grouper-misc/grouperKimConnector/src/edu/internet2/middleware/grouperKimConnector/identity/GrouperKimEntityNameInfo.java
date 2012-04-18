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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import org.kuali.rice.kim.bo.entity.KimEntityName;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 * grouper implementation will simplify which fields you set
 */
public class GrouperKimEntityNameInfo extends KimEntityNameInfo {

  /**
   * deal with first name, last name, and name, convert between the three
   */
  public GrouperKimEntityNameInfo() {
  }

  /**
   * @param name
   */
  public GrouperKimEntityNameInfo(KimEntityName name) {
    super(name);

  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getFirstName()
   */
  @Override
  public String getFirstName() {
    if (!GrouperClientUtils.isBlank(this.firstName)) {
      return this.firstName;
    }
    //if there is a formatted name and no first name, then break up the formatted name
    if (!GrouperClientUtils.isBlank(this.formattedName) && GrouperClientUtils.isBlank(this.lastName)) {
      return GrouperKimUtils.firstName(this.formattedName);
    }
    return super.getFirstName();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getFirstNameUnmasked()
   */
  @Override
  public String getFirstNameUnmasked() {
    if (!GrouperClientUtils.isBlank(this.firstNameUnmasked)) {
      return this.firstNameUnmasked;
    }
    String theFirstName = this.getFirstName();
    if (!GrouperClientUtils.isBlank(theFirstName)) {
      return theFirstName;
    }
    return super.getFirstNameUnmasked();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getFormattedNameUnmasked()
   */
  @Override
  public String getFormattedNameUnmasked() {
    
    if (!GrouperClientUtils.isBlank(this.formattedNameUnmasked)) {
      return this.formattedNameUnmasked;
    }
    if (!GrouperClientUtils.isBlank(this.formattedName)) {
      return this.formattedName;
    }
    
    String theFormattedName = this.getFormattedName();
    if (!GrouperClientUtils.isBlank(theFormattedName)) {
      return theFormattedName;
    }
    
    return super.getFormattedNameUnmasked();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getFormattedName()
   */
  @Override
  public String getFormattedName() {
    
    if (!GrouperClientUtils.isBlank(this.formattedName)) {
      return this.formattedName;
    }
    
    if (!GrouperClientUtils.isBlank(this.formattedNameUnmasked)) {
      return this.formattedNameUnmasked;
    }

    if (!GrouperClientUtils.isBlank(this.firstName) || !GrouperClientUtils.isBlank(this.lastName)) {
      return GrouperClientUtils.trim(GrouperClientUtils.defaultString(this.firstName) + " " + GrouperClientUtils.defaultString(this.lastName));
    }
    
    return super.getFormattedName();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getLastName()
   */
  @Override
  public String getLastName() {
    if (!GrouperClientUtils.isBlank(this.lastName)) {
      return this.lastName;
    }
    //if there is a formatted name and no first name, then break up the formatted name
    if (!GrouperClientUtils.isBlank(this.formattedName) && GrouperClientUtils.isBlank(this.firstName)) {
      return GrouperKimUtils.lastName(this.formattedName);
    }
    return super.getLastName();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getLastNameUnmasked()
   */
  @Override
  public String getLastNameUnmasked() {
    if (!GrouperClientUtils.isBlank(this.lastNameUnmasked)) {
      return this.lastNameUnmasked;
    }
    String theLastName = this.getLastName();
    if (!GrouperClientUtils.isBlank(theLastName)) {
      return theLastName;
    }
    return super.getLastNameUnmasked();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getMiddleName()
   */
  @Override
  public String getMiddleName() {
    if (!GrouperClientUtils.isBlank(this.middleName)) {
      return this.middleName;
    }
    //if there is a formatted name and no first name, then break up the formatted name
    if (!GrouperClientUtils.isBlank(this.formattedName) && GrouperClientUtils.isBlank(this.firstName)
        && GrouperClientUtils.isBlank(this.lastName)) {
      return GrouperKimUtils.middleName(this.formattedName);
    }
    return super.getMiddleName();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getMiddleNameUnmasked()
   */
  @Override
  public String getMiddleNameUnmasked() {
    if (!GrouperClientUtils.isBlank(this.middleNameUnmasked)) {
      return this.middleNameUnmasked;
    }
    String theMiddleName = this.getMiddleName();
    if (!GrouperClientUtils.isBlank(theMiddleName)) {
      return theMiddleName;
    }
    return super.getMiddleNameUnmasked();
  }

}
