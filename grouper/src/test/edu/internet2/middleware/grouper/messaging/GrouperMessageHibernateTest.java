/**
 * Copyright 2015 Internet2
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
package edu.internet2.middleware.grouper.messaging;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MessageDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 *
 */
public class GrouperMessageHibernateTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperMessageHibernateTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMessageHibernateTest("testInsertDelete"));
  }

  /**
   * 
   */
  public void testInsertDelete() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    GrouperMessageHibernate grouperMessageHibernate = new GrouperMessageHibernate();
    grouperMessageHibernate.setMessageBody("test");
    grouperMessageHibernate.setId( GrouperUuid.getUuid() );
    Member fromMember = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    grouperMessageHibernate.setFromMemberId(fromMember.getId());
    grouperMessageHibernate.setGetAttemptCount(0);
    grouperMessageHibernate.setGetAttemptTimeMillis(0L);
    grouperMessageHibernate.setQueueName("abc");
    grouperMessageHibernate.setSentTimeMicros(0L);
    grouperMessageHibernate.setState("state");
    
    GrouperDAOFactory.getFactory().getMessage().saveOrUpdate(grouperMessageHibernate);
    
    grouperMessageHibernate.setState("state2");
    GrouperDAOFactory.getFactory().getMessage().saveOrUpdate(grouperMessageHibernate);
    
    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findById(grouperMessageHibernate.getId(), true);
    assertEquals("state2", grouperMessageHibernate.getState());
  
    GrouperDAOFactory.getFactory().getMessage().delete(grouperMessageHibernate);
    
    
    
  }

}
