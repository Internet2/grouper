/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemDeleteLiteResult.WsStemDeleteLiteResultCode;

/**
 * Result of one stem being deleted.  The number of
 * these result objects will equal the number of stems sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsStemDeleteResult implements ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsStemDeleteResult.class);

  /**
   * empty constructor
   */
  public WsStemDeleteResult() {
    //empty I said
  }

  /**
   * construct with lookup
   * @param wsStemLookup1
   */
  public WsStemDeleteResult(WsStemLookup wsStemLookup1) {
    this.wsStem = new WsStem(wsStemLookup1);
  }

  /** stem data */
  private WsStem wsStem;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * convert string to result code
   * @return the result code
   */
  public WsStemDeleteResultCode resultCode() {
    return WsStemDeleteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * result code of a request
   */
  public static enum WsStemDeleteResultCode {

    /** if transactional call, and rolled back, otherwise success */
    TRANSACTION_ROLLED_BACK {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemDeleteLiteResultCode
       */
      @Override
      public WsStemDeleteLiteResultCode convertToLiteCode() {
        return WsStemDeleteLiteResultCode.EXCEPTION;
      }

    },

    /** successful addition */
    SUCCESS {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemDeleteLiteResultCode
       */
      @Override
      public WsStemDeleteLiteResultCode convertToLiteCode() {
        return WsStemDeleteLiteResultCode.SUCCESS;
      }

    },

    /** invalid query, can only happen if lite query */
    INVALID_QUERY {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemDeleteLiteResultCode
       */
      @Override
      public WsStemDeleteLiteResultCode convertToLiteCode() {
        return WsStemDeleteLiteResultCode.INVALID_QUERY;
      }

    },

    /** the stem was not found */
    SUCCESS_STEM_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemDeleteLiteResultCode
       */
      @Override
      public WsStemDeleteLiteResultCode convertToLiteCode() {
        return WsStemDeleteLiteResultCode.SUCCESS_STEM_NOT_FOUND;
      }

    },

    /** problem with deleting */
    EXCEPTION {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemDeleteLiteResultCode
       */
      @Override
      public WsStemDeleteLiteResultCode convertToLiteCode() {
        return WsStemDeleteLiteResultCode.EXCEPTION;
      }

    },

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES {

      /** 
       * if there is one result, convert to the results code
       * @return WsStemDeleteLiteResultCode
       */
      @Override
      public WsStemDeleteLiteResultCode convertToLiteCode() {
        return WsStemDeleteLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS || this == SUCCESS_STEM_NOT_FOUND;
    }

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsStemDeleteLiteResultCode convertToLiteCode();
  }

  /**
   * assign the code from the enum
   * @param stemDeleteResultCode
   */
  public void assignResultCode(WsStemDeleteResultCode stemDeleteResultCode) {
    this.getResultMetadata().assignResultCode(
        stemDeleteResultCode == null ? null : stemDeleteResultCode.name());
    this.getResultMetadata().assignSuccess(stemDeleteResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * @return the wsStem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * @param wsStem1 the wsStem to set
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsStemLookup1
   */
  public void assignResultCodeException(Exception e, WsStemLookup wsStemLookup1) {
    this.assignResultCode(WsStemDeleteResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsStemLookup1 + ", " + e, e);
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

}
