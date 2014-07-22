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
 * @author mchyzer
 * $Id: HookAsynchronousHandler.java,v 1.1 2008-07-08 20:47:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import edu.internet2.middleware.grouper.hooks.beans.HooksBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * handler when doing an asynchronous hook, normally you would do this in
 * an anonymous inner class
 */
public interface HookAsynchronousHandler {

  /**
   * implement this as the callback to the asynchronous hook
   * @param hooksContext will be massaged to be threadsafe
   * @param hooksBean
   */
  public void callback(HooksContext hooksContext, HooksBean hooksBean);
  
}
