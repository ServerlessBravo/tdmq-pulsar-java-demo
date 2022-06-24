# tdmq-pulsar-java-demo
Message Producer, Consumer and Tag Filter


### Configuration

Pulsar Configuration:

```java
  public static final String SERVICE_URL = "http://pulsar-xxxx.tdmq.ap-gz.qcloud.tencenttdmq.com:5035";
  public static final String USER_TOKEN = "....use....token...";
  public static final String TOPIC_ADDRESS = "persistent://pulsar-xxx/default/chat";

```

### Compile


```bash
mvn package

```

### Run

```bash
mvn run
```


## More information

- [TDMQ](https://cloud.tencent.com/product/tpulsar)
- [TDMQ Doc](https://cloud.tencent.com/document/product/1179)
