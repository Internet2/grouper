/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResult.WsGroupDeleteResultCode;

/**
 * <pre>
 * Class to lookup a group via web service
 * 
 * developers make sure each setter calls this.clearSubject();
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
  public enum GroupFindResult {

    /** found the subject */
    SUCCESS {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsGroupDeleteResultCode convertToDeleteCode() {
        return WsGroupDeleteResultCode.SUCCESS;
      }

    },

    /** uuid doesnt match name */
    GROUP_UUID_DOESNT_MATCH_NAME {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsGroupDeleteResultCode convertToDeleteCode() {
        return WsGroupDeleteResultCode.GROUP_UUID_DOESNT_MATCH_NAME;
      }

    },

    /** cant find the subject */
    GROUP_NOT_FOUND {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsGroupDeleteResultCode convertToDeleteCode() {
        return WsGroupDeleteResultCode.GROUP_NOT_FOUND;
      }

    },

    /** incvalid query (e.g. if everything blank) */
    INVALID_QUERY {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsGroupDeleteResultCode convertToDeleteCode() {
        return WsGroupDeleteResultCode.INVALID_QUERY;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /**
     * convert this code to a delete code
     * @return the code
     */
    public abstract WsGroupDeleteResultCode convertToDeleteCode();

    /**
     * null safe equivalent to convertToDeleteCode
     * @param groupFindResult to convert
     * @return the code
     */
    public static WsGroupDeleteResultCode convertToDeleteCodeStatic(
        GroupFindResult groupFindResult) {
      return groupFindResult == null ? WsGroupDeleteResultCode.EXCEPTION
          : groupFindResult.convertToDeleteCode();
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
   * @return the group
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
   * retrieve the group for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   */
  public void retrieveGroupIfNeeded(GrouperSession grouperSession) {
    this.retrieveGroupIfNeeded(grouperSession, null);
  }

  /**
   * retrieve the group for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @return the group
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public Group retrieveGroupIfNeeded(GrouperSession grouperSession,
      String invalidQueryReason) throws WsInvalidQueryException {
    //see if we already retrieved
    if (this.groupFindResult != null) {
      return this.group;
    }
    try {
      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      boolean hasName = !StringUtils.isBlank(this.groupName);

      //must have a name or uuid
      if (!hasUuid && !hasName) {
        this.groupFindResult = GroupFindResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid group query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      if (hasName) {
        Group theGroup = GroupFinder.findByName(grouperSession, this.groupName);

        //make sure uuid matches 
        if (hasUuid && !StringUtils.equals(this.uuid, theGroup.getUuid())) {
          this.groupFindResult = GroupFindResult.GROUP_UUID_DOESNT_MATCH_NAME;
          String error = "Group name '" + this.groupName + "' and uuid '" + this.uuid
              + "' do not match";
          if (!StringUtils.isEmpty(invalidQueryReason)) {
            throw new WsInvalidQueryException(error + " for '" + invalidQueryReason
                + "', " + this);
          }
          String logMessage = "Invalid query: " + this;
          LOG.warn(logMessage);
        }

        //success
        this.group = theGroup;

      } else if (hasUuid) {
        this.group = GroupFinder.findByUuid(grouperSession, this.uuid);
      }
      //assume success (set otherwise if there is a problem)
      this.groupFindResult = GroupFindResult.SUCCESS;

    } catch (GroupNotFoundException gnf) {
      this.groupFindResult = GroupFindResult.GROUP_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid group for '" + invalidQueryReason
            + "', " + this, gnf);
      }
    }
    return this.group;
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
