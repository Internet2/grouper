/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

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

/**
 * <pre>
 * Class to save a stem via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsStemToSave {

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

	/**
	 * uuid of the stem to find
	 */
	private String stemUuid;

	/** description of stem */
	private String description;

	/** display extension is the friendly name (without path) */
	private String displayExtension;

	/** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
	private String saveMode;

	/** if the stems should be created if not exist (T|F), defaults to false */
	private String createStemsIfNotExist;

	/**
	 * 
	 */
	public WsStemToSave() {
		// empty constructor
	}

	/**
	 * construct from params
	 * @param uuid1 uuid
	 * @param description1 description
	 * @param displayExtension1 display extension
	 * @param saveMode1 INSERT, UPDATE, or INSERT_OR_UPDATE (default)
	 * @param createParentStemsIfNotExist1 if parents should be created if not there
	 * @param stemName1 name of stem
	 */
	public WsStemToSave(String uuid1, String description1,
			String displayExtension1,
			String saveMode1, String createParentStemsIfNotExist1,
			String stemName1) {
		this.stemUuid = uuid1;
		this.description = description1;
		this.displayExtension = displayExtension1;
		this.saveMode = saveMode1;
		this.createStemsIfNotExist = createParentStemsIfNotExist1;
		this.stemName = stemName1;
	}

	/**
	 * description of stem
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * description of stem
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
	 * make sure this is an explicit toString
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/** name of the stem to find (includes stems, e.g. stem1:stem2:stemName */
	private String stemName;

	/**
	 * uuid of the stem to find
	 * 
	 * @return the uuid
	 */
	public String getStemUuid() {
		return this.stemUuid;
	}

	/**
	 * uuid of the stem to find
	 * 
	 * @param uuid1
	 *            the uuid to set
	 */
	public void setStemUuid(String uuid1) {
		this.stemUuid = uuid1;
	}

	/**
	 * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
	 * 
	 * @return the theName
	 */
	public String getStemName() {
		return this.stemName;
	}

	/**
	 * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
	 * 
	 * @param theName
	 *            the theName to set
	 */
	public void setStemName(String theName) {
		this.stemName = theName;
	}

	/**
	 * validate the settings (e.g. that booleans are set correctly)
	 */
	public void validate() {
		try {
			GrouperServiceUtils.booleanValue(this.createStemsIfNotExist, false);
		} catch (Exception e) {
			throw new RuntimeException(
					"createStemsIfNotExist is invalid, must be blank, t, "
							+ "true, f, false (case insensitive): '"
							+ this.createStemsIfNotExist + "', " + this);
		}
		try {
			if (!StringUtils.isBlank(this.saveMode)) {
				//make sure it exists
				SaveMode.valueOfIgnoreCase(this.saveMode);
			}
		} catch (RuntimeException e) {
			throw new RuntimeException(
					"Problem with: " + this, e);
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
	public Stem save(GrouperSession grouperSession)
			throws StemNotFoundException, StemNotFoundException,
			StemAddException, InsufficientPrivilegeException,
			StemModifyException, StemAddException {

		SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);
		boolean createStemsIfNotExistBoolean = GrouperServiceUtils
				.booleanValue(this.createStemsIfNotExist, true);

		Stem stem = Stem.saveStem(grouperSession, this.description,
				this.displayExtension, this.stemName, this.stemUuid,
				theSaveMode,
				createStemsIfNotExistBoolean);
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
}
