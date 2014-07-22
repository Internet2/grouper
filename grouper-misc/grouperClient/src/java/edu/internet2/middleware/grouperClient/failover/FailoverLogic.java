/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouperClient.failover;


/**
 * Logic to run.  If there is a problem or timeout, try a different connection
 * @author mchyzer
 * @param <V> return type of logic
 *
 */
public interface FailoverLogic<V> {

  /**
   * Logic to run.  If there is a problem or timeout, try a different connection
   * Note, if there are threadlocal things to set, make sure to set them in the logic
   * @param failoverLogicBean if running in new thread, and connection name
   * @return whatever it returns
   */
  public abstract V logic(FailoverLogicBean failoverLogicBean);

}
