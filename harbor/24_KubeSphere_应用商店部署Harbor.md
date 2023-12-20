## KubeSphere应用商店部署Harbor

官方文档：

https://www.kubesphere.io/zh/docs/v3.3/application-store/built-in-apps/harbor-app/#%E6%AD%A5%E9%AA%A4-1%E4%BB%8E%E5%BA%94%E7%94%A8%E5%95%86%E5%BA%97%E4%B8%AD%E9%83%A8%E7%BD%B2-harbor



应用商店，找到Harbor/搜索Harbor，点击Harbor，点击安装

基本信息：

```bash
名称：harbor-registry
版本：1.9.3[2.5.3] 最新版
描述：测试项目Harbor镜像仓库服务
```

位置：

```bash
企业空间：test-company
集群：default
项目：project-test
```

下一步：

应用设置：

参考官方示例：

```yaml
## 请注意，192.168.0.9 是示例 IP 地址，您必须使用自己的地址。 
expose:
  type: nodePort
  tls:
    enabled: false
    secretName: ""
    notarySecretName: ""
    commonName: "192.168.0.9"  # 将 commonName 更改成您自己的值。
  nodePort:
    # NodePort 服务的名称。
    name: harbor
    ports:
      http:
        # 使用 HTTP 服务时，Harbor 监听的服务端口。
        port: 80
        # 使用 HTTP 服务时，Harbor 监听的节点端口。
        nodePort: 30002
      https:
        # 使用 HTTPS 服务时，Harbor 监听的服务端口。
        port: 443
        # 使用 HTTPS 服务时，Harbor 监听的服务端口。
        nodePort: 30003
      # 仅在 notary.enabled 设置为 true 时需要此配置。
      notary:
        # Notary 监听的服务端口。
        port: 4443
        # Notary 监听的节点端口。
        nodePort: 30004

externalURL: http://192.168.0.9:30002 # 使用您自己的 IP 地址。

# Harbor admin 的初始密码。启动 Harbor 后可以通过主页修改。
harborAdminPassword: "Harbor12345"
# 用于加密的密钥，必须是包含 16 个字符的字符串。
secretKey: "not-a-secure-key"
```

主要修改：

```yaml
expose:
  type: nodePort
  tls:
    enabled: false
    auto:
      commonName: "192.168.0.112"
      
externalURL: http://192.168.0.112:30002
```

注意 externalURL 使用的是http协议。



修改后的完整的配置文件：

使用http：

