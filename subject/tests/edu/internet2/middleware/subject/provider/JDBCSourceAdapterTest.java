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
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.subject.provider;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectTooManyResults;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * Unit tests for JDBCSourceAdapter.
 */
public class JDBCSourceAdapterTest extends TestCase {

  /**
   * Source adapter
   */
  JDBCSourceAdapter source;

  /**
   * Constructor
   */
  public JDBCSourceAdapterTest() {
    
  }
  /**
   * Constructor
   * @param name 
   */
  public JDBCSourceAdapterTest(String name) {
    super(name);
  }

  /**
   * Setup the fixture.
   */
  @Override
  protected void setUp() {
    this.setUp(null);
  }

  /**
   * Setup the fixture.
   * @param jdbcConnectionProviderString 
   */
  protected void setUp(String jdbcConnectionProviderString) {
    this.source = new JDBCSourceAdapter("jdbc", "JDBC Subject Source");
    //note, this might be null
    if (!StringUtils.isBlank(jdbcConnectionProviderString)) {
      this.source.addInitParam("jdbcConnectionProvider", jdbcConnectionProviderString);
    }
    this.source.addInitParam("maxActive", "4");
    this.source.addInitParam("maxIdle", "4");
    this.source.addInitParam("maxWait", "30");
    this.source.addInitParam("dbDriver", "org.hsqldb.jdbcDriver");
    this.source.addInitParam("dbUrl", "jdbc:hsqldb:file:testDB/hsqldb/subject");
    this.source.addInitParam("dbUser", "sa");
    this.source.addInitParam("dbPwd", "");
    this.source.addInitParam("SubjectID_AttributeType", "id");
    this.source.addInitParam("Name_AttributeType", "name");
    this.source.addInitParam("Description_AttributeType", "description");
    SourceManager.getInstance().sourceMap.put("jdbc", this.source);

    Search search = null;

    search = new Search();
    search.setSearchType("searchSubject");
    search
        .addParam(
            "sql",
            "select subject.subjectid as id, subject.name as name, lfnamet.lfname as "
                + "lfname, loginidt.loginid as loginid, desct.description as description from subject "
                + "left join (select subjectid, value as lfname from subjectattribute where name = 'name') "
                + "as lfnamet on subject.subjectid = lfnamet.subjectid left join (select subjectid, value "
                + "as loginid from subjectattribute where name = 'loginid') as loginidt on subject.subjectid "
                + "= loginidt.subjectid left join (select subjectid, value as description from subjectattribute "
                + "where name = 'description') as desct on subject.subjectid = desct.subjectid where subject.subjectid = ?");
    this.source.loadSearch(search);

    search = new Search();
    search.setSearchType("searchSubjectByIdentifier");
    search.addParam("numParameters", "1");
    search
        .addParam(
            "sql",
            "select subject.subjectid as id, subject.name as name, lfnamet.lfname as "
                + "lfname, loginidt.loginid as loginid, desct.description as description from subject "
                + "left join (select subjectid, value as lfname from subjectattribute where name = 'name') "
                + "as lfnamet on subject.subjectid = lfnamet.subjectid left join (select subjectid, value "
                + "as loginid from subjectattribute where name = 'loginid') as loginidt on subject.subjectid "
                + "= loginidt.subjectid left join (select subjectid, value as description from "
                + "subjectattribute where name = 'description') as desct on subject.subjectid = "
                + "desct.subjectid where loginidt.loginid = ?");
    this.source.loadSearch(search);

    search = new Search();
    search.setSearchType("search");
    search
        .addParam(
            "sql",
            "select subject.subjectid as id, subject.name as name, lfnamet.lfname as "
                + "lfname, loginidt.loginid as loginid, desct.description as description from subject "
                + "left join (select subjectid, value as lfname from subjectattribute where name = 'name') "
                + "as lfnamet on subject.subjectid = lfnamet.subjectid left join (select subjectid, value "
                + "as loginid from subjectattribute where name = 'loginid') as loginidt on subject.subjectid "
                + "= loginidt.subjectid left join (select subjectid, value as description from "
                + "subjectattribute where name = 'description') as desct on subject.subjectid = "
                + "desct.subjectid where (lower(name) like '%'||?||'%') or (lower(lfnamet.lfname) "
                + "like '%'||?||'%') or (lower(loginidt.loginid) like '%'||?||'%') or "
                + "(lower(desct.description) like '%'||?||'%')");
    this.source.loadSearch(search);
    this.source.addSubjectType(SubjectTypeEnum.PERSON.getName());
    try {
      this.source.init();
    } catch (SourceUnavailableException e) {
      fail("JDBCSourceAdapter not available: " + ExceptionUtils.getFullStackTrace(e));
    }
  }

