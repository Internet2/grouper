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

package edu.internet2.middleware.ldappc.util;

import java.util.regex.Pattern;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import edu.internet2.middleware.grouper.shibboleth.dataConnector.ChangeLogDataConnector;

public class IgnoreRequestIDDifferenceListener implements DifferenceListener {

  private static final Logger LOG = LoggerFactory.getLogger(IgnoreRequestIDDifferenceListener.class);

  private static final Pattern errorPatternAD = Pattern
      .compile("LDAP: error code 32 - 00000525: NameErr: DSID-.*?, problem 2001 \\(NO_OBJECT\\), data 0, best match of:");

  public int differenceFound(Difference difference) {

    if (difference.getTestNodeDetail().getNode() != null && difference.getControlNodeDetail().getNode() != null) {

      if (difference.getTestNodeDetail().getNode().getNodeName().equals("requestID")
          && difference.getControlNodeDetail().getNode().getNodeName().equals("requestID")) {
        LOG.debug("ignoring difference {}", difference);
        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
      }

      if (difference.getTestNodeDetail().getNode().getNodeName().equals("#text")
          && difference.getControlNodeDetail().getNode().getNodeName().equals("#text")) {
        if (errorPatternAD.matcher(difference.getTestNodeDetail().getValue()).find()
            && errorPatternAD.matcher(difference.getControlNodeDetail().getValue()).find()) {
          LOG.debug("ignoring difference {}", difference);
          return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }
      }

      // ignore principal names that start with the change log data connector prefix
      if (difference.getTestNodeDetail().getNode().getNodeName().equals("ID")
          && difference.getControlNodeDetail().getNode().getNodeName().equals("ID")
          && difference.getTestNodeDetail().getNode().getNodeValue()
              .startsWith(ChangeLogDataConnector.PRINCIPAL_NAME_PREFIX)
          && difference.getControlNodeDetail().getNode().getNodeValue()
              .startsWith(ChangeLogDataConnector.PRINCIPAL_NAME_PREFIX)) {
        LOG.debug("ignoring changelog ID difference {}", difference);
        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
      }
    }
    return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
  }

  public void skippedComparison(Node control, Node test) {
  }

}
