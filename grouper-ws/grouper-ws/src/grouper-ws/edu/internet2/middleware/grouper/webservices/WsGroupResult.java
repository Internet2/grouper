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
 * @author mchyzer
 *
 */
public class WsGroupResult {
	
	/**
	 * no arg constructor
	 */
	public WsGroupResult() {
		
	}

	/**
	 * construct based on group, assign all fields
	 */
	public WsGroupResult(Group group) {
		if (group != null) {
			this.setCreateSource(group.getCreateSource());
			String createSubjectId = null;
			try {
				Subject createSubject = group.getCreateSubject();
				createSubjectId = createSubject == null ? null : createSubject.getId();
			} catch (SubjectNotFoundException e) {
				//dont do anything if not found, null
			}
			this.setCreateSubjectId(createSubjectId);
			this.setCreateTime(GrouperServiceUtils.dateToString(group.getCreateTime()));
			this.setDescription(group.getDescription());
			this.setDisplayExtension(group.getDisplayExtension());
			this.setDisplayName(group.getDisplayName());
			this.setExtension(group.getExtension());
			this.setIsComposite(group.isComposite() ? "T" : "F");
			this.setModifySource(group.getModifySource());
			
			String modifySubjectId = null;
			try {
				Subject modifySubject = group.getModifySubject();
				modifySubjectId = modifySubject == null ? null : modifySubject.getId();
			} catch (SubjectNotFoundException e) {
				//dont do anything if not found, null
			}
			
			this.setModifySubjectId(modifySubjectId);
			this.setModifyTime(GrouperServiceUtils.dateToString(group.getModifyTime()));
			this.setName(group.getName());
			Stem parentStem = group.getParentStem();
			this.setParentStemName(parentStem == null ? null : parentStem.getName());
			this.setParentStemUuid(parentStem == null ? null : parentStem.getUuid());
			this.setUuid(group.getUuid());
		}
	}
	
	/**
	 * Get (optional and questionable) create source for this group.
	 */
	String createSource;

	/**
	 * id of the subject that created this group
	 */
	String createSubjectId;
	
	/**
	 * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
	 */
	String createTime;

    /**
	 * if composite "T", else "F".
	 * 
     * A composite group is composed of two groups and a set operator 
     * (stored in grouper_composites table)
     * (e.g. union, intersection, etc).  A composite group has no immediate members.
     * All subjects in a composite group are effective members.
	 */
	String isComposite;
	
	/**
	 * friendly description of this group
	 */
	String description;

	/**
	 * friendly extension of this group
	 */
	String displayExtension;
	
	/**
	 * friendly extensions of group and parent stems
	 */
	String displayName;
	
	/**
	 * system name of this group (not including parent stems)
	 */
	String extension;
	
	/**
	 * Get (optional and questionable) modify source for this group.
	 */
	String modifySource;
	
	/**
	 * Get subject that last modified this group.
	 */
	String modifySubjectId;
	
	/**
	 * Get last modified time for this group.  yyyy/mm/dd hh24:mi:ss.SSS
	 */
	String modifyTime;
	
	/**
	 * Full name of the group (all extensions of parent stems, separated by colons, 
	 * and the extention of this group
	 */
	String name;

	/**
	 * Full name of the parent stem including parent stems
	 */
	String parentStemName;
	
	/**
	 * uuid of the parent stem
	 */
	String parentStemUuid;
	
	/**
	 * universally unique identifier of this group
	 */
	String uuid;

	/**
	 * Get (optional and questionable) create source for this group.
	 * @return the createSource
	 */
	public String getCreateSource() {
		return this.createSource;
	}

	/**
	 * Get (optional and questionable) create source for this group.
	 * @param createSource1 the createSource to set
	 */
	public void setCreateSource(String createSource1) {
		this.createSource = createSource1;
	}

	/**
	 * id of the subject that created this group
	 * @return the createSubjectId
	 */
	public String getCreateSubjectId() {
		return this.createSubjectId;
	}

	/**
	 * id of the subject that created this group
	 * @param createSubjectId1 the createSubjectId to set
	 */
	public void setCreateSubjectId(String createSubjectId1) {
		this.createSubjectId = createSubjectId1;
	}

