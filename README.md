# Spring Cloud的注册中心的学习

我们将使用Eureka， Zookeeper 和 Consul作为配置中心。 下面分别讲解这三个组件的应用和相关配置

# Eureka

Eureka作为配置中心，我需要创建一个微服务作为注册中心的server端，其他的服务提供者和消费者需要向eureka server注册和拉取服务列表。 



## 搭建Eureka server

### 创建一个Springboot web 项目

#### 添加@EnableEurekaServer

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
```

#### pom 文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.me.learn</groupId>
        <artifactId>spring-cloud-service-discovery-registry</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>eureka-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>eureka-server</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>


    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```



### 引入spring cloud eureka server的maven

```xml

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
```



### eureka server的配置

```yml
server:
  port: 8671

spring:
  application:
    name: eureka-server

  freemarker:
    template-loader-path: classpath:/templates/
    prefer-file-system-access: false

eureka:
  instance:
    hostname: peer1  # 配置中心 , peer2 配置在hosts文件里
    instance-id: ${spring.application.name}-${server.port}  #服务实例在注册中心的名字
    ip-address: 127.0.0.1
  
  client:
    # 表示是否注册自身到eureka服务器
    register-with-eureka: false
    # 是否从eureka上获取注册信息
    fetch-registry: false
    service-url:
    # 如果eureka server是集群，defaultZone需要配置集群里的其他的node
      defaultZone: http://127.0.0.1:7862/eureka

  server:
    # 关闭eureka server的自我保护功能
    enable-self-preservation: off
    batch-replication: true
    peerEurekaNodesUpdateIntervalMs: 1
```



## Eureka 的client相关代码

### maven 依赖



添加spring-cloud-starter-netflix-eureka-client的依赖

```xml
       <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
```

### Java 代码

在springboot的启动类上加@EnableEurekaClient的注解

```java
@RestController
@SpringBootApplication
@EnableEurekaClient //激活服务发现客户端
public class ConfigClientBootStrapApplication {
    
}
```

### application.yml配置

```yml
server:
#  port: 0  # 随机端口
  port: 8084  # 指定端口

test:
  values: 1,2

spring:
 application:
  name: config-client

eureka:
  client:
    enabled: false
---  # profile for eureka   
#
eureka:
  servers:
    server1:
      host: peer1   # 自定义配置，非eureka内部配置
      port: 8671   # 自定义配置，非eureka内部配置
    server2:
      host: peer2 # 自定义配置，非eureka内部配置
      port: 8672  # 自定义配置，非eureka内部配置
      
      eureka:
  servers:
    server1:
      host: peer1   # 自定义配置，非eureka内部配置
      port: 8671   # 自定义配置，非eureka内部配置
    server2:
      host: peer2 # 自定义配置，非eureka内部配置
      port: 8672  # 自定义配置，非eureka内部配置

  instance:
    hostname: peer1  # 配置中心
    instance-id: ${spring.application.name}-${server.port}  #服务实例在注册中心的名字
    prefer-ip-address: true

  client:
    service-url:
    # 要是非集群环境，指定一个Eureka server就可以
      defaultZone: http://${eureka.servers.server1.host}:${eureka.servers.server1.port}/eureka/ 
    enabled: true
```

# Zookeper

要使用Zookeeper作为配置中心，前提我们要安装一个Zookeeper server， 为了保证注册中心的高可用，zk需要搭建集群。   

服务注册的逻辑 没有Java 代码的更改， 只需引入指定的依赖和相关的配置。 

> 引入的curator的版本应该和zk server的版本一直。 
>
> 关于zk server的安装和集群搭建，请到zookeeper的资料

### 添加zk作为配置中心的依赖

添加spring-cloud-starter-zookeeper-discovery

```xml
    <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
        </dependency>
```

### application.yml的配置

```yaml
server:
#  port: 0  # 随机端口
  port: 8084  # 指定端口

test:
  values: 1,2

spring:
 application:
  name: config-client
 cloud:
   zookeeper:
     discovery:
       enabled: false
   
---  # profile for zookkeeper
spring:
  profiles: zookeeper
  cloud:
    zookeeper:
      discovery:
        enabled: true
      connect-string: 127.0.0.1:2181
```



### 服务注册的信息

注册的服务信息保存在/services/config-client 节点下面。 

我启动两个config service实例，端口分别是8084和8085.



![image-20211130141053522](.\images\image-20211130141053522.png)

# Consul

Consul也是一个独立的中间件，需要单独安装。它既可以做完注册中心也可以配置中心。

让Consul做为配置中心，没有Java 代码的改动，只有jar和配置的代码。

### Consul的安装及启动

我本地安装的consul的版本为：1.10.2. 且consul还提供了页面查看注册实例的信息和配置的信息。 

启动consul： consul.exe  agent   -dev

consul的默认端口是8500. 

### maven 依赖

导入spring-cloud-starter-consul-discovery 和 spring-boot-starter-actuator。 



actuator用来服务监控，consul会调用endpoint: /actuaor/health 来检查服务是否健康

```java
 <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-consul-discovery</artifactId>
 </dependency>
     
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### application.yml的配置

```yaml
server:
#  port: 0  # 随机端口
  port: 8082  # 指定端口

test:
  values: 1,2

spring:
 application:
  name: config-client
 cloud:
   zookeeper:
     discovery:
       enabled: false
   consul:
     enabled: false
     discovery:
       register: false

eureka:
  client:
    enabled: false

---
spring:
  profiles: consul
  cloud:
    consul:
      enabled: true
      port: 8500
      host: 127.0.0.1
      discovery:
        prefer-ip-address: true
        register: true
        health-check-path: /actuator/health

# springboot actuator
management:
  endpoints:
    enabled-by-default: true
    web:
      exposure:
        include: "*"
    jmx:
      exposure:
        include: "*"
```

### 服务启动参数

--spring.profiles.active=consul

# DiscoveryClient

springcloud 提供了DiscoveryClient 用来查找注册的服务实例信息。他可以用来查看各个注册中心的实例。 

它提供了如下的接口

- 获取所有注册服务  

  ​	discoveryClient.getServices()

- 获取指定服务的所有实例

  discoveryClient.getInstances(serviceName)

- 获取指定服务的某个具体实例信息

```java

@RestController
public class TestController {
    @Autowired
    private DiscoveryClient discoveryClient;
    
     @GetMapping("/services")
    public Set<String> getServices() {
        return new LinkedHashSet<>(discoveryClient.getServices());
    }

    @GetMapping("/services/{serviceName}")
    public List<ServiceInstance> getInstances(@PathVariable String serviceName) {
        System.out.println(values);
        return discoveryClient.getInstances(serviceName);
    }

    @GetMapping("/services/{serviceName}/{instanceId}")
    public ServiceInstance getInstatance(@PathVariable String serviceName, @PathVariable String instanceId) {
    return discoveryClient.getInstances(serviceName)
                .stream()
                .filter(serviceInstance -> instanceId.equalsIgnoreCase(serviceInstance.getInstanceId()))
                .findFirst()
                .orElseThrow(() -> {
                    return new RuntimeException("No ServiceInstance Found");
                });
    }

}
```









# 工程代码

**https://github.com/lijed/spring-cloud-eureka.git**