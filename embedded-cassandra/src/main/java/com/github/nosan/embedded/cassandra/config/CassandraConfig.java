/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An Embedded Cassandra config.
 *
 * @author Dmytro Nosan
 */
public class CassandraConfig {

	private String cluster_name = "Test Cluster";

	private String authenticator = "AllowAllAuthenticator";

	private String authorizer = "AllowAllAuthorizer";

	private String role_manager = "CassandraRoleManager";

	private Integer permissions_validity_in_ms = 2000;

	private Integer permissions_cache_max_entries;

	private Integer permissions_update_interval_in_ms;

	private Integer roles_validity_in_ms = 2000;

	private Integer roles_cache_max_entries;

	private Integer roles_update_interval_in_ms;

	private Integer credentials_validity_in_ms = 2000;

	private Integer credentials_cache_max_entries;

	private Integer credentials_update_interval_in_ms;

	private String partitioner = "org.apache.cassandra.dht.Murmur3Partitioner";

	private boolean auto_bootstrap;

	private boolean hinted_handoff_enabled = true;

	private List<String> hinted_handoff_disabled_datacenters = new ArrayList<>();

	private Integer max_hint_window_in_ms = 10800000;

	private String hints_directory;

	private ParameterizedClass seed_provider = new ParameterizedClass(
			"org.apache.cassandra.locator.SimpleSeedProvider",
			Collections.singletonMap("seeds", "127.0.01"));

	private DiskAccessMode disk_access_mode = DiskAccessMode.auto;

	private DiskFailurePolicy disk_failure_policy = DiskFailurePolicy.stop;

	private CommitFailurePolicy commit_failure_policy = CommitFailurePolicy.stop;

	private String initial_token;

	private Integer num_tokens = 256;

	private String allocate_tokens_for_keyspace;

	private Long request_timeout_in_ms = 10000L;

	private Long read_request_timeout_in_ms = 5000L;

	private Long range_request_timeout_in_ms = 10000L;

	private Long write_request_timeout_in_ms = 2000L;

	private Long counter_write_request_timeout_in_ms = 5000L;

	private Long cas_contention_timeout_in_ms = 1000L;

	private Long truncate_request_timeout_in_ms = 60000L;

	private Integer streaming_keep_alive_period_in_secs;

	private boolean cross_node_timeout = false;

	private Long slow_query_log_timeout_in_ms = 500L;

	private Double phi_convict_threshold;

	private Integer concurrent_reads = 32;

	private Integer concurrent_writes = 32;

	private Integer concurrent_counter_writes = 32;

	private Integer concurrent_materialized_view_writes = 32;

	private Integer memtable_flush_writers;

	private Integer memtable_heap_space_in_mb;

	private Integer memtable_offheap_space_in_mb;

	private Float memtable_cleanup_threshold;

	private int storage_port = 7000;

	private int ssl_storage_port = 7001;

	private String listen_address = "localhost";

	private String listen_interface;

	private boolean listen_interface_prefer_ipv6;

	private String broadcast_address;

	private boolean listen_on_broadcast_address;

	private String internode_authenticator;

	private boolean start_rpc = false;

	private String rpc_address = "localhost";

	private String rpc_interface;

	private boolean rpc_interface_prefer_ipv6;

	private String broadcast_rpc_address;

	private int rpc_port = 9160;

	private Integer rpc_listen_backlog;

	private String rpc_server_type = "sync";

	private boolean rpc_keepalive = true;

	private Integer rpc_min_threads;

	private Integer rpc_send_buff_size_in_bytes;

	private Integer rpc_recv_buff_size_in_bytes;

	private Integer internode_send_buff_size_in_bytes;

	private Integer internode_recv_buff_size_in_bytes;

	private boolean start_native_transport = true;

	private int native_transport_port = 9042;

	private Integer native_transport_port_ssl;

	private Integer native_transport_max_threads;

	private Integer native_transport_max_frame_size_in_mb;

	private Long native_transport_max_concurrent_connections;

	private Long native_transport_max_concurrent_connections_per_ip;

	private Integer max_value_size_in_mb;

	private Integer thrift_framed_transport_size_in_mb;

	private boolean snapshot_before_compaction = false;

	private boolean auto_snapshot = true;

	private Integer column_index_size_in_kb = 64;

	private Integer column_index_cache_size_in_kb = 2;

	private Integer batch_size_warn_threshold_in_kb = 5;

	private Integer batch_size_fail_threshold_in_kb = 50;

	private Integer unlogged_batch_across_partitions_warn_threshold = 10;

	private Integer concurrent_compactors;

	private Integer compaction_throughput_mb_per_sec = 16;

	private Integer compaction_large_partition_warning_threshold_mb = 100;

	private Integer min_free_space_per_drive_in_mb;

	private Integer stream_throughput_outbound_megabits_per_sec;

	private Integer inter_dc_stream_throughput_outbound_megabits_per_sec;

	private List<String> data_file_directories = new ArrayList<>();

	private String saved_caches_directory;

	private String commitlog_directory;

	private Integer commitlog_total_space_in_mb;

	private CommitLogSync commitlog_sync = CommitLogSync.periodic;

	private Double commitlog_sync_batch_window_in_ms;

	private Integer commitlog_sync_period_in_ms = 10000;

	private Long commitlog_segment_size_in_mb = 32L;

	private ParameterizedClass commitlog_compression;

	private Integer commitlog_max_compression_buffers_in_pool;

	private TransparentDataEncryptionOptions transparent_data_encryption_options;

	private Integer max_mutation_size_in_kb;

	private boolean cdc_enabled = false;

	private String cdc_raw_directory;

	private Integer cdc_total_space_in_mb;

	private Integer cdc_free_space_check_interval_ms;

	private String endpoint_snitch = "SimpleSnitch";

	private boolean dynamic_snitch;

	private Integer dynamic_snitch_update_interval_in_ms = 100;

	private Integer dynamic_snitch_reset_interval_in_ms = 600000;

	private Double dynamic_snitch_badness_threshold = 0.1;

	private String request_scheduler = "org.apache.cassandra.scheduler.NoScheduler";

	private RequestSchedulerId request_scheduler_id;

	private RequestSchedulerOptions request_scheduler_options;

	private ServerEncryptionOptions server_encryption_options;

	private ClientEncryptionOptions client_encryption_options;

	private InternodeCompression internode_compression = InternodeCompression.dc;

	private Integer hinted_handoff_throttle_in_kb = 1024;

	private Integer batchlog_replay_throttle_in_kb = 1024;

	private Integer max_hints_delivery_threads = 2;

	private Integer hints_flush_period_in_ms = 10000;

	private Integer max_hints_file_size_in_mb = 128;

	private ParameterizedClass hints_compression;

	private Integer sstable_preemptive_open_interval_in_mb = 50;

	private boolean incremental_backups = false;

	private boolean trickle_fsync = false;

	private Integer trickle_fsync_interval_in_kb = 10240;

	private Long key_cache_size_in_mb;

	private Integer key_cache_save_period = 14400;

	private Integer key_cache_keys_to_save;

	private String row_cache_class_name;

	private Long row_cache_size_in_mb;

	private Integer row_cache_save_period;

	private Integer row_cache_keys_to_save;

	private Long counter_cache_size_in_mb;

	private Integer counter_cache_save_period = 7200;

	private Integer counter_cache_keys_to_save;

	private Integer file_cache_size_in_mb;

	private boolean file_cache_round_up;

	private boolean buffer_pool_use_heap_if_exhausted;

	private DiskOptimizationStrategy disk_optimization_strategy;

	private Double disk_optimization_estimate_percentile;

	private Double disk_optimization_page_cross_chance;

	private boolean inter_dc_tcp_nodelay = false;

