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
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

/**
 * extend this to make an ESB processor
 */
public abstract class EsbListenerBase {

  /**
   * 
   * @param eventJsonString
   * @param consumerName
   * @return true if ok, false if not
   */
  public abstract boolean dispatchEvent(String eventJsonString, String consumerName);

  /**
   * disconnect if needed
   */
  public abstract void disconnect();
}
