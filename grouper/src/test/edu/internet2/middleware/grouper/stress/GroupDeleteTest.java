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
package edu.internet2.middleware.grouper.stress;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * @author shilen
 */
public class GroupDeleteTest extends GrouperTest {
  
  /** grouper session */
  private GrouperSession grouperSession;

  /** root stem */
  private Stem root;

  /** top stem */
  private Stem top;
  
  /**
   * 
   */
  public GroupDeleteTest() {
    super();
  }

  /**
   * @param name
   */
  public GroupDeleteTest(String name) {
    super(name);
  }

  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }
  
  /**
   * 
   */
  public void testDeleteGroupWithManyMembers() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "false");

    Group group = top.addChildGroup("group", "group");
    
    // add subjects to group
    for (int i = 1; i <= 10000; i++) {
      RegistrySubject subj = new RegistrySubject();
      subj.setId("person" + i);
      subj.setName("person" + i);
      subj.setTypeString("person");
      GrouperDAOFactory.getFactory().getRegistrySubject().create(subj);

      group.addMember(SubjectFinder.findById(subj.getId(), true));
    }
    
    // now delete the group...
    group.delete();
  }
}