```yaml
expose:
  # Set how to expose the service. Set the type as "ingress", "clusterIP", "nodePort" or "loadBalancer"
  # and fill the information in the corresponding section
  type: nodePort
  tls:
    # Enable TLS or not.
    # Delete the "ssl-redirect" annotations in "expose.ingress.annotations" when TLS is disabled and "expose.type" is "ingress"
    # Note: if the "expose.type" is "ingress" and TLS is disabled,
    # the port must be included in the command when pulling/pushing images.
    # Refer to https://github.com/goharbor/harbor/issues/5291 for details.
    enabled: false
    # The source of the tls certificate. Set as "auto", "secret"
    # or "none" and fill the information in the corresponding section
    # 1) auto: generate the tls certificate automatically
    # 2) secret: read the tls certificate from the specified secret.
    # The tls certificate can be generated manually or by cert manager
    # 3) none: configure no tls certificate for the ingress. If the default
    # tls certificate is configured in the ingress controller, choose this option
    certSource: auto
    auto:
      # The common name used to generate the certificate, it's necessary
      # when the type isn't "ingress"
      commonName: "192.168.0.112"
    secret:
      # The name of secret which contains keys named:
      # "tls.crt" - the certificate
      # "tls.key" - the private key
      secretName: ""
      # The name of secret which contains keys named:
      # "tls.crt" - the certificate
      # "tls.key" - the private key
      # Only needed when the "expose.type" is "ingress".
      notarySecretName: ""
  ingress:
    hosts:
      core: core.harbor.domain
      notary: notary.harbor.domain
    # set to the type of ingress controller if it has specific requirements.
    # leave as `default` for most ingress controllers.
    # set to `gce` if using the GCE ingress controller
    # set to `ncp` if using the NCP (NSX-T Container Plugin) ingress controller
    controller: default
    ## Allow .Capabilities.KubeVersion.Version to be overridden while creating ingress
    kubeVersionOverride: ""
    className: ""
    annotations:
      # note different ingress controllers may require a different ssl-redirect annotation
      # for Envoy, use ingress.kubernetes.io/force-ssl-redirect: "true" and remove the nginx lines below
      ingress.kubernetes.io/ssl-redirect: "true"
      ingress.kubernetes.io/proxy-body-size: "0"
      nginx.ingress.kubernetes.io/ssl-redirect: "true"
      nginx.ingress.kubernetes.io/proxy-body-size: "0"
    notary:
      # notary ingress-specific annotations
      annotations: {}
      # notary ingress-specific labels
      labels: {}
    harbor:
      # harbor ingress-specific annotations
      annotations: {}
      # harbor ingress-specific labels
      labels: {}
  clusterIP:
    # The name of ClusterIP service
    name: harbor
    # Annotations on the ClusterIP service
    annotations: {}
    ports:
      # The service port Harbor listens on when serving HTTP
      httpPort: 80
      # The service port Harbor listens on when serving HTTPS
      httpsPort: 443
      # The service port Notary listens on. Only needed when notary.enabled
      # is set to true
      notaryPort: 4443
  nodePort:
    # The name of NodePort service
    name: harbor
    ports:
      http:
        # The service port Harbor listens on when serving HTTP
        port: 80
        # The node port Harbor listens on when serving HTTP
        nodePort: 30002
      https:
        # The service port Harbor listens on when serving HTTPS
        port: 443
        # The node port Harbor listens on when serving HTTPS
        nodePort: 30003
      # Only needed when notary.enabled is set to true
      notary:
        # The service port Notary listens on
        port: 4443
        # The node port Notary listens on
        nodePort: 30004
  loadBalancer:
    # The name of LoadBalancer service
    name: harbor
    # Set the IP if the LoadBalancer supports assigning IP
    IP: ""
    ports:
      # The service port Harbor listens on when serving HTTP
      httpPort: 80
      # The service port Harbor listens on when serving HTTPS
      httpsPort: 443
      # The service port Notary listens on. Only needed when notary.enabled
      # is set to true
      notaryPort: 4443
    annotations: {}
    sourceRanges: []

# The external URL for Harbor core service. It is used to
# 1) populate the docker/helm commands showed on portal
# 2) populate the token service URL returned to docker/notary client
#
# Format: protocol://domain[:port]. Usually:
# 1) if "expose.type" is "ingress", the "domain" should be
# the value of "expose.ingress.hosts.core"
# 2) if "expose.type" is "clusterIP", the "domain" should be
# the value of "expose.clusterIP.name"
# 3) if "expose.type" is "nodePort", the "domain" should be
# the IP address of k8s node
#
# If Harbor is deployed behind the proxy, set it as the URL of proxy
externalURL: http://192.168.0.112:30002

# The internal TLS used for harbor components secure communicating. In order to enable https
# in each components tls cert files need to provided in advance.
internalTLS:
  # If internal TLS enabled
  enabled: false
  # There are three ways to provide tls
  # 1) "auto" will generate cert automatically
  # 2) "manual" need provide cert file manually in following value
  # 3) "secret" internal certificates from secret
  certSource: "auto"
  # The content of trust ca, only available when `certSource` is "manual"
  trustCa: ""
  # core related cert configuration
  core:
    # secret name for core's tls certs
    secretName: ""
    # Content of core's TLS cert file, only available when `certSource` is "manual"
    crt: ""
    # Content of core's TLS key file, only available when `certSource` is "manual"
    key: ""
  # jobservice related cert configuration
  jobservice:
    # secret name for jobservice's tls certs
    secretName: ""
    # Content of jobservice's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of jobservice's TLS key file, only available when `certSource` is "manual"
    key: ""
  # registry related cert configuration
  registry:
    # secret name for registry's tls certs
    secretName: ""
    # Content of registry's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of registry's TLS key file, only available when `certSource` is "manual"
    key: ""
  # portal related cert configuration
  portal:
    # secret name for portal's tls certs
    secretName: ""
    # Content of portal's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of portal's TLS key file, only available when `certSource` is "manual"
    key: ""
  # chartmuseum related cert configuration
  chartmuseum:
    # secret name for chartmuseum's tls certs
    secretName: ""
    # Content of chartmuseum's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of chartmuseum's TLS key file, only available when `certSource` is "manual"
    key: ""
  # trivy related cert configuration
  trivy:
    # secret name for trivy's tls certs
    secretName: ""
    # Content of trivy's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of trivy's TLS key file, only available when `certSource` is "manual"
    key: ""

ipFamily:
  # ipv6Enabled set to true if ipv6 is enabled in cluster, currently it affected the nginx related component
  ipv6:
    enabled: true
  # ipv4Enabled set to true if ipv4 is enabled in cluster, currently it affected the nginx related component
  ipv4:
    enabled: true

# The persistence is enabled by default and a default StorageClass
# is needed in the k8s cluster to provision volumes dynamically.
# Specify another StorageClass in the "storageClass" or set "existingClaim"
# if you already have existing persistent volumes to use
#
# For storing images and charts, you can also use "azure", "gcs", "s3",
# "swift" or "oss". Set it in the "imageChartStorage" section
persistence:
  enabled: true
  # Setting it to "keep" to avoid removing PVCs during a helm delete
  # operation. Leaving it empty will delete PVCs after the chart deleted
  # (this does not apply for PVCs that are created for internal database
  # and redis components, i.e. they are never deleted automatically)
  resourcePolicy: "keep"
  persistentVolumeClaim:
    registry:
      # Use the existing PVC which must be created manually before bound,
      # and specify the "subPath" if the PVC is shared with other components
      existingClaim: ""
      # Specify the "storageClass" used to provision the volume. Or the default
      # StorageClass will be used (the default).
      # Set it to "-" to disable dynamic provisioning
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 5Gi
      annotations: {}
    chartmuseum:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 5Gi
      annotations: {}
    jobservice:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 1Gi
      annotations: {}
    # If external database is used, the following settings for database will
    # be ignored
    database:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 1Gi
      annotations: {}
    # If external Redis is used, the following settings for Redis will
    # be ignored
    redis:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 1Gi
      annotations: {}
    trivy:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 5Gi
      annotations: {}
  # Define which storage backend is used for registry and chartmuseum to store
  # images and charts. Refer to
  # https://github.com/docker/distribution/blob/master/docs/configuration.md#storage
  # for the detail.
  imageChartStorage:
    # Specify whether to disable `redirect` for images and chart storage, for
    # backends which not supported it (such as using minio for `s3` storage type), please disable
    # it. To disable redirects, simply set `disableredirect` to `true` instead.
    # Refer to
    # https://github.com/docker/distribution/blob/master/docs/configuration.md#redirect
    # for the detail.
    disableredirect: false
    # Specify the "caBundleSecretName" if the storage service uses a self-signed certificate.
    # The secret must contain keys named "ca.crt" which will be injected into the trust store
    # of registry's and chartmuseum's containers.
    # caBundleSecretName:

    # Specify the type of storage: "filesystem", "azure", "gcs", "s3", "swift",
    # "oss" and fill the information needed in the corresponding section. The type
    # must be "filesystem" if you want to use persistent volumes for registry
    # and chartmuseum
    type: filesystem
    filesystem:
      rootdirectory: /storage
      #maxthreads: 100
    azure:
      accountname: accountname
      accountkey: base64encodedaccountkey
      container: containername
      #realm: core.windows.net
    gcs:
      bucket: bucketname
      # The base64 encoded json file which contains the key
      encodedkey: base64-encoded-json-key-file
      #rootdirectory: /gcs/object/name/prefix
      #chunksize: "5242880"
    s3:
      region: us-west-1
      bucket: bucketname
      #accesskey: awsaccesskey
      #secretkey: awssecretkey
      #regionendpoint: http://myobjects.local
      #encrypt: false
      #keyid: mykeyid
      #secure: true
      #skipverify: false
      #v4auth: true
      #chunksize: "5242880"
      #rootdirectory: /s3/object/name/prefix
      #storageclass: STANDARD
      #multipartcopychunksize: "33554432"
      #multipartcopymaxconcurrency: 100
      #multipartcopythresholdsize: "33554432"
    swift:
      authurl: https://storage.myprovider.com/v3/auth
      username: username
      password: password
      container: containername
      #region: fr
      #tenant: tenantname
      #tenantid: tenantid
      #domain: domainname
      #domainid: domainid
      #trustid: trustid
      #insecureskipverify: false
      #chunksize: 5M
      #prefix:
      #secretkey: secretkey
      #accesskey: accesskey
      #authversion: 3
      #endpointtype: public
      #tempurlcontainerkey: false
      #tempurlmethods:
    oss:
      accesskeyid: accesskeyid
      accesskeysecret: accesskeysecret
      region: regionname
      bucket: bucketname
      #endpoint: endpoint
      #internal: false
      #encrypt: false
      #secure: true
      #chunksize: 10M
      #rootdirectory: rootdirectory

imagePullPolicy: IfNotPresent

# Use this set to assign a list of default pullSecrets
imagePullSecrets:
#  - name: docker-registry-secret
#  - name: internal-registry-secret

# The update strategy for deployments with persistent volumes(jobservice, registry
# and chartmuseum): "RollingUpdate" or "Recreate"
# Set it as "Recreate" when "RWM" for volumes isn't supported
updateStrategy:
  type: RollingUpdate

# debug, info, warning, error or fatal
logLevel: info

# The initial password of Harbor admin. Change it from portal after launching Harbor
harborAdminPassword: "Harbor12345"

# The name of the secret which contains key named "ca.crt". Setting this enables the
# download link on portal to download the CA certificate when the certificate isn't
# generated automatically
caSecretName: ""

# The secret key used for encryption. Must be a string of 16 chars.
secretKey: "not-a-secure-key"

# The proxy settings for updating trivy vulnerabilities from the Internet and replicating
# artifacts from/to the registries that cannot be reached directly
proxy:
  httpProxy:
  httpsProxy:
  noProxy: 127.0.0.1,localhost,.local,.internal
  components:
    - core
    - jobservice
    - trivy

# Run the migration job via helm hook
enableMigrateHelmHook: false

# The custom ca bundle secret, the secret must contain key named "ca.crt"
# which will be injected into the trust store for chartmuseum, core, jobservice, registry, trivy components
# caBundleSecretName: ""

## UAA Authentication Options
# If you're using UAA for authentication behind a self-signed
# certificate you will need to provide the CA Cert.
# Set uaaSecretName below to provide a pre-created secret that
# contains a base64 encoded CA Certificate named `ca.crt`.
# uaaSecretName:

# If service exposed via "ingress", the Nginx will not be used
nginx:
  image:
    repository: goharbor/nginx-photon
    tag: v2.5.3
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  replicas: 1
  revisionHistoryLimit: 10
  # resources:
  #  requests:
  #    memory: 256Mi
  #    cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:

portal:
  image:
    repository: goharbor/harbor-portal
    tag: v2.5.3
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  replicas: 1
  revisionHistoryLimit: 10
  # resources:
  #  requests:
  #    memory: 256Mi
  #    cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:

core:
  image:
    repository: goharbor/harbor-core
    tag: v2.5.3
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  replicas: 1
  revisionHistoryLimit: 10
  ## Startup probe values
  startupProbe:
    enabled: true
    initialDelaySeconds: 10
  # resources:
  #  requests:
  #    memory: 256Mi
  #    cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  # Secret is used when core server communicates with other components.
  # If a secret key is not specified, Helm will generate one.
  # Must be a string of 16 chars.
  secret: ""
  # Fill the name of a kubernetes secret if you want to use your own
  # TLS certificate and private key for token encryption/decryption.
  # The secret must contain keys named:
  # "tls.crt" - the certificate
  # "tls.key" - the private key
  # The default key pair will be used if it isn't set
  secretName: ""
  # The XSRF key. Will be generated automatically if it isn't specified
  xsrfKey: ""
  ## The priority class to run the pod as
  priorityClassName:
  # The time duration for async update artifact pull_time and repository
  # pull_count, the unit is second. Will be 10 seconds if it isn't set.
  # eg. artifactPullAsyncFlushDuration: 10
  artifactPullAsyncFlushDuration:

jobservice:
  image:
    repository: goharbor/harbor-jobservice
    tag: v2.5.3
  replicas: 1
  revisionHistoryLimit: 10
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  maxJobWorkers: 10
  # The logger for jobs: "file", "database" or "stdout"
  jobLoggers:
    - file
    # - database
    # - stdout
  # The jobLogger sweeper duration (ignored if `jobLogger` is `stdout`)
  loggerSweeperDuration: 14 #days

  # resources:
  #   requests:
  #     memory: 256Mi
  #     cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  # Secret is used when job service communicates with other components.
  # If a secret key is not specified, Helm will generate one.
  # Must be a string of 16 chars.
  secret: ""
  ## The priority class to run the pod as
  priorityClassName:

registry:
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  registry:
    image:
      repository: goharbor/registry-photon
      tag: v2.5.3
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
  controller:
    image:
      repository: goharbor/harbor-registryctl
      tag: v2.5.3

    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
  replicas: 1
  revisionHistoryLimit: 10
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:
  # Secret is used to secure the upload state from client
  # and registry storage backend.
  # See: https://github.com/docker/distribution/blob/master/docs/configuration.md#http
  # If a secret key is not specified, Helm will generate one.
  # Must be a string of 16 chars.
  secret: ""
  # If true, the registry returns relative URLs in Location headers. The client is responsible for resolving the correct URL.
  relativeurls: false
  credentials:
    username: "harbor_registry_user"
    password: "harbor_registry_password"
    # Login and password in htpasswd string format. Excludes `registry.credentials.username`  and `registry.credentials.password`. May come in handy when integrating with tools like argocd or flux. This allows the same line to be generated each time the template is rendered, instead of the `htpasswd` function from helm, which generates different lines each time because of the salt.
    # htpasswdString: $apr1$XLefHzeG$Xl4.s00sMSCCcMyJljSZb0 # example string
  middleware:
    enabled: false
    type: cloudFront
    cloudFront:
      baseurl: example.cloudfront.net
      keypairid: KEYPAIRID
      duration: 3000s
      ipfilteredby: none
      # The secret key that should be present is CLOUDFRONT_KEY_DATA, which should be the encoded private key
      # that allows access to CloudFront
      privateKeySecret: "my-secret"
  # enable purge _upload directories
  upload_purging:
    enabled: true
    # remove files in _upload directories which exist for a period of time, default is one week.
    age: 168h
    # the interval of the purge operations
    interval: 24h
    dryrun: false

chartmuseum:
  enabled: true
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  # Harbor defaults ChartMuseum to returning relative urls, if you want using absolute url you should enable it by change the following value to 'true'
  absoluteUrl: false
  image:
    repository: goharbor/chartmuseum-photon
    tag: v2.5.3
  replicas: 1
  revisionHistoryLimit: 10
  # resources:
  #  requests:
  #    memory: 256Mi
  #    cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:
  ## limit the number of parallel indexers
  indexLimit: 0

trivy:
  # enabled the flag to enable Trivy scanner
  enabled: true
  image:
    # repository the repository for Trivy adapter image
    repository: goharbor/trivy-adapter-photon
    # tag the tag for Trivy adapter image
    tag: v2.5.3
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  # replicas the number of Pod replicas
  replicas: 1
  # debugMode the flag to enable Trivy debug mode with more verbose scanning log
  debugMode: false
  # vulnType a comma-separated list of vulnerability types. Possible values are `os` and `library`.
  vulnType: "os,library"
  # severity a comma-separated list of severities to be checked
  severity: "UNKNOWN,LOW,MEDIUM,HIGH,CRITICAL"
  # ignoreUnfixed the flag to display only fixed vulnerabilities
  ignoreUnfixed: false
  # insecure the flag to skip verifying registry certificate
  insecure: false
  # gitHubToken the GitHub access token to download Trivy DB
  #
  # Trivy DB contains vulnerability information from NVD, Red Hat, and many other upstream vulnerability databases.
  # It is downloaded by Trivy from the GitHub release page https://github.com/aquasecurity/trivy-db/releases and cached
  # in the local file system (`/home/scanner/.cache/trivy/db/trivy.db`). In addition, the database contains the update
  # timestamp so Trivy can detect whether it should download a newer version from the Internet or use the cached one.
  # Currently, the database is updated every 12 hours and published as a new release to GitHub.
  #
  # Anonymous downloads from GitHub are subject to the limit of 60 requests per hour. Normally such rate limit is enough
  # for production operations. If, for any reason, it's not enough, you could increase the rate limit to 5000
  # requests per hour by specifying the GitHub access token. For more details on GitHub rate limiting please consult
  # https://developer.github.com/v3/#rate-limiting
  #
  # You can create a GitHub token by following the instructions in
  # https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line
  gitHubToken: ""
  # skipUpdate the flag to disable Trivy DB downloads from GitHub
  #
  # You might want to set the value of this flag to `true` in test or CI/CD environments to avoid GitHub rate limiting issues.
  # If the value is set to `true` you have to manually download the `trivy.db` file and mount it in the
  # `/home/scanner/.cache/trivy/db/trivy.db` path.
  skipUpdate: false
  # The offlineScan option prevents Trivy from sending API requests to identify dependencies.
  #
  # Scanning JAR files and pom.xml may require Internet access for better detection, but this option tries to avoid it.
  # For example, the offline mode will not try to resolve transitive dependencies in pom.xml when the dependency doesn't
  # exist in the local repositories. It means a number of detected vulnerabilities might be fewer in offline mode.
  # It would work if all the dependencies are in local.
  # This option doesn’t affect DB download. You need to specify skipUpdate as well as offlineScan in an air-gapped environment.
  offlineScan: false
  # The duration to wait for scan completion
  timeout: 5m0s
  resources:
    requests:
      cpu: 200m
      memory: 512Mi
    limits:
      cpu: 1
      memory: 1Gi
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:

notary:
  enabled: true
  server:
    # set the service account to be used, default if left empty
    serviceAccountName: ""
    # mount the service account token
    automountServiceAccountToken: false
    image:
      repository: goharbor/notary-server-photon
      tag: v2.5.3
    replicas: 1
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
    nodeSelector: {}
    tolerations: []
    affinity: {}
    ## Additional deployment annotations
    podAnnotations: {}
    ## The priority class to run the pod as
    priorityClassName:
  signer:
    # set the service account to be used, default if left empty
    serviceAccountName: ""
    # mount the service account token
    automountServiceAccountToken: false
    image:
      repository: goharbor/notary-signer-photon
      tag: v2.5.3
    replicas: 1
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
    nodeSelector: {}
    tolerations: []
    affinity: {}
    ## Additional deployment annotations
    podAnnotations: {}
    ## The priority class to run the pod as
    priorityClassName:
  # Fill the name of a kubernetes secret if you want to use your own
  # TLS certificate authority, certificate and private key for notary
  # communications.
  # The secret must contain keys named ca.crt, tls.crt and tls.key that
  # contain the CA, certificate and private key.
  # They will be generated if not set.
  secretName: ""

database:
  # if external database is used, set "type" to "external"
  # and fill the connection informations in "external" section
  type: internal
  internal:
    # set the service account to be used, default if left empty
    serviceAccountName: ""
    # mount the service account token
    automountServiceAccountToken: false
    image:
      repository: goharbor/harbor-db
      tag: v2.5.3
    # The initial superuser password for internal database
    password: "changeit"
    # The size limit for Shared memory, pgSQL use it for shared_buffer
    # More details see:
    # https://github.com/goharbor/harbor/issues/15034
    shmSizeLimit: 512Mi
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
    nodeSelector: {}
    tolerations: []
    affinity: {}
    ## The priority class to run the pod as
    priorityClassName:
    initContainer:
      migrator: {}
      # resources:
      #  requests:
      #    memory: 128Mi
      #    cpu: 100m
      permissions: {}
      # resources:
      #  requests:
      #    memory: 128Mi
      #    cpu: 100m
  external:
    host: "192.168.0.1"
    port: "5432"
    username: "user"
    password: "password"
    coreDatabase: "registry"
    notaryServerDatabase: "notary_server"
    notarySignerDatabase: "notary_signer"
    # "disable" - No SSL
    # "require" - Always SSL (skip verification)
    # "verify-ca" - Always SSL (verify that the certificate presented by the
    # server was signed by a trusted CA)
    # "verify-full" - Always SSL (verify that the certification presented by the
    # server was signed by a trusted CA and the server host name matches the one
    # in the certificate)
    sslmode: "disable"
  # The maximum number of connections in the idle connection pool per pod (core+exporter).
  # If it <=0, no idle connections are retained.
  maxIdleConns: 100
  # The maximum number of open connections to the database per pod (core+exporter).
  # If it <= 0, then there is no limit on the number of open connections.
  # Note: the default number of connections is 1024 for postgre of harbor.
  maxOpenConns: 900
  ## Additional deployment annotations
  podAnnotations: {}

redis:
  # if external Redis is used, set "type" to "external"
  # and fill the connection informations in "external" section
  type: internal
  internal:
    # set the service account to be used, default if left empty
    serviceAccountName: ""
    # mount the service account token
    automountServiceAccountToken: false
    image:
      repository: goharbor/redis-photon
      tag: v2.5.3
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
    nodeSelector: {}
    tolerations: []
    affinity: {}
    ## The priority class to run the pod as
    priorityClassName:
  external:
    # support redis, redis+sentinel
    # addr for redis: <host_redis>:<port_redis>
    # addr for redis+sentinel: <host_sentinel1>:<port_sentinel1>,<host_sentinel2>:<port_sentinel2>,<host_sentinel3>:<port_sentinel3>
    addr: "192.168.0.2:6379"
    # The name of the set of Redis instances to monitor, it must be set to support redis+sentinel
    sentinelMasterSet: ""
    # The "coreDatabaseIndex" must be "0" as the library Harbor
    # used doesn't support configuring it
    coreDatabaseIndex: "0"
    jobserviceDatabaseIndex: "1"
    registryDatabaseIndex: "2"
    chartmuseumDatabaseIndex: "3"
    trivyAdapterIndex: "5"
    password: ""
  ## Additional deployment annotations
  podAnnotations: {}

exporter:
  replicas: 1
  revisionHistoryLimit: 10
# resources:
#  requests:
#    memory: 256Mi
#    cpu: 100m
  podAnnotations: {}
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  image:
    repository: goharbor/harbor-exporter
    tag: v2.5.3
  nodeSelector: {}
  tolerations: []
  affinity: {}
  cacheDuration: 23
  cacheCleanInterval: 14400
  ## The priority class to run the pod as
  priorityClassName:

metrics:
  enabled: false
  core:
    path: /metrics
    port: 8001
  registry:
    path: /metrics
    port: 8001
  jobservice:
    path: /metrics
    port: 8001
  exporter:
    path: /metrics
    port: 8001
  ## Create prometheus serviceMonitor to scrape harbor metrics.
  ## This requires the monitoring.coreos.com/v1 CRD. Please see
  ## https://github.com/prometheus-operator/prometheus-operator/blob/master/Documentation/user-guides/getting-started.md
  ##
  serviceMonitor:
    enabled: false
    additionalLabels: {}
    # Scrape interval. If not set, the Prometheus default scrape interval is used.
    interval: ""
    # Metric relabel configs to apply to samples before ingestion.
    metricRelabelings: []
      # - action: keep
      #   regex: 'kube_(daemonset|deployment|pod|namespace|node|statefulset).+'
      #   sourceLabels: [__name__]
    # Relabel configs to apply to samples before ingestion.
    relabelings: []
      # - sourceLabels: [__meta_kubernetes_pod_node_name]
      #   separator: ;
      #   regex: ^(.*)$
      #   targetLabel: nodename
      #   replacement: $1
      #   action: replace

trace:
  enabled: false
  # trace provider: jaeger or otel
  # jaeger should be 1.26+
  provider: jaeger
  # set sample_rate to 1 if you wanna sampling 100% of trace data; set 0.5 if you wanna sampling 50% of trace data, and so forth
  sample_rate: 1
  # namespace used to differentiate different harbor services
  # namespace:
  # attributes is a key value dict contains user defined attributes used to initialize trace provider
  # attributes:
  #   application: harbor
  jaeger:
    # jaeger supports two modes:
    #   collector mode(uncomment endpoint and uncomment username, password if needed)
    #   agent mode(uncomment agent_host and agent_port)
    endpoint: http://hostname:14268/api/traces
    # username:
    # password:
    # agent_host: hostname
    # export trace data by jaeger.thrift in compact mode
    # agent_port: 6831
  otel:
    endpoint: hostname:4318
    url_path: /v1/traces
    compression: false
    insecure: true
    timeout: 10s
```



