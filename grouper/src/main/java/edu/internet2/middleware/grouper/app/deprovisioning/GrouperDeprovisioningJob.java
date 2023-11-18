/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.deprovisioning;

public class GrouperDeprovisioningJob {
  


  /**
   * group that users who are allowed to deprovision other users are in
   * @param affiliation deprovi
   * @return the group name
   */
  public static String retrieveDeprovisioningManagersMustBeInGroupName(String affiliation) {
    
    //  # e.g. managersWhoCanDeprovision_<affiliationName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<affiliationName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":managersWhoCanDeprovision_" + affiliation;

  }

  /**
   * group name which has been deprovisioned
   * @param affiliation
   * @return the group name
   */
  public static String retrieveGroupNameWhichHasBeenDeprovisioned(String affiliation) {
    
    //  # e.g. managersWhoCanDeprovision_<affiliationName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<affiliationName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":usersWhoHaveBeenDeprovisioned_" + affiliation;
  }


}
