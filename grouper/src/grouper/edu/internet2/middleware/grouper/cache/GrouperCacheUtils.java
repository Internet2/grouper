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
/*
 * @author mchyzer
 * $Id: GrouperCacheUtils.java,v 1.2 2009-08-11 20:18:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.cache;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames;
import edu.internet2.middleware.grouper.hooks.examples.AttributeAutoCreateHook;
import edu.internet2.middleware.grouper.subj.cache.SubjectSourceCache;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;
import edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;


/**
 *
 */
public class GrouperCacheUtils {

  /**
   * 
   */
  public static void clearAllCaches() {

    AttributeAutoCreateHook.clearCache();
    ConfigPropertiesCascadeBase.clearCache();
    
    ConfigDatabaseLogic.clearCache();

    ExpirableCache.clearAll();
    
    SubjectSourceCache.clearCache();

    SubjectFinder.internalClearSubjectCustomizerCache();

    GrouperUiApiTextConfig.clearCache();
    
    // whats the difference between these two?
    EhcacheController.ehcacheController().flushCache();
    
    GrouperObjectTypesAttributeNames.clearCache();

  }
  
}
