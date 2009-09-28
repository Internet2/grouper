/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;

public class StemDataConnector extends BaseGrouperDataConnector {

  private static final Logger LOG = GrouperUtil.getLogger(StemDataConnector.class);

  public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
    String msg = "'" + principalName + "' dc '" + this.getId() + "'";
    LOG.debug("resolve {}", msg);
    if (LOG.isDebugEnabled()) {
      for (String attrId : resolutionContext.getAttributeRequestContext().getRequestedAttributesIds()) {
        LOG.debug("resolve {} requested attribute '{}'", msg, attrId);
      }
    }

    Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();

    // find stem
    Stem stem = StemFinder.findByName(getGrouperSession(), principalName, false);
    if (stem == null) {
      LOG.debug("resolve {} stem not found", msg);
      return attributes;
    }
    LOG.debug("resolve {} found stem '{}'", msg, stem);

    // TODO match filter ?

    // internal attributes
    BasicAttribute<String> extension = new BasicAttribute<String>("extension");
    extension.setValues(Arrays.asList(new String[] { stem.getExtension() }));
    attributes.put(extension.getId(), extension);

    BasicAttribute<String> description = new BasicAttribute<String>("description");
    description.setValues(Arrays.asList(new String[] { stem.getDescription() }));
    attributes.put(description.getId(), description);

    BasicAttribute<String> displayExtension = new BasicAttribute<String>("displayExtension");
    displayExtension.setValues(Arrays.asList(new String[] { stem.getDisplayExtension() }));
    attributes.put(displayExtension.getId(), displayExtension);

    BasicAttribute<String> displayName = new BasicAttribute<String>("displayName");
    displayName.setValues(Arrays.asList(new String[] { stem.getDisplayName() }));
    attributes.put(displayName.getId(), displayName);

    BasicAttribute<String> name = new BasicAttribute<String>("name");
    name.setValues(Arrays.asList(new String[] { stem.getName() }));
    attributes.put(name.getId(), name);

    // parent stem
    BasicAttribute<String> parentStemName = new BasicAttribute<String>(PARENT_STEM_NAME_ATTR);
    parentStemName.setValues(Arrays.asList(new String[] { stem.getParentStem().getName() }));
    attributes.put(parentStemName.getId(), parentStemName);

    if (LOG.isDebugEnabled()) {
      LOG.debug("resolve {} attributes {}", msg, attributes.size());
      for (String key : attributes.keySet()) {
        for (Object value : attributes.get(key).getValues()) {
          LOG.debug("resolve {} '{}' : {}", new Object[] { msg, key, PSPUtil.getString(value) });
        }
      }
    }

    return attributes;
  }

  public void validate() throws AttributeResolutionException {

  }
}
