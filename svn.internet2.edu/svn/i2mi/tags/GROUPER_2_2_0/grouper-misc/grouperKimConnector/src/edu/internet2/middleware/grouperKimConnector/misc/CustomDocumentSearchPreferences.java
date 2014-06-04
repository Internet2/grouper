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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.misc;

import java.util.Iterator;
import java.util.List;

import org.kuali.rice.kew.docsearch.DocSearchCriteriaDTO;
import org.kuali.rice.kew.docsearch.DocSearchDTO;
import org.kuali.rice.kew.docsearch.StandardDocumentSearchResultProcessor;
import org.kuali.rice.kew.util.KEWPropertyConstants;
import org.kuali.rice.kns.web.ui.Column;


/**
 * hide title on search results screen
 */
public class CustomDocumentSearchPreferences extends
    StandardDocumentSearchResultProcessor {

  /**
   * @see org.kuali.rice.kew.docsearch.StandardDocumentSearchResultProcessor#constructColumnList(DocSearchCriteriaDTO, List)
   */
  @Override
  public List<Column> constructColumnList(DocSearchCriteriaDTO criteria, List<DocSearchDTO> docSearchDtos) {
    List<Column> columns = super.constructColumnList(criteria, docSearchDtos);
    
    if (columns != null) {
      Iterator<Column> iterator = columns.iterator();
      while (iterator.hasNext()) {
        Column column = iterator.next();
        
        //remove title col
        if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOCUMENT_TITLE.equals(column.getPropertyName())) {
          iterator.remove();
        }
      }
    }
    return columns;
  }

  
  
}