	private MemtableAllocationType memtable_allocation_type = MemtableAllocationType.heap_buffers;

	private Integer tombstone_warn_threshold = 1000;

	private Integer tombstone_failure_threshold = 100000;

	private Long index_summary_capacity_in_mb;

	private Integer index_summary_resize_interval_in_minutes = 60;

	private Integer gc_log_threshold_in_ms;

	private Integer gc_warn_threshold_in_ms = 1000;

	private Integer tracetype_query_ttl = 86400;

	private Integer tracetype_repair_ttl = 604800;

	private String otc_coalescing_strategy;

	private Integer otc_coalescing_window_us;

	private Integer otc_coalescing_enough_coalesced_messages;

	private Integer otc_backlog_expiration_interval_ms;

	private Integer windows_timer_interval = 1;

	private Long prepared_statements_cache_size_mb;

	private Long thrift_prepared_statements_cache_size_mb;

	private boolean enable_user_defined_functions = false;

	private boolean enable_scripted_user_defined_functions = false;

	private boolean enable_materialized_views = true;

	private boolean enable_user_defined_functions_threads;

	private Long user_defined_function_warn_timeout;

	private Long user_defined_function_fail_timeout;

	private UserFunctionTimeoutPolicy user_function_timeout_policy;

	private boolean back_pressure_enabled = false;

	private ParameterizedClass back_pressure_strategy;

	public String getClusterName() {
		return this.cluster_name;
	}

	public void setClusterName(String clusterName) {
		this.cluster_name = clusterName;
	}

	public String getAuthenticator() {
		return this.authenticator;
	}

	public void setAuthenticator(String authenticator) {
		this.authenticator = authenticator;
	}

	public String getAuthorizer() {
		return this.authorizer;
	}

	public void setAuthorizer(String authorizer) {
		this.authorizer = authorizer;
	}

	public String getRoleManager() {
		return this.role_manager;
	}

	public void setRoleManager(String roleManager) {
		this.role_manager = roleManager;
	}

	public Integer getPermissionsValidityInMs() {
		return this.permissions_validity_in_ms;
	}

	public void setPermissionsValidityInMs(Integer permissionsValidityInMs) {
		this.permissions_validity_in_ms = permissionsValidityInMs;
	}

	public Integer getPermissionsCacheMaxEntries() {
		return this.permissions_cache_max_entries;
	}

	public void setPermissionsCacheMaxEntries(Integer permissionsCacheMaxEntries) {
		this.permissions_cache_max_entries = permissionsCacheMaxEntries;
	}

	public Integer getPermissionsUpdateIntervalInMs() {
		return this.permissions_update_interval_in_ms;
	}

	public void setPermissionsUpdateIntervalInMs(Integer permissionsUpdateIntervalInMs) {
		this.permissions_update_interval_in_ms = permissionsUpdateIntervalInMs;
	}

	public Integer getRolesValidityInMs() {
		return this.roles_validity_in_ms;
	}

	public void setRolesValidityInMs(Integer rolesValidityInMs) {
		this.roles_validity_in_ms = rolesValidityInMs;
	}

	public Integer getRolesCacheMaxEntries() {
		return this.roles_cache_max_entries;
	}

	public void setRolesCacheMaxEntries(Integer rolesCacheMaxEntries) {
		this.roles_cache_max_entries = rolesCacheMaxEntries;
	}

	public Integer getRolesUpdateIntervalInMs() {
		return this.roles_update_interval_in_ms;
	}

	public void setRolesUpdateIntervalInMs(Integer rolesUpdateIntervalInMs) {
		this.roles_update_interval_in_ms = rolesUpdateIntervalInMs;
	}

	public Integer getCredentialsValidityInMs() {
		return this.credentials_validity_in_ms;
	}

	public void setCredentialsValidityInMs(Integer credentialsValidityInMs) {
		this.credentials_validity_in_ms = credentialsValidityInMs;
	}

	public Integer getCredentialsCacheMaxEntries() {
		return this.credentials_cache_max_entries;
	}

	public void setCredentialsCacheMaxEntries(Integer credentialsCacheMaxEntries) {
		this.credentials_cache_max_entries = credentialsCacheMaxEntries;
	}

	public Integer getCredentialsUpdateIntervalInMs() {
		return this.credentials_update_interval_in_ms;
	}

	public void setCredentialsUpdateIntervalInMs(Integer credentialsUpdateIntervalInMs) {
		this.credentials_update_interval_in_ms = credentialsUpdateIntervalInMs;
	}

	public String getPartitioner() {
		return this.partitioner;
	}

	public void setPartitioner(String partitioner) {
		this.partitioner = partitioner;
	}

	public boolean isAutoBootstrap() {
		return this.auto_bootstrap;
	}

	public void setAutoBootstrap(boolean autoBootstrap) {
		this.auto_bootstrap = autoBootstrap;
	}

	public boolean isHintedHandoffEnabled() {
		return this.hinted_handoff_enabled;
	}

	public void setHintedHandoffEnabled(boolean hintedHandoffEnabled) {
		this.hinted_handoff_enabled = hintedHandoffEnabled;
	}

	public List<String> getHintedHandoffDisabledDatacenters() {
		return this.hinted_handoff_disabled_datacenters;
	}

	public void setHintedHandoffDisabledDatacenters(
			List<String> hintedHandoffDisabledDatacenters) {
		this.hinted_handoff_disabled_datacenters = hintedHandoffDisabledDatacenters;
	}

	public Integer getMaxHintWindowInMs() {
		return this.max_hint_window_in_ms;
	}

	public void setMaxHintWindowInMs(Integer maxHintWindowInMs) {
		this.max_hint_window_in_ms = maxHintWindowInMs;
	}

	public String getHintsDirectory() {
		return this.hints_directory;
	}

	public void setHintsDirectory(String hintsDirectory) {
		this.hints_directory = hintsDirectory;
	}

	public ParameterizedClass getSeedProvider() {
		return this.seed_provider;
	}

	public void setSeedProvider(ParameterizedClass seedProvider) {
		this.seed_provider = seedProvider;
	}

	public DiskAccessMode getDiskAccessMode() {
		return this.disk_access_mode;
	}

	public void setDiskAccessMode(DiskAccessMode diskAccessMode) {
		this.disk_access_mode = diskAccessMode;
	}

	public DiskFailurePolicy getDiskFailurePolicy() {
		return this.disk_failure_policy;
	}

	public void setDiskFailurePolicy(DiskFailurePolicy diskFailurePolicy) {
		this.disk_failure_policy = diskFailurePolicy;
	}

	public CommitFailurePolicy getCommitFailurePolicy() {
		return this.commit_failure_policy;
	}

	public void setCommitFailurePolicy(CommitFailurePolicy commitFailurePolicy) {
		this.commit_failure_policy = commitFailurePolicy;
	}

	public String getInitialToken() {
		return this.initial_token;
	}

	public void setInitialToken(String initialToken) {
		this.initial_token = initialToken;
	}

	public Integer getNumTokens() {
		return this.num_tokens;
	}

	public void setNumTokens(Integer numTokens) {
		this.num_tokens = numTokens;
	}

	public String getAllocateTokensForKeyspace() {
		return this.allocate_tokens_for_keyspace;
	}

	public void setAllocateTokensForKeyspace(String allocateTokensForKeyspace) {
		this.allocate_tokens_for_keyspace = allocateTokensForKeyspace;
	}

	public Long getRequestTimeoutInMs() {
		return this.request_timeout_in_ms;
	}

	public void setRequestTimeoutInMs(Long requestTimeoutInMs) {
		this.request_timeout_in_ms = requestTimeoutInMs;
	}

	public Long getReadRequestTimeoutInMs() {
		return this.read_request_timeout_in_ms;
	}

