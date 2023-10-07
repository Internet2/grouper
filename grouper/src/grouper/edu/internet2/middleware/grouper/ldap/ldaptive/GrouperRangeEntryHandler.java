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
package edu.internet2.middleware.grouper.ldap.ldaptive;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapURL;
import org.ldaptive.ad.handler.RangeEntryHandler;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class GrouperRangeEntryHandler extends RangeEntryHandler {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperRangeEntryHandler.class);
  
  @Override
  public void handleEntry(final LdapEntry entry) {
    if (entry != null) {

      try {
        
        String dn = handleDn(entry);
        entry.setDn(dn);

        LdapURL ldapURL = getConnection().getLdapURL();
        String baseDn = ldapURL != null ? ldapURL.getBaseDn() : null;
        
        if (!StringUtils.isBlank(baseDn) && !ldapURL.isDefaultBaseDn() && dn.toLowerCase().endsWith(baseDn.toLowerCase())) {
          String dnWithoutSuffix = dn.substring(0, dn.length() - baseDn.length());
          dnWithoutSuffix = dnWithoutSuffix.trim().replaceAll(",$", "");
          entry.setDn(dnWithoutSuffix);
        }
        
        handleAttributes(entry);
        entry.setDn(dn);
      } catch (Exception e) {
        // shouldn't we abort if there's an error here or we'll return partial results??
        LOG.error("Error running search handler for entry=" + entry.getDn(), e);
      }
    }
  }
}
