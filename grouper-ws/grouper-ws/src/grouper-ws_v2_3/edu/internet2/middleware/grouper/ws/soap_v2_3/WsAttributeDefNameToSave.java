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
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;



/**
 * <pre>
 * Class to save an attribute def name via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsAttributeDefNameToSave {

  /** attribute def name lookup (blank if insert) */
  private WsAttributeDefNameLookup wsAttributeDefNameLookup;

  /** attribute def name to save */
  private WsAttributeDefName wsAttributeDefName;

  /** T or F (null if F) */
  private String createParentStemsIfNotExist;
  
  /**
   * if should create parent stems if not exist
   * @return T or F or null (F)
   */
  public String getCreateParentStemsIfNotExist() {
    return this.createParentStemsIfNotExist;
  }

  /**
   * if should create parent stems if not exist
   * @param createParentStemsIfNotExist1 T or F or null (F)
   */
  public void setCreateParentStemsIfNotExist(String createParentStemsIfNotExist1) {
    this.createParentStemsIfNotExist = createParentStemsIfNotExist1;
  }

  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;

  /**
   * what ended up happening
   */
  @XStreamOmitField
  private SaveResultType saveResultType;

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsAttributeDefNameToSave.class);

  /**
   * 
   */
  public WsAttributeDefNameToSave() {
    // empty constructor
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
   * attribute def name lookup (blank if insert)
   * @return the wsAttributeDefNameLookup
   */
  public WsAttributeDefNameLookup getWsAttributeDefNameLookup() {
    return this.wsAttributeDefNameLookup;
  }

  /**
   * attribute def name lookup (blank if insert)
   * @param wsAttributeDefNameLookup1 the wsAttributeDefNameLookup to set
   */
  public void setWsAttributeDefNameLookup(WsAttributeDefNameLookup wsAttributeDefNameLookup1) {
    this.wsAttributeDefNameLookup = wsAttributeDefNameLookup1;
  }

  /**
   * attribute def name to save
   * @return the wsAttributeDefName
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * attribute def name to save
   * @param wsAttributeDefName1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  /**
   * save this attributeDefName
   * 
   * @param grouperSession
   *            to save
   * @return the stem that was inserted or updated
   * @throws StemNotFoundException 
   * @throws StemAddException 
   * @throws AttributeDefNameAddException
   * @throws InsufficientPrivilegeException
   * @throws AttributeDefNameAddAlreadyExistsException
   */
  public AttributeDefName save(GrouperSession grouperSession) {
  
    AttributeDefName attributeDefName = null;
      
    SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);

    if (this.getWsAttributeDefName() == null) {
      throw new WsInvalidQueryException(
          "getWsAttributeDefName is required to save an attributeDefName");
    }

    if (this.getWsAttributeDefNameLookup() == null || this.getWsAttributeDefNameLookup().blank()) {
      WsAttributeDefNameLookup theWsAttributeDefNameLookup = this.getWsAttributeDefNameLookup() == null ? new WsAttributeDefNameLookup() : this.getWsAttributeDefNameLookup();
      
      if (!StringUtils.isBlank(this.getWsAttributeDefName().getUuid())) {
        theWsAttributeDefNameLookup.setUuid(this.getWsAttributeDefName().getUuid());
      } else if (!StringUtils.isBlank(this.getWsAttributeDefName().getName())) {
        theWsAttributeDefNameLookup.setName(this.getWsAttributeDefName().getName());
      }
      
      if (SaveMode.INSERT != theSaveMode || !theWsAttributeDefNameLookup.blank()) {
        this.setWsAttributeDefNameLookup(theWsAttributeDefNameLookup);
      }
      
    }

    if (SaveMode.INSERT != theSaveMode && this.getWsAttributeDefNameLookup() == null) {
      throw new WsInvalidQueryException(
          "wsAttributeDefNameLookup is required to save an attributeDefName (probably just put the name in it)");
    }
     
    this.getWsAttributeDefNameLookup().retrieveAttributeDefNameIfNeeded(grouperSession);

    AttributeDefName attributeDefNameLookedup = this.getWsAttributeDefNameLookup().retrieveAttributeDefName();

    String attributeDefNameLookup = attributeDefNameLookedup == null ? null : attributeDefNameLookedup.getName();

    AttributeDef attributeDef = null;
    
    //we need the attribute definition, find it by id if was passed in
    if (!StringUtils.isBlank(this.getWsAttributeDefName().getAttributeDefId())) {
      attributeDef = AttributeDefFinder.findById(this.getWsAttributeDefName().getAttributeDefId(), false, new QueryOptions().secondLevelCache(false));
      
      if (attributeDef == null) {
        throw new WsInvalidQueryException("Cant find attributeDef by id: " + this.getWsAttributeDefName().getAttributeDefId());
      }
      
      //make sure the name matches
      if (!StringUtils.isBlank(this.getWsAttributeDefName().getAttributeDefName()) && !StringUtils.equals(this.getWsAttributeDefName().getAttributeDefName(), attributeDef.getName())) {
        throw new WsInvalidQueryException("AttributeDef for id: " + attributeDef.getUuid() + " has name: " + attributeDef.getName() 
            + ", but you passed in a different name: " + this.getWsAttributeDefName().getAttributeDefName());
      }
      
    } else if (!StringUtils.isBlank(this.getWsAttributeDefName().getAttributeDefName())) {

      attributeDef = AttributeDefFinder.findByName(this.getWsAttributeDefName().getAttributeDefName(), false, new QueryOptions().secondLevelCache(false));
      
      if (attributeDef == null) {
        throw new WsInvalidQueryException("Cant find attributeDef by name: " + this.getWsAttributeDefName().getAttributeDefName());
      }
      
    } else {
      throw new WsInvalidQueryException(
        "You need to pass in an attributeDefId or attributeDefName!");
    }
    
    AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(grouperSession, attributeDef);
    attributeDefNameSave.assignAttributeDefNameNameToEdit(attributeDefNameLookup);
    attributeDefNameSave.assignUuid(this.getWsAttributeDefName().getUuid()).assignName(this.getWsAttributeDefName().getName());
    attributeDefNameSave.assignDisplayExtension(this.getWsAttributeDefName().getDisplayExtension());
    attributeDefNameSave.assignDescription(this.getWsAttributeDefName().getDescription());
    attributeDefNameSave.assignSaveMode(theSaveMode);
    attributeDefNameSave.assignCreateParentStemsIfNotExist(GrouperUtil.booleanValue(this.getCreateParentStemsIfNotExist(), false));

    if (!StringUtils.isBlank(this.getWsAttributeDefName().getIdIndex())) {
      attributeDefNameSave.assignIdIndex(GrouperUtil.longValue(this.getWsAttributeDefName().getIdIndex()));
    }

    attributeDefName = attributeDefNameSave.save();
    
    this.saveResultType = attributeDefNameSave.getSaveResultType();
  
    return attributeDefName;
  }

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType saveResultType() {
    return this.saveResultType;
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

}