  /**
   * The main method for running the test.
   * @param args 
   */
  public static void main(String args[]) {
    //TestRunner.run(JDBCSourceAdapterTest.class);
    TestRunner.run(new JDBCSourceAdapterTest("testPageQuery"));
  }

  /**
   * 
   */
  public void testPageQuery() {
    
    assertEquals("select penn_id, pennname, name, description, description_lower, first_name, last_name, affiliation_id, person_active, email, email_public from (select penn_id, pennname, name, description, description_lower, first_name, last_name, affiliation_id, person_active, email, email_public from person_source ps where PS.DESCRIPTION_LOWER like '%hyz%' order by description) where rownum <= 5",
        JdbcDatabaseType.oracle.pageQuery("select penn_id, pennname, name, description, description_lower, first_name, last_name, affiliation_id, person_active, email, email_public from person_source ps where PS.DESCRIPTION_LOWER like '%hyz%' order by description", 5));

    assertEquals("select penn_id, pennname, name, description, description_lower, first_name, last_name, affiliation_id, person_active, email, email_public from person_source ps where PS.DESCRIPTION_LOWER like '%hyz%' order by description limit 0,5",
        JdbcDatabaseType.mysql.pageQuery("select penn_id, pennname, name, description, description_lower, first_name, last_name, affiliation_id, person_active, email, email_public from person_source ps where PS.DESCRIPTION_LOWER like '%hyz%' order by description", 5));
    
    assertEquals("select penn_id, pennname, name, description, description_lower, first_name, last_name, affiliation_id, person_active, email, email_public from person_source ps where PS.DESCRIPTION_LOWER like '%hyz%' order by description limit 5",
        JdbcDatabaseType.postgres.pageQuery("select penn_id, pennname, name, description, description_lower, first_name, last_name, affiliation_id, person_active, email, email_public from person_source ps where PS.DESCRIPTION_LOWER like '%hyz%' order by description", 5));
    
  }
  
  /**
   * 
   */
  public void testColumnAliases() {
    
    List<String> aliases = JdbcDatabaseType.columnAliases("select a, c as b, this as that");
    
    assertEquals(3, aliases.size());
    assertEquals("a", aliases.get(0));
    assertEquals("b", aliases.get(1));
    assertEquals("that", aliases.get(2));
    
    assertEquals("select * ", JdbcDatabaseType.selectPart("select * from whatever"));
    assertNull(JdbcDatabaseType.selectPart("select *, this, that"));
    aliases = JdbcDatabaseType.columnAliases("select\n" +
        "   s.subjectid as id, s.name as name,\n" +
        "   (select sa2.value from subjectattribute sa2 where name='name' and sa2.SUBJECTID = s.subjectid) as lfname,\n" +
        "   (select sa3.value from subjectattribute sa3 where name='loginid' and sa3.SUBJECTID = s.subjectid) as loginid,\n" +
        "   (select sa4.value from subjectattribute sa4 where name='description' and sa4.SUBJECTID = s.subjectid) as description,\n" +
        "   (select sa5.value from subjectattribute sa5 where name='email' and sa5.SUBJECTID = s.subjectid) as email\n");
    
    assertEquals(6, aliases.size());
    assertEquals("id", aliases.get(0));
    assertEquals("name", aliases.get(1));
    assertEquals("lfname", aliases.get(2));
    assertEquals("loginid", aliases.get(3));
    assertEquals("description", aliases.get(4));
    assertEquals("email", aliases.get(5));
    
    
  }
  
  /**
   * 
   */
  public void testSqlFromPart() {
    assertEquals("select * ", JdbcDatabaseType.selectPart("select * from whatever"));
    assertNull(JdbcDatabaseType.selectPart("select * fram whatever"));
    assertEquals("select\n" +
        "   s.subjectid as id, s.name as name,\n" +
        "   (select sa2.value from subjectattribute sa2 where name='name' and sa2.SUBJECTID = s.subjectid) as lfname,\n" +
        "   (select sa3.value from subjectattribute sa3 where name='loginid' and sa3.SUBJECTID = s.subjectid) as loginid,\n" +
        "   (select sa4.value from subjectattribute sa4 where name='description' and sa4.SUBJECTID = s.subjectid) as description,\n" +
        "   (select sa5.value from subjectattribute sa5 where name='email' and sa5.SUBJECTID = s.subjectid) as email\n", JdbcDatabaseType.selectPart("select\n" +
"   s.subjectid as id, s.name as name,\n" +
"   (select sa2.value from subjectattribute sa2 where name='name' and sa2.SUBJECTID = s.subjectid) as lfname,\n" +
"   (select sa3.value from subjectattribute sa3 where name='loginid' and sa3.SUBJECTID = s.subjectid) as loginid,\n" +
"   (select sa4.value from subjectattribute sa4 where name='description' and sa4.SUBJECTID = s.subjectid) as description,\n" +
"   (select sa5.value from subjectattribute sa5 where name='email' and sa5.SUBJECTID = s.subjectid) as email\n" +
"from \n" +
"   subject s\n" +
"where\n" +
"   s.subjectid in (\n" +
"      select subjectid from subject where lower(name) like concat('%',concat(?,'%')) union\n" +
"      select subjectid from subjectattribute where searchvalue like concat('%',concat(?,'%'))\n" +
"   )\n"));
  }
  
