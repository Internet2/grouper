/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteLiteResult.WsAttributeDefNameDeleteLiteResultCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;



/**
 * Result of one attribute def name being deleted.  The number of
 * these result objects will equal the number of attribute def names sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsAttributeDefNameDeleteResult  {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefNameDeleteResultCode {
  
    /** in attribute def name lookup, the uuid doesnt match name */
    ATTRIBUTE_DEF_NAME_UUID_DOESNT_MATCH_NAME {
  
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefNameDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameDeleteLiteResultCode.ATTRIBUTE_DEF_NAME_UUID_DOESNT_MATCH_NAME;
      }
  
    },
  
    /** successful addition (lite status code 200) */
    SUCCESS {
  
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefNameDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameDeleteLiteResultCode.SUCCESS;
      }
  
    },
  
    /** invalid query, can only happen if Lite query */
    INVALID_QUERY {
  
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefNameDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameDeleteLiteResultCode.INVALID_QUERY;
      }
  
    },
  
    /** the attributeDefName was not found */
    SUCCESS_ATTRIBUTE_DEF_NAME_NOT_FOUND {
  
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefNameDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameDeleteLiteResultCode.SUCCESS_ATTRIBUTE_DEF_NAME_NOT_FOUND;
      }
  
    },
  
    /** problem with deleting */
    EXCEPTION {
  
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefNameDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameDeleteLiteResultCode.EXCEPTION;
      }
  
    },
  
    /** user not allowed */
    INSUFFICIENT_PRIVILEGES {
  
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefNameDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameDeleteLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }
  
    },
  
    /** transaction rolled back */
    TRANSACTION_ROLLED_BACK {
  
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefNameDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameDeleteLiteResultCode.EXCEPTION;
      }
  
    },
  
    /** if parent stem cant be found */
    PARENT_STEM_NOT_FOUND {
  
      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefNameDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefNameDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefNameDeleteLiteResultCode.PARENT_STEM_NOT_FOUND;
      }
  
    };
  
    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS || this == SUCCESS_ATTRIBUTE_DEF_NAME_NOT_FOUND;
    }
  
    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsAttributeDefNameDeleteLiteResultCode convertToLiteCode();
  }


  /**
   * empty constructor
   */
  public WsAttributeDefNameDeleteResult() {
    //nothing to do
  }

  /**
   * @param wsAttributeDefNameLookup is the attributeDefName lookup to assign
   */
  public WsAttributeDefNameDeleteResult(WsAttributeDefNameLookup wsAttributeDefNameLookup) {
    this.wsAttributeDefName = new WsAttributeDefName((AttributeDefName)null, wsAttributeDefNameLookup); 
  }

  /**
   * attribute def name to be deleted
   */
  private WsAttributeDefName wsAttributeDefName;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefNameDeleteResult.class);

  /**
   * @return the wsAttributeDefName
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * @param wsAttributeDefNameResult1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefNameResult1) {
    this.wsAttributeDefName = wsAttributeDefNameResult1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * create a result based on attributeDefName
   * @param attributeDefName
   * @param wsAttributeDefNameLookup
   * @param includeDetail
   */
  public void assignAttributeDefName(AttributeDefName attributeDefName, WsAttributeDefNameLookup wsAttributeDefNameLookup) {
    this.setWsAttributeDefName(new WsAttributeDefName(attributeDefName, wsAttributeDefNameLookup));
  }

  /**
   * assign the code from the enum
   * @param attributeDefNameDeleteResultCode
   */
  public void assignResultCode(WsAttributeDefNameDeleteResultCode attributeDefNameDeleteResultCode) {
    this.getResultMetadata().assignResultCode(
        attributeDefNameDeleteResultCode == null ? null : attributeDefNameDeleteResultCode.name());
    this.getResultMetadata().assignSuccess(
        GrouperServiceUtils.booleanToStringOneChar(attributeDefNameDeleteResultCode.isSuccess()));
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsAttributeDefNameLookup
   */
  public void assignResultCodeException(Exception e, WsAttributeDefNameLookup wsAttributeDefNameLookup) {
    this.assignResultCode(WsAttributeDefNameDeleteResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsAttributeDefNameLookup + ", " + e, e);
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsAttributeDefNameDeleteResultCode resultCode() {
    return WsAttributeDefNameDeleteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

}