	public void setReadRequestTimeoutInMs(Long readRequestTimeoutInMs) {
		this.read_request_timeout_in_ms = readRequestTimeoutInMs;
	}

	public Long getRangeRequestTimeoutInMs() {
		return this.range_request_timeout_in_ms;
	}

	public void setRangeRequestTimeoutInMs(Long rangeRequestTimeoutInMs) {
		this.range_request_timeout_in_ms = rangeRequestTimeoutInMs;
	}

	public Long getWriteRequestTimeoutInMs() {
		return this.write_request_timeout_in_ms;
	}

	public void setWriteRequestTimeoutInMs(Long writeRequestTimeoutInMs) {
		this.write_request_timeout_in_ms = writeRequestTimeoutInMs;
	}

	public Long getCounterWriteRequestTimeoutInMs() {
		return this.counter_write_request_timeout_in_ms;
	}

	public void setCounterWriteRequestTimeoutInMs(Long counterWriteRequestTimeoutInMs) {
		this.counter_write_request_timeout_in_ms = counterWriteRequestTimeoutInMs;
	}

	public Long getCasContentionTimeoutInMs() {
		return this.cas_contention_timeout_in_ms;
	}

	public void setCasContentionTimeoutInMs(Long casContentionTimeoutInMs) {
		this.cas_contention_timeout_in_ms = casContentionTimeoutInMs;
	}

	public Long getTruncateRequestTimeoutInMs() {
		return this.truncate_request_timeout_in_ms;
	}

	public void setTruncateRequestTimeoutInMs(Long truncateRequestTimeoutInMs) {
		this.truncate_request_timeout_in_ms = truncateRequestTimeoutInMs;
	}

	public Integer getStreamingKeepAlivePeriodInSecs() {
		return this.streaming_keep_alive_period_in_secs;
	}

	public void setStreamingKeepAlivePeriodInSecs(
			Integer streamingKeepAlivePeriodInSecs) {
		this.streaming_keep_alive_period_in_secs = streamingKeepAlivePeriodInSecs;
	}

	public boolean isCrossNodeTimeout() {
		return this.cross_node_timeout;
	}

	public void setCrossNodeTimeout(boolean crossNodeTimeout) {
		this.cross_node_timeout = crossNodeTimeout;
	}

	public Long getSlowQueryLogTimeoutInMs() {
		return this.slow_query_log_timeout_in_ms;
	}

	public void setSlowQueryLogTimeoutInMs(Long slowQueryLogTimeoutInMs) {
		this.slow_query_log_timeout_in_ms = slowQueryLogTimeoutInMs;
	}

	public Double getPhiConvictThreshold() {
		return this.phi_convict_threshold;
	}

	public void setPhiConvictThreshold(Double phiConvictThreshold) {
		this.phi_convict_threshold = phiConvictThreshold;
	}

	public Integer getConcurrentReads() {
		return this.concurrent_reads;
	}

	public void setConcurrentReads(Integer concurrentReads) {
		this.concurrent_reads = concurrentReads;
	}

	public Integer getConcurrentWrites() {
		return this.concurrent_writes;
	}

	public void setConcurrentWrites(Integer concurrentWrites) {
		this.concurrent_writes = concurrentWrites;
	}

	public Integer getConcurrentCounterWrites() {
		return this.concurrent_counter_writes;
	}

	public void setConcurrentCounterWrites(Integer concurrentCounterWrites) {
		this.concurrent_counter_writes = concurrentCounterWrites;
	}

	public Integer getConcurrentMaterializedViewWrites() {
		return this.concurrent_materialized_view_writes;
	}

	public void setConcurrentMaterializedViewWrites(
			Integer concurrentMaterializedViewWrites) {
		this.concurrent_materialized_view_writes = concurrentMaterializedViewWrites;
	}

	public Integer getMemtableFlushWriters() {
		return this.memtable_flush_writers;
	}

	public void setMemtableFlushWriters(Integer memtableFlushWriters) {
		this.memtable_flush_writers = memtableFlushWriters;
	}

	public Integer getMemtableHeapSpaceInMb() {
		return this.memtable_heap_space_in_mb;
	}

	public void setMemtableHeapSpaceInMb(Integer memtableHeapSpaceInMb) {
		this.memtable_heap_space_in_mb = memtableHeapSpaceInMb;
	}

	public Integer getMemtableOffheapSpaceInMb() {
		return this.memtable_offheap_space_in_mb;
	}

	public void setMemtableOffheapSpaceInMb(Integer memtableOffheapSpaceInMb) {
		this.memtable_offheap_space_in_mb = memtableOffheapSpaceInMb;
	}

	public Float getMemtableCleanupThreshold() {
		return this.memtable_cleanup_threshold;
	}

	public void setMemtableCleanupThreshold(Float memtableCleanupThreshold) {
		this.memtable_cleanup_threshold = memtableCleanupThreshold;
	}

	public int getStoragePort() {
		return this.storage_port;
	}

	public void setStoragePort(int storagePort) {
		this.storage_port = storagePort;
	}

	public int getSslStoragePort() {
		return this.ssl_storage_port;
	}

	public void setSslStoragePort(int sslStoragePort) {
		this.ssl_storage_port = sslStoragePort;
	}

	public String getListenAddress() {
		return this.listen_address;
	}

	public void setListenAddress(String listenAddress) {
		this.listen_address = listenAddress;
	}

	public String getListenInterface() {
		return this.listen_interface;
	}

	public void setListenInterface(String listenInterface) {
		this.listen_interface = listenInterface;
	}

	public boolean isListenInterfacePreferIpv6() {
		return this.listen_interface_prefer_ipv6;
	}

	public void setListenInterfacePreferIpv6(boolean listenInterfacePreferIpv6) {
		this.listen_interface_prefer_ipv6 = listenInterfacePreferIpv6;
	}

	public String getBroadcastAddress() {
		return this.broadcast_address;
	}

	public void setBroadcastAddress(String broadcastAddress) {
		this.broadcast_address = broadcastAddress;
	}

	public boolean isListenOnBroadcastAddress() {
		return this.listen_on_broadcast_address;
	}

	public void setListenOnBroadcastAddress(boolean listenOnBroadcastAddress) {
		this.listen_on_broadcast_address = listenOnBroadcastAddress;
	}

	public String getInternodeAuthenticator() {
		return this.internode_authenticator;
	}

	public void setInternodeAuthenticator(String internodeAuthenticator) {
		this.internode_authenticator = internodeAuthenticator;
	}

	public boolean isStartRpc() {
		return this.start_rpc;
	}

	public void setStartRpc(boolean startRpc) {
		this.start_rpc = startRpc;
	}

	public String getRpcAddress() {
		return this.rpc_address;
	}

	public void setRpcAddress(String rpcAddress) {
		this.rpc_address = rpcAddress;
	}

	public String getRpcInterface() {
		return this.rpc_interface;
	}

	public void setRpcInterface(String rpcInterface) {
		this.rpc_interface = rpcInterface;
	}

	public boolean isRpcInterfacePreferIpv6() {
		return this.rpc_interface_prefer_ipv6;
	}

	public void setRpcInterfacePreferIpv6(boolean rpcInterfacePreferIpv6) {
		this.rpc_interface_prefer_ipv6 = rpcInterfacePreferIpv6;
	}

	public String getBroadcastRpcAddress() {
		return this.broadcast_rpc_address;
	}

	public void setBroadcastRpcAddress(String broadcastRpcAddress) {
		this.broadcast_rpc_address = broadcastRpcAddress;
	}

	public int getRpcPort() {
		return this.rpc_port;
	}

	public void setRpcPort(int rpcPort) {
		this.rpc_port = rpcPort;
	}

