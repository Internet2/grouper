package edu.internet2.middleware.grouper.pspng;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.handler.SearchEntryHandler;
import org.slf4j.Logger;

public class LdapSearchProgressHandler implements SearchEntryHandler {
    ProgressMonitor progressMonitor;

    public LdapSearchProgressHandler(int numberOfExpectedResults, Logger LOG, String progressMonitorLabel) {
      progressMonitor = new ProgressMonitor(numberOfExpectedResults, LOG, true, 15, progressMonitorLabel);
    }

    public LdapSearchProgressHandler(Logger LOG, String progressMonitorLabel) {
      this(-1, LOG, progressMonitorLabel);
    }

    @Override
    public HandlerResult<SearchEntry> handle(Connection connection, SearchRequest searchRequest, SearchEntry searchEntry) throws LdapException {
      progressMonitor.workCompleted(1);

      return new HandlerResult<SearchEntry>(searchEntry);
    }


    @Override
    public void initializeRequest(SearchRequest searchRequest) {

    }
}
