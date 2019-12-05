# rpcFramework-demo
模仿Dubbo思想，自己实现的一个分布式RPC框架的Demo。没有很详细的写文档，感兴趣的同学可以阅读《从零开始写分布式服务框架》 by 李业兵一书，我是基于作者提供的源码的基础上进行改进的。

关键实现：  
1）通过Spring自定义标签+FactoryBean的方式使得框架可以与Spring框架结合使用  
2）基于Netty Reactor线程模型+JDK动态代理实现面向接口的RPC调用  
3）每个Client与Provider之间默认保持一个IO连接，Provider端线程池分为IO线程池与业务线程池  
4）使用Zookeeper作为服务注册中心，实现服务的自动注册与发现  
5）基于Netty MessageToByteEncoder与ByteToMessageDecoders实现对自定义数据传输协议的编解码  
6）基于JDK SPI机制实现框架的扩展性，目前的扩展点包含负载均衡机制、序列化机制与请求的过滤器处理机制  
