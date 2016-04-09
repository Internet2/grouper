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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.soap_v2_3.WsHasMemberLiteResult.WsHasMemberLiteResultCode;
import edu.internet2.middleware.grouper.ws.soap_v2_3.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Result of seeing if one subject is a member of a group.  The number of
 * results will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsHasMemberResult {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsHasMemberResult.class);

  /** sujbect info for hasMember */
  private WsSubject wsSubject;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param wsSubjectResult1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubjectResult1) {
    this.wsSubject = wsSubjectResult1;
  }

  /** empty constructor */
  public WsHasMemberResult() {
    //nothing
  }

  /**
   * result based on a subject lookup.  Might set stat codes meaning abort mission
   * @param wsSubjectLookup
   * @param subjectAttributeNamesToRetrieve
   */
  public WsHasMemberResult(WsSubjectLookup wsSubjectLookup,
      String[] subjectAttributeNamesToRetrieve) {

    this.wsSubject = new WsSubject(wsSubjectLookup);

    Subject subject = null;
    
    try {
      subject = wsSubjectLookup.retrieveSubject();
    } catch (SubjectNotFoundException snfe) {
      //if we should do the old way
      if (GrouperWsConfig.retrieveConfig().propertyValueBoolean("ws.hasMember.subjectNotFound.returnsError", false)) {
        throw snfe;
      }
      //this is ok, let the result show
    }

    // make sure the subject is there
    if (subject == null) {
      // see why not
      SubjectFindResult subjectFindResult = wsSubjectLookup.retrieveSubjectFindResult();
      String error = "Subject: " + wsSubjectLookup + " had problems: "
          + subjectFindResult;
      this.getResultMetadata().setResultMessage(error);
      if (subjectFindResult != null) {
        this.assignResultCode(subjectFindResult.convertToHasMemberResultCode());
      }
    } else {
      this.wsSubject = new WsSubject(subject, subjectAttributeNamesToRetrieve, wsSubjectLookup);
    }
  }

  /**
   * result code of a request
   */
  public static enum WsHasMemberResultCode {

    /** found multiple results */
    SUBJECT_DUPLICATE {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.SUBJECT_DUPLICATE;
      }

    },

    /** cant find the subject */
    SUBJECT_NOT_FOUND {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.SUBJECT_NOT_FOUND;
      }

    },

    /** the subject is a member  (success = T) */
    IS_MEMBER {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.IS_MEMBER;
      }

    },

    /** the subject was found and is not a member (success = T) */
    IS_NOT_MEMBER {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.IS_NOT_MEMBER;
      }

    },

    /** problem with query */
    EXCEPTION {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.EXCEPTION;
      }

    },

    /** invalid query (e.g. if everything blank) */
    INVALID_QUERY {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.INVALID_QUERY;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this.equals(IS_MEMBER) || this.equals(IS_NOT_MEMBER) 
      || (!GrouperWsConfig.retrieveConfig().propertyValueBoolean("ws.hasMember.subjectNotFound.returnsError", false) 
          && this.equals(SUBJECT_NOT_FOUND));
    }
    
    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsHasMemberLiteResultCode convertToLiteCode();

  }

  /**
   * assign the code from the enum
   * @param hasMemberResultCode
   */
  public void assignResultCode(WsHasMemberResultCode hasMemberResultCode) {
    this.getResultMetadata().assignResultCode(
        hasMemberResultCode == null ? null : hasMemberResultCode.name());
    this.getResultMetadata().assignSuccess(hasMemberResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsSubjectLookup
   */
  public void assignResultCodeException(Exception e, WsSubjectLookup wsSubjectLookup) {
    this.assignResultCode(WsHasMemberResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsSubjectLookup + ", " + e, e);
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsHasMemberResultCode resultCode() {
    return WsHasMemberResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
