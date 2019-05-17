package com.bee.sample.ch16.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConf {
	@Value("${zk.url}")
	private String zkUrl;

	Log log = LogFactory.getLog(ZookeeperConf.class);

	private boolean isLeader = false;

	// 返回一个ZK客户端对象，并注册为spring的bean
	@Bean
	public CuratorFramework getCuratorFramework() throws Exception {
		// CuratorFramework是一个线程安全的类，可以用它完成所有的zk功能
		// RetryPolicy用于重连策略，当某种原因导致zk不可用的时候进行重连尝试的策略
		// ExponentialBackoffRetry是一种重连策略，每次重连的间隔会越来越长，1000毛秒是初始化的间隔时间，3代表重连次数
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

		// CuratorFramework通过zkUrl和retryPolicy构造
		// zkUrl是ZooKeeper的连接地址，在application.properties中
		CuratorFramework client = CuratorFrameworkFactory.newClient(zkUrl, retryPolicy);
		client.getCuratorListenable().addListener(new CuratorListener() {
			public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
				CuratorEventType type = event.getType();
				if (type == CuratorEventType.WATCHED) {
					WatchedEvent we = event.getWatchedEvent();
					EventType et = we.getType();
					if (we.getPath() != null) {
						log.info(et + ":" + we.getPath());
						client.checkExists().watched().forPath(we.getPath());
					}

				}
			}

		});
		//
		// LeaderSelectorListenerAdapter listener = new
		// LeaderSelectorListenerAdapter() {
		// public void takeLeadership(CuratorFramework client) throws Exception
		// {
		// log.info("get leadership");
		// isLeader = true;
		// //或者进行其他操作
		// }
		// };
		//
		// LeaderSelector selector = new LeaderSelector(client, "/schedule",
		// listener);
		// selector.autoRequeue();
		// selector.start();

		// 调用start开始链接ZooKeeper
		client.start();

		registerSerivce(client);
		ServiceInstance<Map> service = findService(client, "book");
		return client;
	}

	protected void registerSerivce(CuratorFramework client) throws Exception {

		// 构造一个服务描述
		ServiceInstanceBuilder<Map> service = ServiceInstance.builder();
		service.address("192.168.1.100");
		service.port(8080);
		service.name("book");
		Map config = new HashMap();
		config.put("url", "/api/v3/book");
		service.payload(config);

		ServiceInstance<Map> instance = service.build();

		ServiceDiscovery<Map> serviceDiscovery = ServiceDiscoveryBuilder.builder(Map.class).client(client)
				.serializer(new JsonInstanceSerializer<Map>(Map.class)).basePath("/service").build();
		// 服务注册
		serviceDiscovery.registerService(instance);

		serviceDiscovery.start();

	}

	protected ServiceInstance<Map> findService(CuratorFramework client, String serviceName) throws Exception {

		ServiceDiscovery<Map> serviceDiscovery = ServiceDiscoveryBuilder.builder(Map.class).client(client)
				.serializer(new JsonInstanceSerializer<Map>(Map.class)).basePath("/service").build();

		serviceDiscovery.start();

		Collection<ServiceInstance<Map>> all = serviceDiscovery.queryForInstances(serviceName);
		if (all.size() == 0) {
			return null;
		} else {
			// 取第一个服务
			ServiceInstance<Map> service = new ArrayList<ServiceInstance<Map>>(all).get(0);
			System.out.println(service.getAddress());
			System.out.println(service.getPayload());
			return service;

		}

	}

}