	public Integer getRpcListenBacklog() {
		return this.rpc_listen_backlog;
	}

	public void setRpcListenBacklog(Integer rpcListenBacklog) {
		this.rpc_listen_backlog = rpcListenBacklog;
	}

	public String getRpcServerType() {
		return this.rpc_server_type;
	}

	public void setRpcServerType(String rpcServerType) {
		this.rpc_server_type = rpcServerType;
	}

	public boolean isRpcKeepalive() {
		return this.rpc_keepalive;
	}

	public void setRpcKeepalive(boolean rpcKeepalive) {
		this.rpc_keepalive = rpcKeepalive;
	}

	public Integer getRpcMinThreads() {
		return this.rpc_min_threads;
	}

	public void setRpcMinThreads(Integer rpcMinThreads) {
		this.rpc_min_threads = rpcMinThreads;
	}

	public Integer getRpcSendBuffSizeInBytes() {
		return this.rpc_send_buff_size_in_bytes;
	}

	public void setRpcSendBuffSizeInBytes(Integer rpcSendBuffSizeInBytes) {
		this.rpc_send_buff_size_in_bytes = rpcSendBuffSizeInBytes;
	}

	public Integer getRpcRecvBuffSizeInBytes() {
		return this.rpc_recv_buff_size_in_bytes;
	}

	public void setRpcRecvBuffSizeInBytes(Integer rpcRecvBuffSizeInBytes) {
		this.rpc_recv_buff_size_in_bytes = rpcRecvBuffSizeInBytes;
	}

	public Integer getInternodeSendBuffSizeInBytes() {
		return this.internode_send_buff_size_in_bytes;
	}

	public void setInternodeSendBuffSizeInBytes(Integer internodeSendBuffSizeInBytes) {
		this.internode_send_buff_size_in_bytes = internodeSendBuffSizeInBytes;
	}

	public Integer getInternodeRecvBuffSizeInBytes() {
		return this.internode_recv_buff_size_in_bytes;
	}

	public void setInternodeRecvBuffSizeInBytes(Integer internodeRecvBuffSizeInBytes) {
		this.internode_recv_buff_size_in_bytes = internodeRecvBuffSizeInBytes;
	}

	public boolean isStartNativeTransport() {
		return this.start_native_transport;
	}

	public void setStartNativeTransport(boolean startNativeTransport) {
		this.start_native_transport = startNativeTransport;
	}

	public int getNativeTransportPort() {
		return this.native_transport_port;
	}

	public void setNativeTransportPort(int nativeTransportPort) {
		this.native_transport_port = nativeTransportPort;
	}

	public Integer getNativeTransportPortSsl() {
		return this.native_transport_port_ssl;
	}

	public void setNativeTransportPortSsl(Integer nativeTransportPortSsl) {
		this.native_transport_port_ssl = nativeTransportPortSsl;
	}

	public Integer getNativeTransportMaxThreads() {
		return this.native_transport_max_threads;
	}

	public void setNativeTransportMaxThreads(Integer nativeTransportMaxThreads) {
		this.native_transport_max_threads = nativeTransportMaxThreads;
	}

	public Integer getNativeTransportMaxFrameSizeInMb() {
		return this.native_transport_max_frame_size_in_mb;
	}

	public void setNativeTransportMaxFrameSizeInMb(
			Integer nativeTransportMaxFrameSizeInMb) {
		this.native_transport_max_frame_size_in_mb = nativeTransportMaxFrameSizeInMb;
	}

	public Long getNativeTransportMaxConcurrentConnections() {
		return this.native_transport_max_concurrent_connections;
	}

	public void setNativeTransportMaxConcurrentConnections(
			Long nativeTransportMaxConcurrentConnections) {
		this.native_transport_max_concurrent_connections = nativeTransportMaxConcurrentConnections;
	}

	public Long getNativeTransportMaxConcurrentConnectionsPerIp() {
		return this.native_transport_max_concurrent_connections_per_ip;
	}

	public void setNativeTransportMaxConcurrentConnectionsPerIp(
			Long nativeTransportMaxConcurrentConnectionsPerIp) {
		this.native_transport_max_concurrent_connections_per_ip = nativeTransportMaxConcurrentConnectionsPerIp;
	}

	public Integer getMaxValueSizeInMb() {
		return this.max_value_size_in_mb;
	}

	public void setMaxValueSizeInMb(Integer maxValueSizeInMb) {
		this.max_value_size_in_mb = maxValueSizeInMb;
	}

	public Integer getThriftFramedTransportSizeInMb() {
		return this.thrift_framed_transport_size_in_mb;
	}

	public void setThriftFramedTransportSizeInMb(Integer thriftFramedTransportSizeInMb) {
		this.thrift_framed_transport_size_in_mb = thriftFramedTransportSizeInMb;
	}

	public boolean isSnapshotBeforeCompaction() {
		return this.snapshot_before_compaction;
	}

	public void setSnapshotBeforeCompaction(boolean snapshotBeforeCompaction) {
		this.snapshot_before_compaction = snapshotBeforeCompaction;
	}

	public boolean isAutoSnapshot() {
		return this.auto_snapshot;
	}

	public void setAutoSnapshot(boolean autoSnapshot) {
		this.auto_snapshot = autoSnapshot;
	}

	public Integer getColumnIndexSizeInKb() {
		return this.column_index_size_in_kb;
	}

	public void setColumnIndexSizeInKb(Integer columnIndexSizeInKb) {
		this.column_index_size_in_kb = columnIndexSizeInKb;
	}

	public Integer getColumnIndexCacheSizeInKb() {
		return this.column_index_cache_size_in_kb;
	}

	public void setColumnIndexCacheSizeInKb(Integer columnIndexCacheSizeInKb) {
		this.column_index_cache_size_in_kb = columnIndexCacheSizeInKb;
	}

	public Integer getBatchSizeWarnThresholdInKb() {
		return this.batch_size_warn_threshold_in_kb;
	}

	public void setBatchSizeWarnThresholdInKb(Integer batchSizeWarnThresholdInKb) {
		this.batch_size_warn_threshold_in_kb = batchSizeWarnThresholdInKb;
	}

	public Integer getBatchSizeFailThresholdInKb() {
		return this.batch_size_fail_threshold_in_kb;
	}

	public void setBatchSizeFailThresholdInKb(Integer batchSizeFailThresholdInKb) {
		this.batch_size_fail_threshold_in_kb = batchSizeFailThresholdInKb;
	}

	public Integer getUnloggedBatchAcrossPartitionsWarnThreshold() {
		return this.unlogged_batch_across_partitions_warn_threshold;
	}

	public void setUnloggedBatchAcrossPartitionsWarnThreshold(
			Integer unloggedBatchAcrossPartitionsWarnThreshold) {
		this.unlogged_batch_across_partitions_warn_threshold = unloggedBatchAcrossPartitionsWarnThreshold;
	}

	public Integer getConcurrentCompactors() {
		return this.concurrent_compactors;
	}

	public void setConcurrentCompactors(Integer concurrentCompactors) {
		this.concurrent_compactors = concurrentCompactors;
	}

	public Integer getCompactionThroughputMbPerSec() {
		return this.compaction_throughput_mb_per_sec;
	}

	public void setCompactionThroughputMbPerSec(Integer compactionThroughputMbPerSec) {
		this.compaction_throughput_mb_per_sec = compactionThroughputMbPerSec;
	}

	public Integer getCompactionLargePartitionWarningThresholdMb() {
		return this.compaction_large_partition_warning_threshold_mb;
	}

	public void setCompactionLargePartitionWarningThresholdMb(
			Integer compactionLargePartitionWarningThresholdMb) {
		this.compaction_large_partition_warning_threshold_mb = compactionLargePartitionWarningThresholdMb;
	}

