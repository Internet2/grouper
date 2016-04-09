/*******************************************************************************
 * Copyright 2016 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * <pre>
 * Class to save an attribute def via web service
 * 
 * </pre>
 * 
 * @author vsachdeva
 */
public class WsAttributeDefToSave {

  /** attribute def lookup (blank if insert) */
  private WsAttributeDefLookup wsAttributeDefLookup;

  /** attribute def to save */
  private WsAttributeDef wsAttributeDef;

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

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsAttributeDefToSave.class);

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
  public WsAttributeDefToSave() {
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
      if (this.getWsAttributeDef().getAssignableTos() == null || this.getWsAttributeDef().getAssignableTos().length == 0) {
        throw new WsInvalidQueryException("select at least one object type that this attribute type can be assigned to "); 
      }
    } catch (RuntimeException e) {
      throw new WsInvalidQueryException("Problem with save mode: " + e.getMessage()
          + ", " + this, e);
    }
  }

  /**
   * save this attributeDef
   * 
   * @param grouperSession
   *            to save
   * @return the attributeDef that was inserted or updated
   * @throws StemNotFoundException 
   * @throws InsufficientPrivilegeException
   */
  public AttributeDef save(GrouperSession grouperSession) {

    AttributeDef attributeDef = null;

    SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);

    if (this.getWsAttributeDef() == null) {
      throw new WsInvalidQueryException(
          "getWsAttributeDef is required to save an attributeDef");
    }
    if (this.getWsAttributeDef().getAssignableTos() == null) {
      throw new WsInvalidQueryException(
          "atleast one assignable to is required.");
    }

    if (this.getWsAttributeDefLookup() == null
        || this.getWsAttributeDefLookup().blank()) {
      WsAttributeDefLookup theWsAttributeDefLookup = this
          .getWsAttributeDefLookup() == null ? new WsAttributeDefLookup()
              : this.getWsAttributeDefLookup();

      if (!StringUtils.isBlank(this.getWsAttributeDef().getUuid())) {
        theWsAttributeDefLookup.setUuid(this.getWsAttributeDef().getUuid());
      } else if (!StringUtils.isBlank(this.getWsAttributeDef().getName())) {
        theWsAttributeDefLookup.setName(this.getWsAttributeDef().getName());
      }

      if (SaveMode.INSERT != theSaveMode || !theWsAttributeDefLookup.blank()) {
        this.setWsAttributeDefLookup(theWsAttributeDefLookup);
      }

    }

    if (SaveMode.INSERT != theSaveMode && this.getWsAttributeDefLookup() == null) {
      throw new WsInvalidQueryException(
          "wsAttributeDefLookup is required to save an attributeDef (probably just put the name in it)");
    }

    this.getWsAttributeDefLookup().retrieveAttributeDefIfNeeded(grouperSession);

    AttributeDef attributeDefLookedup = this.getWsAttributeDefLookup()
        .retrieveAttributeDef();

    String attributeDefLookup = attributeDefLookedup == null ? null
        : attributeDefLookedup.getName();

    List<String> assignableTo = Arrays
        .asList(this.getWsAttributeDef().getAssignableTos());

    AttributeDefType attributeDefType = AttributeDefType.valueOfIgnoreCase(this
        .getWsAttributeDef().getAttributeDefType(), true);
    AttributeDefValueType attributeDefValueType = AttributeDefValueType
        .valueOfIgnoreCase(this.getWsAttributeDef().getValueType(), true);
    boolean multiAssignable = GrouperUtil.booleanValue(this.getWsAttributeDef()
        .getMultiAssignable(), false);
    boolean multiValued = GrouperUtil.booleanValue(this.getWsAttributeDef()
        .getMultiValued(), false);

    AttributeDefSave attributeDefSave = new AttributeDefSave(grouperSession);
    attributeDefSave.assignAttributeDefNameToEdit(attributeDefLookup);
    attributeDefSave.assignId(this.getWsAttributeDef().getUuid()).assignName(
        this.getWsAttributeDef().getName());
    attributeDefSave.assignDescription(this.getWsAttributeDef().getDescription());
    attributeDefSave.assignSaveMode(theSaveMode);
    attributeDefSave.assignCreateParentStemsIfNotExist(GrouperUtil.booleanValue(
        this.getCreateParentStemsIfNotExist(), false));
    attributeDefSave.assignAttributeDefType(attributeDefType);
    attributeDefSave.assignValueType(attributeDefValueType);
    attributeDefSave.assignMultiAssignable(multiAssignable);
    attributeDefSave.assignMultiValued(multiValued);
    attributeDefSave.assignToAttributeDef(assignableTo.contains("ATTRIBUTE_DEF"));
    attributeDefSave.assignToAttributeDefAssn(assignableTo
        .contains("ATTRIBUTE_DEF_ASSIGNMENT"));
    attributeDefSave.assignToStem(assignableTo.contains("STEM"));
    attributeDefSave.assignToStemAssn(assignableTo.contains("STEM_ASSIGNMENT"));
    attributeDefSave.assignToGroup(assignableTo.contains("GROUP"));
    attributeDefSave.assignToGroupAssn(assignableTo.contains("GROUP_ASSIGNMENT"));
    attributeDefSave.assignToMember(assignableTo.contains("MEMBER"));
    attributeDefSave.assignToMemberAssn(assignableTo.contains("MEMBER_ASSIGNMENT"));
    attributeDefSave.assignToEffMembership(assignableTo.contains("EFFECTIVE_MEMBERSHIP"));
    attributeDefSave.assignToEffMembershipAssn(assignableTo
        .contains("EFFECTIVE_MEMBERSHIP_ASSIGNMENT"));
    attributeDefSave.assignToImmMembership(assignableTo.contains("IMMEDIATE_MEMBERSHIP"));
    attributeDefSave.assignToImmMembershipAssn(assignableTo
        .contains("IMMEDIATE_MEMBERSHIP_ASSIGNMENT"));

    if (!StringUtils.isBlank(this.getWsAttributeDef().getIdIndex())) {
      attributeDefSave.assignIdIndex(GrouperUtil.longValue(this.getWsAttributeDef()
          .getIdIndex()));
    }

    attributeDef = attributeDefSave.save();

    this.saveResultType = attributeDefSave.getSaveResultType();

    return attributeDef;
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
   * @return the wsAttributeDefLookup
   */
  public WsAttributeDefLookup getWsAttributeDefLookup() {
    return this.wsAttributeDefLookup;
  }

  /**
   * @param wsAttributeDefLookup1 the wsAttributeDefLookup to set
   */
  public void setWsAttributeDefLookup(WsAttributeDefLookup wsAttributeDefLookup1) {
    this.wsAttributeDefLookup = wsAttributeDefLookup1;
  }

  /**
   * @return the wsAttributeDef
   */
  public WsAttributeDef getWsAttributeDef() {
    return this.wsAttributeDef;
  }

  /**
   * @param wsAttributeDef1 the wsAttributeDef to set
   */
  public void setWsAttributeDef(WsAttributeDef wsAttributeDef1) {
    this.wsAttributeDef = wsAttributeDef1;
  }
}
