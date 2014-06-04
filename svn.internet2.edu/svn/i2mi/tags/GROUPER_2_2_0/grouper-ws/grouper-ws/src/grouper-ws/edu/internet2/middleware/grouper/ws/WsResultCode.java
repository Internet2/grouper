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
 * @author mchyzer $Id: WsResultCode.java,v 1.2 2008-12-04 07:51:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import edu.internet2.middleware.grouper.misc.GrouperVersion;

/**
 * result code enum
 */
public interface WsResultCode {

  /** is this code a success 
   * @return true or false for success or not
   */
  public boolean isSuccess();

  /** get the http status code associated with this status code, e.g. 200 
   * @return the status code e.g. 200 
   */
  public int getHttpStatusCode();

  /** 
   * name of the result code enum
   * @return the name
   */
  public String name();
  
  /** get the name label for a certain version of client 
   * @param clientVersion 
   * @return */
  public String nameForVersion(GrouperVersion clientVersion);
  
}
