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
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.filter;

import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.subject.Source;

/**
 * Selects {@link Member}s from a {@link Source}.
 */
public class MemberSourceFilter extends AbstractFilter<Member> {

  /** The ID of the {@link Source}. */
  private String sourceId;

  public MemberSourceFilter(String sourceId) {
    this.sourceId = sourceId;
    // test to see if source is available, will throw exceptions if not
    SubjectFinder.getSource(sourceId);
  }

  /** {@inheritDoc} */
  public Set<Member> getResults(GrouperSession s) throws QueryException {
    Set<Member> members = new TreeSet<Member>();
    members.addAll(MemberFinder.findAll(s, SubjectFinder.getSource(sourceId)));
    return members;
  }

  /** {@inheritDoc} */
  public boolean matches(Object member) {
    if (!(member instanceof Member)) {
      return false;
    }
    return ((Member) member).getSubjectSourceId().equals(sourceId);
  }

  /**
   * Return the source identifier.
   * 
   * @return the source id.
   */
  public String getSourceId() {
    return sourceId;
  }

}