  /**
   * A test of Subject ID search capability.
   */
  public void testIdSearch() {
    Subject subject = null;
    try {
      subject = this.source.getSubject("1012", true);
      assertEquals("Searching id = 1012", "1012", subject.getId());
    } catch (SubjectNotFoundException e) {
      fail("Searching id = 1012: not found");
    } catch (SubjectNotUniqueException e) {
      fail("Searching id = 1012: not unique");
    }

    try {
      subject = this.source.getSubject("barry", true);
      fail("Searching id = barry: null expected but found result");
    } catch (SubjectNotFoundException e) {
      assertTrue("Searching id = barry: null expected and found null", true);
    } catch (SubjectNotUniqueException e) {
      fail("Searching id = barry: null expected but found not unique");
    }

    assertNull(this.source.getSubject("barry", false));

  }

  /**
   * A test of Subject identifier search capability.
   */
  public void testIdentifierSearch() {
    Subject subject = null;
    try {
      subject = this.source.getSubjectByIdentifier("babl", true);
      assertEquals("Searching dentifier = 1012", "1012", subject.getId());
    } catch (SubjectNotFoundException e) {
      fail("Searching identifier = babl: result expected but found null");
    } catch (SubjectNotUniqueException e) {
      fail("Searching identifier = babl: expected unique result but found not unique");
    }

    try {
      subject = this.source.getSubjectByIdentifier("barry", true);
      fail("Searching identifier = barry: null expected but found result");
    } catch (SubjectNotFoundException e) {
      assertTrue("Searching identifier = barry: null expected and null found", true);
    } catch (SubjectNotUniqueException e) {
      fail("Searching identifier = barry: null expected but found not unique");
    }
  }

  /**
   * A test of Subject identifier search capability.
   */
  public void testVirtualAttribute() {
    //LOGINID=[babl], LFNAME=[Barry Blair]
    this.source.addInitParam("subjectVirtualAttribute_2_loginIdLfName", "Hey ${subject.getAttributeValue('LOGINID')} and ${subject.getAttributeValue('LFNAME')}");
    this.source.addInitParam("subjectVirtualAttribute_4_loginIdLfNameLoginId", "${subject.getAttributeValue('loginIdLfName')} Hey ${subject.getAttributeValue('LOGINID')} and ${subject.getAttributeValue('LFNAME')}");
    Subject subject = null;
    subject = this.source.getSubjectByIdentifier("babl", true);
    assertEquals("babl", subject.getAttributeValue("LOGINID"));
    assertEquals("Hey babl and Barry Blair", subject.getAttributeValue("loginIdLfName"));
    assertEquals("Hey babl and Barry Blair Hey babl and Barry Blair", subject.getAttributeValue("loginIdLfNameLoginId"));
    
  }

  /**
   * A test of Subject identifier search capability.
   */
  public void testVirtualAttribute2() {
    //LOGINID=[babl], LFNAME=[Barry Blair]
    this.source.addInitParam("subjectVirtualAttributeVariable_JDBCSourceAdapterTest", "edu.internet2.middleware.subject.provider.JDBCSourceAdapterTest");
    this.source.addInitParam("subjectVirtualAttribute_0_loginIdSquared", "${JDBCSourceAdapterTest.appendToSelf(subject.getAttributeValue('LOGINID'))}");
    
    Subject subject = null;
    subject = this.source.getSubjectByIdentifier("babl", true);
    assertEquals("babl", subject.getAttributeValue("LOGINID"));
    assertEquals("bablbabl", subject.getAttributeValue("loginIdSquared"));
    
  }

  /**
   * 
   * @param string
   * @return the string appended to itself
   */
  public static String appendToSelf(String string) {
    return string + string;
  }
  
