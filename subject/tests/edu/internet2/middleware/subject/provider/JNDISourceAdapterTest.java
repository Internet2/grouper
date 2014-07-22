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

import java.util.Set;

import junit.framework.TestCase;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Unit tests for JNDISourceAdapter.
 */
public class JNDISourceAdapterTest extends TestCase {

  /**
   * Source adapter
   */
  JNDISourceAdapter source;

  /**
   * Constructor
   * @param name 
   */
  public JNDISourceAdapterTest(String name) {
    super(name);
  }

  /**
   * Setup the fixture.
   */
  @Override
  protected void setUp() {

    this.source = new JNDISourceAdapter("jndi", "JNDI Subject Source");
    this.source.addInitParam("INITIAL_CONTEXT_FACTORY",
        "com.sun.jndi.ldap.LdapCtxFactory");
    this.source.addInitParam("PROVIDER_URL", "ldap://localhost:389");
    this.source.addInitParam("SECURITY_AUTHENTICATION", "simple");
    this.source.addInitParam("SECURITY_PRINCIPAL", "cn=Manager,dc=example,dc=edu");
    this.source.addInitParam("SECURITY_CREDENTIALS", "secret");
    this.source.addInitParam("SubjectID_AttributeType", "kitnEduPersonRegId");
    this.source.addInitParam("Name_AttributeType", "cn");
    this.source.addInitParam("Description_AttributeType", "description");

    Search search = null;

    search = new Search();
    search.setSearchType("searchSubject");
    search.addParam("filter",
        "(& (kitnEduPersonRegId=%TERM%) (objectclass=kitnEduPerson))");
    search.addParam("scope", "SUBTREE_SCOPE");
    search.addParam("base", "ou=kitn,dc=example,dc=edu");
    this.source.loadSearch(search);

    search = new Search();
    search.setSearchType("searchSubjectByIdentifier");
    search.addParam("filter", "(& (uid=%TERM%) (objectclass=kitnEduPerson))");
    search.addParam("scope", "SUBTREE_SCOPE");
    search.addParam("base", "ou=kitn,dc=example,dc=edu");
    this.source.loadSearch(search);

    search = new Search();
    search.setSearchType("search");
    search
        .addParam("filter",
            "(& (|(uid=%TERM%)(cn=*%TERM%*)(kitnEduPersonRegId=%TERM%))(objectclass=kitnEduPerson))");
    search.addParam("scope", "SUBTREE_SCOPE");
    search.addParam("base", "ou=kitn,dc=example,dc=edu");
    this.source.loadSearch(search);

    this.source.addAttribute("sn");
    this.source.addAttribute("department");

    try {
      this.source.init();
    } catch (SourceUnavailableException e) {
      fail("JDBCSourceAdapter not available: " + e);
    }
  }

  /**
   * The main method for running the test.
   * @param args 
   */
  public static void main(String args[]) {
    junit.textui.TestRunner.run(JNDISourceAdapterTest.class);
  }

  /**
   * A test of Subject ID search capability.
   */
  public void testIdSearch() {
    Subject subject = null;
    try {
      subject = this.source.getSubject("SD00001", true);
      assertEquals("Searching id = SD00001", "SD00001", subject.getId());
    } catch (SubjectNotFoundException e) {
      fail("Searching id = SD00001: not found");
    } catch (SubjectNotUniqueException e) {
      fail("Searching id = SD00001: not unique");
    }

    try {
      subject = this.source.getSubject("chris", true);
      fail("Searching id = chris: null expected but found result");
    } catch (SubjectNotFoundException e) {
      assertTrue("Searching id = chris: null expected and found null", true);
    } catch (SubjectNotUniqueException e) {
      fail("Searching id = barry: chris expected but found not unique");
    }
  }

  /**
   * A test of Subject identifier search capability.
   */
  public void testIdentifierSearch() {
    Subject subject = null;
    try {
      subject = this.source.getSubjectByIdentifier("comalley", true);
      assertEquals("Searching dentifier = SD00001", "SD00001", subject.getId());
    } catch (SubjectNotFoundException e) {
      fail("Searching identifier = comalley: result expected but found null");
    } catch (SubjectNotUniqueException e) {
      fail("Searching identifier = comalley: expected unique result but found not unique");
    }

    try {
      subject = this.source.getSubjectByIdentifier("chris", true);
      fail("Searching identifier = chris: null expected but found result");
    } catch (SubjectNotFoundException e) {
      assertTrue("Searching identifier = chris: null expected and null found", true);
    } catch (SubjectNotUniqueException e) {
      fail("Searching identifier = chris: null expected but found not unique");
    }
    assertNull(this.source.getSubjectByIdentifier("chris", false));
  }

  /**
   * A test of Subject search capability.
   */
  public void testGenericSearch() {
    Set<Subject> set = null;
    Subject subject = null;

    // In the test subject database, IDs are not included in generic search.
    set = this.source.search("SD00001");
    assertEquals("Searching value = SD00001, result size", 1, set.size());

    set = this.source.search("comalley");
    assertEquals("Searching value = comalley, result size", 1, set.size());
    subject = set.toArray(new Subject[0])[0];
    assertEquals("Searching value = comalley", "SD00001", subject.getId());

    set = this.source.search("br");
    assertEquals("Searching value = br, result size", 13, set.size());

    set = this.source.search("beck");
    assertEquals("Searching value = beck, result size", 1, set.size());
    subject = set.toArray(new Subject[0])[0];
    assertEquals("Searching value = beck", "SD00020", subject.getId());
  }

  /**
   * Make sure we can't inject LDAP syntax into filter.
   */
  public void testLDAPInjection() {
    Set set = null;

    // There is a uid of comalley with a description of President,
    // but we shouldn't find him because the parentheses should be escaped.
    set = this.source.search("comalley)(description=President");
    assertEquals("Searching injection", 0, set.size());
  }
}
