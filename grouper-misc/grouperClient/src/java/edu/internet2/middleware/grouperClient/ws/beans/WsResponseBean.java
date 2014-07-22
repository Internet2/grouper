/**
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
 */
/*
 * @author mchyzer $Id: WsResponseBean.java,v 1.1 2008-11-27 14:25:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * bean (used in Rest) which is a response from web service
 */
public interface WsResponseBean {

  /** get the result metadata 
   * @return the result metadata 
   */
  public WsResultMeta getResultMetadata();

  /** 
   * get the response metadata 
   * @return the response metadata 
   */
  public WsResponseMeta getResponseMetadata();
}
