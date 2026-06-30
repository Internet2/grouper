/**
 * Copyright 2019 Internet2
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

package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.misc.GrouperVersion;

/**
 * @author shilen
 */
public interface UpgradeTasksInterface {

  /**
   * update to next version
   */
  public void updateVersionFromPrevious(OtherJobInput otherJobInput);

  public default boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return false;
  }

  public default boolean runOnNewInstall() {
    return false;
  }

  public GrouperVersion versionIntroduced();

  public default boolean upgradeTaskIsDdl() {
    return false;
  }

  /**
   * Brief human-readable description of what this upgrade task does, for display in the admin UI
   * (Configure -&gt; Upgrade tasks).  The text itself is externalized, not hard coded here: the default
   * returns the text registered under the convention key "upgradeTaskDescription_" + the implementing
   * class simple name (see {@link #descriptionByConvention(UpgradeTasksInterface)}).  Defining this on
   * the interface means every upgrade task automatically surfaces a description slot - if the matching
   * text key has not been filled in, the UI shows the key itself so the gap is obvious rather than the
   * task silently shipping with no description.  An anonymous/inline task (no class simple name) should
   * override this to point at an explicit key.
   * @return the externalized description for this task
   */
  public default String description() {
    return UpgradeTasksInterface.descriptionByConvention(this);
  }

  /**
   * Resolve an upgrade task's description from externalized text by convention.  The key is
   * "upgradeTaskDescription_" + the implementing class simple name, e.g. for UpgradeTaskV43 the key is
   * "upgradeTaskDescription_UpgradeTaskV43", which is defined in grouper.textNg.en.us.base.properties.
   * @param upgradeTask the task whose description to resolve
   * @return the externalized description text (or the key itself if no text is registered for it)
   */
  public static String descriptionByConvention(UpgradeTasksInterface upgradeTask) {
    String key = "upgradeTaskDescription_" + upgradeTask.getClass().getSimpleName();
    return GrouperTextContainer.retrieveFromRequest().getText().get(key);
  }
}
