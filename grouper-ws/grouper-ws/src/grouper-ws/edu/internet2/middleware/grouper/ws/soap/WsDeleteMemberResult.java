/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults.WsDeleteMemberResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Result of one subject being deleted from a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsDeleteMemberResult {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsDeleteMemberResult.class);

  /**
   * result code of a request
   */
  public enum WsDeleteMemberResultCode {

    /** cant find group */
    GROUP_NOT_FOUND {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.GROUP_NOT_FOUND;
      }
    },

    /** invalid request */
    INVALID_QUERY {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.INVALID_QUERY;
      }
    },

    /** successful addition */
    SUCCESS {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.SUCCESS;
      }
    },

    /** successful addition */
    SUCCESS_BUT_HAS_EFFECTIVE {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.SUCCESS_BUT_HAS_EFFECTIVE;
      }
    },

    /** successful addition */
    SUCCESS_WASNT_IMMEDIATE {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.SUCCESS_WASNT_IMMEDIATE;
      }
    },

    /** successful addition */
    SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE;
      }
    },

    /** the subject was not found */
    SUBJECT_NOT_FOUND {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.SUBJECT_NOT_FOUND;
      }
    },

    /** problem with deletion */
    EXCEPTION {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.EXCEPTION;
      }

    },

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.INSUFFICIENT_PRIVILEGES;
      }

    },

    /** if one item failed in the transaction, then roll back */
    TRANSACTION_ROLLED_BACK {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        //this should never happen, rolled back on one record
        return WsDeleteMemberResultsCode.EXCEPTION;
      }
    },

    /** subject duplicate found */
    SUBJECT_DUPLICATE {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsDeleteMemberResultsCode convertToResultsCode() {
        return WsDeleteMemberResultsCode.SUBJECT_DUPLICATE;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public abstract boolean isSuccess();

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsDeleteMemberResultsCode convertToResultsCode();

  }

  /** subject that was deleteed */
  private String subjectId;

  /** subject identifier (if this is what was passed in) that was deleteed */
  private String subjectIdentifier;

  /**
   * convert string to result code
   * @return the result code
   */
  public WsDeleteMemberResultCode resultCode() {
    return WsDeleteMemberResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * subject that was deleteed
   * @return the subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject that was deleteed
   * @param subjectId1 the subjectId to set
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * subject identifier (if this is what was passed in) that was deleteed
   * @return the subjectIdentifier
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }

  /**
   * subject identifier (if this is what was passed in) that was deleteed
   * @param subjectIdentifier1 the subjectIdentifier to set
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
  }

  /**
   * assign the code from the enum
   * @param deleteMemberResultCode
   */
  public void assignResultCode(WsDeleteMemberResultCode deleteMemberResultCode) {
    this.getResultMetadata().assignResultCode(
        deleteMemberResultCode == null ? null : deleteMemberResultCode.name());
    this.getResultMetadata().assignSuccess(
        GrouperServiceUtils.booleanToStringOneChar(deleteMemberResultCode.isSuccess()));
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsSubjectLookup
   */
  public void assignResultCodeException(Exception e, WsSubjectLookup wsSubjectLookup) {
    this.assignResultCode(WsDeleteMemberResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsSubjectLookup + ", " + e, e);
  }

  /**
   * assign a success based on four situations based on hasImmediate and hasEffective
   * @param hasImmediate
   * @param hasEffective
   */
  public void assignResultCodeSuccess(boolean hasImmediate, boolean hasEffective) {
    //set success based on scenario
    if (hasEffective) {
      this
          .assignResultCode(hasImmediate ? WsDeleteMemberResultCode.SUCCESS_BUT_HAS_EFFECTIVE
              : WsDeleteMemberResultCode.SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE);
    } else {
      this.assignResultCode(hasImmediate ? WsDeleteMemberResultCode.SUCCESS
          : WsDeleteMemberResultCode.SUCCESS_WASNT_IMMEDIATE);
    }
  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup
   */
  public void processSubject(WsSubjectLookup wsSubjectLookup) {

    this.setSubjectId(wsSubjectLookup.getSubjectId());
    this.setSubjectIdentifier(wsSubjectLookup.getSubjectIdentifier());

    // these will probably match, but just in case
    if (StringUtils.isBlank(this.getSubjectId())) {
      this.setSubjectId(wsSubjectLookup.retrieveSubject().getId());
    }

    SubjectFindResult subjectFindResult = wsSubjectLookup.retrieveSubjectFindResult();

    switch (subjectFindResult) {
      case INVALID_QUERY:
        this.assignResultCode(WsDeleteMemberResultCode.INVALID_QUERY);
        break;
      case SOURCE_UNAVAILABLE:
        this.assignResultCode(WsDeleteMemberResultCode.EXCEPTION);
        break;
      case SUBJECT_DUPLICATE:
        this.assignResultCode(WsDeleteMemberResultCode.SUBJECT_DUPLICATE);
        break;
      case SUBJECT_NOT_FOUND:
        this.assignResultCode(WsDeleteMemberResultCode.SUBJECT_NOT_FOUND);
        break;
      case SUCCESS:
        return;
    }

    this.getResultMetadata().setResultMessage(
        "Subject: " + wsSubjectLookup + " had problems: " + subjectFindResult);

  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

}
