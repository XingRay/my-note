## windows上安装prometheus

下载

https://github.com/prometheus/prometheus/releases/download/v2.45.0/prometheus-2.45.0.windows-amd64.zip



解压

解压到指定目录, 如:

```
D:\develop\prometheus\prometheus-2.45.0.windows-amd64
```

将prometheus解压目录添加到系统路径PATH



测试

```bash
prometheus --help
```

```bash
usage: prometheus [<flags>]

The Prometheus monitoring server


Flags:
  -h, --[no-]help                Show context-sensitive help (also try
                                 --help-long and --help-man).
      --[no-]version             Show application version.
      --config.file="prometheus.yml"
                                 Prometheus configuration file path.
      --web.listen-address="0.0.0.0:9090"
                                 Address to listen on for UI, API, and
                                 telemetry.
      --web.config.file=""       [EXPERIMENTAL] Path to configuration file that
                                 can enable TLS or authentication.
      --web.read-timeout=5m      Maximum duration before timing out read of the
                                 request, and closing idle connections.
      --web.max-connections=512  Maximum number of simultaneous connections.
      --web.external-url=<URL>   The URL under which Prometheus is externally
                                 reachable (for example, if Prometheus is served
                                 via a reverse proxy). Used for generating
                                 relative and absolute links back to Prometheus
                                 itself. If the URL has a path portion, it will
                                 be used to prefix all HTTP endpoints served by
                                 Prometheus. If omitted, relevant URL components
                                 will be derived automatically.
      --web.route-prefix=<path>  Prefix for the internal routes of
                                 web endpoints. Defaults to path of
                                 --web.external-url.
      --web.user-assets=<path>   Path to static asset directory, available at
                                 /user.
      --[no-]web.enable-lifecycle
                                 Enable shutdown and reload via HTTP request.
      --[no-]web.enable-admin-api
                                 Enable API endpoints for admin control actions.
      --[no-]web.enable-remote-write-receiver
                                 Enable API endpoint accepting remote write
                                 requests.
      --web.console.templates="consoles"
                                 Path to the console template directory,
                                 available at /consoles.
      --web.console.libraries="console_libraries"
                                 Path to the console library directory.
      --web.page-title="Prometheus Time Series Collection and Processing Server"
                                 Document title of Prometheus instance.
      --web.cors.origin=".*"     Regex for CORS origin. It is fully anchored.
                                 Example: 'https?://(domain1|domain2)\.com'
      --storage.tsdb.path="data/"
                                 Base path for metrics storage. Use with server
                                 mode only.
      --storage.tsdb.retention=STORAGE.TSDB.RETENTION
                                 [DEPRECATED] How long to retain samples in
                                 storage. This flag has been deprecated,
                                 use "storage.tsdb.retention.time" instead.
                                 Use with server mode only.
      --storage.tsdb.retention.time=STORAGE.TSDB.RETENTION.TIME
                                 How long to retain samples in storage.
                                 When this flag is set it overrides
                                 "storage.tsdb.retention". If neither this
                                 flag nor "storage.tsdb.retention" nor
                                 "storage.tsdb.retention.size" is set,
                                 the retention time defaults to 15d. Units
                                 Supported: y, w, d, h, m, s, ms. Use with
                                 server mode only.
      --storage.tsdb.retention.size=STORAGE.TSDB.RETENTION.SIZE
                                 Maximum number of bytes that can be stored for
                                 blocks. A unit is required, supported units: B,
                                 KB, MB, GB, TB, PB, EB. Ex: "512MB". Based on
                                 powers-of-2, so 1KB is 1024B. Use with server
                                 mode only.
      --[no-]storage.tsdb.no-lockfile
                                 Do not create lockfile in data directory.
                                 Use with server mode only.
      --storage.tsdb.head-chunks-write-queue-size=0
                                 Size of the queue through which head chunks
                                 are written to the disk to be m-mapped,
                                 0 disables the queue completely. Experimental.
                                 Use with server mode only.
      --storage.agent.path="data-agent/"
                                 Base path for metrics storage. Use with agent
                                 mode only.
      --[no-]storage.agent.wal-compression
                                 Compress the agent WAL. Use with agent mode
                                 only.
      --storage.agent.retention.min-time=STORAGE.AGENT.RETENTION.MIN-TIME
                                 Minimum age samples may be before being
                                 considered for deletion when the WAL is
                                 truncated Use with agent mode only.
      --storage.agent.retention.max-time=STORAGE.AGENT.RETENTION.MAX-TIME
                                 Maximum age samples may be before being
                                 forcibly deleted when the WAL is truncated Use
                                 with agent mode only.
      --[no-]storage.agent.no-lockfile
                                 Do not create lockfile in data directory.
                                 Use with agent mode only.
      --storage.remote.flush-deadline=<duration>
                                 How long to wait flushing sample on shutdown or
                                 config reload.
      --storage.remote.read-sample-limit=5e7
                                 Maximum overall number of samples to return via
                                 the remote read interface, in a single query.
                                 0 means no limit. This limit is ignored for
                                 streamed response types. Use with server mode
                                 only.
      --storage.remote.read-concurrent-limit=10
                                 Maximum number of concurrent remote read calls.
                                 0 means no limit. Use with server mode only.
      --storage.remote.read-max-bytes-in-frame=1048576
                                 Maximum number of bytes in a single frame for
                                 streaming remote read response types before
                                 marshalling. Note that client might have limit
                                 on frame size as well. 1MB as recommended by
                                 protobuf by default. Use with server mode only.
      --rules.alert.for-outage-tolerance=1h
                                 Max time to tolerate prometheus outage for
                                 restoring "for" state of alert. Use with server
                                 mode only.
      --rules.alert.for-grace-period=10m
                                 Minimum duration between alert and restored
                                 "for" state. This is maintained only for alerts
                                 with configured "for" time greater than grace
                                 period. Use with server mode only.
      --rules.alert.resend-delay=1m
                                 Minimum amount of time to wait before resending
                                 an alert to Alertmanager. Use with server mode
                                 only.
      --alertmanager.notification-queue-capacity=10000
                                 The capacity of the queue for pending
                                 Alertmanager notifications. Use with server
                                 mode only.
      --query.lookback-delta=5m  The maximum lookback duration for retrieving
                                 metrics during expression evaluations and
                                 federation. Use with server mode only.
      --query.timeout=2m         Maximum time a query may take before being
                                 aborted. Use with server mode only.
      --query.max-concurrency=20
                                 Maximum number of queries executed
                                 concurrently. Use with server mode only.
      --query.max-samples=50000000
                                 Maximum number of samples a single query can
                                 load into memory. Note that queries will fail
                                 if they try to load more samples than this
                                 into memory, so this also limits the number
                                 of samples a query can return. Use with server
                                 mode only.
      --enable-feature= ...      Comma separated feature names
                                 to enable. Valid options: agent,
                                 exemplar-storage, expand-external-labels,
                                 memory-snapshot-on-shutdown,
                                 promql-at-modifier, promql-negative-offset,
                                 promql-per-step-stats, remote-write-receiver
                                 (DEPRECATED), extra-scrape-metrics,
                                 new-service-discovery-manager, auto-gomaxprocs,
                                 no-default-scrape-port, native-histograms. See
                                 https://prometheus.io/docs/prometheus/latest/feature_flags/
                                 for more details.
      --log.level=info           Only log messages with the given severity or
                                 above. One of: [debug, info, warn, error]
      --log.format=logfmt        Output format of log messages. One of: [logfmt,
                                 json]
```



```bash
prometheus --version
```

```
prometheus, version 2.45.0 (branch: HEAD, revision: 8ef767e396bf8445f009f945b0162fd71827f445)
  build user:       root@858b3920627f
  build date:       20230623-15:18:12
  go version:       go1.20.5
  platform:         windows/amd64
  tags:             builtinassets,stringlabels
```



启动

```bash
prometheus --config.file=prometheus.yml
```

正常启动之后可以通过浏览器访问  http://localhost:9090/ 进入 prometheus 的web页面

