/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * Result of one save being saved.  The number of
 * these result objects will equal the number of saves sent in to the method
 * to be saved
 * 
 * @author mchyzer
 */
public class WsStemSaveResult {

  /** stem that is saved */
  private WsStem wsStem = null;

  /** stem lookup for this save */
  private WsStemLookup wsStemLookup = null;

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsStemSaveResult.class);

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public enum WsStemSaveResultCode {

    /** successful addition */
    SUCCESS,

    /** invalid query, can only happen if simple query */
    INVALID_QUERY,

    /** the save was not found */
    STEM_NOT_FOUND,

    /** problem with saving */
    EXCEPTION,

    /** was a success but rolled back */
    TRANSACTION_ROLLED_BACK,

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES;

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  /**
   * assign the code from the enum
   * @param saveSaveResultCode
   */
  public void assignResultCode(WsStemSaveResultCode saveSaveResultCode) {
    this.getResultMetadata().assignResultCode(
        saveSaveResultCode == null ? null : saveSaveResultCode.name());
    this.getResultMetadata().assignSuccess(saveSaveResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsStemToSave
   */
  public void assignResultCodeException(Exception e, WsStemToSave wsStemToSave) {
    if (e instanceof WsInvalidQueryException) {

      if (e.getCause() instanceof StemNotFoundException) {
        this.assignResultCode(WsStemSaveResultCode.STEM_NOT_FOUND);
      } else {
        this.assignResultCode(WsStemSaveResultCode.INVALID_QUERY);
        this.getResultMetadata().setResultMessage(e.getMessage());
      }

    } else {
      this.assignResultCode(WsStemSaveResultCode.EXCEPTION);
      this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    }
    LOG.error(wsStemToSave + ", " + e, e);
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
   * stem lookup for this save
   * @return the wsStemLookup
   */
  public WsStemLookup getWsStemLookup() {
    return this.wsStemLookup;
  }

  /**
   * stem lookup for this save
   * @param wsStemLookup1 the wsStemLookup to set
   */
  public void setWsStemLookup(WsStemLookup wsStemLookup1) {
    this.wsStemLookup = wsStemLookup1;
  }
}
