package edu.internet2.middleware.grouper.misc;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import edu.internet2.middleware.grouper.plugins.FrameworkStarter;

public class GrouperShutdown {

  public GrouperShutdown() {
  }

  public static void shutdown() {
    //FrameworkStarter.getInstance().stop();
  }
}
