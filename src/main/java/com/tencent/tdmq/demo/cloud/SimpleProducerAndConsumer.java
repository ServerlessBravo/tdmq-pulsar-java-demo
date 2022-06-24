package com.tencent.tdmq.demo.cloud;

import java.util.HashMap;
import org.apache.pulsar.client.api.*;

/**
 * 简单的生产和消息例子
 *
 */

public class SimpleProducerAndConsumer {

  public static final String SERVICE_URL = "http://pulsar-xxxx.tdmq.ap-gz.qcloud.tencenttdmq.com:5035";
  public static final String USER_TOKEN = "....use....token...";
  public static final String TOPIC_ADDRESS = "persistent://pulsar-xxx/default/chat";

  public static void main(String[] args) throws PulsarClientException {
    invoke();
  }

  public static void createConsumersAndReceiveMessages(String subscriptionName, String tagName)
      throws PulsarClientException {

    PulsarClient client = PulsarClient.builder()
        // ip:port 替换成路由ID，位于【集群管理】接入点列表
        .serviceUrl(SERVICE_URL)
        // 替换成角色密钥，位于【角色管理】页面
        .authentication(AuthenticationFactory.token(USER_TOKEN))
        .build();
    System.out.println(">> pulsar client created.");

    HashMap<String, String> subProperties = new HashMap<>();
    subProperties.put(tagName, "1");

    // 创建消费者
    Consumer<byte[]> consumer = client.newConsumer()
        // topic完整路径，格式为persistent://集群（租户）ID/命名空间/Topic名称，从【Topic管理】处复制
        .topic(TOPIC_ADDRESS)
        // 需要在控制台Topic详情页创建好一个订阅，此处填写订阅名
        .subscriptionName(subscriptionName)
        // 声明消费模式为exclusive（独占）模式
        .subscriptionType(SubscriptionType.Shared)
        .subscriptionProperties(subProperties)
        // 配置从最早开始消费，否则可能会消费不到历史消息
        .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
        .subscribe();
    System.out.println(">> pulsar consumer created.");

    while (true) {
      // 接收当前offset对应的一条消息
      Message<byte[]> msg = consumer.receive();
      MessageId msgId = msg.getMessageId();
      String value = new String(msg.getValue());
      System.out.println("receive msg with filter room-id:123 " + msgId + ", value:" + value + " with subscription: "
          + subscriptionName);
      // 接收到之后必须要ack，否则offset会一直停留在当前消息，无法继续消费
      consumer.acknowledge(msg);
      // 关闭消费进程
    }
  }

  private static void invoke() throws PulsarClientException {

    // 一个Pulsar client对应一个客户端链接
    // 原则上一个进程一个client，尽量避免重复创建，消耗资源
    // 关于客户端和生产消费者的最佳实践，可以参考官方文档
    // https://cloud.tencent.com/document/product/1179/58090
    PulsarClient client = PulsarClient.builder()
        // ip:port 替换成路由ID，位于【集群管理】接入点列表
        .serviceUrl(SERVICE_URL)
        // 替换成角色密钥，位于【角色管理】页面
        .authentication(AuthenticationFactory.token(USER_TOKEN))
        .build();
    System.out.println(">> pulsar client created.");

    // 创建生产者
    Producer<byte[]> producer = client.newProducer()
        // topic完整路径，格式为persistent://集群（租户）ID/命名空间/Topic名称
        .topic(TOPIC_ADDRESS)
        .enableBatching(false)
        .create();
    System.out.println(">> pulsar producer created.");

    // 生产5条消息带有tag
    for (int i = 0; i < 5; i++) {
      // 发送消息: 带有Tag 123
      String value = "my-sync-message-" + i + " Tag: 123";
      MessageId msgId = producer
          .newMessage()
          .value(value.getBytes())
          .property("123", "TAGS")
          .send();
      System.out.println("deliver msg with tag 123 " + msgId + ",value:" + value);

      // 发送消息: 带有Tag 456
      String value2 = "my-sync-message-" + i + " Tag: 456";
      MessageId msgId2 = producer
          .newMessage()
          .value(value2.getBytes())
          .property("456", "TAGS")
          .send();
      System.out.println("deliver msg with tag 456 " + msgId2 + ",value:" + value2);
    }

    // 关闭生产者
    // 关闭生产者
    producer.close();
    // 关闭客户端
    client.close();

    int numberOfConsumers = 3;
    for (int i = 0; i < numberOfConsumers; i++) {
      new Thread((new Runnable() {
        String subscriptionName;

        public void run() {
          try {
            SimpleProducerAndConsumer.createConsumersAndReceiveMessages(subscriptionName, "123");
          } catch (PulsarClientException e) {
            e.printStackTrace();
          }
        }

        public Runnable SetSubscription(String name) {
          this.subscriptionName = name;
          return this;
        }
      }).SetSubscription("subscribe-" + i)).start();

    }
  }
}
