/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveLiteResult.WsAttributeDefNameSaveLiteResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;



/**
 * Result of one AttributeDefName being saved.  The number of
 * these result objects will equal the number of AttributeDefNames sent in to the method
 * to be saved
 * 
 * @author mchyzer
 */
public class WsAttributeDefNameSaveResult  {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefNameSaveResultCode {
  
    /** successful addition (success: T) */
    SUCCESS_INSERTED {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.SUCCESS_INSERTED;
      }
    },
  
    /** successful addition (success: T) */
    SUCCESS_UPDATED {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.SUCCESS_UPDATED;
      }
    },
  
    /** successful addition (success: T) */
    SUCCESS_NO_CHANGES_NEEDED {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.SUCCESS_NO_CHANGES_NEEDED;
      }
  
    },
  
    /** invalid query, can only happen if Lite query (success: F) */
    INVALID_QUERY {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.INVALID_QUERY;
      }
  
    },
  
    /** the group was not found (success: F) */
    ATTRIBUTE_DEF_NAME_NOT_FOUND {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.ATTRIBUTE_DEF_NAME_NOT_FOUND;
      }
  
    },
  
    /** the stem was not found (success: F) */
    STEM_NOT_FOUND {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.STEM_NOT_FOUND;
      }
  
    },
  
    /** problem with saving (success: F) */
    EXCEPTION {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.EXCEPTION;
      }
  
    },
  
    /** problem with saving (success: F) */
    ATTRIBUTE_DEF_NAME_ALREADY_EXISTS {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.ATTRIBUTE_DEF_NAME_ALREADY_EXISTS;
      }
  
    },
  
    /** user not allowed (success: F) */
    INSUFFICIENT_PRIVILEGES {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameSaveLiteResultCode
       */
      @Override
      public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameSaveLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }
  
    }, 
    
    /** was a success but rolled back */
    TRANSACTION_ROLLED_BACK {
    
          /** 
           * if there is one result, convert to the results code
           * @return WsAttributeDefNameSaveLiteResultCode
           */
          @Override
          public WsAttributeDefNameSaveLiteResultCode convertToLiteCode() {
            return WsAttributeDefNameSaveLiteResultCode.EXCEPTION;
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
     * @return name
     */
    public String nameForVersion(GrouperVersion clientVersion) {
  
      return this.name();
    }
    
  
    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsAttributeDefNameSaveLiteResultCode convertToLiteCode();
  }

  /** group saved */
  private WsAttributeDefName wsAttributeDefName;
  
  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefNameSaveResult.class);

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }
  
  /**
   * @return the wsAttributeDefName
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  
  /**
   * @param wsAttributeDefName1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * assign the code from the enum
   * @param groupSaveResultCode
   * @param clientVersion 
   */
  public void assignResultCode(WsAttributeDefNameSaveResultCode groupSaveResultCode, GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(
        groupSaveResultCode == null ? null : groupSaveResultCode.nameForVersion(clientVersion));
    this.getResultMetadata().assignSuccess(
        GrouperServiceUtils.booleanToStringOneChar(groupSaveResultCode.isSuccess()));
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsAttributeDefNameToSave
   * @param clientVersion 
   */
  public void assignResultCodeException(Exception e, WsAttributeDefNameToSave wsAttributeDefNameToSave, GrouperVersion clientVersion) {
    
    //get root exception (might be wrapped in wsInvalidQuery)
    Throwable mainThrowable = (e instanceof WsInvalidQueryException 
        && e.getCause() != null) ? e.getCause() : e;
    
    if (mainThrowable instanceof InsufficientPrivilegeException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefNameSaveResultCode.INSUFFICIENT_PRIVILEGES, clientVersion);
      
    } else if (mainThrowable  instanceof AttributeDefNameNotFoundException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefNameSaveResultCode.ATTRIBUTE_DEF_NAME_NOT_FOUND, clientVersion);
    } else if (mainThrowable  instanceof StemNotFoundException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefNameSaveResultCode.STEM_NOT_FOUND, clientVersion);
    } else if (e  instanceof WsInvalidQueryException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefNameSaveResultCode.INVALID_QUERY, clientVersion);
    } else if (mainThrowable!= null && (mainThrowable instanceof AttributeDefNameAddAlreadyExistsException
        || mainThrowable.getCause() instanceof AttributeDefNameAddAlreadyExistsException)) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefNameSaveResultCode.ATTRIBUTE_DEF_NAME_ALREADY_EXISTS, clientVersion);
    } else {
      this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(WsAttributeDefNameSaveResultCode.EXCEPTION, clientVersion);
    }
    LOG.error(wsAttributeDefNameToSave + ", " + e, e);
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsAttributeDefNameSaveResultCode resultCode() {
    return WsAttributeDefNameSaveResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * empty
   */
  public WsAttributeDefNameSaveResult() {
    //empty
  }

  /**
   * construct initially with lookup
   * @param wsAttributeDefNameLookup 
   */
  public WsAttributeDefNameSaveResult(WsAttributeDefNameLookup wsAttributeDefNameLookup) {
    this.wsAttributeDefName = new WsAttributeDefName((AttributeDefName)null, wsAttributeDefNameLookup);
  }
}
