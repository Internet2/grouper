/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.WsResultCode;

/**
 * Result of one subject being deleted from a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsDeleteMemberLiteResult {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsDeleteMemberLiteResult.class);

  /**
   * result code of a request
   */
  public static enum WsDeleteMemberLiteResultCode implements WsResultCode {

    /** cant find group (rest http status code 404) (success: F) */
    GROUP_NOT_FOUND(404) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** invalid request (rest http status code 400) (success: F) */
    INVALID_QUERY(400) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** successful addition (rest http status code 200) (success: T) */
    SUCCESS(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

    },

    /** successful addition (rest http status code 200) (success: T) */
    SUCCESS_BUT_HAS_EFFECTIVE(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

    },

    /** successful addition (rest http status code 200) (success: T) */
    SUCCESS_WASNT_IMMEDIATE(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

    },

    /** successful addition (rest http status code 200) (success: T) */
    SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

    },

    /** the subject was not found (rest http status code 200) (success: T) */
    SUBJECT_NOT_FOUND(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** problem with deletion (rest http status code 500) (success: F) */
    EXCEPTION(500) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** user not allowed (rest http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** subject duplicate found (rest http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    }, 

    /** problem deleting existing members (lite http status code 500) (success: F) */
    PROBLEM_DELETING_MEMBERS(500) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public abstract boolean isSuccess();
    
    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsDeleteMemberLiteResultCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;
    
    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

    
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

  }

  /**
   * constructor
   */
  public WsDeleteMemberLiteResult() {
    //empty
  }
  
  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * subject that was added 
   */
  private WsSubject wsSubject;

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * group assigned to
   */
  private WsGroup wsGroup;

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * @param wsSubject1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  /**
   * group assigned to
   * @return the wsGroupLookup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * group assigned to
   * @param theWsGroupLookupAssigned the wsGroupLookup to set
   */
  public void setWsGroup(WsGroup theWsGroupLookupAssigned) {
    this.wsGroup = theWsGroupLookupAssigned;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

}
