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
 * $Id: WsSampleRest.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples.types;


/**
 * interface for generated sample
 */
public interface WsSampleRest extends WsSample {
  
  /** 
   * execute the sample
   * @param wsSampleRestType xhtml vs xml vs json
   */
  public void executeSample(WsSampleRestType wsSampleRestType);
  
  /** 
   * see if this is a valid type (e.g. http params only available
   * for Lite requests)
   * @param wsSampleRestType xhtml vs xml vs json
   * @return if valid
   */
  public boolean validType(WsSampleRestType wsSampleRestType);
  
}
