package edu.internet2.middleware.grouper.app.provisioning.targetDao;


public class TargetDaoTimingInfo {

  private String daoMethod;
  
  private long microsElapsed;

  
  public String getDaoMethod() {
    return daoMethod;
  }

  
  public void setDaoMethod(String daoMethod) {
    this.daoMethod = daoMethod;
  }

  
  public long getMicrosElapsed() {
    return microsElapsed;
  }

  
  public void setMicrosElapsed(long microsElapsed) {
    this.microsElapsed = microsElapsed;
  }


  public TargetDaoTimingInfo(String daoMethod, long startNanos) {
    super();
    this.daoMethod = daoMethod;
    this.microsElapsed = (System.nanoTime() - startNanos)/1000;
  }


  public TargetDaoTimingInfo() {
    super();
    // TODO Auto-generated constructor stub
  }
  
}