	public Integer getMinFreeSpacePerDriveInMb() {
		return this.min_free_space_per_drive_in_mb;
	}

	public void setMinFreeSpacePerDriveInMb(Integer minFreeSpacePerDriveInMb) {
		this.min_free_space_per_drive_in_mb = minFreeSpacePerDriveInMb;
	}

	public Integer getStreamThroughputOutboundMegabitsPerSec() {
		return this.stream_throughput_outbound_megabits_per_sec;
	}

	public void setStreamThroughputOutboundMegabitsPerSec(
			Integer streamThroughputOutboundMegabitsPerSec) {
		this.stream_throughput_outbound_megabits_per_sec = streamThroughputOutboundMegabitsPerSec;
	}

	public Integer getInterDcStreamThroughputOutboundMegabitsPerSec() {
		return this.inter_dc_stream_throughput_outbound_megabits_per_sec;
	}

	public void setInterDcStreamThroughputOutboundMegabitsPerSec(
			Integer interDcStreamThroughputOutboundMegabitsPerSec) {
		this.inter_dc_stream_throughput_outbound_megabits_per_sec = interDcStreamThroughputOutboundMegabitsPerSec;
	}

	public List<String> getDataFileDirectories() {
		return this.data_file_directories;
	}

	public void setDataFileDirectories(List<String> dataFileDirectories) {
		this.data_file_directories = dataFileDirectories;
	}

	public String getSavedCachesDirectory() {
		return this.saved_caches_directory;
	}

	public void setSavedCachesDirectory(String savedCachesDirectory) {
		this.saved_caches_directory = savedCachesDirectory;
	}

	public String getCommitlogDirectory() {
		return this.commitlog_directory;
	}

	public void setCommitlogDirectory(String commitlogDirectory) {
		this.commitlog_directory = commitlogDirectory;
	}

	public Integer getCommitlogTotalSpaceInMb() {
		return this.commitlog_total_space_in_mb;
	}

	public void setCommitlogTotalSpaceInMb(Integer commitlogTotalSpaceInMb) {
		this.commitlog_total_space_in_mb = commitlogTotalSpaceInMb;
	}

	public CommitLogSync getCommitlogSync() {
		return this.commitlog_sync;
	}

	public void setCommitlogSync(CommitLogSync commitlogSync) {
		this.commitlog_sync = commitlogSync;
	}

	public Double getCommitlogSyncBatchWindowInMs() {
		return this.commitlog_sync_batch_window_in_ms;
	}

	public void setCommitlogSyncBatchWindowInMs(Double commitlogSyncBatchWindowInMs) {
		this.commitlog_sync_batch_window_in_ms = commitlogSyncBatchWindowInMs;
	}

	public Integer getCommitlogSyncPeriodInMs() {
		return this.commitlog_sync_period_in_ms;
	}

	public void setCommitlogSyncPeriodInMs(Integer commitlogSyncPeriodInMs) {
		this.commitlog_sync_period_in_ms = commitlogSyncPeriodInMs;
	}

	public Long getCommitlogSegmentSizeInMb() {
		return this.commitlog_segment_size_in_mb;
	}

	public void setCommitlogSegmentSizeInMb(Long commitlogSegmentSizeInMb) {
		this.commitlog_segment_size_in_mb = commitlogSegmentSizeInMb;
	}

	public ParameterizedClass getCommitlogCompression() {
		return this.commitlog_compression;
	}

	public void setCommitlogCompression(ParameterizedClass commitlogCompression) {
		this.commitlog_compression = commitlogCompression;
	}

	public Integer getCommitlogMaxCompressionBuffersInPool() {
		return this.commitlog_max_compression_buffers_in_pool;
	}

	public void setCommitlogMaxCompressionBuffersInPool(
			Integer commitlogMaxCompressionBuffersInPool) {
		this.commitlog_max_compression_buffers_in_pool = commitlogMaxCompressionBuffersInPool;
	}

	public TransparentDataEncryptionOptions getTransparentDataEncryptionOptions() {
		return this.transparent_data_encryption_options;
	}

	public void setTransparentDataEncryptionOptions(
			TransparentDataEncryptionOptions transparentDataEncryptionOptions) {
		this.transparent_data_encryption_options = transparentDataEncryptionOptions;
	}

	public Integer getMaxMutationSizeInKb() {
		return this.max_mutation_size_in_kb;
	}

	public void setMaxMutationSizeInKb(Integer maxMutationSizeInKb) {
		this.max_mutation_size_in_kb = maxMutationSizeInKb;
	}

	public boolean isCdcEnabled() {
		return this.cdc_enabled;
	}

	public void setCdcEnabled(boolean cdcEnabled) {
		this.cdc_enabled = cdcEnabled;
	}

	public String getCdcRawDirectory() {
		return this.cdc_raw_directory;
	}

	public void setCdcRawDirectory(String cdcRawDirectory) {
		this.cdc_raw_directory = cdcRawDirectory;
	}

	public Integer getCdcTotalSpaceInMb() {
		return this.cdc_total_space_in_mb;
	}

	public void setCdcTotalSpaceInMb(Integer cdcTotalSpaceInMb) {
		this.cdc_total_space_in_mb = cdcTotalSpaceInMb;
	}

	public Integer getCdcFreeSpaceCheckIntervalMs() {
		return this.cdc_free_space_check_interval_ms;
	}

	public void setCdcFreeSpaceCheckIntervalMs(Integer cdcFreeSpaceCheckIntervalMs) {
		this.cdc_free_space_check_interval_ms = cdcFreeSpaceCheckIntervalMs;
	}

	public String getEndpointSnitch() {
		return this.endpoint_snitch;
	}

	public void setEndpointSnitch(String endpointSnitch) {
		this.endpoint_snitch = endpointSnitch;
	}

	public boolean isDynamicSnitch() {
		return this.dynamic_snitch;
	}

	public void setDynamicSnitch(boolean dynamicSnitch) {
		this.dynamic_snitch = dynamicSnitch;
	}

	public Integer getDynamicSnitchUpdateIntervalInMs() {
		return this.dynamic_snitch_update_interval_in_ms;
	}

	public void setDynamicSnitchUpdateIntervalInMs(
			Integer dynamicSnitchUpdateIntervalInMs) {
		this.dynamic_snitch_update_interval_in_ms = dynamicSnitchUpdateIntervalInMs;
	}

	public Integer getDynamicSnitchResetIntervalInMs() {
		return this.dynamic_snitch_reset_interval_in_ms;
	}

	public void setDynamicSnitchResetIntervalInMs(
			Integer dynamicSnitchResetIntervalInMs) {
		this.dynamic_snitch_reset_interval_in_ms = dynamicSnitchResetIntervalInMs;
	}

	public Double getDynamicSnitchBadnessThreshold() {
		return this.dynamic_snitch_badness_threshold;
	}

	public void setDynamicSnitchBadnessThreshold(Double dynamicSnitchBadnessThreshold) {
		this.dynamic_snitch_badness_threshold = dynamicSnitchBadnessThreshold;
	}

	public String getRequestScheduler() {
		return this.request_scheduler;
	}

	public void setRequestScheduler(String requestScheduler) {
		this.request_scheduler = requestScheduler;
	}

	public RequestSchedulerId getRequestSchedulerId() {
		return this.request_scheduler_id;
	}

	public void setRequestSchedulerId(RequestSchedulerId requestSchedulerId) {
		this.request_scheduler_id = requestSchedulerId;
	}

	public RequestSchedulerOptions getRequestSchedulerOptions() {
		return this.request_scheduler_options;
	}

	public void setRequestSchedulerOptions(
			RequestSchedulerOptions requestSchedulerOptions) {
		this.request_scheduler_options = requestSchedulerOptions;
	}