	/**
	 * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
	 * @return the createTime
	 */
	public String getCreateTime() {
		return this.createTime;
	}

	/**
	 * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
	 * @param createTime1 the createTime to set
	 */
	public void setCreateTime(String createTime1) {
		this.createTime = createTime1;
	}

	/**
	 * if composite "T", else "F".
	 * 
     * A composite group is composed of two groups and a set operator 
     * (stored in grouper_composites table)
     * (e.g. union, intersection, etc).  A composite group has no immediate members.
     * All subjects in a composite group are effective members.
	 * @return the isComposite
	 */
	public String getIsComposite() {
		return isComposite;
	}

	/**
	 * if composite "T", else "F".
	 * 
     * A composite group is composed of two groups and a set operator 
     * (stored in grouper_composites table)
     * (e.g. union, intersection, etc).  A composite group has no immediate members.
     * All subjects in a composite group are effective members.
	 * @param isComposite the isComposite to set
	 */
	public void setIsComposite(String isComposite) {
		this.isComposite = isComposite;
	}

	/**
	 * friendly description of this group
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * friendly description of this group
	 * @param description1 the description to set
	 */
	public void setDescription(String description1) {
		this.description = description1;
	}

	/**
	 * friendly extension of this group
	 * @return the displayExtension
	 */
	public String getDisplayExtension() {
		return this.displayExtension;
	}

	/**
	 * friendly extension of this group
	 * @param displayExtension1 the displayExtension to set
	 */
	public void setDisplayExtension(String displayExtension1) {
		this.displayExtension = displayExtension1;
	}

	/**
	 * friendly extensions of group and parent stems
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * friendly extensions of group and parent stems
	 * @param displayName1 the displayName to set
	 */
	public void setDisplayName(String displayName1) {
		this.displayName = displayName1;
	}

	/**
	 * system name of this group (not including parent stems)
	 * @return the extension
	 */
	public String getExtension() {
		return this.extension;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}

	/**
	 * Get (optional and questionable) modify source for this group.
	 * @return the modifySource
	 */
	public String getModifySource() {
		return this.modifySource;
	}

	/**
	 * Get (optional and questionable) modify source for this group.
	 * @param modifySource the modifySource to set
	 */
	public void setModifySource(String modifySource) {
		this.modifySource = modifySource;
	}

	/**
	 * Get subject that last modified this group.
	 * @return the modifySubjectId
	 */
	public String getModifySubjectId() {
		return this.modifySubjectId;
	}

	/**
	 * Get subject that last modified this group.
	 * @param modifySubjectId the modifySubjectId to set
	 */
	public void setModifySubjectId(String modifySubjectId) {
		this.modifySubjectId = modifySubjectId;
	}

	/**
	 * Get last modified time for this group.  yyyy/mm/dd hh24:mi:ss.SSS
	 * @return the modifyTime
	 */
	public String getModifyTime() {
		return this.modifyTime;
	}

	/**
	 * Get last modified time for this group.  yyyy/mm/dd hh24:mi:ss.SSS
	 * @param modifyTime the modifyTime to set
	 */
	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}

	/**
	 * Full name of the group (all extensions of parent stems, separated by colons, 
	 * and the extention of this group
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Full name of the group (all extensions of parent stems, separated by colons, 
	 * and the extention of this group
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Full name of the parent stem including parent stems
	 * @return the parentStemName
	 */
	public String getParentStemName() {
		return this.parentStemName;
	}

	/**
	 * Full name of the parent stem including parent stems
	 * @param parentStemName the parentStemName to set
	 */
	public void setParentStemName(String parentStemName) {
		this.parentStemName = parentStemName;
	}

	/**
	 * uuid of the parent stem
	 * @return the parentStemUuid
	 */
	public String getParentStemUuid() {
		return this.parentStemUuid;
	}

	/**
	 * uuid of the parent stem
	 * @param parentStemUuid the parentStemUuid to set
	 */
	public void setParentStemUuid(String parentStemUuid) {
		this.parentStemUuid = parentStemUuid;
	}

	/**
	 * universally unique identifier of this group
	 * @return the uuid
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * universally unique identifier of this group
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
