package com.me.learn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@SpringBootApplication
@EnableEurekaClient //激活服务发现客户端
public class ConfigClientBootStrapApplication {

    @Value("#{'${test.values}'.split(',')}")
    private List<String> values;

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientBootStrapApplication.class, args);
    }

    @Value("${spring.application.name}")
    private String appName;

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
