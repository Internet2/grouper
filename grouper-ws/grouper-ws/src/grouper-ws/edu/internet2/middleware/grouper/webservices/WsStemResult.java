/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Result for finding a stem
 * 
 * @author mchyzer
 * 
 */
public class WsStemResult {

	/**
	 * result code of a request
	 */
	public enum WsStemResultCode {
	
		/** found the stem (or not) */
		SUCCESS,
	
		/** stem had problems */
		PARENT_STEM_NOT_FOUND;
	
		/**
		 * if this is a successful result
		 * 
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	}

	/**
	 * no arg constructor
	 */
	public WsStemResult() {
		// blank

	}

	/**
	 * construct based on stem, assign all fields
	 * 
	 * @param stem is what to construct from
	 * @param assignParentStem if parent stem not found, then dont do this part...
	 */
	public WsStemResult(Stem stem) {
		if (stem != null) {
			this.setCreateSource(stem.getCreateSource());
			String createSubjectIdString = null;
			
			try {
				Subject createSubject = stem.getCreateSubject();
				createSubjectIdString = createSubject == null ? null
						: createSubject.getId();
			} catch (SubjectNotFoundException e) {
				// dont do anything if not found, null
			}
			this.setCreateSubjectId(createSubjectIdString);
			this.setCreateTime(GrouperServiceUtils.dateToString(stem
					.getCreateTime()));
			this.setDescription(stem.getDescription());
			this.setDisplayExtension(stem.getDisplayExtension());
			this.setDisplayName(stem.getDisplayName());
			this.setExtension(stem.getExtension());
			this.setModifySource(stem.getModifySource());

			String modifySubjectIdString = null;
			try {
				Subject modifySubject = stem.getModifySubject();
				modifySubjectIdString = modifySubject == null ? null
						: modifySubject.getId();
			} catch (SubjectNotFoundException e) {
				// dont do anything if not found, null
			}

			this.setModifySubjectId(modifySubjectIdString);
			this.setModifyTime(GrouperServiceUtils.dateToString(stem
					.getModifyTime()));
			this.setName(stem.getName());
			this.setUuid(stem.getUuid());

			this.assignResultCode(WsStemResultCode.SUCCESS);

			//do this at end for proper result code handling
			Stem parentStem = null;
			try {
				parentStem = stem.getParentStem();
				this.setParentStemName(parentStem == null ? null : parentStem
						.getName());
				this.setParentStemUuid(parentStem == null ? null : parentStem
						.getUuid());
			} catch (StemNotFoundException snfe) {
				this.assignResultCode(WsStemResultCode.PARENT_STEM_NOT_FOUND);
			}
		}
	}

	/**
	 * Get (optional and questionable) create source for this stem.
	 */
	private String createSource;

	/**
	 * id of the subject that created this stem
	 */
	private String createSubjectId;

	/**
	 * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
	 */
	private String createTime;

	/**
	 * friendly description of this stem
	 */
	private String description;

	/**
	 * friendly extension of this stem
	 */
	private String displayExtension;

	/**
	 * friendly extensions of stem and parent stems
	 */
	private String displayName;

	/**
	 * system name of this stem (not including parent stems)
	 */
	private String extension;

	/**
	 * Get (optional and questionable) modify source for this stem.
	 */
	private String modifySource;

	/**
	 * Get subject that last modified this stem.
	 */
	private String modifySubjectId;

	/**
	 * Get last modified time for this stem. yyyy/mm/dd hh24:mi:ss.SSS
	 */
	private String modifyTime;

	/**
	 * Full name of the stem (all extensions of parent stems, separated by
	 * colons, and the extention of this stem
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
	 * universally unique identifier of this stem
	 */
	private String uuid;

	/**
	 * <pre> code of the result for this subject SUCCESS: means everything ok SUBJECT_NOT_FOUND: cant find the subject SUBJECT_DUPLICATE: found multiple subjects </pre>
	 */
	private String resultCode;

	/**
	 * T or F as to whether it was a successful assignment 
	 */
	private String success;

	/**
	 * friendly message that could be audited
	 */
	private String resultMessage;

	/**
	 * Get (optional and questionable) create source for this stem.
	 * 
	 * @return the createSource
	 */
	public String getCreateSource() {
		return this.createSource;
	}

