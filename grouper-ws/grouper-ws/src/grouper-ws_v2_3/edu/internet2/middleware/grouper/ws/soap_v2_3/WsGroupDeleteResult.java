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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.ws.soap_v2_3.WsGroupDeleteLiteResult.WsGroupDeleteLiteResultCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Result of one group being deleted.  The number of
 * these result objects will equal the number of groups sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsGroupDeleteResult {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGroupDeleteResult.class);

  /**
   * empty constructor
   */
  public WsGroupDeleteResult() {
    //nothing to do
  }

  /**
   * @param wsGroupLookup is the group lookup to assign
   */
  public WsGroupDeleteResult(WsGroupLookup wsGroupLookup) {
    this.wsGroup = new WsGroup(null, wsGroupLookup, false); 
  }

  /**
   * create a result based on group
   * @param group
   * @param wsGroupLookup
   * @param includeDetail
   */
  public void assignGroup(Group group, WsGroupLookup wsGroupLookup, boolean includeDetail) {
    this.setWsGroup(new WsGroup(group, wsGroupLookup, includeDetail));
  }

  /**
   * group to be deleted
   */
  private WsGroup wsGroup;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public static enum WsGroupDeleteResultCode {

    /** in group lookup, the uuid doesnt match name */
    GROUP_UUID_DOESNT_MATCH_NAME {

      /** 
       * if there is one result, convert to the results code
       * @return WsGroupDeleteLiteResultCode
       */
      @Override
      public WsGroupDeleteLiteResultCode convertToLiteCode() {
        return WsGroupDeleteLiteResultCode.GROUP_UUID_DOESNT_MATCH_NAME;
      }

    },

    /** successful addition (lite status code 200) */
    SUCCESS {

      /** 
       * if there is one result, convert to the results code
       * @return WsGroupDeleteLiteResultCode
       */
      @Override
      public WsGroupDeleteLiteResultCode convertToLiteCode() {
        return WsGroupDeleteLiteResultCode.SUCCESS;
      }

    },

    /** invalid query, can only happen if Lite query */
    INVALID_QUERY {

      /** 
       * if there is one result, convert to the results code
       * @return WsGroupDeleteLiteResultCode
       */
      @Override
      public WsGroupDeleteLiteResultCode convertToLiteCode() {
        return WsGroupDeleteLiteResultCode.INVALID_QUERY;
      }

    },

    /** the group was not found */
    SUCCESS_GROUP_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsGroupDeleteLiteResultCode
       */
      @Override
      public WsGroupDeleteLiteResultCode convertToLiteCode() {
        return WsGroupDeleteLiteResultCode.SUCCESS_GROUP_NOT_FOUND;
      }

    },

    /** problem with deleting */
    EXCEPTION {

      /** 
       * if there is one result, convert to the results code
       * @return WsGroupDeleteLiteResultCode
       */
      @Override
      public WsGroupDeleteLiteResultCode convertToLiteCode() {
        return WsGroupDeleteLiteResultCode.EXCEPTION;
      }

    },

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES {

      /** 
       * if there is one result, convert to the results code
       * @return WsGroupDeleteLiteResultCode
       */
      @Override
      public WsGroupDeleteLiteResultCode convertToLiteCode() {
        return WsGroupDeleteLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    },

    /** transaction rolled back */
    TRANSACTION_ROLLED_BACK {

      /** 
       * if there is one result, convert to the results code
       * @return WsGroupDeleteLiteResultCode
       */
      @Override
      public WsGroupDeleteLiteResultCode convertToLiteCode() {
        return WsGroupDeleteLiteResultCode.EXCEPTION;
      }

    },

    /** if parent stem cant be found */
    PARENT_STEM_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsGroupDeleteLiteResultCode
       */
      @Override
      public WsGroupDeleteLiteResultCode convertToLiteCode() {
        return WsGroupDeleteLiteResultCode.PARENT_STEM_NOT_FOUND;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS || this == SUCCESS_GROUP_NOT_FOUND;
    }

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsGroupDeleteLiteResultCode convertToLiteCode();
  }

  /**
   * assign the code from the enum
   * @param groupDeleteResultCode
   */
  public void assignResultCode(WsGroupDeleteResultCode groupDeleteResultCode) {
    this.getResultMetadata().assignResultCode(
        groupDeleteResultCode == null ? null : groupDeleteResultCode.name());
    this.getResultMetadata().assignSuccess(
        GrouperServiceUtils.booleanToStringOneChar(groupDeleteResultCode.isSuccess()));
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroupResult1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroupResult1) {
    this.wsGroup = wsGroupResult1;
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsGroupLookup
   */
  public void assignResultCodeException(Exception e, WsGroupLookup wsGroupLookup) {
    this.assignResultCode(WsGroupDeleteResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsGroupLookup + ", " + e, e);
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
  public WsGroupDeleteResultCode resultCode() {
    return WsGroupDeleteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

}
