/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.reactive.vertx.config;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Util class for getting initialization parameters for Vert.x.
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
public class VertxConfigLoader {

	public static final String PREFIX = "kumuluzee.reactive.vertx.";
	
	public static boolean isClustered = false;
	
	public static VertxOptions getVertxOptions() {
		VertxOptions vertxOptions = new VertxOptions();		
		
		ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();
		
		boolean clustered = configurationUtil.getBoolean(PREFIX + "clustered")
				.orElse(VertxOptions.DEFAULT_CLUSTERED);
		long blockedThreadCheckInterval = configurationUtil.getLong(PREFIX + "blocked-thread-check-interval")
				.orElse(VertxOptions.DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL);
		boolean fileResolverCachingEnabled = configurationUtil.getBoolean(PREFIX + "file-caching-enabled")
				.orElse(VertxOptions.DEFAULT_FILE_CACHING_ENABLED);
		boolean haEnabled = configurationUtil.getBoolean(PREFIX + "ha-enabled")
				.orElse(VertxOptions.DEFAULT_HA_ENABLED);
		String haGroup = configurationUtil.get(PREFIX + "ha-group")
				.orElse(VertxOptions.DEFAULT_HA_GROUP);
		int internalBlockingPoolSize = configurationUtil.getInteger(PREFIX + "internal-blocking-pool-size")
				.orElse(VertxOptions.DEFAULT_INTERNAL_BLOCKING_POOL_SIZE);
		long maxEventLoopExecuteTime = configurationUtil.getLong(PREFIX + "max-event-loop-execute-time")
				.orElse(VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME);
		long maxWorkerExecuteTime = configurationUtil.getLong(PREFIX + "max-worker-execute-time")
				.orElse(VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME);
		int quorumSize = configurationUtil.getInteger(PREFIX + "quorum-size")
				.orElse(VertxOptions.DEFAULT_QUORUM_SIZE);
		int workerPoolSize = configurationUtil.getInteger(PREFIX + "worker-pool-size")
				.orElse(VertxOptions.DEFAULT_WORKER_POOL_SIZE);
		
		
		vertxOptions.setFileResolverCachingEnabled(fileResolverCachingEnabled)
					.setHAEnabled(haEnabled)
					.setHAGroup(haGroup)
					.setInternalBlockingPoolSize(internalBlockingPoolSize)
					.setMaxEventLoopExecuteTime(maxEventLoopExecuteTime)
					.setMaxWorkerExecuteTime(maxWorkerExecuteTime)
					.setQuorumSize(quorumSize)
					.setWorkerPoolSize(workerPoolSize)
					.setBlockedThreadCheckInterval(blockedThreadCheckInterval);
		
		if(clustered) {
			isClustered = true;
			
			String clusterHost = configurationUtil.get(PREFIX + "cluster-host")
					.orElse(VertxOptions.DEFAULT_CLUSTER_HOST);
			int clusterPort = configurationUtil.getInteger(PREFIX + "cluster-port")
					.orElse(VertxOptions.DEFAULT_CLUSTER_PORT);
			long clusterPingInterval = configurationUtil.getLong(PREFIX + "cluster-ping-interval")
					.orElse(VertxOptions.DEFAULT_CLUSTER_PING_INTERVAL);
			long clusterPingReplyInterval = configurationUtil.getLong(PREFIX + "cluster-ping-reply-interval")
					.orElse(VertxOptions.DEFAULT_CLUSTER_PING_REPLY_INTERVAL);
			String clusterPublicHost = configurationUtil.get(PREFIX + "cluster-public-host")
					.orElse(VertxOptions.DEFAULT_CLUSTER_PUBLIC_HOST);
			int clusterPublicPort = configurationUtil.getInteger(PREFIX + "cluster-public-port")
					.orElse(VertxOptions.DEFAULT_CLUSTER_PUBLIC_PORT);	
			
			ClusterManager mgr = new HazelcastClusterManager();
			vertxOptions.setClusterManager(mgr)
						.setClusterHost(clusterHost)
						.setClusterPort(clusterPort)
						.setClusterPingInterval(clusterPingInterval)
						.setClusterPingReplyInterval(clusterPingReplyInterval);

			if(clusterPublicHost != null) {
				vertxOptions.setClusterPublicHost(clusterPublicHost);
			}
			
			if(clusterPublicPort >= 0) {
				vertxOptions.setClusterPublicPort(clusterPublicPort);
			}
			
		}
		
		return vertxOptions;
	}	
}
