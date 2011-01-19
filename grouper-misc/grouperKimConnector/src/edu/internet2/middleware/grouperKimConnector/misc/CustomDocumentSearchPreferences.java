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
