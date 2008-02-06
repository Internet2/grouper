/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.internet2.middleware.ldappc;

import  edu.internet2.middleware.grouper.GrouperSession;
import  edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;

import  edu.internet2.middleware.subject.Subject;

//import  edu.internet2.middleware.subject.*;

/**
 * Class for Starting and stopping a grouper session 
 * @author Gil Singer
 */
public class GrouperSessionControl 
{
    /**
     * Flag indicating whether a session has been started and not yet stopped.
     */
    private boolean sessionGoing;

    /**
     * Group session
     */
    private GrouperSession session;

    /**
     * The subject for the session
     */
    private Subject subject;

    /**
     * Constructor
     */
    public GrouperSessionControl()
    {
    }
    
    /**
     * Start a GrouperSystem session.
     * @return true if session started; else false
     */
    public boolean startSession(String subjectId)
    {
        boolean started = true;
        try 
        {
            //subject = SubjectFinder.findById(subjectId, "application", InternalSourceAdapter.ID);
              
            GrouperSubjectRetriever grouperSubjectRetriever = new GrouperSubjectRetriever();
            subject = grouperSubjectRetriever.findSubjectById(subjectId);
     
            if (subject == null)
            {
                ErrorLog.error(this.getClass(), "Subject is null in GrouperSessionControl, check the subjectId:" + subjectId);
            }

            try 
            {
                session = GrouperSession.start(subject);
                DebugLog.info("Started GrouperSession: " + session);
            }
            catch (SessionException se) 
            {
                ErrorLog.error(this.getClass(), "Failed to start GrouperSession for subjectId= " 
                       + subjectId + ":    "  + se.getMessage());
                started = false;
            }
            if (session == null)
            {
                ErrorLog.error(this.getClass(), "Session is null in GrouperSessionControl");    
            }
    
        }
        catch (Exception e) {
            ErrorLog.error(this.getClass(), "Failed to find GrouperSession: "  + e.getMessage());
            started = false;
        }
        sessionGoing = started;
        return started;
    }
        
    /**
     * Stop a Grouper session.
     * @return true if session stopped; else false
     */
    public boolean stopSession()
    {
        boolean stopped = true;
        try 
        {
            // Need to save session string before stopping it so that
            // we can print it's description. (Fixes GRP-14. KAH)
            String sessionString = session.toString();
            session.stop();
            DebugLog.info("Stopped GrouperSession: " + sessionString);
        }
        catch (SessionException se) 
        {
            ErrorLog.error(this.getClass(), "Failed to stop GrouperSession: " + se.getMessage());
            stopped = false;
        }
        sessionGoing = !stopped;
        return stopped;
    }

    /**
     * Get the session.
     * @return The session
     */
    public GrouperSession getSession()
    {
        return session;
    }

    /**
     * Determine if the session is running
     * @return The session
     */
    public boolean isSessionGoing()
    {
        return sessionGoing;
    }

} 

