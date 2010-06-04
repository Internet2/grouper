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

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class IgnoreRequestIDDifferenceListener implements DifferenceListener {

  private static final Logger LOG = LoggerFactory.getLogger(IgnoreRequestIDDifferenceListener.class);

  public static final String NODE_NAME = "requestID";

  public int differenceFound(Difference difference) {

    if (difference.getTestNodeDetail().getNode() != null && difference.getControlNodeDetail().getNode() != null) {
      if (difference.getTestNodeDetail().getNode().getNodeName().equals(NODE_NAME)
          && difference.getControlNodeDetail().getNode().getNodeName().equals(NODE_NAME)) {
        LOG.debug("ignoring difference {}", difference);
        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
      }
    }
    return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
  }

  public void skippedComparison(Node control, Node test) {
  }

}
