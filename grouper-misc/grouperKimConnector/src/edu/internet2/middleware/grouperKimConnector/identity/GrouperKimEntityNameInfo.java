/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.bo.entity.KimEntityName;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo;


/**
 * grouper implementation will simplify which fields you set
 */
public class GrouperKimEntityNameInfo extends KimEntityNameInfo {

  /**
   * 
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
//    String firstName = super.getFirstName();
//    if (StringUtils.isBlank(firstName)) {
//      
//    }
    return null;
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getFirstNameUnmasked()
   */
  @Override
  public String getFirstNameUnmasked() {
    return super.getFirstNameUnmasked();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getFormattedNameUnmasked()
   */
  @Override
  public String getFormattedNameUnmasked() {
    return super.getFormattedNameUnmasked();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getLastName()
   */
  @Override
  public String getLastName() {
    return super.getLastName();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getLastNameUnmasked()
   */
  @Override
  public String getLastNameUnmasked() {
    return super.getLastNameUnmasked();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getMiddleName()
   */
  @Override
  public String getMiddleName() {
    return super.getMiddleName();
  }

  /**
   * 
   * @see org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo#getMiddleNameUnmasked()
   */
  @Override
  public String getMiddleNameUnmasked() {
    return super.getMiddleNameUnmasked();
  }

}
