/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 * Result of one member being retrieved from a group. 
 * 
 * @author mchyzer
 */
public class WsGetMembersResult {
	
	/** prefix of attribute that refers to subject: subject. */
	public static final String SUBJECT_ATTRIBUTE_PREFIX = "subject.";

	/**
	 * no-arg constructor
	 */
	public WsGetMembersResult() {
		//nothing
	}
	
	/**
	 * construct with member to set internal fields
	 * @param member
	 * @param retrieveExtendedSubjectDataBoolean true to retrieve subject info (more than just the id)
	 */
	public WsGetMembersResult(Member member, boolean retrieveExtendedSubjectDataBoolean) {
		this.setSubjectType(member.getSubjectType().getName());
		this.setSubjectId(member.getSubjectId());
		
		//if getting the subject data (extra queries)
		if (retrieveExtendedSubjectDataBoolean) {
			Subject subject = null;
			try {
				subject = member.getSubject();
			} catch (SubjectNotFoundException snfe) {
				//I guess just ignore if not found, fields will be null
			}
			if (subject != null) {
				this.setSubjectDescription(subject.getDescription());
				this.setSubjectName(subject.getName());
	
				{
					//see if attribute0
					String attributeName0 = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE0);
					if (!StringUtils.isBlank(attributeName0) && attributeName0.startsWith(SUBJECT_ATTRIBUTE_PREFIX)) {
						attributeName0 = attributeName0.substring(SUBJECT_ATTRIBUTE_PREFIX.length());
						this.setAttribute0(subject.getAttributeValue(attributeName0));
					}
				}
	
				{
					//see if attribute1
					String attributeName1 = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE1);
					if (!StringUtils.isBlank(attributeName1) && attributeName1.startsWith(SUBJECT_ATTRIBUTE_PREFIX)) {
						attributeName1 = attributeName1.substring(SUBJECT_ATTRIBUTE_PREFIX.length());
						this.setAttribute1(subject.getAttributeValue(attributeName1));
					}
				}
	
				{
					//see if attribute2
					String attributeName2 = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE2);
					if (!StringUtils.isBlank(attributeName2) && attributeName2.startsWith(SUBJECT_ATTRIBUTE_PREFIX)) {
						attributeName2 = attributeName2.substring(SUBJECT_ATTRIBUTE_PREFIX.length());
						this.setAttribute2(subject.getAttributeValue(attributeName2));
					}
				}
			}
		}
		
	}
	
	/** subject that was added */
	private String subjectId;
	
	/** subject type of this member (person, group, application) */
	private String subjectType;
	
	/** 
	 * subject name, or group name, or application name returned, 
	 * this is extended subject data 
	 */
	private String subjectName;
	
	/** 
	 * description of the subject, this is extended subject data 
	 */
	private String subjectDescription;
	
	/** 
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute0 value, this is extended subject data 
	 */
	private String attribute0;
	
	/** 
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute1 value, this is extended subject data 
	 */
	private String attribute1;
	
	/** 
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute2 value, this is extended subject data 
	 */
	private String attribute2;
	
	/**
	 * subject that was added
	 * @return the subjectId
	 */
	public String getSubjectId() {
		return this.subjectId;
	}

	/**
	 * subject that was added
	 * @param subjectId1 the subjectId to set
	 */
	public void setSubjectId(String subjectId1) {
		this.subjectId = subjectId1;
	}

	/**
	 * subject type of this member (person, group, application)
	 * @return the subjectType
	 */
	public String getSubjectType() {
		return this.subjectType;
	}

	/**
	 * subject type of this member (person, group, application)
	 * @param subjectType1 the subjectType to set
	 */
	public void setSubjectType(String subjectType1) {
		this.subjectType = subjectType1;
	}

	/**
	 * subject name, or group name, or application name returned, 
	 * this is extended subject data 
	 * @return the subjectName
	 */
	public String getSubjectName() {
		return this.subjectName;
	}

	/**
	 * subject name, or group name, or application name returned, 
	 * this is extended subject data 
	 * @param subjectName1 the subjectName to set
	 */
	public void setSubjectName(String subjectName1) {
		this.subjectName = subjectName1;
	}

	/**
	 * description of the subject, this is extended subject data 
	 * @return the subjectDescription
	 */
	public String getSubjectDescription() {
		return this.subjectDescription;
	}

	/**
	 * description of the subject, this is extended subject data 
	 * @param subjectDescription1 the subjectDescription to set
	 */
	public void setSubjectDescription(String subjectDescription1) {
		this.subjectDescription = subjectDescription1;
	}

	/**
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute0 value, this is extended subject data 
	 * @return the attribute0
	 */
	public String getAttribute0() {
		return this.attribute0;
	}

	/**
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute0 value, this is extended subject data 
	 * @param attribute0a the attribute0 to set
	 */
	public void setAttribute0(String attribute0a) {
		this.attribute0 = attribute0a;
	}

	/**
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute1 value, this is extended subject data 
	 * @return the attribute1
	 */
	public String getAttribute1() {
		return this.attribute1;
	}

	/**
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute1 value, this is extended subject data 
	 * @param attribute1a the attribute1 to set
	 */
	public void setAttribute1(String attribute1a) {
		this.attribute1 = attribute1a;
	}

	/**
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute2 value, this is extended subject data 
	 * @return the attribute2
	 */
	public String getAttribute2() {
		return this.attribute2;
	}

	/**
	 * if attributes are being sent back per config in the grouper.properties, 
	 * this is attribute2 value, this is extended subject data 
	 * @param attribute2a the attribute2 to set
	 */
	public void setAttribute2(String attribute2a) {
		this.attribute2 = attribute2a;
	}
}