使用https：

```yaml
expose:
  # Set how to expose the service. Set the type as "ingress", "clusterIP", "nodePort" or "loadBalancer"
  # and fill the information in the corresponding section
  type: nodePort
  tls:
    # Enable TLS or not.
    # Delete the "ssl-redirect" annotations in "expose.ingress.annotations" when TLS is disabled and "expose.type" is "ingress"
    # Note: if the "expose.type" is "ingress" and TLS is disabled,
    # the port must be included in the command when pulling/pushing images.
    # Refer to https://github.com/goharbor/harbor/issues/5291 for details.
    enabled: true
    # The source of the tls certificate. Set as "auto", "secret"
    # or "none" and fill the information in the corresponding section
    # 1) auto: generate the tls certificate automatically
    # 2) secret: read the tls certificate from the specified secret.
    # The tls certificate can be generated manually or by cert manager
    # 3) none: configure no tls certificate for the ingress. If the default
    # tls certificate is configured in the ingress controller, choose this option
    certSource: auto
    auto:
      # The common name used to generate the certificate, it's necessary
      # when the type isn't "ingress"
      commonName: "192.168.0.112"
    secret:
      # The name of secret which contains keys named:
      # "tls.crt" - the certificate
      # "tls.key" - the private key
      secretName: ""
      # The name of secret which contains keys named:
      # "tls.crt" - the certificate
      # "tls.key" - the private key
      # Only needed when the "expose.type" is "ingress".
      notarySecretName: ""
  ingress:
    hosts:
      core: core.harbor.domain
      notary: notary.harbor.domain
    # set to the type of ingress controller if it has specific requirements.
    # leave as `default` for most ingress controllers.
    # set to `gce` if using the GCE ingress controller
    # set to `ncp` if using the NCP (NSX-T Container Plugin) ingress controller
    controller: default
    ## Allow .Capabilities.KubeVersion.Version to be overridden while creating ingress
    kubeVersionOverride: ""
    className: ""
    annotations:
      # note different ingress controllers may require a different ssl-redirect annotation
      # for Envoy, use ingress.kubernetes.io/force-ssl-redirect: "true" and remove the nginx lines below
      ingress.kubernetes.io/ssl-redirect: "true"
      ingress.kubernetes.io/proxy-body-size: "0"
      nginx.ingress.kubernetes.io/ssl-redirect: "true"
      nginx.ingress.kubernetes.io/proxy-body-size: "0"
    notary:
      # notary ingress-specific annotations
      annotations: {}
      # notary ingress-specific labels
      labels: {}
    harbor:
      # harbor ingress-specific annotations
      annotations: {}
      # harbor ingress-specific labels
      labels: {}
  clusterIP:
    # The name of ClusterIP service
    name: harbor
    # Annotations on the ClusterIP service
    annotations: {}
    ports:
      # The service port Harbor listens on when serving HTTP
      httpPort: 80
      # The service port Harbor listens on when serving HTTPS
      httpsPort: 443
      # The service port Notary listens on. Only needed when notary.enabled
      # is set to true
      notaryPort: 4443
  nodePort:
    # The name of NodePort service
    name: harbor
    ports:
      http:
        # The service port Harbor listens on when serving HTTP
        port: 80
        # The node port Harbor listens on when serving HTTP
        nodePort: 30002
      https:
        # The service port Harbor listens on when serving HTTPS
        port: 443
        # The node port Harbor listens on when serving HTTPS
        nodePort: 30003
      # Only needed when notary.enabled is set to true
      notary:
        # The service port Notary listens on
        port: 4443
        # The node port Notary listens on
        nodePort: 30004
  loadBalancer:
    # The name of LoadBalancer service
    name: harbor
    # Set the IP if the LoadBalancer supports assigning IP
    IP: ""
    ports:
      # The service port Harbor listens on when serving HTTP
      httpPort: 80
      # The service port Harbor listens on when serving HTTPS
      httpsPort: 443
      # The service port Notary listens on. Only needed when notary.enabled
      # is set to true
      notaryPort: 4443
    annotations: {}
    sourceRanges: []

# The external URL for Harbor core service. It is used to
# 1) populate the docker/helm commands showed on portal
# 2) populate the token service URL returned to docker/notary client
#
# Format: protocol://domain[:port]. Usually:
# 1) if "expose.type" is "ingress", the "domain" should be
# the value of "expose.ingress.hosts.core"
# 2) if "expose.type" is "clusterIP", the "domain" should be
# the value of "expose.clusterIP.name"
# 3) if "expose.type" is "nodePort", the "domain" should be
# the IP address of k8s node
#
# If Harbor is deployed behind the proxy, set it as the URL of proxy
externalURL: https://192.168.0.112:30003

# The internal TLS used for harbor components secure communicating. In order to enable https
# in each components tls cert files need to provided in advance.
internalTLS:
  # If internal TLS enabled
  enabled: false
  # There are three ways to provide tls
  # 1) "auto" will generate cert automatically
  # 2) "manual" need provide cert file manually in following value
  # 3) "secret" internal certificates from secret
  certSource: "auto"
  # The content of trust ca, only available when `certSource` is "manual"
  trustCa: ""
  # core related cert configuration
  core:
    # secret name for core's tls certs
    secretName: ""
    # Content of core's TLS cert file, only available when `certSource` is "manual"
    crt: ""
    # Content of core's TLS key file, only available when `certSource` is "manual"
    key: ""
  # jobservice related cert configuration
  jobservice:
    # secret name for jobservice's tls certs
    secretName: ""
    # Content of jobservice's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of jobservice's TLS key file, only available when `certSource` is "manual"
    key: ""
  # registry related cert configuration
  registry:
    # secret name for registry's tls certs
    secretName: ""
    # Content of registry's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of registry's TLS key file, only available when `certSource` is "manual"
    key: ""
  # portal related cert configuration
  portal:
    # secret name for portal's tls certs
    secretName: ""
    # Content of portal's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of portal's TLS key file, only available when `certSource` is "manual"
    key: ""
  # chartmuseum related cert configuration
  chartmuseum:
    # secret name for chartmuseum's tls certs
    secretName: ""
    # Content of chartmuseum's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of chartmuseum's TLS key file, only available when `certSource` is "manual"
    key: ""
  # trivy related cert configuration
  trivy:
    # secret name for trivy's tls certs
    secretName: ""
    # Content of trivy's TLS key file, only available when `certSource` is "manual"
    crt: ""
    # Content of trivy's TLS key file, only available when `certSource` is "manual"
    key: ""

ipFamily:
  # ipv6Enabled set to true if ipv6 is enabled in cluster, currently it affected the nginx related component
  ipv6:
    enabled: true
  # ipv4Enabled set to true if ipv4 is enabled in cluster, currently it affected the nginx related component
  ipv4:
    enabled: true

# The persistence is enabled by default and a default StorageClass
# is needed in the k8s cluster to provision volumes dynamically.
# Specify another StorageClass in the "storageClass" or set "existingClaim"
# if you already have existing persistent volumes to use
#
# For storing images and charts, you can also use "azure", "gcs", "s3",
# "swift" or "oss". Set it in the "imageChartStorage" section
persistence:
  enabled: true
  # Setting it to "keep" to avoid removing PVCs during a helm delete
  # operation. Leaving it empty will delete PVCs after the chart deleted
  # (this does not apply for PVCs that are created for internal database
  # and redis components, i.e. they are never deleted automatically)
  resourcePolicy: "keep"
  persistentVolumeClaim:
    registry:
      # Use the existing PVC which must be created manually before bound,
      # and specify the "subPath" if the PVC is shared with other components
      existingClaim: ""
      # Specify the "storageClass" used to provision the volume. Or the default
      # StorageClass will be used (the default).
      # Set it to "-" to disable dynamic provisioning
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 5Gi
      annotations: {}
    chartmuseum:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 5Gi
      annotations: {}
    jobservice:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 1Gi
      annotations: {}
    # If external database is used, the following settings for database will
    # be ignored
    database:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 1Gi
      annotations: {}
    # If external Redis is used, the following settings for Redis will
    # be ignored
    redis:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 1Gi
      annotations: {}
    trivy:
      existingClaim: ""
      storageClass: ""
      subPath: ""
      accessMode: ReadWriteOnce
      size: 5Gi
      annotations: {}
  # Define which storage backend is used for registry and chartmuseum to store
  # images and charts. Refer to
  # https://github.com/docker/distribution/blob/master/docs/configuration.md#storage
  # for the detail.
  imageChartStorage:
    # Specify whether to disable `redirect` for images and chart storage, for
    # backends which not supported it (such as using minio for `s3` storage type), please disable
    # it. To disable redirects, simply set `disableredirect` to `true` instead.
    # Refer to
    # https://github.com/docker/distribution/blob/master/docs/configuration.md#redirect
    # for the detail.
    disableredirect: false
    # Specify the "caBundleSecretName" if the storage service uses a self-signed certificate.
    # The secret must contain keys named "ca.crt" which will be injected into the trust store
    # of registry's and chartmuseum's containers.
    # caBundleSecretName:

    # Specify the type of storage: "filesystem", "azure", "gcs", "s3", "swift",
    # "oss" and fill the information needed in the corresponding section. The type
    # must be "filesystem" if you want to use persistent volumes for registry
    # and chartmuseum
    type: filesystem
    filesystem:
      rootdirectory: /storage
      #maxthreads: 100
    azure:
      accountname: accountname
      accountkey: base64encodedaccountkey
      container: containername
      #realm: core.windows.net
    gcs:
      bucket: bucketname
      # The base64 encoded json file which contains the key
      encodedkey: base64-encoded-json-key-file
      #rootdirectory: /gcs/object/name/prefix
      #chunksize: "5242880"
    s3:
      region: us-west-1
      bucket: bucketname
      #accesskey: awsaccesskey
      #secretkey: awssecretkey
      #regionendpoint: http://myobjects.local
      #encrypt: false
      #keyid: mykeyid
      #secure: true
      #skipverify: false
      #v4auth: true
      #chunksize: "5242880"
      #rootdirectory: /s3/object/name/prefix
      #storageclass: STANDARD
      #multipartcopychunksize: "33554432"
      #multipartcopymaxconcurrency: 100
      #multipartcopythresholdsize: "33554432"
    swift:
      authurl: https://storage.myprovider.com/v3/auth
      username: username
      password: password
      container: containername
      #region: fr
      #tenant: tenantname
      #tenantid: tenantid
      #domain: domainname
      #domainid: domainid
      #trustid: trustid
      #insecureskipverify: false
      #chunksize: 5M
      #prefix:
      #secretkey: secretkey
      #accesskey: accesskey
      #authversion: 3
      #endpointtype: public
      #tempurlcontainerkey: false
      #tempurlmethods:
    oss:
      accesskeyid: accesskeyid
      accesskeysecret: accesskeysecret
      region: regionname
      bucket: bucketname
      #endpoint: endpoint
      #internal: false
      #encrypt: false
      #secure: true
      #chunksize: 10M
      #rootdirectory: rootdirectory

imagePullPolicy: IfNotPresent

# Use this set to assign a list of default pullSecrets
imagePullSecrets:
#  - name: docker-registry-secret
#  - name: internal-registry-secret

# The update strategy for deployments with persistent volumes(jobservice, registry
# and chartmuseum): "RollingUpdate" or "Recreate"
# Set it as "Recreate" when "RWM" for volumes isn't supported
updateStrategy:
  type: RollingUpdate

# debug, info, warning, error or fatal
logLevel: info

# The initial password of Harbor admin. Change it from portal after launching Harbor
harborAdminPassword: "Harbor12345"

# The name of the secret which contains key named "ca.crt". Setting this enables the
# download link on portal to download the CA certificate when the certificate isn't
# generated automatically
caSecretName: ""

# The secret key used for encryption. Must be a string of 16 chars.
secretKey: "not-a-secure-key"

# The proxy settings for updating trivy vulnerabilities from the Internet and replicating
# artifacts from/to the registries that cannot be reached directly
proxy:
  httpProxy:
  httpsProxy:
  noProxy: 127.0.0.1,localhost,.local,.internal
  components:
    - core
    - jobservice
    - trivy

# Run the migration job via helm hook
enableMigrateHelmHook: false

# The custom ca bundle secret, the secret must contain key named "ca.crt"
# which will be injected into the trust store for chartmuseum, core, jobservice, registry, trivy components
# caBundleSecretName: ""

## UAA Authentication Options
# If you're using UAA for authentication behind a self-signed
# certificate you will need to provide the CA Cert.
# Set uaaSecretName below to provide a pre-created secret that
# contains a base64 encoded CA Certificate named `ca.crt`.
# uaaSecretName:

# If service exposed via "ingress", the Nginx will not be used
nginx:
  image:
    repository: goharbor/nginx-photon
    tag: v2.5.3
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  replicas: 1
  revisionHistoryLimit: 10
  # resources:
  #  requests:
  #    memory: 256Mi
  #    cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:

portal:
  image:
    repository: goharbor/harbor-portal
    tag: v2.5.3
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  replicas: 1
  revisionHistoryLimit: 10
  # resources:
  #  requests:
  #    memory: 256Mi
  #    cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:

core:
  image:
    repository: goharbor/harbor-core
    tag: v2.5.3
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  replicas: 1
  revisionHistoryLimit: 10
  ## Startup probe values
  startupProbe:
    enabled: true
    initialDelaySeconds: 10
  # resources:
  #  requests:
  #    memory: 256Mi
  #    cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  # Secret is used when core server communicates with other components.
  # If a secret key is not specified, Helm will generate one.
  # Must be a string of 16 chars.
  secret: ""
  # Fill the name of a kubernetes secret if you want to use your own
  # TLS certificate and private key for token encryption/decryption.
  # The secret must contain keys named:
  # "tls.crt" - the certificate
  # "tls.key" - the private key
  # The default key pair will be used if it isn't set
  secretName: ""
  # The XSRF key. Will be generated automatically if it isn't specified
  xsrfKey: ""
  ## The priority class to run the pod as
  priorityClassName:
  # The time duration for async update artifact pull_time and repository
  # pull_count, the unit is second. Will be 10 seconds if it isn't set.
  # eg. artifactPullAsyncFlushDuration: 10
  artifactPullAsyncFlushDuration:

jobservice:
  image:
    repository: goharbor/harbor-jobservice
    tag: v2.5.3
  replicas: 1
  revisionHistoryLimit: 10
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  maxJobWorkers: 10
  # The logger for jobs: "file", "database" or "stdout"
  jobLoggers:
    - file
    # - database
    # - stdout
  # The jobLogger sweeper duration (ignored if `jobLogger` is `stdout`)
  loggerSweeperDuration: 14 #days

  # resources:
  #   requests:
  #     memory: 256Mi
  #     cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  # Secret is used when job service communicates with other components.
  # If a secret key is not specified, Helm will generate one.
  # Must be a string of 16 chars.
  secret: ""
  ## The priority class to run the pod as
  priorityClassName:

registry:
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  registry:
    image:
      repository: goharbor/registry-photon
      tag: v2.5.3
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
  controller:
    image:
      repository: goharbor/harbor-registryctl
      tag: v2.5.3

    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
  replicas: 1
  revisionHistoryLimit: 10
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:
  # Secret is used to secure the upload state from client
  # and registry storage backend.
  # See: https://github.com/docker/distribution/blob/master/docs/configuration.md#http
  # If a secret key is not specified, Helm will generate one.
  # Must be a string of 16 chars.
  secret: ""
  # If true, the registry returns relative URLs in Location headers. The client is responsible for resolving the correct URL.
  relativeurls: false
  credentials:
    username: "harbor_registry_user"
    password: "harbor_registry_password"
    # Login and password in htpasswd string format. Excludes `registry.credentials.username`  and `registry.credentials.password`. May come in handy when integrating with tools like argocd or flux. This allows the same line to be generated each time the template is rendered, instead of the `htpasswd` function from helm, which generates different lines each time because of the salt.
    # htpasswdString: $apr1$XLefHzeG$Xl4.s00sMSCCcMyJljSZb0 # example string
  middleware:
    enabled: false
    type: cloudFront
    cloudFront:
      baseurl: example.cloudfront.net
      keypairid: KEYPAIRID
      duration: 3000s
      ipfilteredby: none
      # The secret key that should be present is CLOUDFRONT_KEY_DATA, which should be the encoded private key
      # that allows access to CloudFront
      privateKeySecret: "my-secret"
  # enable purge _upload directories
  upload_purging:
    enabled: true
    # remove files in _upload directories which exist for a period of time, default is one week.
    age: 168h
    # the interval of the purge operations
    interval: 24h
    dryrun: false

chartmuseum:
  enabled: true
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  # Harbor defaults ChartMuseum to returning relative urls, if you want using absolute url you should enable it by change the following value to 'true'
  absoluteUrl: false
  image:
    repository: goharbor/chartmuseum-photon
    tag: v2.5.3
  replicas: 1
  revisionHistoryLimit: 10
  # resources:
  #  requests:
  #    memory: 256Mi
  #    cpu: 100m
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:
  ## limit the number of parallel indexers
  indexLimit: 0

trivy:
  # enabled the flag to enable Trivy scanner
  enabled: true
  image:
    # repository the repository for Trivy adapter image
    repository: goharbor/trivy-adapter-photon
    # tag the tag for Trivy adapter image
    tag: v2.5.3
  # set the service account to be used, default if left empty
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  # replicas the number of Pod replicas
  replicas: 1
  # debugMode the flag to enable Trivy debug mode with more verbose scanning log
  debugMode: false
  # vulnType a comma-separated list of vulnerability types. Possible values are `os` and `library`.
  vulnType: "os,library"
  # severity a comma-separated list of severities to be checked
  severity: "UNKNOWN,LOW,MEDIUM,HIGH,CRITICAL"
  # ignoreUnfixed the flag to display only fixed vulnerabilities
  ignoreUnfixed: false
  # insecure the flag to skip verifying registry certificate
  insecure: false
  # gitHubToken the GitHub access token to download Trivy DB
  #
  # Trivy DB contains vulnerability information from NVD, Red Hat, and many other upstream vulnerability databases.
  # It is downloaded by Trivy from the GitHub release page https://github.com/aquasecurity/trivy-db/releases and cached
  # in the local file system (`/home/scanner/.cache/trivy/db/trivy.db`). In addition, the database contains the update
  # timestamp so Trivy can detect whether it should download a newer version from the Internet or use the cached one.
  # Currently, the database is updated every 12 hours and published as a new release to GitHub.
  #
  # Anonymous downloads from GitHub are subject to the limit of 60 requests per hour. Normally such rate limit is enough
  # for production operations. If, for any reason, it's not enough, you could increase the rate limit to 5000
  # requests per hour by specifying the GitHub access token. For more details on GitHub rate limiting please consult
  # https://developer.github.com/v3/#rate-limiting
  #
  # You can create a GitHub token by following the instructions in
  # https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line
  gitHubToken: ""
  # skipUpdate the flag to disable Trivy DB downloads from GitHub
  #
  # You might want to set the value of this flag to `true` in test or CI/CD environments to avoid GitHub rate limiting issues.
  # If the value is set to `true` you have to manually download the `trivy.db` file and mount it in the
  # `/home/scanner/.cache/trivy/db/trivy.db` path.
  skipUpdate: false
  # The offlineScan option prevents Trivy from sending API requests to identify dependencies.
  #
  # Scanning JAR files and pom.xml may require Internet access for better detection, but this option tries to avoid it.
  # For example, the offline mode will not try to resolve transitive dependencies in pom.xml when the dependency doesn't
  # exist in the local repositories. It means a number of detected vulnerabilities might be fewer in offline mode.
  # It would work if all the dependencies are in local.
  # This option doesn’t affect DB download. You need to specify skipUpdate as well as offlineScan in an air-gapped environment.
  offlineScan: false
  # The duration to wait for scan completion
  timeout: 5m0s
  resources:
    requests:
      cpu: 200m
      memory: 512Mi
    limits:
      cpu: 1
      memory: 1Gi
  nodeSelector: {}
  tolerations: []
  affinity: {}
  ## Additional deployment annotations
  podAnnotations: {}
  ## The priority class to run the pod as
  priorityClassName:

notary:
  enabled: true
  server:
    # set the service account to be used, default if left empty
    serviceAccountName: ""
    # mount the service account token
    automountServiceAccountToken: false
    image:
      repository: goharbor/notary-server-photon
      tag: v2.5.3
    replicas: 1
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
    nodeSelector: {}
    tolerations: []
    affinity: {}
    ## Additional deployment annotations
    podAnnotations: {}
    ## The priority class to run the pod as
    priorityClassName:
  signer:
    # set the service account to be used, default if left empty
    serviceAccountName: ""
    # mount the service account token
    automountServiceAccountToken: false
    image:
      repository: goharbor/notary-signer-photon
      tag: v2.5.3
    replicas: 1
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
    nodeSelector: {}
    tolerations: []
    affinity: {}
    ## Additional deployment annotations
    podAnnotations: {}
    ## The priority class to run the pod as
    priorityClassName:
  # Fill the name of a kubernetes secret if you want to use your own
  # TLS certificate authority, certificate and private key for notary
  # communications.
  # The secret must contain keys named ca.crt, tls.crt and tls.key that
  # contain the CA, certificate and private key.
  # They will be generated if not set.
  secretName: ""

database:
  # if external database is used, set "type" to "external"
  # and fill the connection informations in "external" section
  type: internal
  internal:
    # set the service account to be used, default if left empty
    serviceAccountName: ""
    # mount the service account token
    automountServiceAccountToken: false
    image:
      repository: goharbor/harbor-db
      tag: v2.5.3
    # The initial superuser password for internal database
    password: "changeit"
    # The size limit for Shared memory, pgSQL use it for shared_buffer
    # More details see:
    # https://github.com/goharbor/harbor/issues/15034
    shmSizeLimit: 512Mi
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
    nodeSelector: {}
    tolerations: []
    affinity: {}
    ## The priority class to run the pod as
    priorityClassName:
    initContainer:
      migrator: {}
      # resources:
      #  requests:
      #    memory: 128Mi
      #    cpu: 100m
      permissions: {}
      # resources:
      #  requests:
      #    memory: 128Mi
      #    cpu: 100m
  external:
    host: "192.168.0.1"
    port: "5432"
    username: "user"
    password: "password"
    coreDatabase: "registry"
    notaryServerDatabase: "notary_server"
    notarySignerDatabase: "notary_signer"
    # "disable" - No SSL
    # "require" - Always SSL (skip verification)
    # "verify-ca" - Always SSL (verify that the certificate presented by the
    # server was signed by a trusted CA)
    # "verify-full" - Always SSL (verify that the certification presented by the
    # server was signed by a trusted CA and the server host name matches the one
    # in the certificate)
    sslmode: "disable"
  # The maximum number of connections in the idle connection pool per pod (core+exporter).
  # If it <=0, no idle connections are retained.
  maxIdleConns: 100
  # The maximum number of open connections to the database per pod (core+exporter).
  # If it <= 0, then there is no limit on the number of open connections.
  # Note: the default number of connections is 1024 for postgre of harbor.
  maxOpenConns: 900
  ## Additional deployment annotations
  podAnnotations: {}

redis:
  # if external Redis is used, set "type" to "external"
  # and fill the connection informations in "external" section
  type: internal
  internal:
    # set the service account to be used, default if left empty
    serviceAccountName: ""
    # mount the service account token
    automountServiceAccountToken: false
    image:
      repository: goharbor/redis-photon
      tag: v2.5.3
    # resources:
    #  requests:
    #    memory: 256Mi
    #    cpu: 100m
    nodeSelector: {}
    tolerations: []
    affinity: {}
    ## The priority class to run the pod as
    priorityClassName:
  external:
    # support redis, redis+sentinel
    # addr for redis: <host_redis>:<port_redis>
    # addr for redis+sentinel: <host_sentinel1>:<port_sentinel1>,<host_sentinel2>:<port_sentinel2>,<host_sentinel3>:<port_sentinel3>
    addr: "192.168.0.2:6379"
    # The name of the set of Redis instances to monitor, it must be set to support redis+sentinel
    sentinelMasterSet: ""
    # The "coreDatabaseIndex" must be "0" as the library Harbor
    # used doesn't support configuring it
    coreDatabaseIndex: "0"
    jobserviceDatabaseIndex: "1"
    registryDatabaseIndex: "2"
    chartmuseumDatabaseIndex: "3"
    trivyAdapterIndex: "5"
    password: ""
  ## Additional deployment annotations
  podAnnotations: {}

exporter:
  replicas: 1
  revisionHistoryLimit: 10
# resources:
#  requests:
#    memory: 256Mi
#    cpu: 100m
  podAnnotations: {}
  serviceAccountName: ""
  # mount the service account token
  automountServiceAccountToken: false
  image:
    repository: goharbor/harbor-exporter
    tag: v2.5.3
  nodeSelector: {}
  tolerations: []
  affinity: {}
  cacheDuration: 23
  cacheCleanInterval: 14400
  ## The priority class to run the pod as
  priorityClassName:

metrics:
  enabled: false
  core:
    path: /metrics
    port: 8001
  registry:
    path: /metrics
    port: 8001
  jobservice:
    path: /metrics
    port: 8001
  exporter:
    path: /metrics
    port: 8001
  ## Create prometheus serviceMonitor to scrape harbor metrics.
  ## This requires the monitoring.coreos.com/v1 CRD. Please see
  ## https://github.com/prometheus-operator/prometheus-operator/blob/master/Documentation/user-guides/getting-started.md
  ##
  serviceMonitor:
    enabled: false
    additionalLabels: {}
    # Scrape interval. If not set, the Prometheus default scrape interval is used.
    interval: ""
    # Metric relabel configs to apply to samples before ingestion.
    metricRelabelings: []
      # - action: keep
      #   regex: 'kube_(daemonset|deployment|pod|namespace|node|statefulset).+'
      #   sourceLabels: [__name__]
    # Relabel configs to apply to samples before ingestion.
    relabelings: []
      # - sourceLabels: [__meta_kubernetes_pod_node_name]
      #   separator: ;
      #   regex: ^(.*)$
      #   targetLabel: nodename
      #   replacement: $1
      #   action: replace

trace:
  enabled: false
  # trace provider: jaeger or otel
  # jaeger should be 1.26+
  provider: jaeger
  # set sample_rate to 1 if you wanna sampling 100% of trace data; set 0.5 if you wanna sampling 50% of trace data, and so forth
  sample_rate: 1
  # namespace used to differentiate different harbor services
  # namespace:
  # attributes is a key value dict contains user defined attributes used to initialize trace provider
  # attributes:
  #   application: harbor
  jaeger:
    # jaeger supports two modes:
    #   collector mode(uncomment endpoint and uncomment username, password if needed)
    #   agent mode(uncomment agent_host and agent_port)
    endpoint: http://hostname:14268/api/traces
    # username:
    # password:
    # agent_host: hostname
    # export trace data by jaeger.thrift in compact mode
    # agent_port: 6831
  otel:
    endpoint: hostname:4318
    url_path: /v1/traces
    compression: false
    insecure: true
    timeout: 10s
```

