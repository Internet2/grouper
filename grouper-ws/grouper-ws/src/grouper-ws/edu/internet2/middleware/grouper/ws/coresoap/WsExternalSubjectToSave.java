/*******************************************************************************
 * Copyright 2016 Internet2
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
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectSave;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * <pre>
 * Class to save a external subject via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsExternalSubjectToSave {

  /** external subject lookup (blank if insert) */
  private WsExternalSubjectLookup wsExternalSubjectLookup;

  /** external subject to save */
  private WsExternalSubject wsExternalSubject;

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsExternalSubjectToSave.class);

  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;

  /**
   * what ended up happening
   */
  @XStreamOmitField
  private SaveResultType saveResultType;

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType saveResultType() {
    return this.saveResultType;
  }
  
  /**
   * 
   */
  public WsExternalSubjectToSave() {
    // empty constructor
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * validate the settings (e.g. that booleans are set correctly)
   */
  public void validate() {
    try {
      if (!StringUtils.isBlank(this.saveMode)) {
        //make sure it exists
        SaveMode.valueOfIgnoreCase(this.saveMode);
      }
    } catch (RuntimeException e) {
      throw new WsInvalidQueryException("Problem with save mode: " + e.getMessage()
          + ", " + this, e);
    }
  }

  /**
   * save this external subject
   * 
   * @param grouperSession
   *            to save
   * @return the stem that was inserted or updated
   */
  public ExternalSubject save(GrouperSession grouperSession) {

    ExternalSubject externalSubject = null;
      
    try {
      SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);
  
      if (this.getWsExternalSubjectLookup() == null) {
        this.setWsExternalSubjectLookup(new WsExternalSubjectLookup());
        this.getWsExternalSubjectLookup().setIdentifier(this.getWsExternalSubject().getIdentifier());
      }
       
      this.getWsExternalSubjectLookup().retrieveExternalSubjectIfNeeded(grouperSession);
  
      ExternalSubject externalSubjectLookedup = this.getWsExternalSubjectLookup().retrieveExternalSubject();
  
      String indentifierLookedUp = externalSubjectLookedup == null ? null : externalSubjectLookedup.getIdentifier();
  
      ExternalSubjectSave externalSubjectSave = new ExternalSubjectSave(grouperSession);
      externalSubjectSave.assignIdentifierToEdit(indentifierLookedUp);
      externalSubjectSave.assignIdentifier(this.getWsExternalSubject().getIdentifier());
      externalSubjectSave.assignUuid(this.getWsExternalSubject().getUuid());
      externalSubjectSave.assignName(this.getWsExternalSubject().getName());
      externalSubjectSave.assignEmail(this.getWsExternalSubject().getEmail());
      externalSubjectSave.assignVettedEmailAddresses(this.getWsExternalSubject().getVettedEmailAddresses());
      externalSubjectSave.assignSaveMode(theSaveMode);
 
      for (WsExternalSubjectAttribute wsExternalSubjectAttribute : 
          GrouperUtil.nonNull(this.getWsExternalSubject().getWsExternalSubjectAttributes(), WsExternalSubjectAttribute.class)) {

        externalSubjectSave.addAttribute(wsExternalSubjectAttribute.getAttributeSystemName(), wsExternalSubjectAttribute.getAttributeValue());
      
      }
      
      externalSubject = externalSubjectSave.save();
      
      this.saveResultType = externalSubjectSave.getSaveResultType();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return externalSubject;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @return the saveMode
   */
  public String getSaveMode() {
    return this.saveMode;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param saveMode1 the saveMode to set
   */
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }

  /**
   * @return the wsExternalSubjectLookup
   */
  public WsExternalSubjectLookup getWsExternalSubjectLookup() {
    return this.wsExternalSubjectLookup;
  }

  /**
   * @param wsExternalSubjectLookup1 the wsGroupLookup to set
   */
  public void setWsExternalSubjectLookup(WsExternalSubjectLookup wsExternalSubjectLookup1) {
    this.wsExternalSubjectLookup = wsExternalSubjectLookup1;
  }

  /**
   * @return the wsGroup
   */
  public WsExternalSubject getWsExternalSubject() {
    return this.wsExternalSubject;
  }

  /**
   * @param wsExternalSubject1 the wsGroup to set
   */
  public void setWsExternalSubject(WsExternalSubject wsExternalSubject1) {
    this.wsExternalSubject = wsExternalSubject1;
  }
}
