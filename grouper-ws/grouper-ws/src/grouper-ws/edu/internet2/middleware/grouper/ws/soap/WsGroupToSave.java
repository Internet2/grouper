/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupAddException;
import edu.internet2.middleware.grouper.GroupModifyException;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.SaveMode;
import edu.internet2.middleware.grouper.StemAddException;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * Class to save a group via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsGroupToSave {

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

  /**
   * uuid of the group to find
   */
  private String uuid;

  /** description of group */
  private String description;

  /** display extension is the friendly name (without path) */
  private String displayExtension;

  /** if the stems should be created if not exist (T|F), defaults to false */
  private String createStemsIfNotExist;

  /**
   * 
   */
  public WsGroupToSave() {
    // empty constructor
  }

  /**
   * construct with fields
   * @param uuid1 uuid
   * @param description1 description
   * @param displayExtension1 display extension
   * @param saveMode1 save mode
   * @param createStemsIfNotExist1 if parent stems should be created if not exist
   * @param groupName1 group name
   */
  public WsGroupToSave(String uuid1, String description1, String displayExtension1,
      String saveMode1, String createStemsIfNotExist1, String groupName1) {
    this.uuid = uuid1;
    this.description = description1;
    this.displayExtension = displayExtension1;
    this.saveMode = saveMode1;
    this.createStemsIfNotExist = createStemsIfNotExist1;
    this.groupName = groupName1;
  }

  /**
   * description of group
   * 
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * description of group
   * 
   * @param description1
   *            the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * display extension is the friendly name (without path)
   * 
   * @return the displayExtension
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * display extension is the friendly name (without path)
   * 
   * @param displayExtension1
   *            the displayExtension to set
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * if the stems should be created if not exist, defaults to false
   * 
   * @return the createStemsIfNotExist
   */
  public String getCreateStemsIfNotExist() {
    return this.createStemsIfNotExist;
  }

  /**
   * if the stems should be created if not exist, defaults to false
   * 
   * @param createStemsIfNotExist1
   *            the createStemsIfNotExist to set
   */
  public void setCreateStemsIfNotExist(String createStemsIfNotExist1) {
    this.createStemsIfNotExist = createStemsIfNotExist1;
  }

  /**
   * explicit to string
   * @return the string
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /** name of the group to find (includes stems, e.g. stem1:stem2:groupName */
  private String groupName;

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) 
   */
  private String saveMode;

  /**
   * uuid of the group to find
   * 
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the group to find
   * 
   * @param uuid1
   *            the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * name of the group to find (includes stems, e.g. stem1:stem2:groupName
   * 
   * @return the theName
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * name of the group to find (includes stems, e.g. stem1:stem2:groupName
   * 
   * @param theName
   *            the theName to set
   */
  public void setGroupName(String theName) {
    this.groupName = theName;
  }

  /**
   * validate the settings (e.g. that booleans are set correctly)
   */
  public void validate() {
    try {
      GrouperUtil.booleanValue(this.createStemsIfNotExist, false);
    } catch (Exception e) {
      throw new RuntimeException("createStemsIfNotExist is invalid, must be blank, t, "
          + "true, f, false (case insensitive): '" + this.createStemsIfNotExist + "', "
          + this);
    }
    try {
      if (!StringUtils.isBlank(this.saveMode)) {
        //make sure it exists
        SaveMode.valueOfIgnoreCase(this.saveMode);
      }
    } catch (RuntimeException e) {
      throw new RuntimeException("Problem with: " + this, e);
    }
  }

  /**
   * save this group
   * 
   * @param grouperSession
   *            to save
   * @return the group that was inserted or updated
   * @throws StemNotFoundException if problem
   * @throws GroupNotFoundException if problem
   * @throws GroupAddException if problem
   * @throws InsufficientPrivilegeException if problem
   * @throws GroupModifyException if problem
   * @throws StemAddException if problem
   */
  public Group save(GrouperSession grouperSession) throws StemNotFoundException,
      GroupNotFoundException, GroupAddException, InsufficientPrivilegeException,
      GroupModifyException, StemAddException {

    SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);
    boolean createStemsIfNotExistBoolean = GrouperUtil.booleanValue(
        this.createStemsIfNotExist, true);

    Group group = Group.saveGroup(grouperSession, this.description,
        this.displayExtension, this.groupName, this.uuid, theSaveMode,
        createStemsIfNotExistBoolean);
    return group;
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
}