点击`安装`即可。



下载证书

使用https安装之后还需要安装证书到各个节点，harbor的自签名证书在 pod/harbor-registry-core-xxx内部的容器内，进入容器可以看到

```bash
sh-5.0$ pwd
/etc/core/ca
sh-5.0$ ls
ca.crt  tls.crt  tls.key
```

这些就是harbor的自签名证书了，但是这些不是文件，而是软连接，后面用 kubectl cp 复制时会报错，查看文件列表：

```bash
ls -al /etc/core/ca
```

```bash
sh-5.0$ ls -al /etc/core/ca
total 4
drwxrwsrwt 3 root harbor  140 2023-07-03 05:40 .
drwxr-xr-x 4 root root   4096 2023-07-03 05:40 ..
drwxr-sr-x 2 root harbor  100 2023-07-03 05:40 ..2023_07_03_05_40_56.411351526
lrwxrwxrwx 1 root harbor   31 2023-07-03 05:40 ..data -> ..2023_07_03_05_40_56.411351526
lrwxrwxrwx 1 root harbor   13 2023-07-03 05:40 ca.crt -> ..data/ca.crt
lrwxrwxrwx 1 root harbor   14 2023-07-03 05:40 tls.crt -> ..data/tls.crt
lrwxrwxrwx 1 root harbor   14 2023-07-03 05:40 tls.key -> ..data/tls.key
```