	public ServerEncryptionOptions getServerEncryptionOptions() {
		return this.server_encryption_options;
	}

	public void setServerEncryptionOptions(
			ServerEncryptionOptions serverEncryptionOptions) {
		this.server_encryption_options = serverEncryptionOptions;
	}

	public ClientEncryptionOptions getClientEncryptionOptions() {
		return this.client_encryption_options;
	}

	public void setClientEncryptionOptions(
			ClientEncryptionOptions clientEncryptionOptions) {
		this.client_encryption_options = clientEncryptionOptions;
	}

	public InternodeCompression getInternodeCompression() {
		return this.internode_compression;
	}

	public void setInternodeCompression(InternodeCompression internodeCompression) {
		this.internode_compression = internodeCompression;
	}

	public Integer getHintedHandoffThrottleInKb() {
		return this.hinted_handoff_throttle_in_kb;
	}

	public void setHintedHandoffThrottleInKb(Integer hintedHandoffThrottleInKb) {
		this.hinted_handoff_throttle_in_kb = hintedHandoffThrottleInKb;
	}

	public Integer getBatchlogReplayThrottleInKb() {
		return this.batchlog_replay_throttle_in_kb;
	}

	public void setBatchlogReplayThrottleInKb(Integer batchlogReplayThrottleInKb) {
		this.batchlog_replay_throttle_in_kb = batchlogReplayThrottleInKb;
	}

	public Integer getMaxHintsDeliveryThreads() {
		return this.max_hints_delivery_threads;
	}

	public void setMaxHintsDeliveryThreads(Integer maxHintsDeliveryThreads) {
		this.max_hints_delivery_threads = maxHintsDeliveryThreads;
	}

	public Integer getHintsFlushPeriodInMs() {
		return this.hints_flush_period_in_ms;
	}

	public void setHintsFlushPeriodInMs(Integer hintsFlushPeriodInMs) {
		this.hints_flush_period_in_ms = hintsFlushPeriodInMs;
	}

	public Integer getMaxHintsFileSizeInMb() {
		return this.max_hints_file_size_in_mb;
	}

	public void setMaxHintsFileSizeInMb(Integer maxHintsFileSizeInMb) {
		this.max_hints_file_size_in_mb = maxHintsFileSizeInMb;
	}

	public ParameterizedClass getHintsCompression() {
		return this.hints_compression;
	}

	public void setHintsCompression(ParameterizedClass hintsCompression) {
		this.hints_compression = hintsCompression;
	}

	public Integer getSstablePreemptiveOpenIntervalInMb() {
		return this.sstable_preemptive_open_interval_in_mb;
	}

	public void setSstablePreemptiveOpenIntervalInMb(
			Integer sstablePreemptiveOpenIntervalInMb) {
		this.sstable_preemptive_open_interval_in_mb = sstablePreemptiveOpenIntervalInMb;
	}

	public boolean isIncrementalBackups() {
		return this.incremental_backups;
	}

	public void setIncrementalBackups(boolean incrementalBackups) {
		this.incremental_backups = incrementalBackups;
	}

	public boolean isTrickleFsync() {
		return this.trickle_fsync;
	}

	public void setTrickleFsync(boolean trickleFsync) {
		this.trickle_fsync = trickleFsync;
	}

	public Integer getTrickleFsyncIntervalInKb() {
		return this.trickle_fsync_interval_in_kb;
	}

	public void setTrickleFsyncIntervalInKb(Integer trickleFsyncIntervalInKb) {
		this.trickle_fsync_interval_in_kb = trickleFsyncIntervalInKb;
	}

	public Long getKeyCacheSizeInMb() {
		return this.key_cache_size_in_mb;
	}

	public void setKeyCacheSizeInMb(Long keyCacheSizeInMb) {
		this.key_cache_size_in_mb = keyCacheSizeInMb;
	}

	public Integer getKeyCacheSavePeriod() {
		return this.key_cache_save_period;
	}

	public void setKeyCacheSavePeriod(Integer keyCacheSavePeriod) {
		this.key_cache_save_period = keyCacheSavePeriod;
	}

	public Integer getKeyCacheKeysToSave() {
		return this.key_cache_keys_to_save;
	}

	public void setKeyCacheKeysToSave(Integer keyCacheKeysToSave) {
		this.key_cache_keys_to_save = keyCacheKeysToSave;
	}

	public String getRowCacheClassName() {
		return this.row_cache_class_name;
	}

	public void setRowCacheClassName(String rowCacheClassName) {
		this.row_cache_class_name = rowCacheClassName;
	}

	public Long getRowCacheSizeInMb() {
		return this.row_cache_size_in_mb;
	}

	public void setRowCacheSizeInMb(Long rowCacheSizeInMb) {
		this.row_cache_size_in_mb = rowCacheSizeInMb;
	}

	public Integer getRowCacheSavePeriod() {
		return this.row_cache_save_period;
	}

	public void setRowCacheSavePeriod(Integer rowCacheSavePeriod) {
		this.row_cache_save_period = rowCacheSavePeriod;
	}

	public Integer getRowCacheKeysToSave() {
		return this.row_cache_keys_to_save;
	}

	public void setRowCacheKeysToSave(Integer rowCacheKeysToSave) {
		this.row_cache_keys_to_save = rowCacheKeysToSave;
	}

	public Long getCounterCacheSizeInMb() {
		return this.counter_cache_size_in_mb;
	}

	public void setCounterCacheSizeInMb(Long counterCacheSizeInMb) {
		this.counter_cache_size_in_mb = counterCacheSizeInMb;
	}

	public Integer getCounterCacheSavePeriod() {
		return this.counter_cache_save_period;
	}

	public void setCounterCacheSavePeriod(Integer counterCacheSavePeriod) {
		this.counter_cache_save_period = counterCacheSavePeriod;
	}

	public Integer getCounterCacheKeysToSave() {
		return this.counter_cache_keys_to_save;
	}

	public void setCounterCacheKeysToSave(Integer counterCacheKeysToSave) {
		this.counter_cache_keys_to_save = counterCacheKeysToSave;
	}

	public Integer getFileCacheSizeInMb() {
		return this.file_cache_size_in_mb;
	}

	public void setFileCacheSizeInMb(Integer fileCacheSizeInMb) {
		this.file_cache_size_in_mb = fileCacheSizeInMb;
	}

	public boolean isFileCacheRoundUp() {
		return this.file_cache_round_up;
	}

	public void setFileCacheRoundUp(boolean fileCacheRoundUp) {
		this.file_cache_round_up = fileCacheRoundUp;
	}

	public boolean isBufferPoolUseHeapIfExhausted() {
		return this.buffer_pool_use_heap_if_exhausted;
	}

	public void setBufferPoolUseHeapIfExhausted(boolean bufferPoolUseHeapIfExhausted) {
		this.buffer_pool_use_heap_if_exhausted = bufferPoolUseHeapIfExhausted;
	}

	public DiskOptimizationStrategy getDiskOptimizationStrategy() {
		return this.disk_optimization_strategy;
	}

	public void setDiskOptimizationStrategy(
			DiskOptimizationStrategy diskOptimizationStrategy) {
		this.disk_optimization_strategy = diskOptimizationStrategy;
	}

	public Double getDiskOptimizationEstimatePercentile() {
		return this.disk_optimization_estimate_percentile;
	}

	public void setDiskOptimizationEstimatePercentile(
			Double diskOptimizationEstimatePercentile) {
		this.disk_optimization_estimate_percentile = diskOptimizationEstimatePercentile;
	}

	public Double getDiskOptimizationPageCrossChance() {
		return this.disk_optimization_page_cross_chance;
	}