	/**
	 * Get (optional and questionable) create source for this stem.
	 * 
	 * @param createSource1
	 *            the createSource to set
	 */
	public void setCreateSource(String createSource1) {
		this.createSource = createSource1;
	}

	/**
	 * id of the subject that created this stem
	 * 
	 * @return the createSubjectId
	 */
	public String getCreateSubjectId() {
		return this.createSubjectId;
	}

	/**
	 * id of the subject that created this stem
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
	 * friendly description of this stem
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * friendly description of this stem
	 * 
	 * @param description1
	 *            the description to set
	 */
	public void setDescription(String description1) {
		this.description = description1;
	}

	/**
	 * friendly extension of this stem
	 * 
	 * @return the displayExtension
	 */
	public String getDisplayExtension() {
		return this.displayExtension;
	}

	/**
	 * friendly extension of this stem
	 * 
	 * @param displayExtension1
	 *            the displayExtension to set
	 */
	public void setDisplayExtension(String displayExtension1) {
		this.displayExtension = displayExtension1;
	}

	/**
	 * friendly extensions of stem and parent stems
	 * 
	 * @return the displayName
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * friendly extensions of stem and parent stems
	 * 
	 * @param displayName1
	 *            the displayName to set
	 */
	public void setDisplayName(String displayName1) {
		this.displayName = displayName1;
	}

	/**
	 * system name of this stem (not including parent stems)
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
	 * Get (optional and questionable) modify source for this stem.
	 * 
	 * @return the modifySource
	 */
	public String getModifySource() {
		return this.modifySource;
	}

	/**
	 * Get (optional and questionable) modify source for this stem.
	 * 
	 * @param modifySource1
	 *            the modifySource to set
	 */
	public void setModifySource(String modifySource1) {
		this.modifySource = modifySource1;
	}

	/**
	 * Get subject that last modified this stem.
	 * 
	 * @return the modifySubjectId
	 */
	public String getModifySubjectId() {
		return this.modifySubjectId;
	}

	/**
	 * Get subject that last modified this stem.
	 * 
	 * @param modifySubjectId1
	 *            the modifySubjectId to set
	 */
	public void setModifySubjectId(String modifySubjectId1) {
		this.modifySubjectId = modifySubjectId1;
	}

	/**
	 * Get last modified time for this stem. yyyy/mm/dd hh24:mi:ss.SSS
	 * 
	 * @return the modifyTime
	 */
	public String getModifyTime() {
		return this.modifyTime;
	}

	/**
	 * Get last modified time for this stem. yyyy/mm/dd hh24:mi:ss.SSS
	 * 
	 * @param modifyTime1
	 *            the modifyTime to set
	 */
	public void setModifyTime(String modifyTime1) {
		this.modifyTime = modifyTime1;
	}

	/**
	 * Full name of the stem (all extensions of parent stems, separated by
	 * colons, and the extention of this stem
	 * 
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Full name of the stem (all extensions of parent stems, separated by
	 * colons, and the extention of this stem
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
	 * universally unique identifier of this stem
	 * 
	 * @return the uuid
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * universally unique identifier of this stem
	 * 
	 * @param uuid1
	 *            the uuid to set
	 */
	public void setUuid(String uuid1) {
		this.uuid = uuid1;
	}

	/**
	 * assign the code from the enum
	 * 
	 * @param wsStemResultCode
	 */
	public void assignResultCode(WsStemResultCode wsStemResultCode) {
		this.setResultCode(wsStemResultCode == null ? null
				: wsStemResultCode.name());
		this.setSuccess(wsStemResultCode.isSuccess() ? "T" : "F");
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 *  
	 * </pre>
	 * 
	 * @return the resultCode
	 */
	public String getResultCode() {
		return this.resultCode;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * 
	 * @return the success
	 */
	public String getSuccess() {
		return this.success;
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 *  
	 * </pre>
	 * 
	 * @param resultCode1
	 *            the resultCode to set
	 */
	public void setResultCode(String resultCode1) {
		this.resultCode = resultCode1;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * 
	 * @param success1
	 *            the success to set
	 */
	public void setSuccess(String success1) {
		this.success = success1;
	}

	/**
	 * friendly message that could be audited
	 * 
	 * @return the errorMessage
	 */
	public String getResultMessage() {
		return this.resultMessage;
	}

	/**
	 * friendly message that could be audited
	 * 
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setResultMessage(String errorMessage) {
		this.resultMessage = errorMessage;
	}
}