可以看到这些文件指向 `..data/xx` 的文件，而  `..data` 指向同目录下的`..2023_07_03_05_40_56.411351526`，这里目录名是系统自动生成的。查看文件：

```bash
sh-5.0$ ls -al /etc/core/ca/..2023_07_03_05_40_56.411351526
total 12
drwxr-sr-x 2 root harbor  100 2023-07-03 05:40 .
drwxrwsrwt 3 root harbor  140 2023-07-03 05:40 ..
-rw-r--r-- 1 root harbor 1127 2023-07-03 05:40 ca.crt
-rw-r--r-- 1 root harbor 1155 2023-07-03 05:40 tls.crt
-rw-r--r-- 1 root harbor 1675 2023-07-03 05:40 tls.key
```

这些就是证书文件了，但是由于`kubectl cp`指令存在问题，需要先把文件复制到工作目录，也就是一进入容器的目录，重新进入harbor-core容器，执行：

```bash
sh-5.0$ pwd
/harbor
```

可以看到harbor的工作目录是： `/harbor`，在容器内创建目录存储证书文件

```bash
mkdir -p /harbor/ca
```

复制证书文件：

```bash
cp -r /etc/core/ca/..2023_07_03_05_40_56.411351526/* /harbor/ca
```



回到`k8s-master01`节点，通过指令下载这些证书先，查看pod信息：