	public void setDiskOptimizationPageCrossChance(
			Double diskOptimizationPageCrossChance) {
		this.disk_optimization_page_cross_chance = diskOptimizationPageCrossChance;
	}

	public boolean isInterDcTcpNodelay() {
		return this.inter_dc_tcp_nodelay;
	}

	public void setInterDcTcpNodelay(boolean interDcTcpNodelay) {
		this.inter_dc_tcp_nodelay = interDcTcpNodelay;
	}

	public MemtableAllocationType getMemtableAllocationType() {
		return this.memtable_allocation_type;
	}

	public void setMemtableAllocationType(MemtableAllocationType memtableAllocationType) {
		this.memtable_allocation_type = memtableAllocationType;
	}

	public Integer getTombstoneWarnThreshold() {
		return this.tombstone_warn_threshold;
	}

	public void setTombstoneWarnThreshold(Integer tombstoneWarnThreshold) {
		this.tombstone_warn_threshold = tombstoneWarnThreshold;
	}

	public Integer getTombstoneFailureThreshold() {
		return this.tombstone_failure_threshold;
	}

	public void setTombstoneFailureThreshold(Integer tombstoneFailureThreshold) {
		this.tombstone_failure_threshold = tombstoneFailureThreshold;
	}

	public Long getIndexSummaryCapacityInMb() {
		return this.index_summary_capacity_in_mb;
	}

	public void setIndexSummaryCapacityInMb(Long indexSummaryCapacityInMb) {
		this.index_summary_capacity_in_mb = indexSummaryCapacityInMb;
	}

	public Integer getIndexSummaryResizeIntervalInMinutes() {
		return this.index_summary_resize_interval_in_minutes;
	}

	public void setIndexSummaryResizeIntervalInMinutes(
			Integer indexSummaryResizeIntervalInMinutes) {
		this.index_summary_resize_interval_in_minutes = indexSummaryResizeIntervalInMinutes;
	}

	public Integer getGcLogThresholdInMs() {
		return this.gc_log_threshold_in_ms;
	}

	public void setGcLogThresholdInMs(Integer gcLogThresholdInMs) {
		this.gc_log_threshold_in_ms = gcLogThresholdInMs;
	}

	public Integer getGcWarnThresholdInMs() {
		return this.gc_warn_threshold_in_ms;
	}

	public void setGcWarnThresholdInMs(Integer gcWarnThresholdInMs) {
		this.gc_warn_threshold_in_ms = gcWarnThresholdInMs;
	}

	public Integer getTracetypeQueryTtl() {
		return this.tracetype_query_ttl;
	}

	public void setTracetypeQueryTtl(Integer tracetypeQueryTtl) {
		this.tracetype_query_ttl = tracetypeQueryTtl;
	}

	public Integer getTracetypeRepairTtl() {
		return this.tracetype_repair_ttl;
	}

	public void setTracetypeRepairTtl(Integer tracetypeRepairTtl) {
		this.tracetype_repair_ttl = tracetypeRepairTtl;
	}

	public String getOtcCoalescingStrategy() {
		return this.otc_coalescing_strategy;
	}

	public void setOtcCoalescingStrategy(String otcCoalescingStrategy) {
		this.otc_coalescing_strategy = otcCoalescingStrategy;
	}

	public Integer getOtcCoalescingWindowUs() {
		return this.otc_coalescing_window_us;
	}

	public void setOtcCoalescingWindowUs(Integer otcCoalescingWindowUs) {
		this.otc_coalescing_window_us = otcCoalescingWindowUs;
	}

	public Integer getOtcCoalescingEnoughCoalescedMessages() {
		return this.otc_coalescing_enough_coalesced_messages;
	}

	public void setOtcCoalescingEnoughCoalescedMessages(
			Integer otcCoalescingEnoughCoalescedMessages) {
		this.otc_coalescing_enough_coalesced_messages = otcCoalescingEnoughCoalescedMessages;
	}

	public Integer getOtcBacklogExpirationIntervalMs() {
		return this.otc_backlog_expiration_interval_ms;
	}

	public void setOtcBacklogExpirationIntervalMs(
			Integer otcBacklogExpirationIntervalMs) {
		this.otc_backlog_expiration_interval_ms = otcBacklogExpirationIntervalMs;
	}

	public Integer getWindowsTimerInterval() {
		return this.windows_timer_interval;
	}

	public void setWindowsTimerInterval(Integer windowsTimerInterval) {
		this.windows_timer_interval = windowsTimerInterval;
	}

	public Long getPreparedStatementsCacheSizeMb() {
		return this.prepared_statements_cache_size_mb;
	}

	public void setPreparedStatementsCacheSizeMb(Long preparedStatementsCacheSizeMb) {
		this.prepared_statements_cache_size_mb = preparedStatementsCacheSizeMb;
	}

	public Long getThriftPreparedStatementsCacheSizeMb() {
		return this.thrift_prepared_statements_cache_size_mb;
	}

	public void setThriftPreparedStatementsCacheSizeMb(
			Long thriftPreparedStatementsCacheSizeMb) {
		this.thrift_prepared_statements_cache_size_mb = thriftPreparedStatementsCacheSizeMb;
	}

	public boolean isEnableUserDefinedFunctions() {
		return this.enable_user_defined_functions;
	}

	public void setEnableUserDefinedFunctions(boolean enableUserDefinedFunctions) {
		this.enable_user_defined_functions = enableUserDefinedFunctions;
	}

	public boolean isEnableScriptedUserDefinedFunctions() {
		return this.enable_scripted_user_defined_functions;
	}

	public void setEnableScriptedUserDefinedFunctions(
			boolean enableScriptedUserDefinedFunctions) {
		this.enable_scripted_user_defined_functions = enableScriptedUserDefinedFunctions;
	}

	public boolean isEnableMaterializedViews() {
		return this.enable_materialized_views;
	}

	public void setEnableMaterializedViews(boolean enableMaterializedViews) {
		this.enable_materialized_views = enableMaterializedViews;
	}

	public boolean isEnableUserDefinedFunctionsThreads() {
		return this.enable_user_defined_functions_threads;
	}

	public void setEnableUserDefinedFunctionsThreads(
			boolean enableUserDefinedFunctionsThreads) {
		this.enable_user_defined_functions_threads = enableUserDefinedFunctionsThreads;
	}

	public Long getUserDefinedFunctionWarnTimeout() {
		return this.user_defined_function_warn_timeout;
	}

	public void setUserDefinedFunctionWarnTimeout(Long userDefinedFunctionWarnTimeout) {
		this.user_defined_function_warn_timeout = userDefinedFunctionWarnTimeout;
	}

	public Long getUserDefinedFunctionFailTimeout() {
		return this.user_defined_function_fail_timeout;
	}

	public void setUserDefinedFunctionFailTimeout(Long userDefinedFunctionFailTimeout) {
		this.user_defined_function_fail_timeout = userDefinedFunctionFailTimeout;
	}

	public UserFunctionTimeoutPolicy getUserFunctionTimeoutPolicy() {
		return this.user_function_timeout_policy;
	}

	public void setUserFunctionTimeoutPolicy(
			UserFunctionTimeoutPolicy userFunctionTimeoutPolicy) {
		this.user_function_timeout_policy = userFunctionTimeoutPolicy;
	}

	public boolean isBackPressureEnabled() {
		return this.back_pressure_enabled;
	}

	public void setBackPressureEnabled(boolean backPressureEnabled) {
		this.back_pressure_enabled = backPressureEnabled;
	}

	public ParameterizedClass getBackPressureStrategy() {
		return this.back_pressure_strategy;
	}

	public void setBackPressureStrategy(ParameterizedClass backPressureStrategy) {
		this.back_pressure_strategy = backPressureStrategy;
	}

	public enum InternodeEncryption {

