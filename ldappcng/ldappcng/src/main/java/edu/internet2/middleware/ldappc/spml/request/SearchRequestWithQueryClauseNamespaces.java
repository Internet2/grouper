/*******************************************************************************
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
 ******************************************************************************/
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

package edu.internet2.middleware.ldappc.spml.request;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openspml.v2.msg.PrefixAndNamespaceTuple;
import org.openspml.v2.msg.spml.QueryClause;
import org.openspml.v2.msg.spmlref.HasReference;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.SearchRequest;

/**
 * This class extends {@link SearchRequest} to include all {@link QueryClause} namespaces. Otherwise, if a
 * {@link HasReference} {@link QueryClause} is present, the xmlns:spmlref='urn:oasis:names:tc:SPML:2:0:reference'
 * namespace will be omitted after marshaling.
 * 
 * @see <a href="http://java.net/jira/browse/OPENSPML-24">OPENSPML-24</a>
 */
public class SearchRequestWithQueryClauseNamespaces extends SearchRequest {

  /**
   * {@inheritDoc}
   * 
   * The element name is set to "SearchRequest".
   */
  @Override
  public String getElementName() {
    return "SearchRequest";
  }

  /**
   * {@inheritDoc}
   * 
   * Much like the {@link Query} class, namespace info is gathered from {@link QueryClauses}.
   */
  @Override
  public PrefixAndNamespaceTuple[] getNamespacesInfo() {

    // the set of PrefixAndNamespaceTuple objects to be returned
    Set<PrefixAndNamespaceTuple> set = new LinkedHashSet<PrefixAndNamespaceTuple>();

    // add PrefixAndNamespaceTuples from super class, SearchRequst
    set.addAll(Arrays.asList(super.getNamespacesInfo()));

    // for every QueryClause, add PrefixAndNamespaceTuples to the set to be returned
    if (this.getQuery() != null && this.getQuery().getQueryClauses() != null) {
      for (QueryClause queryClause : this.getQuery().getQueryClauses()) {
        set.addAll(Arrays.asList(queryClause.getNamespacesInfo()));
      }
    }

    return set.toArray(new PrefixAndNamespaceTuple[set.size()]);
  }
}
