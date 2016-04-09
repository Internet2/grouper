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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemCopy;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemMove;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * <pre>
 * Class to save a stem via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsStemToSave {

  /** stem lookup (blank if insert) */
  private WsStemLookup wsStemLookup;

  /** stem to save */
  private WsStem wsStem;

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
  private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

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
  public WsStemToSave() {
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
   * save this stem
   * 
   * @param grouperSession
   *            to save
   * @return the stem that was inserted or updated
   * @throws StemNotFoundException
   * @throws StemNotFoundException
   * @throws StemAddException
   * @throws InsufficientPrivilegeException
   * @throws StemModifyException
   * @throws StemAddException
   */
  public Stem save(GrouperSession grouperSession) throws StemNotFoundException,
      StemNotFoundException, StemAddException, InsufficientPrivilegeException,
      StemModifyException, StemAddException {

    SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);

    this.getWsStemLookup().retrieveStemIfNeeded(grouperSession, false);

    Stem stemLookedup = this.getWsStemLookup().retrieveStem();

    String stemNameLookup = stemLookedup == null ? null : stemLookedup.getName();

    StemSave stemSave = new StemSave(grouperSession);
    stemSave.assignStemNameToEdit(stemNameLookup);
    stemSave.assignUuid(this.getWsStem().getUuid()).assignName(this.getWsStem().getName());
    stemSave.assignDisplayExtension(this.getWsStem().getDisplayExtension());
    stemSave.assignDescription(this.getWsStem().getDescription());
    stemSave.assignSaveMode(theSaveMode);
    stemSave.assignCreateParentStemsIfNotExist(GrouperUtil.booleanValue(this.getCreateParentStemsIfNotExist(), false));
    
    if (!StringUtils.isBlank(this.getWsStem().getIdIndex())) {
      stemSave.assignIdIndex(GrouperUtil.longValue(this.getWsStem().getIdIndex()));
    }

    Stem stem = stemSave.save();
    
    this.saveResultType = stemSave.getSaveResultType();
    
    return stem;
  }

  /**
   * move this stem
   * 
   * @param grouperSession
   *            to save
   * @param toStem
   * @param moveAssignAlternateName
   * @return the stem that was moved
   */
  public Stem move(GrouperSession grouperSession, Stem toStem, Boolean moveAssignAlternateName) {

    Stem stem = null;
              
    this.getWsStemLookup().retrieveStemIfNeeded(grouperSession, true);

    Stem stemLookedup = this.getWsStemLookup().retrieveStem();

    StemMove stemMove = new StemMove(stemLookedup, toStem);

    if (moveAssignAlternateName != null) {
      stemMove.assignAlternateName(moveAssignAlternateName);
    }
    
    stemMove.save();
    stem = StemFinder.findByName(grouperSession, toStem.getName() + Stem.DELIM + stemLookedup.getExtension(), true);
    
    this.saveResultType = SaveResultType.INSERT;
    
    return stem;
  }

  /**
   * copy this stem
   * 
   * @param grouperSession
   *            to save
   * @param toStem
   * @param copyPrivilegesOfGroup 
   * @param copyGroupAsPrivilege 
   * @param copyListMembersOfGroup 
   * @param copyListGroupAsMember 
   * @param copyAttributes 
   * @param moveAssignAlternateName
   * @param copyPrivilegesOfStem
   * @return the group that was moved
   */
  public Stem copy(GrouperSession grouperSession, Stem toStem, Boolean copyPrivilegesOfGroup,
      Boolean copyGroupAsPrivilege, Boolean copyListMembersOfGroup, 
      Boolean copyListGroupAsMember, Boolean copyAttributes, Boolean copyPrivilegesOfStem) {

    Stem stem = null;

    this.getWsStemLookup().retrieveStemIfNeeded(grouperSession, true);

    Stem stemLookedup = this.getWsStemLookup().retrieveStem();

    StemCopy stemCopy = new StemCopy(stemLookedup, toStem);

    if (copyPrivilegesOfGroup != null) {
      stemCopy.copyPrivilegesOfGroup(copyPrivilegesOfGroup);
    }
    if (copyGroupAsPrivilege != null) {
      stemCopy.copyGroupAsPrivilege(copyGroupAsPrivilege);
    }
    if (copyListMembersOfGroup != null) {
      stemCopy.copyListMembersOfGroup(copyListMembersOfGroup);
    }
    if (copyListGroupAsMember != null) {
      stemCopy.copyListGroupAsMember(copyListGroupAsMember);
    }
    if (copyAttributes != null) {
      stemCopy.copyAttributes(copyAttributes);
    }
    if (copyPrivilegesOfStem != null) {
      stemCopy.copyPrivilegesOfStem(copyPrivilegesOfStem);
    }
    
    stemCopy.save();
    stem = StemFinder.findByName(grouperSession, toStem.getName() + Stem.DELIM + stemLookedup.getExtension(), true);
    
    this.saveResultType = SaveResultType.INSERT;
    
    return stem;

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
   * @return the wsStemLookup
   */
  public WsStemLookup getWsStemLookup() {
    return this.wsStemLookup;
  }

  /**
   * @param wsStemLookup1 the wsStemLookup to set
   */
  public void setWsStemLookup(WsStemLookup wsStemLookup1) {
    this.wsStemLookup = wsStemLookup1;
  }

  /**
   * @return the wsStem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * @param wsStem1 the wsStem to set
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }
}
