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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap_v2_3.WsAttributeDefSaveLiteResult.WsAttributeDefSaveLiteResultCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

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
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefSaveResult.class);

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
   * assign the code from the enum
   * @param attributeDefSaveResultCode
   * @param clientVersion 
   */
  public void assignResultCode(WsAttributeDefSaveResultCode attributeDefSaveResultCode,
      GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(
        attributeDefSaveResultCode == null ? null : attributeDefSaveResultCode
            .nameForVersion(clientVersion));
    this.getResultMetadata()
        .assignSuccess(
            GrouperServiceUtils.booleanToStringOneChar(attributeDefSaveResultCode
                .isSuccess()));
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsAttributeDefToSave
   * @param clientVersion
   */
  public void assignResultCodeException(Exception e,
      WsAttributeDefToSave wsAttributeDefToSave, GrouperVersion clientVersion) {

    //get root exception (might be wrapped in wsInvalidQuery)
    Throwable mainThrowable = (e instanceof WsInvalidQueryException
        && e.getCause() != null) ? e.getCause() : e;

    if (mainThrowable instanceof InsufficientPrivilegeException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefSaveResultCode.INSUFFICIENT_PRIVILEGES,
          clientVersion);

    } else if (mainThrowable instanceof AttributeDefNotFoundException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefSaveResultCode.ATTRIBUTE_DEF_NOT_FOUND,
          clientVersion);
    } else if (mainThrowable instanceof StemNotFoundException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefSaveResultCode.STEM_NOT_FOUND, clientVersion);
    } else if (e instanceof WsInvalidQueryException) {
      this.getResultMetadata().setResultMessage(mainThrowable.getMessage());
      this.assignResultCode(WsAttributeDefSaveResultCode.INVALID_QUERY, clientVersion);
    } else {
      this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(WsAttributeDefSaveResultCode.EXCEPTION, clientVersion);
    }
    LOG.error(wsAttributeDefToSave + ", " + e, e);
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsAttributeDefSaveResultCode resultCode() {
    return WsAttributeDefSaveResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * empty
   */
  public WsAttributeDefSaveResult() {
    //empty
  }

  /**
   * construct initially with lookup
   * @param attributeDef
   * @param wsAttributeDefLookup 
   */
  public WsAttributeDefSaveResult(AttributeDef attributeDef,
      WsAttributeDefLookup wsAttributeDefLookup) {
    this.wsAttributeDef = new WsAttributeDef(attributeDef, wsAttributeDefLookup);
  }
}
