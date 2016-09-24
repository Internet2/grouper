/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
package edu.internet2.middleware.tierInstrumentationCollector.corebeans;


/**
 * base class that beans extends
 * @author mchyzer
 */
public abstract class TicResponseBeanBase {

  /**
   * construct
   */
  public TicResponseBeanBase() {
    this.getMeta().setTierHttpStatusCode(200);
    //this.getMeta().setLastModified(StandardApiServerUtils.convertToIso8601(new Date(TaasRestServlet.getStartupTime())));
  }

  /**
   * override this for subject objects to scimify
   * make this a scim response
   */
  public void scimify() {
    this.getMeta().scimify();
  }
  
  /**
   * 
   */
  private String[] schemas = null;

  
  /**
   * @return the schemas
   */
  public String[] getSchemas() {
    return this.schemas;
  }

  
  /**
   * @param theSchemas the schemas to set
   */
  public void setSchemas(String[] theSchemas) {
    this.schemas = theSchemas;
  }

  /**
   * meta about resource
   */
  private TicMeta meta = new TicMeta();
  
  /**
   * meta about resource
   * @return the meta
   */
  public TicMeta getMeta() {
    return this.meta;
  }
  
  /**
   * meta about resource
   * @param _meta the meta to set
   */
  public void setMeta(TicMeta _meta) {
    this.meta = _meta;
  }
  
}
