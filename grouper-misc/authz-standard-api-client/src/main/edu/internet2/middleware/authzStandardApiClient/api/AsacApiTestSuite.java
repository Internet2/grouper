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
/*
 * @author mchyzer
 * $Id: AsacApiDefaultResource.java,v 1.7 2009-12-13 06:33:06 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient.api;

import edu.internet2.middleware.authzStandardApiClient.testSuite.AsacTestSuiteResults;
import edu.internet2.middleware.authzStandardApiClient.testSuite.AsacTestSuiteVerbose;



/**
 * class to run non destructive test suite against the server
 */
public class AsacApiTestSuite extends AsacApiRequestBase {
  
  /**
   * if the indent flag should be sent to the server
   */
  private boolean indent = false;
  
  /**
   * if indent flag should be sent
   * @param indent1
   * @return this for chaining
   */
  public AsacApiTestSuite assignIndent(boolean indent1) {
    this.indent = indent1;
    return this;
  }
  
  /**
   * if the indent flag should be sent to the server
   * @return the indent
   */
  public boolean isIndent() {
    return this.indent;
  }

  
  /** verbose level, low, medium, high */
  private AsacTestSuiteVerbose verbose = AsacTestSuiteVerbose.medium;

  /**
   * assign verbose level, low, medium, high
   * @param theVerbose
   * @return this for chaining
   */
  public AsacApiTestSuite assignVerbose(AsacTestSuiteVerbose asacTestSuiteVerbose) {
    this.verbose = asacTestSuiteVerbose;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public AsacTestSuiteResults execute() {
    this.validate();
    
    AsacTestSuiteResults asacTestSuiteResults = new AsacTestSuiteResults();
    
    asacTestSuiteResults.setVerbose(this.verbose);
    asacTestSuiteResults.setIndent(this.indent);
    
    asacTestSuiteResults.runAllTestSuites();
    
    return asacTestSuiteResults;
    
    
  }
 
  
}
