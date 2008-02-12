/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Result for finding a group
 * 
 * @author mchyzer
 * 
 */
public class WsGroupResult {

	/**
	 * no arg constructor
	 */
	public WsGroupResult() {
		// blank

	}

	/**
	 * construct based on group, assign all fields
	 * 
	 * @param group is what to construct from
	 */
	public WsGroupResult(Group group) {
		if (group != null) {
			this.setCreateSource(group.getCreateSource());
			String createSubjectIdString = null;
			try {
				Subject createSubject = group.getCreateSubject();
				createSubjectIdString = createSubject == null ? null
						: createSubject.getId();
			} catch (SubjectNotFoundException e) {
				// dont do anything if not found, null
			}
			this.setCreateSubjectId(createSubjectIdString);
			this.setCreateTime(GrouperServiceUtils.dateToString(group
					.getCreateTime()));
			this.setDescription(group.getDescription());
			this.setDisplayExtension(group.getDisplayExtension());
			this.setDisplayName(group.getDisplayName());
			this.setExtension(group.getExtension());
			this.setIsComposite(group.isComposite() ? "T" : "F");
			this.setModifySource(group.getModifySource());

			String modifySubjectIdString = null;
			try {
				Subject modifySubject = group.getModifySubject();
				modifySubjectIdString = modifySubject == null ? null
						: modifySubject.getId();
			} catch (SubjectNotFoundException e) {
				// dont do anything if not found, null
			}

			this.setModifySubjectId(modifySubjectIdString);
			this.setModifyTime(GrouperServiceUtils.dateToString(group
					.getModifyTime()));
			this.setName(group.getName());
			Stem parentStem = group.getParentStem();
			this.setParentStemName(parentStem == null ? null : parentStem
					.getName());
			this.setParentStemUuid(parentStem == null ? null : parentStem
					.getUuid());
			this.setUuid(group.getUuid());
		}
	}

	/**
	 * Get (optional and questionable) create source for this group.
	 */
	private String createSource;

	/**
	 * id of the subject that created this group
	 */
	private String createSubjectId;

	/**
	 * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
	 */
	private String createTime;

	/**
	 * if composite "T", else "F".
	 * 
	 * A composite group is composed of two groups and a set operator (stored in
	 * grouper_composites table) (e.g. union, intersection, etc). A composite
	 * group has no immediate members. All subjects in a composite group are
	 * effective members.
	 */
	private String isComposite;

	/**
	 * friendly description of this group
	 */
	private String description;

	/**
	 * friendly extension of this group
	 */
	private String displayExtension;

	/**
	 * friendly extensions of group and parent stems
	 */
	private String displayName;

	/**
	 * system name of this group (not including parent stems)
	 */
	private String extension;

	/**
	 * Get (optional and questionable) modify source for this group.
	 */
	private String modifySource;

	/**
	 * Get subject that last modified this group.
	 */
	private String modifySubjectId;

	/**
	 * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
	 */
	private String modifyTime;

	/**
	 * Full name of the group (all extensions of parent stems, separated by
	 * colons, and the extention of this group
	 */
	private String name;

	/**
	 * Full name of the parent stem including parent stems
	 */
	private String parentStemName;

	/**
	 * uuid of the parent stem
	 */
	private String parentStemUuid;

	/**
	 * universally unique identifier of this group
	 */
	private String uuid;

	/**
	 * Get (optional and questionable) create source for this group.
	 * 
	 * @return the createSource
	 */
	public String getCreateSource() {
		return this.createSource;
	}

	/**
	 * Get (optional and questionable) create source for this group.
	 * 
	 * @param createSource1
	 *            the createSource to set
	 */
	public void setCreateSource(String createSource1) {
		this.createSource = createSource1;
	}

	/**
	 * id of the subject that created this group
	 * 
	 * @return the createSubjectId
	 */
	public String getCreateSubjectId() {
		return this.createSubjectId;
	}

	/**
	 * id of the subject that created this group
	 * 
	 * @param createSubjectId1
	 *            the createSubjectId to set
	 */
	public void setCreateSubjectId(String createSubjectId1) {
		this.createSubjectId = createSubjectId1;
	}

	/**
	 * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
	 * 
	 * @return the createTime
	 */
	public String getCreateTime() {
		return this.createTime;
	}

	/**
	 * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
	 * 
	 * @param createTime1
	 *            the createTime to set
	 */
	public void setCreateTime(String createTime1) {
		this.createTime = createTime1;
	}

