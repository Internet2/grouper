/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.bo.entity.KimEntityName;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo;

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
    if (!StringUtils.isBlank(this.firstName)) {
      return this.firstName;
    }
    //if there is a formatted name and no first name, then break up the formatted name
    if (!StringUtils.isBlank(this.formattedName) && StringUtils.isBlank(this.lastName)) {
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
    if (!StringUtils.isBlank(this.firstNameUnmasked)) {
      return this.firstNameUnmasked;
    }
    String theFirstName = this.getFirstName();
    if (!StringUtils.isBlank(theFirstName)) {
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
    
    if (!StringUtils.isBlank(this.formattedNameUnmasked)) {
      return this.formattedNameUnmasked;
    }
    if (!StringUtils.isBlank(this.formattedName)) {
      return this.formattedName;
    }
    
    String theFormattedName = this.getFormattedName();
    if (!StringUtils.isBlank(theFormattedName)) {
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
    
    if (!StringUtils.isBlank(this.formattedName)) {
      return this.formattedName;
    }
    
    if (!StringUtils.isBlank(this.formattedNameUnmasked)) {
      return this.formattedNameUnmasked;
    }

    if (!StringUtils.isBlank(this.firstName) || !StringUtils.isBlank(this.lastName)) {
      return StringUtils.trim(StringUtils.defaultString(this.firstName) + " " + StringUtils.defaultString(this.lastName));
    }
    
    return super.getFormattedName();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getLastName()
   */
  @Override
  public String getLastName() {
    if (!StringUtils.isBlank(this.lastName)) {
      return this.lastName;
    }
    //if there is a formatted name and no first name, then break up the formatted name
    if (!StringUtils.isBlank(this.formattedName) && StringUtils.isBlank(this.firstName)) {
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
    if (!StringUtils.isBlank(this.lastNameUnmasked)) {
      return this.lastNameUnmasked;
    }
    String theLastName = this.getLastName();
    if (!StringUtils.isBlank(theLastName)) {
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
    if (!StringUtils.isBlank(this.middleName)) {
      return this.middleName;
    }
    //if there is a formatted name and no first name, then break up the formatted name
    if (!StringUtils.isBlank(this.formattedName) && StringUtils.isBlank(this.firstName)
        && StringUtils.isBlank(this.lastName)) {
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
    if (!StringUtils.isBlank(this.middleNameUnmasked)) {
      return this.middleNameUnmasked;
    }
    String theMiddleName = this.getMiddleName();
    if (!StringUtils.isBlank(theMiddleName)) {
      return theMiddleName;
    }
    return super.getMiddleNameUnmasked();
  }

}
