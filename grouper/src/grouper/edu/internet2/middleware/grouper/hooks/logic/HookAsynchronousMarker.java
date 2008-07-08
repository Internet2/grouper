/*
 * @author mchyzer
 * $Id: HookAsynchronousMarker.java,v 1.1 2008-07-08 20:47:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;


/**
 * Marker interface for your hook.  If a hook implementation (e.g. 
 * GroupHooks subclass) has this interface, then any hooks called will be 
 * in another thread
 */
public interface HookAsynchronousMarker {

}
