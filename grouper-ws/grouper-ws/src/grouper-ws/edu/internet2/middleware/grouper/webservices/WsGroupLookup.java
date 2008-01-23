/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperSession;

/**
 * <pre>
 * Class to lookup a group via web service
 * 
 * developers make sure each setter calls this.clearSubject();
 * TODO: add in extensions in the query
 * </pre>
 * @author mchyzer
 */
public class WsGroupLookup {
	
	/**
	 * logger 
	 */
	private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

	/** find the group */
	private Group group = null;
	
	/** result of group find */
	private enum GroupFindResult {
		
		/** found the subject */
		SUCCESS, 
		
		/** found multiple results */
		GROUP_NOT_UNIQUE, 
		
		/** cant find the subject */
		GROUP_NOT_FOUND,
		
		/** incvalid query (e.g. if everything blank) */
		INVALID_QUERY,
		
		/** when the source if not available */
		SOURCE_UNAVAILABLE;
		
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	}

	/**
	 * uuid of the group to find
	 */
	private String uuid;
	
	/**
	 * <pre>
	 * 
	 * Note: this is not a javabean property because we dont want it in the web service
	 * </pre>
	 * @return the subject
	 */
	public Group retrieveGroup() {
		return this.group;
	}

	/**
	 * <pre>
	 * 
	 * Note: this is not a javabean property because we dont want it in the web service
	 * </pre>
	 * @return the subjectFindResult, this is never null
	 */
	public GroupFindResult retrieveGroupFindResult() {
		return this.groupFindResult;
	}

    /**
     * make sure this is an explicit toString
     */
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }

	/**
	 * pass in a grouper session
	 * @param grouperSession 
	 */
	public void retrieveGroupIfNeeded(GrouperSession grouperSession) {
		//see if we already retrieved
		if (this.groupFindResult != null) {
			return;
		}
		try {
			//assume success (set otherwise if there is a problem)
			this.groupFindResult = GroupFindResult.SUCCESS;
			
			boolean hasUuid = !StringUtils.isBlank(this.uuid);
			
			boolean hasName = !StringUtils.isBlank(this.groupName);

			//must have a name or uuid
			if (!hasUuid && !hasName) {
				this.groupFindResult = GroupFindResult.INVALID_QUERY;
				String logMessage = "Invalid query: " + this;
				LOG.warn(logMessage);
				//TODO remove:
				System.out.println(logMessage);
				return;
			}
			
			if (hasName) {
				this.group = GroupFinder.findByName(grouperSession, this.groupName);
			} else if (hasUuid) {
				this.group = GroupFinder.findByUuid(grouperSession, this.uuid);
			}
			
		} catch (GroupNotFoundException gnf) {
			LOG.warn(this, gnf);
			this.groupFindResult = GroupFindResult.GROUP_NOT_FOUND;
			//TODO remove
			gnf.printStackTrace();
		}
		
	}

	
	/**
	 * clear the subject if a setter is called
	 */
	private void clearGroup() {
		this.group = null;
		this.groupFindResult = null;
	}
	
	/** name of the group to find (includes stems, e.g. stem1:stem2:groupName */
	private String groupName;
	
	/** result of subject find */
	private GroupFindResult groupFindResult = null;

	/**
	 * uuid of the group to find
	 * @return the uuid
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * uuid of the group to find
	 * @param uuid1 the uuid to set
	 */
	public void setUuid(String uuid1) {
		this.uuid = uuid1;
		this.clearGroup();
	}

	/**
	 * name of the group to find (includes stems, e.g. stem1:stem2:groupName
	 * @return the theName
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * name of the group to find (includes stems, e.g. stem1:stem2:groupName
	 * @param theName the theName to set
	 */
	public void setGroupName(String theName) {
		this.groupName = theName;
		this.clearGroup();
	}

	/**
	 * 
	 */
	public WsGroupLookup() {
		//blank
	}

	/**
	 * @param groupName1 
	 * @param uuid1
	 */
	public WsGroupLookup(String groupName1, String uuid1) {
		this.uuid = uuid1;
		this.setGroupName(groupName1);
	}
		
}
