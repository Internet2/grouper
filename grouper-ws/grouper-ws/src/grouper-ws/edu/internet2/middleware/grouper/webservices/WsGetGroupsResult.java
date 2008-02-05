/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import edu.internet2.middleware.grouper.Group;



/**
 * Result of one group being retrieved since a user is a member of it.  The number of
 * groups will equal the number of groups the user is a member of (provided the filter matches)
 * 
 * @author mchyzer
 */
public class WsGetGroupsResult {

	/**
	 * friendly description of this group
	 */
	String description;
	/**
	 * friendly extensions of group and parent stems
	 */
	String displayName;
	/**
	 * if composite "T", else "F". A composite group is composed of two groups and a set operator  (stored in grouper_composites table) (e.g. union, intersection, etc).  A composite group has no immediate members. All subjects in a composite group are effective members.
	 */
	String isComposite;
	/**
	 * Full name of the group (all extensions of parent stems, separated by colons,  and the extention of this group
	 */
	String name;
	/**
	 * universally unique identifier of this group
	 */
	String uuid;

	/**
	 * no arg constructor
	 */
	public WsGetGroupsResult() {
		//blank
		
	}

	/**
	 * construct based on group, assign all fields
	 * @param group 
	 */
	public WsGetGroupsResult(Group group) {
		if (group != null) {
			this.setDescription(group.getDescription());
			this.setDisplayName(group.getDisplayName());
			this.setIsComposite(group.isComposite() ? "T" : "F");
			this.setName(group.getName());
			this.setUuid(group.getUuid());
		}
	}

	/**
	 * friendly description of this group
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * friendly extensions of group and parent stems
	 * @return the displayName
	 */
	public String getDisplayName() {
		return this.displayName;
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
		return this.isComposite;
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
	 * universally unique identifier of this group
	 * @return the uuid
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * friendly description of this group
	 * @param description1 the description to set
	 */
	public void setDescription(String description1) {
		this.description = description1;
	}

	/**
	 * friendly extensions of group and parent stems
	 * @param displayName1 the displayName to set
	 */
	public void setDisplayName(String displayName1) {
		this.displayName = displayName1;
	}

	/**
	 * if composite "T", else "F".
	 * 
	 * A composite group is composed of two groups and a set operator 
	 * (stored in grouper_composites table)
	 * (e.g. union, intersection, etc).  A composite group has no immediate members.
	 * All subjects in a composite group are effective members.
	 * @param isComposite1 the isComposite to set
	 */
	public void setIsComposite(String isComposite1) {
		this.isComposite = isComposite1;
	}

	/**
	 * Full name of the group (all extensions of parent stems, separated by colons, 
	 * and the extention of this group
	 * @param name1 the name to set
	 */
	public void setName(String name1) {
		this.name = name1;
	}

	/**
	 * universally unique identifier of this group
	 * @param uuid1 the uuid to set
	 */
	public void setUuid(String uuid1) {
		this.uuid = uuid1;
	}
}
