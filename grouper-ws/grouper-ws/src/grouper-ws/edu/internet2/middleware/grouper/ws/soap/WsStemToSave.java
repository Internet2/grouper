/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.SaveMode;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemAddException;
import edu.internet2.middleware.grouper.StemModifyException;
import edu.internet2.middleware.grouper.StemNotFoundException;
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

    Stem stem = Stem.saveStem(grouperSession, stemNameLookup, this.getWsStem()
        .getDescription(), this.getWsStem().getDisplayExtension(), this.getWsStem()
        .getName(), this.getWsStem().getUuid(), theSaveMode, false);
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
