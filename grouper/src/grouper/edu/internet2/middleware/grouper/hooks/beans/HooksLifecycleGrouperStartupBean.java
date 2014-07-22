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
 * $Id: HooksLifecycleGrouperStartupBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean to hold objects for grouper startup hooks
 */
@GrouperIgnoreDbVersion
public class HooksLifecycleGrouperStartupBean extends HooksBean {
  
  /**
   */
  public HooksLifecycleGrouperStartupBean() {
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksLifecycleGrouperStartupBean clone() {
    return GrouperUtil.clone(this, null);
  }
}
