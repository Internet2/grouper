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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;

/**
 * A {@link DataConnector} which returns {@link Stem}s.
 */
public class StemDataConnector extends BaseGrouperDataConnector {

  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(StemDataConnector.class);

  private Stem rootStem;

  /** {@inheritDoc} */
  public Map<String, BaseAttribute> resolve(final ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    Map<String, BaseAttribute> attributes = (Map<String, BaseAttribute>) GrouperSession.callbackGrouperSession(
        getGrouperSession(), new GrouperSessionHandler() {

          public Map<String, BaseAttribute> callback(GrouperSession grouperSession) throws GrouperSessionException {

            String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
            String msg = "'" + principalName + "' dc '" + getId() + "'";
            LOG.debug("resolve {}", msg);
            if (LOG.isTraceEnabled()) {
              LOG.trace("resolve {} requested attribute ids {}", msg, resolutionContext.getAttributeRequestContext()
                  .getRequestedAttributesIds());
              if (resolutionContext.getAttributeRequestContext().getRequestedAttributesIds() != null) {
                for (String attrId : resolutionContext.getAttributeRequestContext().getRequestedAttributesIds()) {
                  LOG.trace("resolve {} requested attribute '{}'", msg, attrId);
                }
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

            // root stem ?
            if (stem.equals(getRootStem())) {
              LOG.debug("resolve {} returning empty map for root stem", msg);
              return attributes;
            }

            // FUTURE match filter ?

            // FUTURE return attributes, child groups, etc ?

            // extension
            BasicAttribute<String> extension = new BasicAttribute<String>(GrouperConfig.ATTRIBUTE_EXTENSION);
            extension.setValues(GrouperUtil.toList(stem.getExtension()));
            attributes.put(extension.getId(), extension);

            // display extension
            BasicAttribute<String> displayExtension = new BasicAttribute<String>(
                GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION);
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

            // parent stem
            Stem parentStem = stem.getParentStem();
            if (!parentStem.equals(getRootStem())) {
              BasicAttribute<String> parentStemNameAttr = new BasicAttribute<String>(PARENT_STEM_NAME_ATTR);
              parentStemNameAttr.setValues(GrouperUtil.toList(parentStem.getName()));
              attributes.put(parentStemNameAttr.getId(), parentStemNameAttr);
            }

            // attribute defs
            for (String attributeDefName : getAttributeDefNames()) {
              List<String> values = stem.getAttributeValueDelegate().retrieveValuesString(attributeDefName);
              if (values != null && !values.isEmpty()) {
                BasicAttribute<String> basicAttribute = new BasicAttribute<String>(attributeDefName);
                basicAttribute.setValues(values);
                attributes.put(attributeDefName, basicAttribute);
              }
            }

            if (LOG.isDebugEnabled()) {
              LOG.debug("resolve {} attributes {}", msg, attributes.size());
              for (String key : attributes.keySet()) {
                for (Object value : attributes.get(key).getValues()) {
                  LOG.debug("resolve {} '{}' : {}", new Object[] { msg, key, value });
                }
              }
            }

            return attributes;
          }
        });

    return attributes;
  }

  public void validate() throws AttributeResolutionException {

  }

  /**
   * Get the root stem. Re-uses the stem object.
   * 
   * @return the root stem
   */
  private Stem getRootStem() {
    if (rootStem == null) {

      rootStem = (Stem) GrouperSession.callbackGrouperSession(getGrouperSession(), new GrouperSessionHandler() {

        public Stem callback(GrouperSession grouperSession) throws GrouperSessionException {
          return StemFinder.findRootStem(grouperSession);
        }
      });
    }

    return rootStem;
  }

  /**
   * {@inheritDoc}
   * 
   * Stems which are parent to all groups returned from {@link BaseGrouperDataConnector#getGroups()} are returned. The
   * root stem is omitted.
   */
  public Set<String> getAllIdentifiers() {
    return this.getAllIdentifiers(null);
  }

  /**
   * {@inheritDoc}
   * 
   * see {@link StemDataConnector#getAllIdentifiers()}
   * 
   */
  public Set<String> getAllIdentifiers(Date updatedSince) {
    Set<String> identifiers = new TreeSet<String>();
    for (String stemName : GrouperUtil.findParentStemNames(this.getGroups(updatedSince))) {
      // omit root
      if (!stemName.equals(Stem.DELIM)) {
        identifiers.add(stemName);
      }
    }
    return identifiers;
  }
}