		all, none, dc, rack

	}

	public enum CommitLogSync {

		periodic, batch

	}

	public enum InternodeCompression {

		all, none, dc

	}

	public enum DiskAccessMode {

		auto, mmap, mmap_index_only, standard,

	}

	public enum MemtableAllocationType {

		unslabbed_heap_buffers, heap_buffers, offheap_buffers, offheap_objects

	}

	public enum DiskFailurePolicy {

		best_effort, stop, ignore, stop_paranoid, die

	}

	public enum CommitFailurePolicy {

		stop, stop_commit, ignore, die,

	}

	public enum UserFunctionTimeoutPolicy {

		ignore, die, die_immediate

	}

	public enum RequestSchedulerId {

		keyspace

	}

	public enum DiskOptimizationStrategy {

		ssd, spinning

	}

	public static class ClientEncryptionOptions {

		private String keystore;

		private String keystorePassword;

		private String truststore;

		private String truststorePassword;

		private List<String> cipherSuites = new ArrayList<>();

		private String protocol;

		private String algorithm;

		private String storeType;

		private boolean requireClientAuth;

		private boolean requireEndpointVerification;

		private boolean enabled;

		private boolean optional;

		public String getKeystore() {
			return this.keystore;
		}

		public void setKeystore(String keystore) {
			this.keystore = keystore;
		}

		public String getKeystorePassword() {
			return this.keystorePassword;
		}

		public void setKeystorePassword(String keystorePassword) {
			this.keystorePassword = keystorePassword;
		}

		public String getTruststore() {
			return this.truststore;
		}

		public void setTruststore(String truststore) {
			this.truststore = truststore;
		}

		public String getTruststorePassword() {
			return this.truststorePassword;
		}

		public void setTruststorePassword(String truststorePassword) {
			this.truststorePassword = truststorePassword;
		}

		public List<String> getCipherSuites() {
			return this.cipherSuites;
		}

		public void setCipherSuites(List<String> cipherSuites) {
			this.cipherSuites = cipherSuites;
		}

		public String getProtocol() {
			return this.protocol;
		}

		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		public String getAlgorithm() {
			return this.algorithm;
		}

		public void setAlgorithm(String algorithm) {
			this.algorithm = algorithm;
		}

		public String getStoreType() {
			return this.storeType;
		}

		public void setStoreType(String storeType) {
			this.storeType = storeType;
		}

		public boolean isRequireClientAuth() {
			return this.requireClientAuth;
		}

		public void setRequireClientAuth(boolean requireClientAuth) {
			this.requireClientAuth = requireClientAuth;
		}

		public boolean isRequireEndpointVerification() {
			return this.requireEndpointVerification;
		}

		public void setRequireEndpointVerification(boolean requireEndpointVerification) {
			this.requireEndpointVerification = requireEndpointVerification;
		}

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isOptional() {
			return this.optional;
		}

		public void setOptional(boolean optional) {
			this.optional = optional;
		}

	}

	public static class ServerEncryptionOptions {

		private String keystore;

		private String keystorePassword;

		private String truststore;

		private String truststorePassword;

		private List<String> cipherSuites = new ArrayList<>();

		private String protocol;

		private String algorithm;

		private String storeType;

		private boolean requireClientAuth;

		private boolean requireEndpointVerification;

		private InternodeEncryption internodeEncryption;

		public String getKeystore() {
			return this.keystore;
		}

		public void setKeystore(String keystore) {
			this.keystore = keystore;
		}

		public String getKeystorePassword() {
			return this.keystorePassword;
		}

		public void setKeystorePassword(String keystorePassword) {
			this.keystorePassword = keystorePassword;
		}

		public String getTruststore() {
			return this.truststore;
		}

		public void setTruststore(String truststore) {
			this.truststore = truststore;
		}

		public String getTruststorePassword() {
			return this.truststorePassword;
		}

		public void setTruststorePassword(String truststorePassword) {
			this.truststorePassword = truststorePassword;
		}

		public List<String> getCipherSuites() {
			return this.cipherSuites;
		}

		public void setCipherSuites(List<String> cipherSuites) {
			this.cipherSuites = cipherSuites;
		}

		public String getProtocol() {
			return this.protocol;
		}

		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		public String getAlgorithm() {
			return this.algorithm;
		}

		public void setAlgorithm(String algorithm) {
			this.algorithm = algorithm;
		}

		public String getStoreType() {
			return this.storeType;
		}

		public void setStoreType(String storeType) {
			this.storeType = storeType;
		}

		public boolean isRequireClientAuth() {
			return this.requireClientAuth;
		}

		public void setRequireClientAuth(boolean requireClientAuth) {
			this.requireClientAuth = requireClientAuth;
		}

		public boolean isRequireEndpointVerification() {
			return this.requireEndpointVerification;
		}

		public void setRequireEndpointVerification(boolean requireEndpointVerification) {
			this.requireEndpointVerification = requireEndpointVerification;
		}

		public InternodeEncryption getInternodeEncryption() {
			return this.internodeEncryption;
		}

		public void setInternodeEncryption(InternodeEncryption internodeEncryption) {
			this.internodeEncryption = internodeEncryption;
		}

	}

	public static class TransparentDataEncryptionOptions {

		private boolean enabled;

		private Integer chunkLengthKb;

		private String cipher;

		private String keyAlias;

		private Integer ivLength;

		private ParameterizedClass keyProvider;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Integer getChunkLengthKb() {
			return this.chunkLengthKb;
		}

		public void setChunkLengthKb(Integer chunkLengthKb) {
			this.chunkLengthKb = chunkLengthKb;
		}

		public String getCipher() {
			return this.cipher;
		}

		public void setCipher(String cipher) {
			this.cipher = cipher;
		}

		public String getKeyAlias() {
			return this.keyAlias;
		}

		public void setKeyAlias(String keyAlias) {
			this.keyAlias = keyAlias;
		}

		public Integer getIvLength() {
			return this.ivLength;
		}

		public void setIvLength(Integer ivLength) {
			this.ivLength = ivLength;
		}

		public ParameterizedClass getKeyProvider() {
			return this.keyProvider;
		}

		public void setKeyProvider(ParameterizedClass keyProvider) {
			this.keyProvider = keyProvider;
		}

	}

	public static class RequestSchedulerOptions {

		private Integer throttleLimit;

		private Integer defaultWeight;

		private Map<String, Integer> weights = new LinkedHashMap<>();

		public RequestSchedulerOptions(Integer throttleLimit, Integer defaultWeight,
				Map<String, Integer> weights) {
			this.throttleLimit = throttleLimit;
			this.defaultWeight = defaultWeight;
			this.weights = weights;
		}

		public RequestSchedulerOptions() {
		}

		public Integer getThrottleLimit() {
			return this.throttleLimit;
		}

		public void setThrottleLimit(Integer throttleLimit) {
			this.throttleLimit = throttleLimit;
		}

		public Integer getDefaultWeight() {
			return this.defaultWeight;
		}

		public void setDefaultWeight(Integer defaultWeight) {
			this.defaultWeight = defaultWeight;
		}

		public Map<String, Integer> getWeights() {
			return this.weights;
		}

		public void setWeights(Map<String, Integer> weights) {
			this.weights = weights;
		}

	}

	public static class ParameterizedClass {

		private String className;

		private Map<String, String> parameters = new LinkedHashMap<>();

		public ParameterizedClass(String className, Map<String, String> parameters) {
			this.className = className;
			this.parameters = parameters;
		}

		public ParameterizedClass() {
		}

		public String getClassName() {
			return this.className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public Map<String, String> getParameters() {
			return this.parameters;
		}

		public void setParameters(Map<String, String> parameters) {
			this.parameters = parameters;
		}

	}

}