	/**
	 * if composite "T", else "F".
	 * 
	 * A composite group is composed of two groups and a set operator (stored in
	 * grouper_composites table) (e.g. union, intersection, etc). A composite
	 * group has no immediate members. All subjects in a composite group are
	 * effective members.
	 * 
	 * @return the isComposite
	 */
	public String getIsComposite() {
		return this.isComposite;
	}

	/**
	 * if composite "T", else "F".
	 * 
	 * A composite group is composed of two groups and a set operator (stored in
	 * grouper_composites table) (e.g. union, intersection, etc). A composite
	 * group has no immediate members. All subjects in a composite group are
	 * effective members.
	 * 
	 * @param isComposite1
	 *            the isComposite to set
	 */
	public void setIsComposite(String isComposite1) {
		this.isComposite = isComposite1;
	}

	/**
	 * friendly description of this group
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * friendly description of this group
	 * 
	 * @param description1
	 *            the description to set
	 */
	public void setDescription(String description1) {
		this.description = description1;
	}

	/**
	 * friendly extension of this group
	 * 
	 * @return the displayExtension
	 */
	public String getDisplayExtension() {
		return this.displayExtension;
	}

	/**
	 * friendly extension of this group
	 * 
	 * @param displayExtension1
	 *            the displayExtension to set
	 */
	public void setDisplayExtension(String displayExtension1) {
		this.displayExtension = displayExtension1;
	}

	/**
	 * friendly extensions of group and parent stems
	 * 
	 * @return the displayName
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * friendly extensions of group and parent stems
	 * 
	 * @param displayName1
	 *            the displayName to set
	 */
	public void setDisplayName(String displayName1) {
		this.displayName = displayName1;
	}

	/**
	 * system name of this group (not including parent stems)
	 * 
	 * @return the extension
	 */
	public String getExtension() {
		return this.extension;
	}

	/**
	 * @param extension1
	 *            the extension to set
	 */
	public void setExtension(String extension1) {
		this.extension = extension1;
	}

	/**
	 * Get (optional and questionable) modify source for this group.
	 * 
	 * @return the modifySource
	 */
	public String getModifySource() {
		return this.modifySource;
	}

	/**
	 * Get (optional and questionable) modify source for this group.
	 * 
	 * @param modifySource1
	 *            the modifySource to set
	 */
	public void setModifySource(String modifySource1) {
		this.modifySource = modifySource1;
	}

	/**
	 * Get subject that last modified this group.
	 * 
	 * @return the modifySubjectId
	 */
	public String getModifySubjectId() {
		return this.modifySubjectId;
	}

	/**
	 * Get subject that last modified this group.
	 * 
	 * @param modifySubjectId1
	 *            the modifySubjectId to set
	 */
	public void setModifySubjectId(String modifySubjectId1) {
		this.modifySubjectId = modifySubjectId1;
	}

	/**
	 * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
	 * 
	 * @return the modifyTime
	 */
	public String getModifyTime() {
		return this.modifyTime;
	}

	/**
	 * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
	 * 
	 * @param modifyTime1
	 *            the modifyTime to set
	 */
	public void setModifyTime(String modifyTime1) {
		this.modifyTime = modifyTime1;
	}

	/**
	 * Full name of the group (all extensions of parent stems, separated by
	 * colons, and the extention of this group
	 * 
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Full name of the group (all extensions of parent stems, separated by
	 * colons, and the extention of this group
	 * 
	 * @param name1
	 *            the name to set
	 */
	public void setName(String name1) {
		this.name = name1;
	}

	/**
	 * Full name of the parent stem including parent stems
	 * 
	 * @return the parentStemName
	 */
	public String getParentStemName() {
		return this.parentStemName;
	}

	/**
	 * Full name of the parent stem including parent stems
	 * 
	 * @param parentStemName1
	 *            the parentStemName to set
	 */
	public void setParentStemName(String parentStemName1) {
		this.parentStemName = parentStemName1;
	}

	/**
	 * uuid of the parent stem
	 * 
	 * @return the parentStemUuid
	 */
	public String getParentStemUuid() {
		return this.parentStemUuid;
	}

	/**
	 * uuid of the parent stem
	 * 
	 * @param parentStemUuid1
	 *            the parentStemUuid to set
	 */
	public void setParentStemUuid(String parentStemUuid1) {
		this.parentStemUuid = parentStemUuid1;
	}

	/**
	 * universally unique identifier of this group
	 * 
	 * @return the uuid
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * universally unique identifier of this group
	 * 
	 * @param uuid1
	 *            the uuid to set
	 */
	public void setUuid(String uuid1) {
		this.uuid = uuid1;
	}
}
