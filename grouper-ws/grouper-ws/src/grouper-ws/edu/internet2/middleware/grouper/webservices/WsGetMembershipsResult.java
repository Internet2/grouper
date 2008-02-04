/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;


/**
 * Result of one member being retrieved from a group. 
 * 
 * @author mchyzer
 */
public class WsGetMembershipsResult {
	
	/** stem name this membership is realted to */
	private String stemName = null;
	
	/** id of the membership */
	private String membershipId = null;
	
	/** list name of the membership */
	private String listName = null;
	
	/** list type of the membership */
	private String listType = null;
	
	/** membership type of the membership */
	private String membershipType = null;
	
	/** depth of the membership */
	private int depth = -1;
	
	/**
	 * id of the membership
	 * @return the membershipId
	 */
	public String getMembershipId() {
		return this.membershipId;
	}

	/**
	 * id of the membership
	 * @param membershipId1 the membershipId to set
	 */
	public void setMembershipId(String membershipId1) {
		this.membershipId = membershipId1;
	}

	/**
	 * no-arg constructor
	 */
	public WsGetMembershipsResult() {
		//nothing
	}
	
	/** group name this membership is related to */
	private String groupName=null;
	
	/**
	 * construct with member to set internal fields
	 * @param membership
	 * @param retrieveExtendedSubjectDataBoolean true to retrieve subject info (more than just the id)
	 */
	public WsGetMembershipsResult(Membership membership, boolean retrieveExtendedSubjectDataBoolean) {
		this.setMembershipId(membership.getUuid());
		this.setMembershipType(membership.getType());
		this.setCreateTime(GrouperServiceUtils.dateToString(membership.getCreateTime()));
		this.setDepth(membership.getDepth());
		Group group = null;
		try {
			group = membership.getGroup();
		} catch (GroupNotFoundException gnfe) {
			//group info is null if not there
		}
		this.setGroupName(group == null ? null : group.getName());
		Field listField = membership.getList();
		FieldType listFieldType = listField == null ? null : listField.getType();
		this.setListType(listFieldType == null ? null : listFieldType.toString());
		this.setListName(listField == null ? null : listField.getName());
		Stem stem = null;
		try {
			stem = membership.getStem();
		} catch (StemNotFoundException snfe) {
			//stem fields will be null if this happens
		}
		this.setStemName(stem == null ? null : stem.getName());
		Member member = null;
		try {
			member = membership.getMember();
		} catch (MemberNotFoundException mnfe) {
			//member fields will be null if this happens
		}
		if (member != null) {
			
			//if getting the subject data (extra queries)
			if (retrieveExtendedSubjectDataBoolean) {
				Subject subject = null;
				try {
					subject = member.getSubject();
				} catch (SubjectNotFoundException snfe) {
					//I guess just ignore if not found, fields will be null
				}
				this.setSubjectId(member.getSubjectId());
				SubjectType theSubjectType = subject.getType();
				this.setSubjectType(theSubjectType == null ? null : theSubjectType.getName());
				this.setSubjectDescription(subject.getDescription());
				this.setSubjectName(subject.getName());

				{
					//see if attribute0
					String attributeName0 = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE0);
					if (!StringUtils.isBlank(attributeName0) && attributeName0.startsWith(
							WsGetMembersResult.SUBJECT_ATTRIBUTE_PREFIX)) {
						attributeName0 = attributeName0.substring(
								WsGetMembersResult.SUBJECT_ATTRIBUTE_PREFIX.length());
						this.setAttribute0(subject.getAttributeValue(attributeName0));
					}
				}
	
				{
					//see if attribute1
					String attributeName1 = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE1);
					if (!StringUtils.isBlank(attributeName1) && attributeName1.startsWith(
							WsGetMembersResult.SUBJECT_ATTRIBUTE_PREFIX)) {
						attributeName1 = attributeName1.substring(
								WsGetMembersResult.SUBJECT_ATTRIBUTE_PREFIX.length());
						this.setAttribute1(subject.getAttributeValue(attributeName1));
					}
				}
	
				{
					//see if attribute2
					String attributeName2 = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE2);
					if (!StringUtils.isBlank(attributeName2) && attributeName2.startsWith(
							WsGetMembersResult.SUBJECT_ATTRIBUTE_PREFIX)) {
						attributeName2 = attributeName2.substring(
								WsGetMembersResult.SUBJECT_ATTRIBUTE_PREFIX.length());
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
	
	/** timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS */
	private String createTime;
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

	/**
	 * list name of the membership
	 * @return the listName
	 */
	public String getListName() {
		return this.listName;
	}

	/**
	 * list name of the membership
	 * @param listName1 the listName to set
	 */
	public void setListName(String listName1) {
		this.listName = listName1;
	}

	/**
	 * list type of the membership
	 * @return the listType
	 */
	public String getListType() {
		return this.listType;
	}

	/**
	 * list type of the membership
	 * @param listType1 the listType to set
	 */
	public void setListType(String listType1) {
		this.listType = listType1;
	}

	/**
	 * @return the membershipType
	 */
	public String getMembershipType() {
		return this.membershipType;
	}

	/**
	 * @param membershipType1 the membershipType to set
	 */
	public void setMembershipType(String membershipType1) {
		this.membershipType = membershipType1;
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return this.depth;
	}

	/**
	 * @param depth1 the depth to set
	 */
	public void setDepth(int depth1) {
		this.depth = depth1;
	}

	/**
	 * timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS
	 * @return the createTime
	 */
	public String getCreateTime() {
		return this.createTime;
	}

	/**
	 * timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS
	 * @param createTime1 the createTime to set
	 */
	public void setCreateTime(String createTime1) {
		this.createTime = createTime1;
	}

	/**
	 * stem name this membership is realted to
	 * @return the stemName
	 */
	public String getStemName() {
		return this.stemName;
	}

	/**
	 * stem name this membership is realted to
	 * @param stemName1 the stemName to set
	 */
	public void setStemName(String stemName1) {
		this.stemName = stemName1;
	}

	/**
	 * group name this membership is related to
	 * @return the groupName
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * group name this membership is related to
	 * @param groupName1 the groupName to set
	 */
	public void setGroupName(String groupName1) {
		this.groupName = groupName1;
	}
}
