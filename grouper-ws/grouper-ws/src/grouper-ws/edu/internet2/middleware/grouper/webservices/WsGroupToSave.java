/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupAddException;
import edu.internet2.middleware.grouper.GroupModifyException;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.StemAddException;
import edu.internet2.middleware.grouper.StemNotFoundException;

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

	/** if to retrieve with name if uuid isnt specified (T|F), defaults to true */
	private String retrieveViaNameIfNoUuid;

	/** if the group should be created if it doesnt exist (T|F), defaults to true */
	private String createGroupIfNotExist;

	/** if the stems should be created if not exist (T|F), defaults to false */
	private String createStemsIfNotExist;

	/**
	 * 
	 */
	public WsGroupToSave() {
		// empty constructor
	}

	/**
	 * @param uuid1
	 * @param description1
	 * @param displayExtension1
	 * @param retrieveViaNameIfNoUuid1
	 * @param createGroupIfNotExist1
	 * @param createStemsIfNotExist1
	 * @param groupName1
	 */
	public WsGroupToSave(String uuid1, String description1,
			String displayExtension1, String retrieveViaNameIfNoUuid1,
			String createGroupIfNotExist1, String createStemsIfNotExist1,
			String groupName1) {
		this.uuid = uuid1;
		this.description = description1;
		this.displayExtension = displayExtension1;
		this.retrieveViaNameIfNoUuid = retrieveViaNameIfNoUuid1;
		this.createGroupIfNotExist = createGroupIfNotExist1;
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
	 * if to retrieve with name if uuid isnt specified, defaults to true
	 * 
	 * @return the retrieveViaNameIfNoUuid
	 */
	public String getRetrieveViaNameIfNoUuid() {
		return this.retrieveViaNameIfNoUuid;
	}

	/**
	 * if to retrieve with name if uuid isnt specified, defaults to true
	 * 
	 * @param retrieveViaNameIfNoUuid1
	 *            the retrieveViaNameIfNoUuid to set
	 */
	public void setRetrieveViaNameIfNoUuid(String retrieveViaNameIfNoUuid1) {
		this.retrieveViaNameIfNoUuid = retrieveViaNameIfNoUuid1;
	}

	/**
	 * if the group should be created if it doesnt exist, defaults to true
	 * 
	 * @return the createGroupIfNotExist
	 */
	public String getCreateGroupIfNotExist() {
		return this.createGroupIfNotExist;
	}

	/**
	 * if the group should be created if it doesnt exist, defaults to true
	 * 
	 * @param createGroupIfNotExist1
	 *            the createGroupIfNotExist to set
	 */
	public void setCreateGroupIfNotExist(String createGroupIfNotExist1) {
		this.createGroupIfNotExist = createGroupIfNotExist1;
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

	/** name of the group to find (includes stems, e.g. stem1:stem2:groupName */
	private String groupName;

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
			GrouperServiceUtils.booleanValue(this.createGroupIfNotExist, true);
		} catch (Exception e) {
			throw new RuntimeException(
					"createGroupIfNotExist is invalid, must be blank, t, "
							+ "true, f, false (case insensitive): '"
							+ this.createGroupIfNotExist + "', " + this);
		}
		try {
			GrouperServiceUtils.booleanValue(this.createStemsIfNotExist, false);
		} catch (Exception e) {
			throw new RuntimeException(
					"createStemsIfNotExist is invalid, must be blank, t, "
							+ "true, f, false (case insensitive): '"
							+ this.createStemsIfNotExist + "', " + this);
		}
		try {
			GrouperServiceUtils
					.booleanValue(this.retrieveViaNameIfNoUuid, true);
		} catch (Exception e) {
			throw new RuntimeException(
					"retrieveViaNameIfNoUuid is invalid, must be blank, t, "
							+ "true, f, false (case insensitive): '"
							+ this.retrieveViaNameIfNoUuid + "', " + this);
		}
	}

	/**
	 * save this group
	 * 
	 * @param grouperSession
	 *            to save
	 * @return the group that was inserted or updated
	 * @throws StemNotFoundException
	 * @throws GroupNotFoundException
	 * @throws GroupAddException
	 * @throws InsufficientPrivilegeException
	 * @throws GroupModifyException
	 * @throws StemAddException
	 */
	public Group save(GrouperSession grouperSession)
			throws StemNotFoundException, GroupNotFoundException,
			GroupAddException, InsufficientPrivilegeException,
			GroupModifyException, StemAddException {

		boolean retrieveViaNameIfNotUuidBoolean = GrouperServiceUtils
				.booleanValue(this.retrieveViaNameIfNoUuid, true);
		boolean createGroupIfNotExistBoolean = GrouperServiceUtils
				.booleanValue(this.createGroupIfNotExist, true);
		boolean createStemsIfNotExistBoolean = GrouperServiceUtils
				.booleanValue(this.createStemsIfNotExist, true);

		Group group = Group.saveGroup(grouperSession, this.description,
				this.displayExtension, this.groupName, this.uuid,
				retrieveViaNameIfNotUuidBoolean, createGroupIfNotExistBoolean,
				createStemsIfNotExistBoolean);
		return group;
	}
}
