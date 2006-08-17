/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.grouper.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSetting4.java,v 1.1 2006-08-17 16:28:18 blair Exp $
 * @since   1.1.0
 */
public class TestSetting4 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestSetting4.class);


  public TestSetting4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {  
    LOG.debug("tearDown");
  }

  public void testHibernateProps() {
    LOG.info("testHibernateProps");

    String k = "hibernate.show_sql";
    String v = "false";
    Assert.assertTrue(k, GrouperConfig.getHibernateProperty(k).equals(v));
    k = "hibernate.cache.provider_class";
    v = "net.sf.hibernate.cache.EhCacheProvider";
    Assert.assertTrue(k, GrouperConfig.getHibernateProperty(k).equals(v));
    k = "hibernate.cache.use_query_cache";
    v = "true";
    Assert.assertTrue(k, GrouperConfig.getHibernateProperty(k).equals(v));
  } // public void testHibernateProps()

} // public class TestSetting4

