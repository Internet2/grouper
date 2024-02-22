/**
 * Copyright 2024 Internet2
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

package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;


/**
 *
 */
public class GrouperRulesDaemon extends OtherJobBase {


  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {

    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("rules.enable", true)) {
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Skipping since rules.enable is set to false");
      return null;
    }
    
    RuleEngine.daemon(otherJobInput.getHib3GrouperLoaderLog());
        
    return null;
  }
}
