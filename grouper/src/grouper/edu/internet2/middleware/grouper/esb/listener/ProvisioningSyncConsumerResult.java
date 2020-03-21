package edu.internet2.middleware.grouper.esb.listener;


public class ProvisioningSyncConsumerResult {

  public ProvisioningSyncConsumerResult() {
    // TODO Auto-generated constructor stub
  }

  /**
   * last processed sequence number
   */
  private Long lastProcessedSequenceNumber;

  
  public Long getLastProcessedSequenceNumber() {
    return lastProcessedSequenceNumber;
  }

  
  public void setLastProcessedSequenceNumber(Long lastProcessedSequenceNumber) {
    this.lastProcessedSequenceNumber = lastProcessedSequenceNumber;
  }
  
  
  
}
