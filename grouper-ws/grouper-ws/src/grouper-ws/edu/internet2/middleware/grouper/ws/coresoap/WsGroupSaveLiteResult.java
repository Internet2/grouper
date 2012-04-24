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
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;

/**
 * <pre>
 * results for the groups save call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsGroupSaveLiteResult implements WsResponseBean {

  /**
   * result code of a request
   */
  public static enum WsGroupSaveLiteResultCode implements WsResultCode {

    /** didnt find the groups, inserted them (lite http status code 201) (success: T) */
    SUCCESS_INSERTED(201) {
      
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

    /** found the groups, saved them (lite http status code 201) (success: T) */
    SUCCESS_UPDATED(201) {
      
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

    /** found the groups, saved them (lite http status code 201) (success: T) */
    SUCCESS_NO_CHANGES_NEEDED(201) {
      
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

    /** either overall exception, or one or more groups had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem, group already exists (lite http status code 500) (success: F) */
    GROUP_ALREADY_EXISTS(500),
    
    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400), 
    
    /** the group was not found  (lite http status code 404) (success: F) */
    GROUP_NOT_FOUND(404), 
    
    /** user not allowed (lite http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403), 
    
    /** the stem was not found  (lite http status code 404) (success: F) */
    STEM_NOT_FOUND(404);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGroupSaveLiteResultCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

  }

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGroupSaveLiteResult.class);

  /**
   * prcess an exception, log, etc
   * @param wsGroupSaveResultsCodeOverride
   * @param theError
   * @param e
   * @param clientVersion
   */
  public void assignResultCodeException(
      WsGroupSaveLiteResultCode wsGroupSaveResultsCodeOverride, String theError, Exception e, GrouperVersion clientVersion) {

    if (e instanceof WsInvalidQueryException) {
      wsGroupSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupSaveResultsCodeOverride, WsGroupSaveLiteResultCode.INVALID_QUERY);
      //      if (e.getCause() instanceof GroupNotFoundException) {
      //        wsGroupSaveResultsCodeOverride = WsGroupSaveResultsCode.GROUP_NOT_FOUND;
      //      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGroupSaveResultsCodeOverride, clientVersion);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGroupSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupSaveResultsCodeOverride, WsGroupSaveLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGroupSaveResultsCodeOverride, clientVersion);

    }
  }

  /**
   * assign the code from the enum
   * @param groupSaveResultsCode
   * @param clientVersion 
   */
  public void assignResultCode(WsGroupSaveLiteResultCode groupSaveResultsCode, GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(groupSaveResultsCode, clientVersion);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsGroupSaveLiteResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGroupSaveLiteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * group saved 
   */
  private WsGroup wsGroup;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
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
   * empty
   */
  public WsGroupSaveLiteResult() {
    //empty
  }

  /**
   * construct from results of other
   * @param wsGroupSaveResults
   */
  public WsGroupSaveLiteResult(WsGroupSaveResults wsGroupSaveResults) {
  
    this.getResultMetadata().copyFields(wsGroupSaveResults.getResultMetadata());
  
    WsGroupSaveResult wsGroupSaveResult = GrouperServiceUtils
        .firstInArrayOfOne(wsGroupSaveResults.getResults());
    if (wsGroupSaveResult != null) {
      this.getResultMetadata().copyFields(wsGroupSaveResult.getResultMetadata());

      try {
        this.getResultMetadata().assignResultCode(
            wsGroupSaveResult.resultCode().convertToLiteCode());
      } catch (RuntimeException re) {
        GrouperVersion clientVersion = GrouperWsVersionUtils.retrieveCurrentClientVersion();
        //before 1.4 we had SUCCESS and nothing more descriptive, which isnt a real enum anymore
        if (clientVersion != null && clientVersion.lessThanArg(GrouperVersion.valueOfIgnoreCase("v1_4_000"))) {
          this.getResultMetadata().setResultCode(wsGroupSaveResult.getResultMetadata().getResultCode());
        } else {
          throw re;
        }
        
      }

      this.setWsGroup(wsGroupSaveResult.getWsGroup());
    }
  }

}
