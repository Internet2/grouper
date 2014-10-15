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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.shibboleth.filter.Filter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;

/**
 * A {@link DataConnector} which returns {@link Stem} attributes.
 */
public class StemDataConnector extends BaseGrouperDataConnector<Stem> {

  /** Logger, */
  private static final Logger LOG = LoggerFactory.getLogger(StemDataConnector.class);

  /** The root stem. */
  private Stem rootStem;

  /** {@inheritDoc} */
  public Map<String, BaseAttribute> resolve(final ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    Map<String, BaseAttribute> attributes = (Map<String, BaseAttribute>) GrouperSession.callbackGrouperSession(
        getGrouperSession(), new GrouperSessionHandler() {

          public Map<String, BaseAttribute> callback(GrouperSession grouperSession) throws GrouperSessionException {

            String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

            LOG.debug("Stem data connector '{}' - Resolve principal '{}'", getId(), principalName);
            LOG.trace("Stem data connector '{}' - Resolve principal '{}' requested attributes {}", new Object[] {
                getId(), principalName, resolutionContext.getAttributeRequestContext().getRequestedAttributesIds() });

            if (principalName.startsWith(CHANGELOG_PRINCIPAL_NAME_PREFIX)) {
              LOG.debug("Stem data connector '{}' - Ignoring principal name '{}'", getId(), principalName);
              return Collections.EMPTY_MAP;
            }

            // find stem
            Stem stem = StemFinder.findByName(getGrouperSession(), principalName, false);
            if (stem == null) {
              LOG.debug("Stem data connector '{}' - Resolve principal '{}' unable to find stem.", getId(),
                  principalName);
              return Collections.EMPTY_MAP;
            }
            LOG.debug("Stem data connector '{}' - Resolve principal '{}' found stem '{}'", new Object[] { getId(),
                principalName, stem });

            // root stem ?
            if (stem.equals(getRootStem())) {
              LOG.debug("Stem data connector '{}' - Resolve principal '{}' returning emtpy map for root stem.",
                  getId(), principalName);
              return Collections.EMPTY_MAP;
            }

            // match filter
            Filter<Stem> matchQueryFilter = getFilter();
            if (matchQueryFilter != null && !matchQueryFilter.matches(stem)) {
              LOG.debug("Stem data connector '{}' - Resolve principal '{}' stem '{}' does not match filter.",
                  new Object[] { getId(), principalName, stem });
              return Collections.EMPTY_MAP;
            }

            // build attributes
            Map<String, BaseAttribute> attributes = buildAttributes(stem);

            LOG.debug("Stem data connector '{}' - Resolve principal '{}' attributes {}", new Object[] { getId(),
                principalName, attributes });

            if (LOG.isTraceEnabled()) {
              for (String key : attributes.keySet()) {
                for (Object value : attributes.get(key).getValues()) {
                  LOG.trace("Stem data connector '{}' - Resolve principal '{}' attribute {} : '{}'", new Object[] {
                      getId(), principalName, key, value });
                }
              }
            }

            return attributes;
          }
        });

    return attributes;
  }

  /**
   * Return attributes for the given {@link Stem}.
   * 
   * @param stem the stem
   * @return the attributes
   */
  protected Map<String, BaseAttribute> buildAttributes(Stem stem) {

    Map<String, BaseAttribute> attributes = new LinkedHashMap<String, BaseAttribute>();

    // extension
    BasicAttribute<String> extension = new BasicAttribute<String>(GrouperConfig.ATTRIBUTE_EXTENSION);
    extension.setValues(GrouperUtil.toList(stem.getExtension()));
    attributes.put(extension.getId(), extension);

    // display extension
    BasicAttribute<String> displayExtension = new BasicAttribute<String>(GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION);
    displayExtension.setValues(GrouperUtil.toList(stem.getDisplayExtension()));
    attributes.put(displayExtension.getId(), displayExtension);

    // name
    BasicAttribute<String> name = new BasicAttribute<String>(GrouperConfig.ATTRIBUTE_NAME);
    name.setValues(GrouperUtil.toList(stem.getName()));
    attributes.put(name.getId(), name);

    // displayName
    BasicAttribute<String> displayName = new BasicAttribute<String>(GrouperConfig.ATTRIBUTE_DISPLAY_NAME);
    displayName.setValues(GrouperUtil.toList(stem.getDisplayName()));
    attributes.put(displayName.getId(), displayName);

    // description
    String description = stem.getDescription();
    if (description != null && !description.equals(GrouperConfig.EMPTY_STRING)) {
      BasicAttribute<String> descriptionAttr = new BasicAttribute<String>(GrouperConfig.ATTRIBUTE_DESCRIPTION);
      descriptionAttr.setValues(GrouperUtil.toList(description));
      attributes.put(descriptionAttr.getId(), descriptionAttr);
    }
        
    // IntegerID 
    attributes.put("IdIndex", new BasicAttribute(stem.getIdIndex()+""));

    // attribute defs
    for (String attributeDefName : getAttributeDefNames()) {
      List<String> values = stem.getAttributeValueDelegate().retrieveValuesString(attributeDefName);
      if (values != null && !values.isEmpty()) {
        BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeDefName);
        basicAttribute.setValues(values);
        attributes.put(attributeDefName, basicAttribute);
      }
    }

    return attributes;
  }

  public void validate() throws AttributeResolutionException {

  }

  /**
   * Get the root stem. Re-uses the stem object.
   * 
   * @return the root stem
   */
  public Stem getRootStem() {
    if (rootStem == null) {

      rootStem = (Stem) GrouperSession.callbackGrouperSession(getGrouperSession(), new GrouperSessionHandler() {

        public Stem callback(GrouperSession grouperSession) throws GrouperSessionException {
          return StemFinder.findRootStem(grouperSession);
        }
      });
    }

    return rootStem;
  }
}