  /**
   * test search on all pooling strategies
   * @throws Throwable 
   */
  public void testGenericSearchThread() throws Throwable {
    String[] jdbcConnectionProviders = new String[] {
        // DbcpJdbcConnectionProvider.class.getName(),
        C3p0JdbcConnectionProvider.class.getName() };
    int numberOfThreads = 50;
    Thread[] threads = new Thread[numberOfThreads];
    final Throwable[] throwables = new Throwable[1];
    for (final String jdbcConnectionProvider : jdbcConnectionProviders) {

      try {
        this.setUp(jdbcConnectionProvider);
        //test pooling
        for (int i = 0; i < numberOfThreads; i++) {
          threads[i] = new Thread(new Runnable() {

            public void run() {
              try {
                JDBCSourceAdapterTest.this.genericSearchHelper(jdbcConnectionProvider);
              } catch (Throwable t) {
                throwables[0] = t;
              }
            }

          });
          threads[i].start();

        }
        //wait for all the threads
        for (int i = 0; i < numberOfThreads; i++) {
          threads[i].join();
        }
        //see if any had exceptions
        if (throwables[0] != null) {
          throw throwables[0];
        }
      } catch (Throwable re) {
        SubjectUtils.injectInException(re, "Problem with jdbcConnectionProvider: "
            + jdbcConnectionProvider);
        throw re;
      }
    }
  }

  /**
   * test search on all pooling strategies
   * @throws Throwable 
   */
  public void testGenericSearch() throws Throwable {
    String[] jdbcConnectionProviders = new String[] {
        // DbcpJdbcConnectionProvider.class.getName(),
        C3p0JdbcConnectionProvider.class.getName() };
    for (String jdbcConnectionProvider : jdbcConnectionProviders) {

      try {
        this.setUp(jdbcConnectionProvider);
        JDBCSourceAdapterTest.this.genericSearchHelper(jdbcConnectionProvider);
      } catch (Throwable re) {
        SubjectUtils.injectInException(re, "Problem with jdbcConnectionProvider: "
            + jdbcConnectionProvider);
        throw re;
      }
    }

  }

  /**
   * test too many results
   */
  public void testTooManyResults() {
    
    Set<Subject> set = null;
    Subject subject = null;
    
    this.source.addInitParam("maxResults", "3");
    
    try {
      this.source.init();
    } catch (SourceUnavailableException e) {
      fail("JDBCSourceAdapter not available: " + ExceptionUtils.getFullStackTrace(e));
    }

    try {
      set = this.source.search("%e%");
      fail("Should not get here");
    } catch (SubjectTooManyResults e) {
      //good
    }
    
    
    
    set = this.source.search("babl");
    assertEquals("Searching value = babl, result size", 1, set
        .size());
    subject = set.toArray(new Subject[0])[0];
    assertEquals("Searching value = babl", "1012", subject
        .getId());
  }
  
  /**
   * A test of Subject search capability.
   * @param jdbcConnectionProvider 
   */
  public void genericSearchHelper(String jdbcConnectionProvider) {
    Set<Subject> set = null;
    Subject subject = null;

    // In the test subject database, IDs are not included in generic search.
    set = this.source.search("1012");
    assertEquals("Searching value = 1012, result size: " + jdbcConnectionProvider, 0, set
        .size());

    set = this.source.search("babl");
    assertEquals("Searching value = babl, result size: " + jdbcConnectionProvider, 1, set
        .size());
    subject = set.toArray(new Subject[0])[0];
    assertEquals("Searching value = babl: " + jdbcConnectionProvider, "1012", subject
        .getId());

    set = this.source.search("barry");
    assertEquals("Searching value = barry, result size: " + jdbcConnectionProvider, 12,
        set.size());

    set = this.source.search("beth%porter");
    assertEquals("Searching value = beth%porter, result size: " + jdbcConnectionProvider,
        1, set.size());
    subject = set.toArray(new Subject[0])[0];
    assertEquals("Searching value = beth%porter, id: " + jdbcConnectionProvider, "1119",
        subject.getId());
  }

  /**
   * Make sure we can't inject SQL into statement.
   */
  public void testSQLInjection() {

    try {
      this.source.getSubject("1012' and loginid = 'babl", true);
      fail("Searching id = 1012: null expected but found result");
    } catch (SubjectNotFoundException e) {
      assertTrue("Searching id = 1012: not found", true);
    } catch (SubjectNotUniqueException e) {
      fail("Searching id = 1012: null expected, but found not unique");
    }
  }
}
