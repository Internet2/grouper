/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.ldap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ldaptive.ResultCode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * Result codes an LDAP search ignores when it runs for the loader.
 *
 * By default (ldap.&lt;id&gt;.searchIgnoreResultCodes not set) an LDAP search treats
 * TIME_LIMIT_EXCEEDED, SIZE_LIMIT_EXCEEDED and PARTIAL_RESULTS as success and returns the entries
 * it got so far.  That is fine for lookups, but the loader syncs a group to the search result: a
 * search the directory cut off (time limit) or that skipped referrals (PARTIAL_RESULTS, typical for
 * Active Directory) would remove every member that was not returned.  So loader searches
 * (GrouperLoaderResultset) run inside {@link #callForLoader(Callable)}, and while they do, the
 * default ignore set drops TIME_LIMIT_EXCEEDED and PARTIAL_RESULTS: the search throws and the
 * job fails instead of syncing a partial population.  SIZE_LIMIT_EXCEEDED stays ignored, as in
 * Grouper 4, since it is the old default and paged searches do not hit it.
 *
 * A site that wants the loader to accept those codes sets ldap.&lt;id&gt;.searchIgnoreResultCodes
 * explicitly; an explicit value is used as is.
 * </pre>
 */
public class LdapLoaderSearchResultCodes {

  /** result codes a loader search does not ignore unless the site configures them explicitly */
  private static final Set<ResultCode> NOT_IGNORED_FOR_LOADER_BY_DEFAULT = Collections.unmodifiableSet(
      new HashSet<ResultCode>(GrouperUtil.toSet(ResultCode.TIME_LIMIT_EXCEEDED, ResultCode.PARTIAL_RESULTS)));

  /** true while the current thread runs a loader search */
  private static final ThreadLocal<Boolean> loaderSearch = new ThreadLocal<Boolean>();

  /**
   * run a loader search: LDAP searches in the callable fail on a time limit or partial results
   * unless the site configured searchIgnoreResultCodes explicitly
   * @param <T> return type
   * @param callable the search
   * @return the callable's result
   */
  public static <T> T callForLoader(Callable<T> callable) {
    // keep the outer value so nested calls restore it
    Boolean previous = loaderSearch.get();
    loaderSearch.set(Boolean.TRUE);
    try {
      return callable.call();
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (previous == null) {
        loaderSearch.remove();
      } else {
        loaderSearch.set(previous);
      }
    }
  }

  /**
   * @return true if the current thread runs a loader search
   */
  public static boolean isLoaderSearch() {
    return Boolean.TRUE.equals(loaderSearch.get());
  }

  /**
   * result codes the search on this thread should ignore
   * @param ldapServerId the ldap connection id
   * @param configuredIgnoreResultCodes LdapConfiguration.getSearchIgnoreResultCodes() (configured or default)
   * @return the configured set outside the loader, or when the site set searchIgnoreResultCodes
   * explicitly; for a loader search on the default, the set without TIME_LIMIT_EXCEEDED and PARTIAL_RESULTS
   */
  public static Set<ResultCode> ignoreResultCodes(String ldapServerId, Set<ResultCode> configuredIgnoreResultCodes) {
    Set<ResultCode> result = new HashSet<ResultCode>();
    if (configuredIgnoreResultCodes != null) {
      result.addAll(configuredIgnoreResultCodes);
    }
    if (!isLoaderSearch()) {
      return result;
    }
    // an explicit setting is the site's choice, even for the loader
    String explicitValue = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".searchIgnoreResultCodes");
    if (!GrouperUtil.isBlank(explicitValue)) {
      return result;
    }
    result.removeAll(NOT_IGNORED_FOR_LOADER_BY_DEFAULT);
    return result;
  }
}
