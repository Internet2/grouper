package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.Basic.RecoverOk;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Confirm.SelectOk;
import com.rabbitmq.client.AMQP.Exchange.BindOk;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.AMQP.Exchange.DeleteOk;
import com.rabbitmq.client.AMQP.Exchange.UnbindOk;
import com.rabbitmq.client.AMQP.Queue.PurgeOk;
import com.rabbitmq.client.AMQP.Tx.CommitOk;
import com.rabbitmq.client.AMQP.Tx.RollbackOk;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Command;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.FlowListener;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public enum RabbitMQConnectionFactoryFake implements RabbitMQConnectionFactory {
  
  INSTANACE {
    
    @Override
    public Connection getConnection(String messagingSystemName) {
      return new FakeConnection();
    }

    @Override
    public void closeConnection(String messagingSystemName) {
      
    }
    
  };

}

class FakeConnection implements com.rabbitmq.client.Connection {
  
  public static final Map<String, List<? extends Object>> recordedValues = new HashMap<String, List<? extends Object>>();
  
  @Override
  public void addShutdownListener(ShutdownListener arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ShutdownSignalException getCloseReason() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isOpen() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void notifyListeners() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeShutdownListener(ShutdownListener arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void abort() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void abort(int arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void abort(int arg0, String arg1) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void abort(int arg0, String arg1, int arg2) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addBlockedListener(BlockedListener arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clearBlockedListeners() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close(int arg0) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close(int arg0, String arg1) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close(int arg0, String arg1, int arg2) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Channel createChannel() throws IOException {
    return new FakeChannel();
  }

  @Override
  public Channel createChannel(int arg0) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InetAddress getAddress() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getChannelMax() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Map<String, Object> getClientProperties() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getClientProvidedName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExceptionHandler getExceptionHandler() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getFrameMax() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getHeartbeat() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getPort() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Map<String, Object> getServerProperties() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean removeBlockedListener(BlockedListener arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setId(String arg0) {
    // TODO Auto-generated method stub
    
  }
  
}

class FakeChannel implements Channel {

  @Override
  public void addShutdownListener(ShutdownListener arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ShutdownSignalException getCloseReason() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isOpen() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void notifyListeners() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeShutdownListener(ShutdownListener arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void abort() throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void abort(int arg0, String arg1) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addConfirmListener(ConfirmListener arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addFlowListener(FlowListener arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addReturnListener(ReturnListener arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void asyncRpc(Method arg0) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void basicAck(long arg0, boolean arg1) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void basicCancel(String arg0) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String basicConsume(String arg0, Consumer arg1) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String basicConsume(String arg0, boolean arg1, Consumer arg2) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String basicConsume(String arg0, boolean arg1, Map<String, Object> arg2, Consumer arg3) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String basicConsume(String arg0, boolean arg1, String arg2, Consumer arg3) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String basicConsume(String arg0, boolean arg1, String arg2, boolean arg3, boolean arg4,
      Map<String, Object> arg5, Consumer arg6) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GetResponse basicGet(String arg0, boolean arg1) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void basicNack(long arg0, boolean arg1, boolean arg2) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void basicPublish(String arg0, String arg1, BasicProperties arg2, byte[] arg3) throws IOException {
    // TODO Auto-generated method stub
    FakeConnection.recordedValues.put("basicPublish", Arrays.asList(arg0, arg1, arg2, arg3));
  }

  @Override
  public void basicPublish(String arg0, String arg1, boolean arg2, BasicProperties arg3, byte[] arg4)
      throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void basicPublish(String arg0, String arg1, boolean arg2, boolean arg3, BasicProperties arg4, byte[] arg5)
      throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void basicQos(int arg0) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void basicQos(int arg0, boolean arg1) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void basicQos(int arg0, int arg1, boolean arg2) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public RecoverOk basicRecover() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RecoverOk basicRecover(boolean arg0) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void basicReject(long arg0, boolean arg1) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clearConfirmListeners() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clearFlowListeners() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clearReturnListeners() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close() throws IOException, TimeoutException {
    FakeConnection.recordedValues.put("close", Arrays.asList());
  }

  @Override
  public void close(int arg0, String arg1) throws IOException, TimeoutException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public SelectOk confirmSelect() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long consumerCount(String arg0) throws IOException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public BindOk exchangeBind(String arg0, String arg1, String arg2) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BindOk exchangeBind(String arg0, String arg1, String arg2, Map<String, Object> arg3) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void exchangeBindNoWait(String arg0, String arg1, String arg2, Map<String, Object> arg3) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public DeclareOk exchangeDeclare(String arg0, String arg1) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeclareOk exchangeDeclare(String arg0, BuiltinExchangeType arg1) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeclareOk exchangeDeclare(String arg0, String arg1, boolean arg2) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeclareOk exchangeDeclare(String arg0, BuiltinExchangeType arg1, boolean arg2) throws IOException {
    FakeConnection.recordedValues.put("exchangeDeclare", Arrays.asList(arg0, arg1, arg2));
    return null;
  }

  @Override
  public DeclareOk exchangeDeclare(String arg0, String arg1, boolean arg2, boolean arg3, Map<String, Object> arg4)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeclareOk exchangeDeclare(String arg0, BuiltinExchangeType arg1, boolean arg2, boolean arg3,
      Map<String, Object> arg4) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeclareOk exchangeDeclare(String arg0, String arg1, boolean arg2, boolean arg3, boolean arg4,
      Map<String, Object> arg5) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeclareOk exchangeDeclare(String arg0, BuiltinExchangeType arg1, boolean arg2, boolean arg3, boolean arg4,
      Map<String, Object> arg5) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void exchangeDeclareNoWait(String arg0, String arg1, boolean arg2, boolean arg3, boolean arg4,
      Map<String, Object> arg5) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void exchangeDeclareNoWait(String arg0, BuiltinExchangeType arg1, boolean arg2, boolean arg3, boolean arg4,
      Map<String, Object> arg5) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public DeclareOk exchangeDeclarePassive(String arg0) throws IOException {
    FakeConnection.recordedValues.put("exchangeDeclarePassive", Arrays.asList(arg0));
    return null;
  }

  @Override
  public DeleteOk exchangeDelete(String arg0) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeleteOk exchangeDelete(String arg0, boolean arg1) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void exchangeDeleteNoWait(String arg0, boolean arg1) throws IOException {
  }

  @Override
  public UnbindOk exchangeUnbind(String arg0, String arg1, String arg2) throws IOException {
    return null;
  }

  @Override
  public UnbindOk exchangeUnbind(String arg0, String arg1, String arg2, Map<String, Object> arg3) throws IOException {
    return null;
  }

  @Override
  public void exchangeUnbindNoWait(String arg0, String arg1, String arg2, Map<String, Object> arg3) throws IOException {
  }

  @Override
  public boolean flowBlocked() {
    return false;
  }

  @Override
  public int getChannelNumber() {
    return 0;
  }

  @Override
  public Connection getConnection() {
    return null;
  }

  @Override
  public Consumer getDefaultConsumer() {
    return null;
  }

  @Override
  public long getNextPublishSeqNo() {
    return 0;
  }

  @Override
  public long messageCount(String arg0) throws IOException {
    return 0;
  }

  @Override
  public com.rabbitmq.client.AMQP.Queue.BindOk queueBind(String arg0, String arg1, String arg2) throws IOException {
    return null;
  }

  @Override
  public com.rabbitmq.client.AMQP.Queue.BindOk queueBind(String arg0, String arg1, String arg2,
      Map<String, Object> arg3) throws IOException {
    return null;
  }

  @Override
  public void queueBindNoWait(String arg0, String arg1, String arg2, Map<String, Object> arg3) throws IOException {}

  @Override
  public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclare() throws IOException {
    return null;
  }

  @Override
  public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclare(String arg0, boolean arg1, boolean arg2, boolean arg3,
      Map<String, Object> arg4) throws IOException {
    FakeConnection.recordedValues.put("queueDeclare", Arrays.asList(arg0, arg1, arg2, arg3, arg4));
    return null;
  }

  @Override
  public void queueDeclareNoWait(String arg0, boolean arg1, boolean arg2, boolean arg3, Map<String, Object> arg4)
      throws IOException {
  }

  @Override
  public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclarePassive(String arg0) throws IOException {
    FakeConnection.recordedValues.put("queueDeclarePassive", Arrays.asList(arg0));
    return null;
  }

  @Override
  public com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete(String arg0) throws IOException {
    return null;
  }

  @Override
  public com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete(String arg0, boolean arg1, boolean arg2)
      throws IOException {
    return null;
  }

  @Override
  public void queueDeleteNoWait(String arg0, boolean arg1, boolean arg2) throws IOException {
  }

  @Override
  public PurgeOk queuePurge(String arg0) throws IOException {
    return null;
  }

  @Override
  public com.rabbitmq.client.AMQP.Queue.UnbindOk queueUnbind(String arg0, String arg1, String arg2) throws IOException {
    return null;
  }

  @Override
  public com.rabbitmq.client.AMQP.Queue.UnbindOk queueUnbind(String arg0, String arg1, String arg2,
      Map<String, Object> arg3) throws IOException {
    return null;
  }

  @Override
  public boolean removeConfirmListener(ConfirmListener arg0) {
    return false;
  }

  @Override
  public boolean removeFlowListener(FlowListener arg0) {
    return false;
  }

  @Override
  public boolean removeReturnListener(ReturnListener arg0) {
    return false;
  }

  @Override
  public Command rpc(Method arg0) throws IOException {
    return null;
  }

  @Override
  public void setDefaultConsumer(Consumer arg0) {}

  @Override
  public CommitOk txCommit() throws IOException {
    return null;
  }

  @Override
  public RollbackOk txRollback() throws IOException {
    return null;
  }

  @Override
  public com.rabbitmq.client.AMQP.Tx.SelectOk txSelect() throws IOException {
    return null;
  }

  @Override
  public boolean waitForConfirms() throws InterruptedException {
    return false;
  }

  @Override
  public boolean waitForConfirms(long arg0) throws InterruptedException, TimeoutException {
    return false;
  }

  @Override
  public void waitForConfirmsOrDie() throws IOException, InterruptedException {}

  @Override
  public void waitForConfirmsOrDie(long arg0) throws IOException, InterruptedException, TimeoutException {}
  
}