```bash
kubectl get pod -A
```

```
project-test                   harbor-registry-chartmuseum-5cc9f86bb-7jgtz                       1/1     Running     0               11m
project-test                   harbor-registry-core-845696ff5c-6bdgq                             1/1     Running     0               11m
project-test                   harbor-registry-database-0                                        1/1     Running     0               11m
project-test                   harbor-registry-jobservice-56cdc4df56-wb7l5                       1/1     Running     0               11m
project-test                   harbor-registry-nginx-85fcc676bd-2l89p                            1/1     Running     0               11m
project-test                   harbor-registry-notary-server-548dffbcb9-kbtxg                    1/1     Running     0               11m
project-test                   harbor-registry-notary-signer-6d47ff7ccb-t5h5f                    1/1     Running     0               11m
project-test                   harbor-registry-portal-64bf959f98-l57tf                           1/1     Running     0               11m
project-test                   harbor-registry-redis-0                                           1/1     Running     0               11m
project-test                   harbor-registry-registry-5bb64cbb55-kf5dq                         2/2     Running     0               11m
project-test                   harbor-registry-trivy-0                                           1/1     Running     0               11m
```

找到 harbor-registry-core 的pod的namespace和全名，执行：

```bash
mkdir -p ~/harbor/ca
```

可以使用 `kubectl cp` 命令将容器内容复制到本地。命令的格式如下：

