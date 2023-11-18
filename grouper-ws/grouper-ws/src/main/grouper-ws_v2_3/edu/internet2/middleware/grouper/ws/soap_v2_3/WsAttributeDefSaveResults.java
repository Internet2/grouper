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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.WsResultCode;

/**
 * <pre>
 * results for the attribute defs save call.
 * 
 * result code:
 * code of the result for this attribute def overall
 * SUCCESS: means everything ok
 * EXCEPTION: cant find the attribute def
 * INVALID_QUERY: e.g. if everything blank
 * </pre>
 * @author vsachdeva
 */
public class WsAttributeDefSaveResults {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefSaveResultsCode implements WsResultCode {

    /** found the attribute defs, saved them (lite http status code 201) (success: T) */
    SUCCESS(201),

    /** either overall exception, or one or more attribute defs had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem saving existing attribute defs (lite http status code 500) (success: F) */
    PROBLEM_SAVING_ATTRIBUTE_DEFS(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name for version
     */
    @Override
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * @return true if success
     */
    @Override
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsAttributeDefSaveResultsCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    @Override
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

  }

  /**
   * results for each attribute defs sent in
   */
  private WsAttributeDefSaveResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each attribute def sent in
   * @return the results
   */
  public WsAttributeDefSaveResult[] getResults() {
    return this.results;
  }

  /**
   * results for each attribute def sent in
   * @param results1 the results to set
   */
  public void setResults(WsAttributeDefSaveResult[] results1) {
    this.results = results1;
  }

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

}
