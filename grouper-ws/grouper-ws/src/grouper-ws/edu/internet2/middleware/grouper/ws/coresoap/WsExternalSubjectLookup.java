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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectDeleteResult.WsExternalSubjectDeleteResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperWsToStringCompact;

/**
 * <pre>
 * Class to lookup an attribute def via web service
 * 
 * developers make sure each setter calls this.clearAttributeDef();
 * </pre>
 * @author mchyzer
 */
public class WsExternalSubjectLookup implements GrouperWsToStringCompact {

  /**
   * see if blank
   * @return true if blank
   */
  public boolean blank() {
    return StringUtils.isBlank(this.identifier)
      && this.externalSubject == null && this.externalSubjectFindResult == null;
  }

  
  /**
   * see if this attributeDef lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.identifier);
  }
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsExternalSubjectLookup.class);

  /** find the external subject */
  @XStreamOmitField
  private ExternalSubject externalSubject = null;
  
  /** result of extneral subject find */
  public static enum ExternalSubjectFindResult {

    /** found the attributeDef */
    SUCCESS {

      @Override
      public WsExternalSubjectDeleteResultCode convertToExternalSubjectDeleteResultCode() {
        return WsExternalSubjectDeleteResultCode.SUCCESS;
      }
    },

    /** cant find the externalSubject */
    EXTERNAL_SUBJECT_NOT_FOUND {

      @Override
      public WsExternalSubjectDeleteResultCode convertToExternalSubjectDeleteResultCode() {
        return WsExternalSubjectDeleteResultCode.SUCCESS_EXTERNAL_SUBJECT_NOT_FOUND;
      }
    },

    /** invalid query (e.g. if everything blank) */
    INVALID_QUERY {

      @Override
      public WsExternalSubjectDeleteResultCode convertToExternalSubjectDeleteResultCode() {
        return WsExternalSubjectDeleteResultCode.INVALID_QUERY;
      }
    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /**
     * convert this code to a delete code
     * @return the code
     */
    public abstract WsExternalSubjectDeleteResultCode convertToExternalSubjectDeleteResultCode();

    /**
     * null safe equivalent to convertToDeleteCode
     * @param externalSubjectFindResult to convert
     * @return the code
     */
    public static WsExternalSubjectDeleteResultCode convertToExternalSubjectDeleteCodeStatic(
        ExternalSubjectFindResult externalSubjectFindResult) {
      return externalSubjectFindResult == null ? WsExternalSubjectDeleteResultCode.EXCEPTION
          : externalSubjectFindResult.convertToExternalSubjectDeleteResultCode();
    }
    


  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the attributeDef
   */
  public ExternalSubject retrieveExternalSubject() {
    return this.externalSubject;
  }
  
  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the attributeDefFindResult, this is never null
   */
  public ExternalSubjectFindResult retrieveExternalSubjectFindResult() {
    return this.externalSubjectFindResult;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * retrieve the external subject for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   */
  public void retrieveExternalSubjectIfNeeded(GrouperSession grouperSession) {
    this.retrieveExternalSubjectIfNeeded(grouperSession, null);
  }

  /**
   * retrieve the external subject for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @return the attributeDef
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public ExternalSubject retrieveExternalSubjectIfNeeded(GrouperSession grouperSession,
      String invalidQueryReason) throws WsInvalidQueryException {
    
    //see if we already retrieved
    if (this.externalSubjectFindResult != null) {
      return this.externalSubject;
    }
    try {
      //assume success (set otherwise if there is a problem)
      this.externalSubjectFindResult = ExternalSubjectFindResult.SUCCESS;

      boolean hasIdentifier = !StringUtils.isBlank(this.identifier);

      //must have a name or uuid
      if (!hasIdentifier) {
        this.externalSubjectFindResult = ExternalSubjectFindResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid externalSubject query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      ExternalSubject theExternalSubject = null;
      
      if (hasIdentifier) {
        
        theExternalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier(this.identifier, true, null);
            //new QueryOptions().secondLevelCache(false));

      }

      //success
      this.externalSubject = theExternalSubject;

    } catch (Exception e) {
      this.externalSubjectFindResult = ExternalSubjectFindResult.EXTERNAL_SUBJECT_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid externalSubject for '" + invalidQueryReason
            + "', " + this, e);
      }
    }
    return this.externalSubject;
  }
  
  /**
   * clear the attributeDef if a setter is called
   */
  private void clearExternalSubject() {
    this.externalSubject = null;
  }

  /** name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef */
  private String identifier;

  /** result of external subject find */
  @XStreamOmitField
  private ExternalSubjectFindResult externalSubjectFindResult = null;

  /**
   * name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef
   * @return the theName
   */
  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef
   * @param theName the theName to set
   */
  public void setIdentifier(String theName) {
    this.identifier = theName;
    this.clearExternalSubject();
  }

  /**
   * make sure this is an explicit toString
   * @return return a compact to string
   */
  public String toStringCompact() {
    if (!StringUtils.isBlank(this.identifier)) {
      return "name: " + this.identifier;
    }
    return "blank";
  }

  /**
   * 
   */
  public WsExternalSubjectLookup() {
    //blank
  }

  /**
   * @param identifier1 
   */
  public WsExternalSubjectLookup(String identifier1) {
    this.setIdentifier(identifier1);
  }

}
