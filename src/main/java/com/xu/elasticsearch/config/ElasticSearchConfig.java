package com.xu.elasticsearch.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author charlie Created on 2018/1/29.
 *
 * ElasticSearch配置
 */
@Configuration
public class ElasticSearchConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchConfig.class);

	/**
	 * elk集群地址
	 */
	@Value("${elasticsearch.ip}")
	private String hostName;
	/**
	 * 端口
	 */
	@Value("${elasticsearch.port}")
	private String port;
	/**
	 * 集群名称
	 */
	@Value("${elasticsearch.cluster.name}")
	private String clusterName;

	/**
	 * 连接池
	 */
	@Value("${elasticsearch.pool}")
	private String poolSize;

	@Bean
	public TransportClient init() throws UnknownHostException {
		TransportClient client = null;
		try {
			Settings settings = Settings.builder().put("cluster.name", clusterName)
					.put("client.transport.sniff", false)//增加嗅探机制，找到ES集群
					.put("thread_pool.search.size", Integer.parseInt(poolSize))//增加线程池个数，暂时设为5
					.build();
			client = new PreBuiltTransportClient(settings)
					.addTransportAddress(
							new TransportAddress(InetAddress.getByName(hostName), Integer.valueOf(port)));
		} catch (Exception e) {
			LOGGER.error("elasticsearch TransportClient create error!!!", e);
		}
		return client;
	}
}
