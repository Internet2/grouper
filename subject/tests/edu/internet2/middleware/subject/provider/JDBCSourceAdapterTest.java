/*
 Copyright 2006-2007 The University Of Chicago
 Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
 Copyright 2006-2007 EDUCAUSE
 
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

package edu.internet2.middleware.subject.provider;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * Unit tests for JDBCSourceAdapter.
 */
public class JDBCSourceAdapterTest
        extends TestCase {
    /**
     * Source adapter
     */
    JDBCSourceAdapter source;

    /**
     * Constructor
     */
    public JDBCSourceAdapterTest(String name) {
        super(name);
    }

    /**
     * Setup the fixture.
     */
    protected void setUp() {
      this.setUp(null);
    }

      
    /**
     * Setup the fixture.
     * @param jdbcConnectionProviderString 
     */
    protected void setUp(String jdbcConnectionProviderString) {
        source = new JDBCSourceAdapter("jdbc", "JDBC Subject Source");
        //note, this might be null
        if (!StringUtils.isBlank(jdbcConnectionProviderString)) {
          source.addInitParam("jdbcConnectionProvider", jdbcConnectionProviderString);
        }
        source.addInitParam("maxActive", "4");
        source.addInitParam("maxIdle", "4");
        source.addInitParam("maxWait", "30");
        source.addInitParam("dbDriver", "org.hsqldb.jdbcDriver");
        source.addInitParam("dbUrl", "jdbc:hsqldb:file:testDB/hsqldb/subject");
        source.addInitParam("dbUser", "sa");
        source.addInitParam("dbPwd", "");
        source.addInitParam("SubjectID_AttributeType", "id");
        source.addInitParam("Name_AttributeType", "name");
        source.addInitParam("Description_AttributeType", "description");

        Search search = null;

        search = new Search();
        search.setSearchType("searchSubject");
        search.addParam("numParameters", "1");
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
        source.loadSearch(search);

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
        source.loadSearch(search);

        search = new Search();
        search.setSearchType("search");
        search.addParam("numParameters", "4");
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
        source.loadSearch(search);

        try {
            source.init();
        } catch (SourceUnavailableException e) {
            fail("JDBCSourceAdapter not available: " + ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) {
      TestRunner.run(JDBCSourceAdapterTest.class);
      //TestRunner.run(new JDBCSourceAdapterTest("testGenericSearchThread"));
    }

    /**
     * A test of Subject ID search capability.
     */
    public void testIdSearch() {
        Subject subject = null;
        try {
            subject = source.getSubject("1012");
            assertEquals("Searching id = 1012", "1012", subject.getId());
        } catch (SubjectNotFoundException e) {
            fail("Searching id = 1012: not found");
        } catch (SubjectNotUniqueException e) {
            fail("Searching id = 1012: not unique");
        }

        try {
            subject = source.getSubject("barry");
            fail("Searching id = barry: null expected but found result");
        } catch (SubjectNotFoundException e) {
            assertTrue("Searching id = barry: null expected and found null",
                    true);
        } catch (SubjectNotUniqueException e) {
            fail("Searching id = barry: null expected but found not unique");
        }
    }

    /**
     * A test of Subject identifier search capability.
     */
    public void testIdentifierSearch() {
        Subject subject = null;
        try {
            subject = source.getSubjectByIdentifier("babl");
            assertEquals("Searching dentifier = 1012", "1012", subject.getId());
        } catch (SubjectNotFoundException e) {
            fail("Searching identifier = babl: result expected but found null");
        } catch (SubjectNotUniqueException e) {
            fail("Searching identifier = babl: expected unique result but found not unique");
        }

        try {
            subject = source.getSubjectByIdentifier("barry");
            fail("Searching identifier = barry: null expected but found result");
        } catch (SubjectNotFoundException e) {
            assertTrue(
                    "Searching identifier = barry: null expected and null found",
                    true);
        } catch (SubjectNotUniqueException e) {
            fail("Searching identifier = barry: null expected but found not unique");
        }
    }

    /**
     * test search on all pooling strategies
     * @throws Throwable 
     */
    public void testGenericSearchThread() throws Throwable {
      String[] jdbcConnectionProviders = new String[]{
          DbcpJdbcConnectionProvider.class.getName(),
          C3p0JdbcConnectionProvider.class.getName()
      };
      int numberOfThreads = 50;
      Thread[] threads = new Thread[numberOfThreads];
      final Throwable[] throwables = new Throwable[1];
      for (final String jdbcConnectionProvider : jdbcConnectionProviders) {
        
        try {
          this.setUp(jdbcConnectionProvider);
          //test pooling
          for (int i=0;i<numberOfThreads;i++) {
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
          for (int i=0;i<numberOfThreads;i++) { 
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
      String[] jdbcConnectionProviders = new String[]{
          DbcpJdbcConnectionProvider.class.getName(),
          C3p0JdbcConnectionProvider.class.getName()
      };      
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
     * A test of Subject search capability.
     * @param jdbcConnectionProvider 
     */
    public void genericSearchHelper(String jdbcConnectionProvider) {
        Set set = null;
        Subject subject = null;

        // In the test subject database, IDs are not included in generic search.
        set = source.search("1012");
        assertEquals("Searching value = 1012, result size: " + jdbcConnectionProvider, 0, set.size());

        set = source.search("babl");
        assertEquals("Searching value = babl, result size: " + jdbcConnectionProvider, 1, set.size());
        subject = ((Subject[]) set.toArray(new Subject[0]))[0];
        assertEquals("Searching value = babl: " + jdbcConnectionProvider, "1012", subject.getId());

        set = source.search("barry");
        assertEquals("Searching value = barry, result size: " + jdbcConnectionProvider, 12, set.size());

        set = source.search("beth%porter");
        assertEquals("Searching value = beth%porter, result size: " + jdbcConnectionProvider, 1, set
                .size());
        subject = ((Subject[]) set.toArray(new Subject[0]))[0];
        assertEquals("Searching value = beth%porter, id: " + jdbcConnectionProvider, "1119", subject
                .getId());
    }

    /**
     * Make sure we can't inject SQL into statement.
     */
    public void testSQLInjection() {
        Subject subject = null;
        try {
            subject = source.getSubject("1012' and loginid = 'babl");
            fail("Searching id = 1012: null expected but found result");
        } catch (SubjectNotFoundException e) {
            assertTrue("Searching id = 1012: not found", true);
        } catch (SubjectNotUniqueException e) {
            fail("Searching id = 1012: null expected, but found not unique");
        }
    }
}
