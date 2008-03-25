/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Result of one stem being deleted.  The number of
 * these result objects will equal the number of stems sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsStemDeleteResult {

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
    this.wsStemLookup = wsStemLookup1;
  }

  /** stem lookup */
  private WsStemLookup wsStemLookup;

  /** stem data */
  private WsStem wsStem;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public enum WsStemDeleteResultCode {

    /** if transactional call, and rolled back, otherwise success */
    TRANSACTION_ROLLED_BACK,

    /** successful addition */
    SUCCESS,

    /** invalid query, can only happen if lite query */
    INVALID_QUERY,

    /** the stem was not found */
    STEM_NOT_FOUND,

    /** problem with deleting */
    EXCEPTION,

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES;

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS || this == STEM_NOT_FOUND;
    }
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
   * @return the wsStemLookup
   */
  public WsStemLookup getWsStemLookup() {
    return this.wsStemLookup;
  }

  /**
   * @param wsStemLookup1 the wsStemLookup to set
   */
  public void setWsStemLookup(WsStemLookup wsStemLookup1) {
    this.wsStemLookup = wsStemLookup1;
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

}
