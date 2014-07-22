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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;



/**
 * utils about emails
 */
public class GrouperEmailUtils {

  /**
   * get the subject attribute name for a source id
   * @param sourceId
   * @return the attribute name
   */
  public static String emailAttributeNameForSource(String sourceId) {
    Source source = SourceManager.getInstance().getSource(sourceId);
    return source.getInitParam("emailAttributeName");
  }
}
