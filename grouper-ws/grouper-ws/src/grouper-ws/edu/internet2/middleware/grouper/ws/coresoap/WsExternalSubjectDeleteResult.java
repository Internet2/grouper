/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Result of one externalSubject being deleted.  The number of
 * these result objects will equal the number of externalSubjects sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsExternalSubjectDeleteResult implements ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsExternalSubjectDeleteResult.class);

  /**
   * empty constructor
   */
  public WsExternalSubjectDeleteResult() {
    //nothing to do
  }

  /**
   * @param wsExternalSubjectLookup is the externalSubject lookup to assign
   */
  public WsExternalSubjectDeleteResult(WsExternalSubjectLookup wsExternalSubjectLookup) {
    this.wsExternalSubject = new WsExternalSubject(null, wsExternalSubjectLookup); 
  }

  /**
   * create a result based on externalSubject
   * @param externalSubject
   * @param wsExternalSubjectLookup
   * @param includeDetail
   */
  public void assignExternalSubject(ExternalSubject externalSubject, WsExternalSubjectLookup wsExternalSubjectLookup) {
    this.setWsExternalSubject(new WsExternalSubject(externalSubject, wsExternalSubjectLookup));
  }

  /**
   * externalSubject to be deleted
   */
  private WsExternalSubject wsExternalSubject;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public static enum WsExternalSubjectDeleteResultCode {

    /** in externalSubject lookup, the uuid doesnt match name */
    EXTERNAL_SUBJECT_UUID_DOESNT_MATCH_NAME {

    },

    /** successful addition (lite status code 200) */
    SUCCESS {

    },

    /** invalid query, can only happen if Lite query */
    INVALID_QUERY {

    },

    /** the externalSubject was not found */
    SUCCESS_EXTERNAL_SUBJECT_NOT_FOUND {

    },

    /** problem with deleting */
    EXCEPTION {

    },

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES {

    },

    /** transaction rolled back */
    TRANSACTION_ROLLED_BACK {

    },

    /** if parent stem cant be found */
    PARENT_STEM_NOT_FOUND {


    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS || this == SUCCESS_EXTERNAL_SUBJECT_NOT_FOUND;
    }

  }

  /**
   * assign the code from the enum
   * @param externalSubjectDeleteResultCode
   */
  public void assignResultCode(WsExternalSubjectDeleteResultCode externalSubjectDeleteResultCode) {
    this.getResultMetadata().assignResultCode(
        externalSubjectDeleteResultCode == null ? null : externalSubjectDeleteResultCode.name());
    this.getResultMetadata().assignSuccess(
        GrouperServiceUtils.booleanToStringOneChar(externalSubjectDeleteResultCode.isSuccess()));
  }

  /**
   * @return the wsExternalSubject
   */
  public WsExternalSubject getWsExternalSubject() {
    return this.wsExternalSubject;
  }

  /**
   * @param wsExternalSubjectResult1 the wsExternalSubject to set
   */
  public void setWsExternalSubject(WsExternalSubject wsExternalSubjectResult1) {
    this.wsExternalSubject = wsExternalSubjectResult1;
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsExternalSubjectLookup
   */
  public void assignResultCodeException(Exception e, WsExternalSubjectLookup wsExternalSubjectLookup) {
    this.assignResultCode(WsExternalSubjectDeleteResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsExternalSubjectLookup + ", " + e, e);
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
   * convert string to result code
   * @return the result code
   */
  public WsExternalSubjectDeleteResultCode resultCode() {
    return WsExternalSubjectDeleteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

}
