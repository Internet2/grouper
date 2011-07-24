/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.GroupAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveLiteResult.WsGroupSaveLiteResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Result of one group being saved.  The number of
 * these result objects will equal the number of groups sent in to the method
 * to be saved
 * 
 * @author mchyzer
 */
public class WsGroupSaveResult  {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGroupSaveResult.class);

  /** group saved */
  private WsGroup wsGroup;
  
  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public static enum WsGroupSaveResultCode {

    /** successful addition (success: T) */
    SUCCESS_INSERTED {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.SUCCESS_INSERTED;
      }

      /** get the name label for a certain version of client 
       * @param clientVersion 
       * @return */
      @Override
      public String nameForVersion(GrouperVersion clientVersion) {

        //before 1.4 we had SUCCESS and nothing more descriptive
        if (clientVersion != null && clientVersion.lessThanArg(GrouperVersion.valueOfIgnoreCase("v1_4_000"))) {
          return "SUCCESS";
        }
        return this.name();
      }
      

    },

    /** successful addition (success: T) */
    SUCCESS_UPDATED {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.SUCCESS_UPDATED;
      }

      /** get the name label for a certain version of client 
       * @param clientVersion 
       * @return */
      @Override
      public String nameForVersion(GrouperVersion clientVersion) {

        //before 1.4 we had SUCCESS and nothing more descriptive
        if (clientVersion != null && clientVersion.lessThanArg(GrouperVersion.valueOfIgnoreCase("v1_4_000"))) {
          return "SUCCESS";
        }
        return this.name();
      }

    },

    /** successful addition (success: T) */
    SUCCESS_NO_CHANGES_NEEDED {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.SUCCESS_NO_CHANGES_NEEDED;
      }

      /** get the name label for a certain version of client 
       * @param clientVersion 
       * @return */
      @Override
      public String nameForVersion(GrouperVersion clientVersion) {

        //before 1.4 we had SUCCESS and nothing more descriptive
        if (clientVersion != null && clientVersion.lessThanArg(GrouperVersion.valueOfIgnoreCase("v1_4_000"))) {
          return "SUCCESS";
        }
        return this.name();
      }

    },

    /** invalid query, can only happen if Lite query (success: F) */
    INVALID_QUERY {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.INVALID_QUERY;
      }

    },

    /** the group was not found (success: F) */
    GROUP_NOT_FOUND {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.GROUP_NOT_FOUND;
      }

    },

    /** the stem was not found (success: F) */
    STEM_NOT_FOUND {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.STEM_NOT_FOUND;
      }

    },

    /** problem with saving (success: F) */
    EXCEPTION {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.EXCEPTION;
      }

    },

    /** problem with saving (success: F) */
    GROUP_ALREADY_EXISTS {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.GROUP_ALREADY_EXISTS;
      }

    },

    /** user not allowed (success: F) */
    INSUFFICIENT_PRIVILEGES {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsGroupSaveLiteResultCode
       */
      @Override
      public WsGroupSaveLiteResultCode convertToLiteCode() {
        return WsGroupSaveLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    }, 
    
    /** was a success but rolled back */
    TRANSACTION_ROLLED_BACK {
    
          /** 
           * if there is one result, convert to the results code
           * @return WsGroupSaveLiteResultCode
           */
          @Override
          public WsGroupSaveLiteResultCode convertToLiteCode() {
            return WsGroupSaveLiteResultCode.EXCEPTION;
          }
    
        };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {

      return this.name();
    }
    

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsGroupSaveLiteResultCode convertToLiteCode();
  }

  
  /**
   * assign the code from the enum
   * @param groupSaveResultCode
   * @param clientVersion 
   */
  public void assignResultCode(WsGroupSaveResultCode groupSaveResultCode, GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(
        groupSaveResultCode == null ? null : groupSaveResultCode.nameForVersion(clientVersion));
    this.getResultMetadata().assignSuccess(
        GrouperServiceUtils.booleanToStringOneChar(groupSaveResultCode.isSuccess()));
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }
  
  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  
  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * empty
   */
  public WsGroupSaveResult() {
    //empty
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsGroupToSave
   * @param clientVersion 
   */
  public void assignResultCodeException(Exception e, WsGroupToSave wsGroupToSave, GrouperVersion clientVersion) {
    
    //get root exception (might be wrapped in wsInvalidQuery)
    Throwable mainThrowable = (e instanceof WsInvalidQueryException 
        && e.getCause() != null) ? e.getCause() : e;
    
    if (mainThrowable instanceof InsufficientPrivilegeException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsGroupSaveResultCode.INSUFFICIENT_PRIVILEGES, clientVersion);
      
    } else if (mainThrowable  instanceof GroupNotFoundException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsGroupSaveResultCode.GROUP_NOT_FOUND, clientVersion);
    } else if (mainThrowable  instanceof StemNotFoundException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsGroupSaveResultCode.STEM_NOT_FOUND, clientVersion);
    } else if (e  instanceof WsInvalidQueryException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsGroupSaveResultCode.INVALID_QUERY, clientVersion);
    } else if (mainThrowable!= null && (mainThrowable instanceof GroupAddAlreadyExistsException
        || mainThrowable.getCause() instanceof GroupAddAlreadyExistsException)) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsGroupSaveResultCode.GROUP_ALREADY_EXISTS, clientVersion);
    } else {
      this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(WsGroupSaveResultCode.EXCEPTION, clientVersion);
    }
    LOG.error(wsGroupToSave + ", " + e, e);
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsGroupSaveResultCode resultCode() {
    return WsGroupSaveResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * construct initially with lookup
   * @param wsGroupLookup 
   */
  public WsGroupSaveResult(WsGroupLookup wsGroupLookup) {
    this.wsGroup = new WsGroup(null, wsGroupLookup, false);
  }
}
