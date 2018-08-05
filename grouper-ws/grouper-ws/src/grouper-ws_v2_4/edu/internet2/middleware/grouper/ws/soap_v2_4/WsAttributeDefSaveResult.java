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

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.soap_v2_4.WsAttributeDefSaveLiteResult.WsAttributeDefSaveLiteResultCode;

/**
 * Result of one AttributeDef being saved.  The number of
 * these result objects will equal the number of AttributeDefs sent in to the method
 * to be saved
 * 
 * @author vsachdeva
 */
public class WsAttributeDefSaveResult {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefSaveResultCode {

    /** successful addition (success: T) */
    SUCCESS_INSERTED {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.SUCCESS_INSERTED;
      }
    },

    /** successful addition (success: T) */
    SUCCESS_UPDATED {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.SUCCESS_UPDATED;
      }
    },

    /** successful addition (success: T) */
    SUCCESS_NO_CHANGES_NEEDED {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.SUCCESS_NO_CHANGES_NEEDED;
      }

    },

    /** invalid query, can only happen if Lite query (success: F) */
    INVALID_QUERY {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.INVALID_QUERY;
      }

    },

    /** the attribute def was not found (success: F) */
    ATTRIBUTE_DEF_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.ATTRIBUTE_DEF_NOT_FOUND;
      }

    },

    /** the stem was not found (success: F) */
    STEM_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.STEM_NOT_FOUND;
      }

    },

    /** problem with saving (success: F) */
    EXCEPTION {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.EXCEPTION;
      }

    },

    /** user not allowed (success: F) */
    INSUFFICIENT_PRIVILEGES {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    },

    /** was a success but rolled back */
    TRANSACTION_ROLLED_BACK {

      /** 
       * if there is one result, convert to the results code
       * @return WsAttributeDefSaveLiteResultCode
       */
      @Override
      public WsAttributeDefSaveLiteResultCode convertToLiteCode() {
        return WsAttributeDefSaveLiteResultCode.EXCEPTION;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    public String nameForVersion(GrouperVersion clientVersion) {

      return this.name();
    }

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsAttributeDefSaveLiteResultCode convertToLiteCode();
  }

  /** attribute def saved */
  private WsAttributeDef wsAttributeDef;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @return the wsAttributeDef
   */
  public WsAttributeDef getWsAttributeDef() {
    return this.wsAttributeDef;
  }

  /**
   * @param wsAttributeDef1 the wsAttributeDef to set
   */
  public void setWsAttributeDef(WsAttributeDef wsAttributeDef1) {
    this.wsAttributeDef = wsAttributeDef1;
  }

  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * empty
   */
  public WsAttributeDefSaveResult() {
    //empty
  }
}