```bash
kubectl cp <namespace>/<pod_name>:<container_path> <local_path>
```

注意 container_path 不能使用绝对路径，必须使用相对于工作目录`/harbor`的相对路径，这里改为：

```bash
kubectl cp project-test/harbor-registry-core-845696ff5c-6bdgq:ca ~/harbor/ca
```

这样就把证书文件复制到 `k8s-master01`节点了。

由于部分子节点不能执行`kubectl cp`指令，这里在其他所有节点使用`scp`指令从`k8s-master01`复制到所有其他节点上，其他的所有节点执行：

```bash
mkdir -p ~/harbor/ca
```

```bash
scp -rP 22 root@192.168.0.112:/root/harbor/ca/* /root/harbor/ca
```

输入yes和ssh连接账号（root）的密码即可。



安装证书

所有节点执行：

1 复制证书文件到证书目录

```bash
sudo cp ~/harbor/ca/* /usr/local/share/ca-certificates
```

2 刷新证书

update-ca-certificates 会添加 /etc/ca-certificates.conf 配置文件中指定的证书，另外所有 /usr/local/share/ca-certificates/*.crt 会被列为隐式信任

```bash
sudo update-ca-certificates
```

后续有需要可以删除证书

```bash
sudo rm /usr/local/share/ca-certificates/root_ca.crt
```

```bash
sudo update-ca-certificates --fresh
```

输出：

```
root@k8s-node01:~# sudo update-ca-certificates
Updating certificates in /etc/ssl/certs...
rehash: warning: skipping ca-certificates.crt,it does not contain exactly one certificate or CRL
2 added, 0 removed; done.
Running hooks in /etc/ca-certificates/update.d...
done.
```

所有节点的容器重启：

```bash
systemctl restart containerd
```

这样各个节点上 `harbor` 的证书就安装完成了。

为了访问方便，可以将证书通过sftp下载到开发机（windows），双击证书安装 

点击`安装`，导入存储位置选择 `本地计算机`，选择 `将所有的证书都放入下列存储`，点击`浏览`，选择 `受信任的根证书颁发机构`，点击`确定`，点击`下一步`，点击`完成`。



安装完成后访问Harbor：

http://192.168.0.112:30002

https://192.168.0.112:30003

默认帐户和密码 `admin/Harbor12345`



创建项目：

project-test

私有



修改机器人账号的默认后缀

https://number1.co.za/rancher-cannot-use-harbor-robot-account-imagepullbackoff-pull-access-denied/

`系统管理-配置管理-系统设置-机器人账户名称前缀`



创建机器人账号：

test
过期时间：-1

描述： 测试项目Harbor机器人账号

覆盖项目：project-test



点击添加，此时会提示产生了随机密钥，只会显示一次：

```bash
名称：robot_test
随机密钥：eSBDoEqVAG1WlY2u9lakzkXR7TbjE0Tc
```



在所有k8s节点上执行：

```bash
nerdctl login 192.168.0.112:30003 -u robot_test -p eSBDoEqVAG1WlY2u9lakzkXR7TbjE0Tc
```

输出：

```bash
WARN[0000] WARNING! Using --password via the CLI is insecure. Use --password-stdin.
WARNING: Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
```



在非k8s节点上，执行：

```bash
podman login 192.168.0.112:30003 -u robot_test -p eSBDoEqVAG1WlY2u9lakzkXR7TbjE0Tc
```

此时会报错

```bash
Error: authenticating creds for "192.168.0.112:30003": pinging container registry 192.168.0.112:30003: Get "https://192.168.0.112:30003/v2/": x509: certificate signed by unknown authority
```

解决办法：参考  

https://blog.csdn.net/tom_fans/article/details/107620248

https://docs.docker.com/engine/security/certificates/



先将Harbor的证书上传到服务器，通过sftp或者scp，保存到任意目录，如 `~/webapp/harbor/ca`，然后将该目录下的证书都复制到指定目录：

创建目录：

```bash
sudo mkdir -p /etc/docker/certs.d
```

在创建子目录，名为访问地址。如域名或者ip+port，如：`harbor.test.com`， 这里使用ip+port

```
mkdir -p /etc/docker/certs.d/192.168.0.112:30003
```

复制Harbor的证书到这个指定目录：

```bash
sudo cp ~/webapp/harbor/ca/* /etc/docker/certs.d/192.168.0.112:30003
```

再执行：

```bash
podman login 192.168.0.112:30003 -u robot_test -p eSBDoEqVAG1WlY2u9lakzkXR7TbjE0Tc
```

还是会报错：

```bash
Error: authenticating creds for "192.168.0.112:30003": creating new docker client: missing client certificate tls.cert for key tls.key
```

根据提示，是找不到与 `tls.key` 对应的 `tls.cert` 文件，将 `tls.crt` 重命名或者复制一份

```bash
sudo cp /etc/docker/certs.d/192.168.0.112:30003/tls.crt /etc/docker/certs.d/192.168.0.112:30003/tls.cert
```

此时在服务器内就存有证书：

```bash
/etc/docker/certs.d/192.168.0.112:30003/ca.crt
/etc/docker/certs.d/192.168.0.112:30003/tls.cert
/etc/docker/certs.d/192.168.0.112:30003/tls.key
```

再使用podman登录Harbor

```bash
podman login 192.168.0.112:30003 -u robot_test -p eSBDoEqVAG1WlY2u9lakzkXR7TbjE0Tc
```

输出：

```bash
Login Succeeded!
```

登录成功。



在流水线中推送镜像前需要登录Harbor，由于devops-worker创建的 Jenkins-Agent：maven容器中也没有配置Harbor的证书，因此在流水线中会无法登录和推送镜像。需要把证书文件挂载到Agent中，但是Agent是由 kubesphere-devops-system 项目中的ConfigMap定义的模板创建的，因此要修改创建Agent的模板，修改 企业空间：system-workspace / 项目空间：kubesphere-devops-system / 配置-配置字典/ jenkins-casc-config ，编辑设置 `jenkinsyaml` 和 `jenkins_user.yaml`，找到maven代理这部分定义，修改为：

```
                containers:
                - name: "maven"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                  volumeMounts:
                  - name: config-volume
                    mountPath: /opt/apache-maven-3.5.3/conf/settings.xml
                    subPath: settings.xml
                  - name: podman-config-volume
                    mountPath: /etc/containers/registries.conf
                    subPath: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/ca.crt
                    subPath: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.cert
                    subPath: tls.cert
                  - name: harbor-ca-tls-key-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.key
                    subPath: tls.key
                volumes:
                  - name: config-volume
                    configMap:
                      name: ks-devops-agent
                      items:
                      - key: MavenSetting
                        path: settings.xml
                  - name: podman-config-volume
                    configMap:
                      name: ks-devops-agent
                      items:
                      - key: PodmanSetting
                        path: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: ca.crt
                        path: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.cert
                        path: tls.cert
                  - name: harbor-ca-tls-key-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.key
                        path: tls.key
                securityContext:
                  fsGroup: 1000
```

保存，然后到  企业空间：system-workspace / 项目空间：kubesphere-devops-worker/ 配置-配置字典- 创建：

基本信息：

```bash
名称：harbor-ca
别名：project-test-harbor-ca
描述：Harbor镜像服务器https证书
```

点击`下一步`

数据设置：添加Harbor自签名证书的三个文件即可：

键：ca.crt

值：

```bash
-----BEGIN CERTIFICATE-----
MIIDEzCCAfugAwIBAgIQUN99VsO3O6Pn/UR6ym1j4TANBgkqhkiG9w0BAQsFADAU
MRIwEAYDVQQDEwloYXJib3ItY2EwHhcNMjMwNzAzMDU0MDUwWhcNMjQwNzAyMDU0
MDUwWjAUMRIwEAYDVQQDEwloYXJib3ItY2EwggEiMA0GCSqGSIb3DQEBAQUAA4IB
DwAwggEKAoIBAQDN6Ke7KfdLeMD0wRgPgTmKN4L2XMBty63I0DPMGRsLFJZbWMJQ
G1PYwsdZeKWCsC+hzpofaeYoTZG+0J6rObcqSLPucZsbnlkqb0RaVW8JUHVc2SPB
Wxv9axZt8jKHHAJ7u31Gn7WXjErQZHOJ22WRY1JD9ISSCNIvewc7GwdBILcECtoq
FDbrH0kCNO1FjrNyfQK7/pmDX5eQ45RlK/M7VO47Nf+IdbHulOjVLhjFUTBwBGfo
mvieVGO4n5yvmNSOBPUhsGiMTOSI61g7HeyaITHMLTu9unaVbtvhXtd+++0gTMVU
OaVi/nqtu3hrzw5xEUFDKyP/aLrK/hYRixutAgMBAAGjYTBfMA4GA1UdDwEB/wQE
AwICpDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwDwYDVR0TAQH/BAUw
AwEB/zAdBgNVHQ4EFgQU72hvl68lJb7Fr07jwXPH3DkzdTYwDQYJKoZIhvcNAQEL
BQADggEBAJA5g3lxWYkcPfWMiuU+yrSUiqMzQ1JdaWFcieD1/Jyn8BZ0PBCHOB4a
s7hKEZFbDxn7CKf149EHweGmYogwxTtbD0HhOWEdNFLVL/szbIjfPJyuU0WTL8E/
w8S5p8BNensG0/TbIo/wZTqsTpQlGupJwBTr3qmhCHJiW1dGpdNOLApKLdbHPsWM
nWO5/jeiYgGL4rFYQziS8mTUZCMt2ucoLKv+NxEKLuN3qwgC3SeC40rwFsgtt4k9
EmeLGlvbfjjBnOfvqO0Y1WCxOfO3D/1uF7KXJH1ZiZIcz+PrJvn+T2lv+nGb9yVR
VGM5om6ZCoZGQcklgEc4q5cuHN5S2pM=
-----END CERTIFICATE-----
```



键：tls.cert

值：

```bash
-----BEGIN CERTIFICATE-----
MIIDKDCCAhCgAwIBAgIRAM+Za/qrlQ2fPM0vi5K44TYwDQYJKoZIhvcNAQELBQAw
FDESMBAGA1UEAxMJaGFyYm9yLWNhMB4XDTIzMDcwMzA1NDA1MFoXDTI0MDcwMjA1
NDA1MFowGDEWMBQGA1UEAxMNMTkyLjE2OC4wLjExMjCCASIwDQYJKoZIhvcNAQEB
BQADggEPADCCAQoCggEBANlRgdeui+oodGVISAoMkYk6HjXR/FFTgm1eKZoaXYPi
HxUTLBftd8xKttlY17tcP0xNpvkbHuC0PKXs1RylFdriKaxss0puNnKSMhE6vq8O
pMEJgtYEtDSRdfZgQyoufSh7sccRNKPchZ6wJ+cpfJInLNjFsGAPqKPS0TsnlAjz
joKIlLVvpN6uz3eJKzZKb4nBdFoA73RZWpGSQ315dJGarPR1LFtuzsQDTFHgTz1F
lrDZL1Nix13paDdo+DVXook3jDHH0nP+ap8q5+KwM71+RxyZUHSXujIhSWztBCdE
RogeO7SNZ7IuGtVSoo28/hlULNgIiBeSN0VBjdRuLZ0CAwEAAaNxMG8wDgYDVR0P
AQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAMBgNVHRMB
Af8EAjAAMB8GA1UdIwQYMBaAFO9ob5evJSW+xa9O48Fzx9w5M3U2MA8GA1UdEQQI
MAaHBMCoAHAwDQYJKoZIhvcNAQELBQADggEBAAgl5hdEN0SBa6kzaHnqPPLU5vKD
zvl12/vCqwVGK8gc/PAyGYap4dUpoGKlvtrtykNnJruRI0PPTMv+hY7EVBXYXDxp
K4D5acRtsEesKW4IjM6AfqlRbs7uonj6IGQStnhgjmi1DiMS9YQYwOV7AStrsHJV
g+dwpV/DxixUcHUbmFUZL+nhuQsIrqWCXV6VETaQj97fPPK13Z3IK5Nt3gIHr9ST
K3pjFjROcvBPEvU4VDTsVsSf5M8bccij/4oe47kc5D3FoZIOA/QGcGyIkBZNpvtH
m0A7sXTZPgXDoKYIthw70cuRu7hpQcMQC+XSUv1DE8jYVich8DtMR0YJTME=
-----END CERTIFICATE-----
```



键：tls.key

```bash
-----BEGIN RSA PRIVATE KEY-----
MIIEogIBAAKCAQEA2VGB166L6ih0ZUhICgyRiToeNdH8UVOCbV4pmhpdg+IfFRMs
F+13zEq22VjXu1w/TE2m+Rse4LQ8pezVHKUV2uIprGyzSm42cpIyETq+rw6kwQmC
1gS0NJF19mBDKi59KHuxxxE0o9yFnrAn5yl8kics2MWwYA+oo9LROyeUCPOOgoiU
tW+k3q7Pd4krNkpvicF0WgDvdFlakZJDfXl0kZqs9HUsW27OxANMUeBPPUWWsNkv
U2LHXeloN2j4NVeiiTeMMcfSc/5qnyrn4rAzvX5HHJlQdJe6MiFJbO0EJ0RGiB47
tI1nsi4a1VKijbz+GVQs2AiIF5I3RUGN1G4tnQIDAQABAoIBAElCdeAMonQckSh3
lnl0xTcalYaVNFcCLOzLmoCttKq59rp3DR0/22vOIBfjIE60CU6iutAtOVqMyAkz
bqBKcrSoG15aApLr1oUHDcPLJu8Co73DhAy75zf1dWvKBbpZk62rDUJLZZB7zbuy
LLF7xdfLRw4Ijq3DeZlIf2pOrFPbkD7wwyfsvOW2Tc3lP7WLHNEDpcU6t3QTWaoy
sGHJfE418xV6Edg766YzBXmwAplJWHc0RdPXSgfE6/7s7nQc2860CsOSNU0O+B82
KVqc6VvoJ+53oXE39bYwcL4UWlVmriCnkFbaXb2S0toinKDcoZOoSMn24H/Ni04U
Oxhn6oECgYEA61dbmqQsGRH/1YOqGbzJPK4bE2MztZRevkG8Cp0cpOLON+Uj0wsZ
unMCNU/ttARGGREzIaQcmOsYu/CXs8jkai1oQF4RX9FIpj6Z9nz0vsT+/kgwLEDu
QCCycic8qHzJL1d0gzpzGFsepaUqb/nuizYISsOjed3RkuJWL8t52OECgYEA7GUi
u4LDcxz7UdQ5wXRfoDfeVjNo3B+zQi3J+qt6sazNfDFWJ2dHL/xWzjUcp1P5XSZA
LUVChXUcJ+d3OpvN2JIiFSaQEmY4hFrWtzJT2r6om2HxrTN+wUVUwi1aswmi4CJy
5pn7qn+q6LzqRwAcVYWpEEyBNSDN64aUQ1GVgD0CgYAWmf8sLNQnXDkrskdlzWGu
ODJVfFN8/tDSiNGcW1Zi531SlOkJ3akM5PqzUAfOIBLzWVmFw5MThJCNjB+lnoeB
QvceJ2qXvyuw+5YhvAJtR9INbbViqqG2+uzoVahXrhRMPaPs7nIbRrT8x435zxD/
waKuO3e5vnngF2ibknkTQQKBgDhJuKR3dXySeRE5/GqhACgRRYK3CQ/pCHEIoCBs
9nEaGW+p+760K9I0PqMpKGJ7b1QLyJo+9KD8irDBv7UX5kLcQPtSTFnlNy3kx12l
wSvD3DfrcXVxXow8qvr1e7RH5h2CdmOMZM1rStHSMeKoFxcSFXiJDvcMkZu0VtdU
tMltAoGAFdyh55GukJp6iU7cHAng7/x/SkXxCphFjY7KaMQJ+Px5XfQl3l4cOU1w
aqQ232U2afcRN0mHfTcmqKgXQLSv/N/MNwPg+ipcWwKlo7Thga2rKJwafY3t9I57
bOndBCnquUeOmYTtDNSQd+w1dOo32M1SHMlNijTdFcLqazGL7hk=
-----END RSA PRIVATE KEY-----
```

完成后保存即可。之后在KubeSphere的devops项目使用Jenkins的maven代理就可以正常访问Harbor了。







回到KubeSphere，在devops项目创建凭证：

```bash
名称：harbor-robot-test
类型：用户名和密码
用户名：robot_test
密码/令牌：eSBDoEqVAG1WlY2u9lakzkXR7TbjE0Tc
描述：测试项目的Harbor镜像仓库账号
```



在流水线中使用Harbor作为镜像仓库：

修改devops-wroker中 podman的设置：

```ini
unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "jkzyghm3.mirror.aliyuncs.com"
insecure = true

#Harbor
[[registry]]
prefix = "192.168.0.112:30002"
location = "192.168.0.112:30002"
insecure = true
```



修改流水线的Jenkinsfile：

```groovy
environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'    
    GITHUB_ACCOUNT = 'kubesphere'
    REGISTRY = '192.168.0.112:30003'
    REGISTRY_NAMESPACE = 'project-test'
  }
```

这里

```bash
    REGISTRY = '192.168.0.112:30003'
    REGISTRY_NAMESPACE = 'project-test'
```

使用Harbor的服务器IP和Harbor服务中创建的项目名。

```groovy
stage('推送镜像-hospital-manage') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'harbor-robot-test' ,passwordVariable : 'REGISTRY_PASSWORD' ,usernameVariable : 'REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  hospital-manage:latest $REGISTRY/$REGISTRY_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
                sh 'podman push  $REGISTRY/$REGISTRY_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }
```

推送镜像改为使用Harbor的凭证，服务器会使用在环境变量中设置好的Harbor的服务器地址作为参数重新打tag，然后上传到Harbor服务器。



在项目空间中，也要创建Harbor的凭证，

项目空间-配置-保密字典-创建：

基本信息：

```bash
名称：harbor-robot-test
别名：project-test-harbor-robot-test
描述：测试项目Harbor镜像仓库机器人账号
```

数据设置：

```bash
类型：镜像服务信息
镜像服务地址：
	协议：https://
	地址：192.168.0.112:30003
用户名：robot_test
邮箱：
密码：eSBDoEqVAG1WlY2u9lakzkXR7TbjE0Tc
```

点击 `验证`，如果提示验证通过则没有问题。



代码中的deploy.yml也要修改，注意修改带有注释的部分：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: hospital-manage
  name: hospital-manage
  namespace: project-test   #一定要写命名空间， 一般为k8s集群中的项目名
spec:
  progressDeadlineSeconds: 600
  replicas: 1
  selector:
    matchLabels:
      app: hospital-manage
  strategy:
    rollingUpdate:
      maxSurge: 50%
      maxUnavailable: 50%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: hospital-manage
    spec:
      imagePullSecrets:
        - name: harbor-robot-test  #前面在项目空间下配置访问Harbor的登录凭证名
      containers:
      # 注意这里要和Jenkinsfile中的环境变量名保持一致
        - image: $REGISTRY/$REGISTRY_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER
 #         readinessProbe:
 #           httpGet:
 #             path: /actuator/health
 #             port: 8080
 #           timeoutSeconds: 10
 #           failureThreshold: 30
 #           periodSeconds: 5
          imagePullPolicy: Always
          name: app
          ports:
            - containerPort: 8080
              protocol: TCP
          resources:
            limits:
              cpu: 300m
              memory: 600Mi
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app: hospital-manage
  name: hospital-manage
  namespace: project-test   #一定要写命名空间， 一般为k8s集群中的项目名
spec:
  ports:
    - name: http
      port: 8080
      protocol: TCP
      targetPort: 8080
  selector:
    app: hospital-manage
  sessionAffinity: None
  type: ClusterIP
```

修改完成后推送到代码服务器。



