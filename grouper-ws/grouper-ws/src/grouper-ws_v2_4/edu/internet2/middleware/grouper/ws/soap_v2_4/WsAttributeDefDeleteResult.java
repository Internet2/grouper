/*******************************************************************************
 * Copyright 2016 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_4;

import edu.internet2.middleware.grouper.ws.soap_v2_4.WsAttributeDefDeleteLiteResult.WsAttributeDefDeleteLiteResultCode;

/**
 * Result of one attribute def being deleted.  The number of
 * these result objects will equal the number of attribute defs sent in to the method
 * to be deleted
 * 
 * @author vsachdeva
 */
public class WsAttributeDefDeleteResult {

  /**
   * empty constructor
   */
  public WsAttributeDefDeleteResult() {
    //nothing to do
  }

  /**
   * attributeDef to be deleted
   */
  private WsAttributeDef wsAttributeDef;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public static enum WsAttributeDefDeleteResultCode {

    /** successful deletion (lite status code 200) */
    SUCCESS {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefDeleteLiteResultCode.SUCCESS;
      }

    },

    /** invalid query, can only happen if Lite query */
    INVALID_QUERY {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefDeleteLiteResultCode.INVALID_QUERY;
      }

    },

    /** the attribute def was not found */
    SUCCESS_ATTRIBUTE_DEF_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefDeleteLiteResultCode.SUCCESS_ATTRIBUTE_DEF_NOT_FOUND;
      }

    },

    /** problem with deleting */
    EXCEPTION {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefDeleteLiteResultCode.EXCEPTION;
      }

    },

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefDeleteLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    },

    /** transaction rolled back */
    TRANSACTION_ROLLED_BACK {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefDeleteLiteResultCode
       */
      @Override
      public WsAttributeDefDeleteLiteResultCode convertToLiteCode() {
        return WsAttributeDefDeleteLiteResultCode.EXCEPTION;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS || this == SUCCESS_ATTRIBUTE_DEF_NOT_FOUND;
    }

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsAttributeDefDeleteLiteResultCode convertToLiteCode();
  }

  /**
   * @return the wsAttributeDef
   */
  public WsAttributeDef getWsAttributeDef() {
    return this.wsAttributeDef;
  }

  /**
   * @param wsAttributeDef1 to set
   */
  public void setWsAttributeDef(WsAttributeDef wsAttributeDef1) {
    this.wsAttributeDef = wsAttributeDef1;
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
