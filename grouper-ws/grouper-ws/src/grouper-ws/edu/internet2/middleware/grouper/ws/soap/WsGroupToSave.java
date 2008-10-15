/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * <pre>
 * Class to save a group via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsGroupToSave {

  /** stem lookup (blank if insert) */
  private WsGroupLookup wsGroupLookup;

  /** stem to save */
  private WsGroup wsGroup;

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;

  /**
   * 
   */
  public WsGroupToSave() {
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
   * save this group
   * 
   * @param grouperSession
   *            to save
   * @return the stem that was inserted or updated
   * @throws StemNotFoundException 
   * @throws GroupNotFoundException
   * @throws GroupNotFoundException
   * @throws StemAddException 
   * @throws GroupAddException
   * @throws InsufficientPrivilegeException
   * @throws GroupModifyException
   * @throws GroupAddException
   */
  public Group save(GrouperSession grouperSession) throws StemNotFoundException,
      GroupNotFoundException, StemAddException, InsufficientPrivilegeException,
      GroupModifyException, GroupAddException {

    SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);

    this.getWsGroupLookup().retrieveGroupIfNeeded(grouperSession);

    Group groupLookedup = this.getWsGroupLookup().retrieveGroup();

    String groupNameLookup = groupLookedup == null ? null : groupLookedup.getName();

    Group group = Group.saveGroup(grouperSession, groupNameLookup, 
        this.getWsGroup().getUuid(), 
        this.getWsGroup().getName(), this.getWsGroup().getDisplayExtension(), 
        this.getWsGroup().getDescription(), theSaveMode, false);
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

  /**
   * @return the wsGroupLookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  /**
   * @param wsGroupLookup1 the wsGroupLookup to set
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }
}
