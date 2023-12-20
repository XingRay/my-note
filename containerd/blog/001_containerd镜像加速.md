# containerd 镜像加速

## 1 安装Containerd

[Ubuntu 22.04] 安装containerd

https://blog.csdn.net/IOT_AI/article/details/131916926



## 2 镜像配置建议

网上大多数配置containerd镜像加速的文章都是直接修改：/etc/containerd/config.toml配置文件，这种方式在较新版本的contaienrd中已经被废弃，将来肯定会被移除，只不过现在还可以使用而已。另外，这种方式有一个不好的地方就是，每次修改/etc/containerd/config.toml配置文件，都需要执行systemctl restart containerd.service命令重启containerd

  新版本的containerd镜像仓库配置都是建议放在一个单独的文件夹当中，并且在/etc/containerd/config.toml配置文件当中打开config_path配置，指向镜像仓库配置目录即可。这种方式只需要在第一次修改/etc/containerd/config.toml文件打开config_path配置时需要重启containerd，后续我们增加镜像仓库配置都无需重启containerd，非常方便。

  若我们在/etc/containerd/config.toml配置文件中指定config_path = /etc/containerd/certs.d，那么containerd镜像仓库的格式如下：

```shell
$ tree /etc/containerd/certs.d
/etc/containerd/certs.d
/etc/containerd/certs.d/
├── 192.168.11.20
│   └── hosts.toml
└── docker.io
    └── hosts.toml
```

  可以看到，第一级目录为镜像仓库的域名或者IP:ADDR，第二级为hosts.toml文件

  hosts.toml文件中的内容仅支持：server, capabilities, ca, client, skip_verify, [header], override_path

  hosts.toml文件示例如下：

```toml
[host."https://mirror.registry"]
  capabilities = ["pull"]
  ca = "/etc/certs/mirror.pem"
  skip_verify = false
  [host."https://mirror.registry".header]
    x-custom-2 = ["value1", "value2"]

[host."https://mirror-bak.registry/us"]
  capabilities = ["pull"]
  skip_verify = true

[host."http://mirror.registry"]
  capabilities = ["pull"]

[host."https://test-1.registry"]
  capabilities = ["pull", "resolve", "push"]
  ca = ["/etc/certs/test-1-ca.pem", "/etc/certs/special.pem"]
  client = [["/etc/certs/client.cert", "/etc/certs/client.key"],["/etc/certs/client.pem", ""]]

[host."https://test-2.registry"]
  client = "/etc/certs/client.pem"

[host."https://test-3.registry"]
  client = ["/etc/certs/client-1.pem", "/etc/certs/client-2.pem"]

[host."https://non-compliant-mirror.registry/v2/upstream"]
  capabilities = ["pull"]
  override_path = true
```

  特别需要注意的是，hosts.toml中可以配置多个镜像仓库，containerd下载竟像时会根据配置的顺序使用镜像仓库，只有当上一个仓库下载失败才会使用下一个镜像仓库。因此，镜像仓库的配置原则就是镜像仓库下载速度越快，那么这个仓库就应该放在最前面。



## 3 镜像加速配置

```shell
# docker hub镜像加速
mkdir -p /etc/containerd/certs.d/docker.io
cat > /etc/containerd/certs.d/docker.io/hosts.toml << EOF
server = "https://docker.io"
[host."https://dockerproxy.com"]
  capabilities = ["pull", "resolve"]

[host."https://docker.m.daocloud.io"]
  capabilities = ["pull", "resolve"]

[host."https://reg-mirror.qiniu.com"]
  capabilities = ["pull", "resolve"]

[host."https://registry.docker-cn.com"]
  capabilities = ["pull", "resolve"]

[host."http://hub-mirror.c.163.com"]
  capabilities = ["pull", "resolve"]

EOF

# registry.k8s.io镜像加速
mkdir -p /etc/containerd/certs.d/registry.k8s.io
tee /etc/containerd/certs.d/registry.k8s.io/hosts.toml << 'EOF'
server = "https://registry.k8s.io"

[host."https://k8s.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# docker.elastic.co镜像加速
mkdir -p /etc/containerd/certs.d/docker.elastic.co
tee /etc/containerd/certs.d/docker.elastic.co/hosts.toml << 'EOF'
server = "https://docker.elastic.co"

[host."https://elastic.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# gcr.io镜像加速
mkdir -p /etc/containerd/certs.d/gcr.io
tee /etc/containerd/certs.d/gcr.io/hosts.toml << 'EOF'
server = "https://gcr.io"

[host."https://gcr.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# ghcr.io镜像加速
mkdir -p /etc/containerd/certs.d/ghcr.io
tee /etc/containerd/certs.d/ghcr.io/hosts.toml << 'EOF'
server = "https://ghcr.io"

[host."https://ghcr.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# k8s.gcr.io镜像加速
mkdir -p /etc/containerd/certs.d/k8s.gcr.io
tee /etc/containerd/certs.d/k8s.gcr.io/hosts.toml << 'EOF'
server = "https://k8s.gcr.io"

[host."https://k8s-gcr.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# mcr.m.daocloud.io镜像加速
mkdir -p /etc/containerd/certs.d/mcr.microsoft.com
tee /etc/containerd/certs.d/mcr.microsoft.com/hosts.toml << 'EOF'
server = "https://mcr.microsoft.com"

[host."https://mcr.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# nvcr.io镜像加速
mkdir -p /etc/containerd/certs.d/nvcr.io
tee /etc/containerd/certs.d/nvcr.io/hosts.toml << 'EOF'
server = "https://nvcr.io"

[host."https://nvcr.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# quay.io镜像加速
mkdir -p /etc/containerd/certs.d/quay.io
tee /etc/containerd/certs.d/quay.io/hosts.toml << 'EOF'
server = "https://quay.io"

[host."https://quay.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# registry.jujucharms.com镜像加速
mkdir -p /etc/containerd/certs.d/registry.jujucharms.com
tee /etc/containerd/certs.d/registry.jujucharms.com/hosts.toml << 'EOF'
server = "https://registry.jujucharms.com"

[host."https://jujucharms.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF

# rocks.canonical.com镜像加速
mkdir -p /etc/containerd/certs.d/rocks.canonical.com
tee /etc/containerd/certs.d/rocks.canonical.com/hosts.toml << 'EOF'
server = "https://rocks.canonical.com"

[host."https://rocks-canonical.m.daocloud.io"]
  capabilities = ["pull", "resolve", "push"]
EOF
```

  注意，住里面除了docker.io仓库，其余仓库的镜像仓库都是使用了daocloud的镜像仓库，daocloud镜像仓库并非支持所有镜像的下载，其支持的镜像列表可以参考：daocloud镜像仓库支持列表

https://github.com/DaoCloud/public-image-mirror/blob/main/mirror.txt

```shell
docker.elastic.co/eck/eck-operator
docker.elastic.co/elasticsearch/elasticsearch
docker.elastic.co/kibana/kibana
docker.elastic.co/kibana/kibana-oss
docker.io/alpine
docker.io/alpine/helm
docker.io/amambadev/jenkins
docker.io/amambadev/jenkins-agent-base
docker.io/amambadev/jenkins-agent-go
docker.io/amambadev/jenkins-agent-maven
docker.io/amambadev/jenkins-agent-nodejs
docker.io/amambadev/jenkins-agent-python
docker.io/amazon/aws-alb-ingress-controller
docker.io/amazon/aws-ebs-csi-driver
docker.io/apache/skywalking-java-agent
docker.io/apache/skywalking-oap-server
docker.io/apache/skywalking-ui
docker.io/apitable/backend-server
docker.io/apitable/init-appdata
docker.io/apitable/init-db
docker.io/apitable/openresty
docker.io/apitable/room-server
docker.io/apitable/web-server
docker.io/aquasec/kube-bench
docker.io/aquasec/kube-hunter
docker.io/aquasec/trivy
docker.io/arey/mysql-client
docker.io/bitnami/bitnami-shell
docker.io/bitnami/contour
docker.io/bitnami/elasticsearch
docker.io/bitnami/elasticsearch-curator
docker.io/bitnami/elasticsearch-exporter
docker.io/bitnami/envoy
docker.io/bitnami/grafana
docker.io/bitnami/grafana-operator
docker.io/bitnami/kafka
docker.io/bitnami/kubeapps-apis
docker.io/bitnami/kubeapps-apprepository-controller
docker.io/bitnami/kubeapps-dashboard
docker.io/bitnami/kubeapps-kubeops
docker.io/bitnami/kubectl
docker.io/bitnami/kubernetes-event-exporter
docker.io/bitnami/mariadb
docker.io/bitnami/minideb
docker.io/bitnami/nginx
docker.io/bitnami/postgresql
docker.io/bitnami/wordpress
docker.io/bitpoke/mysql-operator
docker.io/bitpoke/mysql-operator-orchestrator
docker.io/bitpoke/mysql-operator-sidecar-5.7
docker.io/bitpoke/mysql-operator-sidecar-8.0
docker.io/busybox
docker.io/byrnedo/alpine-curl
docker.io/caddy
docker.io/calico/apiserver
docker.io/calico/cni
docker.io/calico/csi
docker.io/calico/kube-controllers
docker.io/calico/node
docker.io/calico/node-driver-registrar
docker.io/calico/pod2daemon-flexvol
docker.io/calico/typha
docker.io/cdkbot/hostpath-provisioner-amd64
docker.io/cdkbot/registry-amd64
docker.io/centos
docker.io/centos/tools
docker.io/cfmanteiga/alpine-bash-curl-jq
docker.io/cfssl/cfssl
docker.io/cilium/json-mock
docker.io/clickhouse/clickhouse-server
docker.io/clickhouse/integration-helper
docker.io/cloudnativelabs/kube-router
docker.io/coredns/coredns
docker.io/csiplugin/snapshot-controller
docker.io/curlimages/curl
docker.io/datawire/ambassador
docker.io/datawire/ambassador-operator
docker.io/debian
docker.io/directxman12/k8s-prometheus-adapter
docker.io/docker
docker.io/dpage/pgadmin4
docker.io/elastic/filebeat
docker.io/envoyproxy/envoy
docker.io/envoyproxy/envoy-distroless
docker.io/envoyproxy/nighthawk-dev
docker.io/f5networks/f5-ipam-controller
docker.io/f5networks/k8s-bigip-ctlr
docker.io/fabulousjohn/kafka-manager
docker.io/falcosecurity/event-generator
docker.io/falcosecurity/falco-driver-loader
docker.io/falcosecurity/falco-exporter
docker.io/falcosecurity/falco-no-driver
docker.io/falcosecurity/falcosidekick
docker.io/falcosecurity/falcosidekick-ui
docker.io/fellah/gitbook
docker.io/flannelcni/flannel-cni-plugin
docker.io/flant/shell-operator
docker.io/fluent/fluent-bit
docker.io/fluent/fluentd
docker.io/fortio/fortio
docker.io/foxdalas/kafka-manager
docker.io/frrouting/frr
docker.io/goharbor/chartmuseum-photon
docker.io/goharbor/harbor-core
docker.io/goharbor/harbor-db
docker.io/goharbor/harbor-exporter
docker.io/goharbor/harbor-jobservice
docker.io/goharbor/harbor-operator
docker.io/goharbor/harbor-portal
docker.io/goharbor/harbor-registryctl
docker.io/goharbor/nginx-photon
docker.io/goharbor/notary-server-photon
docker.io/goharbor/notary-signer-photon
docker.io/goharbor/redis-photon
docker.io/goharbor/registry-photon
docker.io/goharbor/trivy-adapter-photon
docker.io/golang
docker.io/grafana/grafana
docker.io/grafana/tempo
docker.io/halverneus/static-file-server
docker.io/haproxy
docker.io/honkit/honkit
docker.io/integratedcloudnative/ovn4nfv-k8s-plugin
docker.io/istio/citadel
docker.io/istio/examples-bookinfo-details-v1
docker.io/istio/examples-bookinfo-productpage-v1
docker.io/istio/examples-bookinfo-ratings-v1
docker.io/istio/examples-bookinfo-reviews-v1
docker.io/istio/examples-bookinfo-reviews-v2
docker.io/istio/examples-bookinfo-reviews-v3
docker.io/istio/examples-helloworld-v1
docker.io/istio/examples-helloworld-v2
docker.io/istio/galley
docker.io/istio/install-cni
docker.io/istio/kubectl
docker.io/istio/mixer
docker.io/istio/operator
docker.io/istio/pilot
docker.io/istio/proxyv2
docker.io/istio/sidecar_injector
docker.io/jaegertracing/all-in-one
docker.io/jaegertracing/jaeger-agent
docker.io/jaegertracing/jaeger-collector
docker.io/jaegertracing/jaeger-es-index-cleaner
docker.io/jaegertracing/jaeger-es-rollover
docker.io/jaegertracing/jaeger-operator
docker.io/jaegertracing/jaeger-query
docker.io/jaegertracing/spark-dependencies
docker.io/java
docker.io/jboss/keycloak
docker.io/jenkins/jnlp-slave
docker.io/jertel/elastalert2
docker.io/jimmidyson/configmap-reload
docker.io/joosthofman/wget
docker.io/joseluisq/static-web-server
docker.io/jujusolutions/juju-db
docker.io/jujusolutions/jujud-operator
docker.io/k8scloudprovider/cinder-csi-plugin
docker.io/karmada/karmada-agent
docker.io/karmada/karmada-aggregated-apiserver
docker.io/karmada/karmada-controller-manager
docker.io/karmada/karmada-descheduler
docker.io/karmada/karmada-scheduler
docker.io/karmada/karmada-scheduler-estimator
docker.io/karmada/karmada-search
docker.io/karmada/karmada-webhook
docker.io/kedacore/keda
docker.io/kedacore/keda-metrics-apiserver
docker.io/kennethreitz/httpbin
docker.io/keyval/otel-go-agent
docker.io/kindest/base
docker.io/kindest/haproxy
docker.io/kindest/node
docker.io/kiwigrid/k8s-sidecar
docker.io/kubeedge/cloudcore
docker.io/kubeovn/kube-ovn
docker.io/kuberhealthy/dns-resolution-check
docker.io/kuberhealthy/kuberhealthy
docker.io/kubernetesui/dashboard
docker.io/kubernetesui/dashboard-amd64
docker.io/kubernetesui/metrics-scraper
docker.io/library/alpine
docker.io/library/busybox
docker.io/library/caddy
docker.io/library/centos
docker.io/library/debian
docker.io/library/docker
docker.io/library/golang
docker.io/library/haproxy
docker.io/library/java
docker.io/library/mariadb
docker.io/library/mongo
docker.io/library/mysql
docker.io/library/nats-streaming
docker.io/library/nextcloud
docker.io/library/nginx
docker.io/library/node
docker.io/library/openjdk
docker.io/library/percona
docker.io/library/perl
docker.io/library/phpmyadmin
docker.io/library/postgres
docker.io/library/python
docker.io/library/rabbitmq
docker.io/library/redis
docker.io/library/registry
docker.io/library/traefik
docker.io/library/ubuntu
docker.io/library/wordpress
docker.io/library/zookeeper
docker.io/longhornio/backing-image-manager
docker.io/longhornio/csi-attacher
docker.io/longhornio/csi-node-driver-registrar
docker.io/longhornio/csi-provisioner
docker.io/longhornio/csi-resizer
docker.io/longhornio/csi-snapshotter
docker.io/longhornio/longhorn-engine
docker.io/longhornio/longhorn-instance-manager
docker.io/longhornio/longhorn-manager
docker.io/longhornio/longhorn-share-manager
docker.io/longhornio/longhorn-ui
docker.io/mariadb
docker.io/merbridge/merbridge
docker.io/metallb/controller
docker.io/metallb/speaker
docker.io/minio/console
docker.io/minio/kes
docker.io/minio/logsearchapi
docker.io/minio/mc
docker.io/minio/minio
docker.io/minio/operator
docker.io/mirantis/k8s-netchecker-agent
docker.io/mirantis/k8s-netchecker-server
docker.io/mirrorgooglecontainers/defaultbackend-amd64
docker.io/mirrorgooglecontainers/hpa-example
docker.io/moby/buildkit
docker.io/mohsinonxrm/mongodb-agent
docker.io/mohsinonxrm/mongodb-kubernetes-operator
docker.io/mohsinonxrm/mongodb-kubernetes-operator-version-upgrade-post-start-hook
docker.io/mohsinonxrm/mongodb-kubernetes-readiness
docker.io/mongo
docker.io/multiarch/qemu-user-static
docker.io/mysql
docker.io/n8nio/n8n
docker.io/nacos/nacos-server
docker.io/nats-streaming
docker.io/neuvector/controller
docker.io/neuvector/enforcer
docker.io/neuvector/manager
docker.io/neuvector/scanner
docker.io/neuvector/updater
docker.io/nextcloud
docker.io/nfvpe/multus
docker.io/nginx
docker.io/nginxdemos/hello
docker.io/node
docker.io/oamdev/cluster-gateway
docker.io/oamdev/kube-webhook-certgen
docker.io/oamdev/terraform-controller
docker.io/oamdev/vela-apiserver
docker.io/oamdev/vela-core
docker.io/oamdev/vela-rollout
docker.io/oamdev/velaux
docker.io/oliver006/redis_exporter
docker.io/openebs/admission-server
docker.io/openebs/linux-utils
docker.io/openebs/m-apiserver
docker.io/openebs/node-disk-manager
docker.io/openebs/node-disk-operator
docker.io/openebs/openebs-k8s-provisioner
docker.io/openebs/provisioner-localpv
docker.io/openebs/snapshot-controller
docker.io/openebs/snapshot-provisioner
docker.io/openjdk
docker.io/openkruise/kruise-manager
docker.io/openpolicyagent/gatekeeper
docker.io/openstorage/stork
docker.io/openzipkin/zipkin
docker.io/osixia/openldap
docker.io/otel/demo
docker.io/otel/opentelemetry-collector
docker.io/otel/opentelemetry-collector-contrib
docker.io/percona
docker.io/percona/mongodb_exporter
docker.io/perl
docker.io/phpmyadmin
docker.io/phpmyadmin/phpmyadmin
docker.io/pingcap/coredns
docker.io/portainer/portainer-ce
docker.io/postgres
docker.io/prom/alertmanager
docker.io/prom/mysqld-exporter
docker.io/prom/node-exporter
docker.io/prom/prometheus
docker.io/prometheuscommunity/postgres-exporter
docker.io/python
docker.io/rabbitmq
docker.io/rabbitmqoperator/cluster-operator
docker.io/rancher/helm-controller
docker.io/rancher/k3d-tools
docker.io/rancher/k3s
docker.io/rancher/kubectl
docker.io/rancher/local-path-provisioner
docker.io/rclone/rclone
docker.io/redis
docker.io/redislabs/redisearch
docker.io/registry
docker.io/sonobuoy/cluster-inventory
docker.io/sonobuoy/kube-bench
docker.io/sonobuoy/sonobuoy
docker.io/sonobuoy/systemd-logs
docker.io/squidfunk/mkdocs-material
docker.io/swaggerapi/swagger-codegen-cli
docker.io/tgagor/centos-stream
docker.io/thanosio/thanos
docker.io/timberio/vector
docker.io/traefik
docker.io/ubuntu
docker.io/velero/velero
docker.io/victoriametrics/operator
docker.io/victoriametrics/victoria-logs
docker.io/victoriametrics/victoria-metrics
docker.io/victoriametrics/vmagent
docker.io/victoriametrics/vmalert
docker.io/victoriametrics/vminsert
docker.io/victoriametrics/vmselect
docker.io/victoriametrics/vmstorage
docker.io/weaveworks/scope
docker.io/weaveworks/weave-kube
docker.io/weaveworks/weave-npc
docker.io/wordpress
docker.io/xueshanf/install-socat
docker.io/zenko/kafka-manager
docker.io/zookeeper
gcr.io/cadvisor/cadvisor
gcr.io/distroless/base
gcr.io/distroless/static
gcr.io/distroless/static-debian11
gcr.io/google-containers/pause
gcr.io/google.com/cloudsdktool/cloud-sdk
gcr.io/google_containers/hyperkube
gcr.io/heptio-images/ks-guestbook-demo
gcr.io/istio-release/app_sidecar_base_centos_7
gcr.io/istio-release/app_sidecar_base_centos_8
gcr.io/istio-release/base
gcr.io/istio-release/distroless
gcr.io/istio-release/iptables
gcr.io/istio-testing/app
gcr.io/istio-testing/build-tools
gcr.io/istio-testing/buildkit
gcr.io/istio-testing/dotdotpwn
gcr.io/istio-testing/ext-authz
gcr.io/istio-testing/fake-gce-metadata
gcr.io/istio-testing/fake-stackdriver
gcr.io/istio-testing/fuzz_tomcat
gcr.io/istio-testing/jwttool
gcr.io/istio-testing/kind-node
gcr.io/istio-testing/kindest/node
gcr.io/istio-testing/mynewproxy
gcr.io/istio-testing/myproxy
gcr.io/istio-testing/operator
gcr.io/istio-testing/pilot
gcr.io/istio-testing/proxyv2
gcr.io/k8s-staging-etcd/etcd
gcr.io/k8s-staging-gateway-api/admission-server
gcr.io/k8s-staging-kube-state-metrics/kube-state-metrics
gcr.io/k8s-staging-nfd/node-feature-discovery
gcr.io/k8s-staging-test-infra/krte
gcr.io/kaniko-project/executor
gcr.io/knative-releases/knative.dev/client/cmd/kn
gcr.io/knative-releases/knative.dev/eventing/cmd/apiserver_receive_adapter
gcr.io/knative-releases/knative.dev/eventing/cmd/controller
gcr.io/knative-releases/knative.dev/eventing/cmd/in_memory/channel_controller
gcr.io/knative-releases/knative.dev/eventing/cmd/in_memory/channel_dispatcher
gcr.io/knative-releases/knative.dev/eventing/cmd/mtbroker/filter
gcr.io/knative-releases/knative.dev/eventing/cmd/mtbroker/ingress
gcr.io/knative-releases/knative.dev/eventing/cmd/mtchannel_broker
gcr.io/knative-releases/knative.dev/eventing/cmd/mtping
gcr.io/knative-releases/knative.dev/eventing/cmd/webhook
gcr.io/knative-releases/knative.dev/net-istio/cmd/controller
gcr.io/knative-releases/knative.dev/net-istio/cmd/webhook
gcr.io/knative-releases/knative.dev/net-kourier/cmd/kourier
gcr.io/knative-releases/knative.dev/serving/cmd/activator
gcr.io/knative-releases/knative.dev/serving/cmd/autoscaler
gcr.io/knative-releases/knative.dev/serving/cmd/controller
gcr.io/knative-releases/knative.dev/serving/cmd/default-domain
gcr.io/knative-releases/knative.dev/serving/cmd/domain-mapping
gcr.io/knative-releases/knative.dev/serving/cmd/domain-mapping-webhook
gcr.io/knative-releases/knative.dev/serving/cmd/queue
gcr.io/knative-releases/knative.dev/serving/cmd/webhook
gcr.io/kuar-demo/kuard-amd64
gcr.io/kubebuilder/kube-rbac-proxy
gcr.io/kubecost1/cost-model
gcr.io/kubecost1/frontend
gcr.io/tekton-releases/github.com/tektoncd/dashboard/cmd/dashboard
gcr.io/tekton-releases/github.com/tektoncd/operator/cmd/kubernetes/operator
gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/controller
gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/entrypoint
gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/git-init
gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/imagedigestexporter
gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/webhook
gcr.io/tekton-releases/github.com/tektoncd/results/cmd/api
gcr.io/tekton-releases/github.com/tektoncd/results/cmd/watcher
gcr.io/tekton-releases/github.com/tektoncd/triggers/cmd/controllers
gcr.io/tekton-releases/github.com/tektoncd/triggers/cmd/webhook
ghcr.io/aquasecurity/trivy
ghcr.io/aquasecurity/trivy-db
ghcr.io/aquasecurity/trivy-java-db
ghcr.io/chaos-mesh/chaos-daemon
ghcr.io/chaos-mesh/chaos-dashboard
ghcr.io/chaos-mesh/chaos-dlv
ghcr.io/chaos-mesh/chaos-kernel
ghcr.io/chaos-mesh/chaos-mesh
ghcr.io/clusterpedia-io/clusterpedia/apiserver
ghcr.io/clusterpedia-io/clusterpedia/clustersynchro-manager
ghcr.io/daocloud/ckube
ghcr.io/daocloud/dao-2048
ghcr.io/dependabot/dependabot-core
ghcr.io/dependabot/dependabot-core-development
ghcr.io/dexidp/dex
ghcr.io/dtzar/helm-kubectl
ghcr.io/ferryproxy/ferry/ferry-controller
ghcr.io/ferryproxy/ferry/ferry-tunnel
ghcr.io/fluxcd/helm-controller
ghcr.io/fluxcd/kustomize-controller
ghcr.io/fluxcd/notification-controller
ghcr.io/fluxcd/source-controller
ghcr.io/helm/chartmuseum
ghcr.io/hwameistor/admission
ghcr.io/hwameistor/apiserver
ghcr.io/hwameistor/drbd-reactor
ghcr.io/hwameistor/drbd9-bionic
ghcr.io/hwameistor/drbd9-focal
ghcr.io/hwameistor/drbd9-jammy
ghcr.io/hwameistor/drbd9-kylin10
ghcr.io/hwameistor/drbd9-rhel7
ghcr.io/hwameistor/drbd9-rhel8
ghcr.io/hwameistor/drbd9-rhel9
ghcr.io/hwameistor/drbd9-shipper
ghcr.io/hwameistor/evictor
ghcr.io/hwameistor/exporter
ghcr.io/hwameistor/hwameistor-ui
ghcr.io/hwameistor/local-disk-manager
ghcr.io/hwameistor/local-storage
ghcr.io/hwameistor/operator
ghcr.io/hwameistor/scheduler
ghcr.io/hwameistor/self-signed
ghcr.io/k8snetworkplumbingwg/multus-cni
ghcr.io/k8snetworkplumbingwg/network-resources-injector
ghcr.io/k8snetworkplumbingwg/sriov-cni
ghcr.io/k8snetworkplumbingwg/sriov-network-device-plugin
ghcr.io/k8snetworkplumbingwg/sriov-network-operator
ghcr.io/k8snetworkplumbingwg/sriov-network-operator-config-daemon
ghcr.io/k8snetworkplumbingwg/sriov-network-operator-webhook
ghcr.io/klts-io/kubernetes-lts/coredns
ghcr.io/klts-io/kubernetes-lts/etcd
ghcr.io/klts-io/kubernetes-lts/kube-apiserver
ghcr.io/klts-io/kubernetes-lts/kube-controller-manager
ghcr.io/klts-io/kubernetes-lts/kube-proxy
ghcr.io/klts-io/kubernetes-lts/kube-scheduler
ghcr.io/klts-io/kubernetes-lts/pause
ghcr.io/ksmartdata/logical-backup
ghcr.io/kube-vip/kube-vip
ghcr.io/kubean-io/kubean-operator
ghcr.io/kubean-io/kubespray
ghcr.io/kubean-io/spray-job
ghcr.io/megacloudcontainer/kube-hunter
ghcr.io/megacloudcontainer/kubeaudit
ghcr.io/open-telemetry/demo
ghcr.io/open-telemetry/opentelemetry-go-instrumentation/autoinstrumentation-go
ghcr.io/open-telemetry/opentelemetry-operator/autoinstrumentation-dotnet
ghcr.io/open-telemetry/opentelemetry-operator/autoinstrumentation-java
ghcr.io/open-telemetry/opentelemetry-operator/autoinstrumentation-nodejs
ghcr.io/open-telemetry/opentelemetry-operator/autoinstrumentation-python
ghcr.io/open-telemetry/opentelemetry-operator/opentelemetry-operator
ghcr.io/openfaas/basic-auth
ghcr.io/openfaas/faas-netes
ghcr.io/openfaas/gateway
ghcr.io/openfaas/queue-worker
ghcr.io/openinsight-proj/demo
ghcr.io/openinsight-proj/elastic-alert
ghcr.io/openinsight-proj/openinsight
ghcr.io/openinsight-proj/opentelemetry-demo-helm-chart/adservice
ghcr.io/openinsight-proj/opentelemetry-demo-helm-chart/sentinel
ghcr.io/ovn-org/ovn-kubernetes/ovn-kube-f
ghcr.io/ovn-org/ovn-kubernetes/ovn-kube-u
ghcr.io/projectcontour/contour
ghcr.io/pterodactyl/yolks
ghcr.io/scholzj/zoo-entrance
ghcr.io/spidernet-io/cni-plugins/meta-plugins
ghcr.io/spidernet-io/egressgateway-agent
ghcr.io/spidernet-io/egressgateway-controller
ghcr.io/spidernet-io/spiderdoctor-agent
ghcr.io/spidernet-io/spiderdoctor-controller
ghcr.io/spidernet-io/spiderpool/spiderpool-agent
ghcr.io/spidernet-io/spiderpool/spiderpool-base
ghcr.io/spidernet-io/spiderpool/spiderpool-controller
ghcr.io/spiffe/spire-agent
ghcr.io/spiffe/spire-server
ghcr.io/sumologic/tailing-sidecar
ghcr.io/sumologic/tailing-sidecar-operator
quay.io/argoproj/argo-events
quay.io/argoproj/argo-rollouts
quay.io/argoproj/argocd
quay.io/argoproj/argocd-applicationset
quay.io/argoproj/argocli
quay.io/argoproj/argoexec
quay.io/argoproj/kubectl-argo-rollouts
quay.io/argoproj/workflow-controller
quay.io/argoprojlabs/argocd-image-updater
quay.io/brancz/kube-rbac-proxy
quay.io/calico/apiserver
quay.io/calico/cni
quay.io/calico/ctl
quay.io/calico/kube-controllers
quay.io/calico/node
quay.io/calico/pod2daemon-flexvol
quay.io/calico/typha
quay.io/cilium/certgen
quay.io/cilium/cilium
quay.io/cilium/cilium-envoy
quay.io/cilium/cilium-etcd-operator
quay.io/cilium/cilium-init
quay.io/cilium/clustermesh-apiserver
quay.io/cilium/hubble-export-stdout
quay.io/cilium/hubble-relay
quay.io/cilium/hubble-ui
quay.io/cilium/hubble-ui-backend
quay.io/cilium/json-mock
quay.io/cilium/kvstoremesh
quay.io/cilium/operator
quay.io/cilium/operator-alibabacloud
quay.io/cilium/operator-generic
quay.io/cilium/startup-script
quay.io/cilium/tetragon
quay.io/cilium/tetragon-operator
quay.io/containers/skopeo
quay.io/coreos/etcd
quay.io/coreos/flannel
quay.io/datawire/ambassador-operator
quay.io/external_storage/cephfs-provisioner
quay.io/external_storage/local-volume-provisioner
quay.io/external_storage/nfs-client-provisioner
quay.io/external_storage/rbd-provisioner
quay.io/fluentd_elasticsearch/elasticsearch
quay.io/fluentd_elasticsearch/fluentd
quay.io/goswagger/swagger
quay.io/grafana-operator/grafana_plugins_init
quay.io/iovisor/bcc
quay.io/jaegertracing/jaeger-operator
quay.io/jetstack/cert-manager-cainjector
quay.io/jetstack/cert-manager-controller
quay.io/jetstack/cert-manager-ctl
quay.io/jetstack/cert-manager-webhook
quay.io/k8scsi/csi-attacher
quay.io/k8scsi/csi-node-driver-registrar
quay.io/k8scsi/csi-provisioner
quay.io/k8scsi/csi-resizer
quay.io/k8scsi/csi-snapshotter
quay.io/k8scsi/livenessprobe
quay.io/k8scsi/snapshot-controller
quay.io/keycloak/keycloak
quay.io/kiali/kiali
quay.io/kiwigrid/k8s-sidecar
quay.io/kubespray/kubespray
quay.io/kubevirt/cdi-apiserver
quay.io/kubevirt/cdi-cloner
quay.io/kubevirt/cdi-controller
quay.io/kubevirt/cdi-importer
quay.io/kubevirt/cdi-operator
quay.io/kubevirt/cdi-uploadproxy
quay.io/kubevirt/cdi-uploadserver
quay.io/kubevirt/virt-api
quay.io/kubevirt/virt-controller
quay.io/kubevirt/virt-exportserver
quay.io/kubevirt/virt-handler
quay.io/kubevirt/virt-launcher
quay.io/kubevirt/virt-operator
quay.io/l23network/k8s-netchecker-agent
quay.io/l23network/k8s-netchecker-server
quay.io/metallb/controller
quay.io/metallb/speaker
quay.io/minio/minio
quay.io/mongodb/mongodb-agent
quay.io/mongodb/mongodb-kubernetes-operator
quay.io/mongodb/mongodb-kubernetes-operator-version-upgrade-post-start-hook
quay.io/mongodb/mongodb-kubernetes-readinessprobe
quay.io/nmstate/kubernetes-nmstate-handler
quay.io/nmstate/kubernetes-nmstate-operator
quay.io/operator-framework/olm
quay.io/opstree/redis
quay.io/opstree/redis-exporter
quay.io/opstree/redis-operator
quay.io/piraeusdatastore/drbd-reactor
quay.io/piraeusdatastore/drbd9-centos7
quay.io/piraeusdatastore/piraeus-client
quay.io/piraeusdatastore/piraeus-csi
quay.io/piraeusdatastore/piraeus-ha-controller
quay.io/piraeusdatastore/piraeus-operator
quay.io/prometheus-operator/prometheus-config-reloader
quay.io/prometheus-operator/prometheus-operator
quay.io/prometheus/alertmanager
quay.io/prometheus/blackbox-exporter
quay.io/prometheus/node-exporter
quay.io/prometheus/prometheus
quay.io/prometheuscommunity/elasticsearch-exporter
quay.io/spotahome/redis-operator
quay.io/strimzi/jmxtrans
quay.io/strimzi/kafka
quay.io/strimzi/kafka-bridge
quay.io/strimzi/kaniko-executor
quay.io/strimzi/maven-builder
quay.io/strimzi/operator
quay.io/submariner/submariner
quay.io/submariner/submariner-gateway
quay.io/submariner/submariner-globalnet
quay.io/submariner/submariner-networkplugin-syncer
quay.io/submariner/submariner-operator
quay.io/submariner/submariner-operator-index
quay.io/submariner/submariner-route-agent
quay.io/tigera/operator
registry.k8s.io/addon-resizer
registry.k8s.io/build-image/debian-iptables
registry.k8s.io/build-image/go-runner
registry.k8s.io/build-image/kube-cross
registry.k8s.io/cluster-api/cluster-api-controller
registry.k8s.io/cluster-api/kubeadm-bootstrap-controller
registry.k8s.io/cluster-api/kubeadm-control-plane-controller
registry.k8s.io/conformance
registry.k8s.io/coredns
registry.k8s.io/coredns/coredns
registry.k8s.io/cpa/cluster-proportional-autoscaler
registry.k8s.io/cpa/cluster-proportional-autoscaler-amd64
registry.k8s.io/cpa/cluster-proportional-autoscaler-arm64
registry.k8s.io/debian-base
registry.k8s.io/dns/k8s-dns-node-cache
registry.k8s.io/etcd
registry.k8s.io/etcd/etcd
registry.k8s.io/ingress-nginx/controller
registry.k8s.io/ingress-nginx/e2e-test-runner
registry.k8s.io/ingress-nginx/kube-webhook-certgen
registry.k8s.io/kube-apiserver
registry.k8s.io/kube-apiserver-amd64
registry.k8s.io/kube-controller-manager
registry.k8s.io/kube-controller-manager-amd64
registry.k8s.io/kube-proxy
registry.k8s.io/kube-proxy-amd64
registry.k8s.io/kube-registry-proxy
registry.k8s.io/kube-scheduler
registry.k8s.io/kube-scheduler-amd64
registry.k8s.io/kube-state-metrics/kube-state-metrics
registry.k8s.io/kueue/kueue
registry.k8s.io/kwok/cluster
registry.k8s.io/kwok/kwok
registry.k8s.io/metrics-server
registry.k8s.io/metrics-server-amd64
registry.k8s.io/metrics-server/metrics-server
registry.k8s.io/metrics-server/metrics-server-amd64
registry.k8s.io/nfd/node-feature-discovery
registry.k8s.io/node-problem-detector/node-problem-detector
registry.k8s.io/node-test
registry.k8s.io/node-test-amd64
registry.k8s.io/pause
registry.k8s.io/prometheus-adapter/prometheus-adapter
registry.k8s.io/sig-storage/csi-attacher
registry.k8s.io/sig-storage/csi-node-driver-registrar
registry.k8s.io/sig-storage/csi-provisioner
registry.k8s.io/sig-storage/csi-resizer
registry.k8s.io/sig-storage/csi-snapshotter
registry.k8s.io/sig-storage/livenessprobe
registry.k8s.io/sig-storage/local-volume-provisioner
registry.k8s.io/sig-storage/nfs-subdir-external-provisioner
registry.k8s.io/sig-storage/snapshot-controller
registry.opensource.zalan.do/acid/logical-backup
registry.opensource.zalan.do/acid/pgbouncer
registry.opensource.zalan.do/acid/postgres-operator
registry.opensource.zalan.do/acid/spilo-14
registry.opensource.zalan.do/acid/spilo-15
```



## 4 镜像仓库加速验证

注意：对于nerdctl命令来说，会自动使用/etc/containerd/certs.d目录下的配置镜像加速，但是对于ctr命令，需要指定--hosts-dir=/etc/containerd/certs.d。举个栗子：ctr i pull --hosts-dir=/etc/containerd/certs.d registry.k8s.io/sig-storage/csi-provisioner:v3.5.0，如果要确定此命令是否真的使用了镜像加速，可以增加--debug=true参数，譬如：ctr --debug=true i pull --hosts-dir=/etc/containerd/certs.d registry.k8s.io/sig-storage/csi-provisioner:v3.5.0

  镜像仓库配置如下

```shell
root@containerd:~# tree /etc/containerd/certs.d/
/etc/containerd/certs.d/
├── 192.168.11.20
│   └── hosts.toml
├── docker.io
│   └── hosts.toml
├── gcr.io
│   └── hosts.toml
├── k8s.gcr.io
│   └── hosts.toml
└── registry.k8s.io
    └── hosts.toml

5 directories, 5 files
root@containerd:~#
root@containerd:~#
root@containerd:~# nerdctl images
REPOSITORY    TAG    IMAGE ID    CREATED    PLATFORM    SIZE    BLOB SIZE
root@containerd:~#
root@containerd:~#
```



### 4.1. registry.k8s.io镜像仓库验证

```shell
root@containerd:~#
root@containerd:~# nerdctl --debug=true image pull registry.k8s.io/sig-storage/csi-provisioner:v3.5.0
DEBU[0000] verifying process skipped
DEBU[0000] Found hosts dir "/etc/containerd/certs.d"
DEBU[0000] Ignoring hosts dir "/etc/docker/certs.d"      error="stat /etc/docker/certs.d: no such file or directory"
DEBU[0000] The image will be unpacked for platform {"amd64" "linux" "" [] ""}, snapshotter "overlayfs".
DEBU[0000] fetching                                      image="registry.k8s.io/sig-storage/csi-provisioner:v3.5.0"
DEBU[0000] loading host directory                        dir=/etc/containerd/certs.d/registry.k8s.io
DEBU[0000] resolving                                     host=k8s.m.daocloud.io
DEBU[0000] do request                                    host=k8s.m.daocloud.io request.header.accept="application/vnd.docker.distribution.manifest.v2+json, application/vnd.docker.distribution.manifest.list.v2+json, application/vnd.oci.image.manifest.v1+json, application/vnd.oci.image.index.v1+json, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=HEAD url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/manifests/v3.5.0?ns=registry.k8s.io"
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0: resolving      |--------------------------------------|
elapsed: 0.6 s                                      total:   0.0 B (0.0 B/s)
DEBU[0000] fetch response received                       host=k8s.m.daocloud.io response.header.cache-status=MISS response.header.connection=keep-alive response.header.content-length=2035 response.header.content-type=application/vnd.docker.distribution.manifest.list.v2+json response.header.date="Fri, 28 Jul 2023 02:32:08 GMT" response.header.docker-content-digest="sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f" response.header.docker-distribution-api-version=registry/2.0 response.header.etag="\"sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f\"" response.header.server=nginx response.header.x-content-type-options=nosniff response.status="200 OK" url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/manifests/v3.5.0?ns=registry.k8s.io"
DEBU[0000] resolved                                      desc.digest="sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f" host=k8s.m.daocloud.io
DEBU[0000] loading host directory                        dir=/etc/containerd/certs.d/registry.k8s.io
DEBU[0000] fetch                                         digest="sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f" mediatype=application/vnd.docker.distribution.manifest.list.v2+json size=2035
DEBU[0000] do request                                    digest="sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f" mediatype=application/vnd.docker.distribution.manifest.list.v2+json request.header.accregistry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                            resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f: downloading    |--------------------------------------|    0.0 B/2.0 KiB
elapsed: 0.9 s                                                                 total:   0.0 B (0.0 B/s)
DEBU[0001] fetch response received                       digest="sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f" mediatype=application/vnd.docker.distribution.manifest.list.v2+json response.header.cache-status=MISS response.header.connection=keep-alive response.header.content-length=2035 response.header.content-type=application/vnd.docker.distribution.manifest.list.v2+json response.header.date="Fri, 28 Jul 2023 02:32:09 GMT" response.header.docker-content-digest="sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f" response.header.docker-distribution-api-version=registry/2.0 response.header.etag="\"sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f\"" response.header.server=nginx response.header.x-content-type-options=nosniff response.status="200 OK" size=2035 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/manifests/sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f?ns=registry.k8s.io"
DEBU[0001] fetch                                         digest="sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427" mediatype=application/vnd.docker.distribution.manifest.v2+json size=2403
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: downloading    |--------------------------------------|    0.0 B/2.3 KiB
elapsed: 1.2 s                                                                    total:  2.0 Ki (1.7 KiB/s)
DEBU[0001] fetch response received                       digest="sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427" mediatype=application/vnd.docker.distribution.manifest.v2+json response.header.cache-status=MISS response.header.connection=keep-alive response.header.content-length=2403 response.header.content-type=application/vnd.docker.distribution.manifest.v2+json response.header.date="Fri, 28 Jul 2023 02:32:09 GMT" response.header.docker-content-digest="sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427" response.header.docker-distribution-api-version=registry/2.0 response.header.etag="\"sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427\"" response.header.server=nginx response.header.x-content-type-options=nosniff response.status="200 OK" size=2403 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/manifests/sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427?ns=registry.k8s.io"
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   downloading    |--------------------------------------|    0.0 B/2.5 KiB
elapsed: 1.6 s                                                                    total:  4.3 Ki (2.7 KiB/s)
DEBU[0001] fetch response received                       digest="sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8" mediatype=application/vnd.docker.container.image.v1+json response.header.accept-ranges=bytes response.header.connection=keep-alive response.header.content-length=2541 response.header.content-type=application/octet-stream response.header.date="Fri, 28 Jul 2023 02:28:51 GMT" response.header.etag="\"AQAAADwQbzn_wQZL4cGWH7HiWhM3_AJg\"" response.header.last-modified="Thu, 27 Apr 2023 23:03:54 GMT" response.header.server=nginx response.header.vary="Accept-Encoding,Origin" response.status="200 OK" size=2541 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8?ns=registry.k8s.io"
DEBU[0001] fetch                                         digest="sha256:fff4e558ad3a0d44f9ec0bf32771df4a102c6d93fce253dd3eb6657865b78a49" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=716282
DEBU[0001] fetch                                         digest="sha256:1e3d9b7d145208fa8fa3ee1c9612d0adaac7255f1bbc9ddea7e461e0b317805c" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=113
DEBU[0001] fetch                                         digest="sha256:10f855b03c8aee4fb0b9b7031c333640d684bd9ee6045f11f9892c7fea394701" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=103735
DEBU[0001] fetch                                         digest="sha256:fe5ca62666f04366c8e7f605aa82997d71320183e99962fa76b3209fdfbb8b58" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=21202
DEBU[0001] fetch                                         digest="sha256:6a0243a6417a8582ce3ef1a15252bfabfd17c7ea69a0acd7fff4b9d6cd8c8f40" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=27370029
DEBU[0001] fetch                                         digest="sha256:fcb6f6d2c9986d9cd6a2ea3cc2936e5fc613e09f1af9042329011e43057f3265" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=317
DEBU[0001] fetch                                         digest="sha256:5627a970d25e752d971a501ec7e35d0d6fdcd4a3ce9e958715a686853024794a" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=130562
DEBU[0001] fetch                                         digest="sha256:e8c73c638ae9ec5ad70c49df7e484040d889cca6b4a9af056579c3d058ea93f0" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=198
DEBU[0001] fetch                                         digest="sha256:4aa0ea1413d37a58615488592a0b827ea4b2e48fa5a77cf707d0e35f025e613f" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=385
DEBU[0001] fetch                                         digest="sha256:7c881f9ab25e0d86562a123b5fb56aebf8aa0ddd7d48ef602faf8d1e7cf43d8c" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=355
DEBU[0001] do request                                    digest="sha256:7c881f9ab25e0d86562a123b5fb56aebf8aa0ddd7d48ef602faf8d1e7cf43d8c" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="application/vnd.docker.image.rootfs.diff.tar.gzip, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=GET size=355 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:7c881f9ab25e0d86562a123b5fb56aebf8aa0ddd7d48ef602faf8d1e7cf43d8c?ns=registry.k8s.io"
DEBU[0001] do request                                    digest="sha256:fff4e558ad3a0d44f9ec0bf32771df4a102c6d93fce253dd3eb6657865b78a49" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="application/vnd.docker.image.rootfs.diff.tar.gzip, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=GET size=716282 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:fff4e558ad3a0d44f9ec0bf32771df4a102c6d93fce253dd3eb6657865b78a49?ns=registry.k8s.io"
DEBU[0001] do request                                    digest="sha256:fcb6f6d2c9986d9cd6a2ea3cc2936e5fc613e09f1af9042329011e43057f3265" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="application/vnd.docker.image.rootfs.diff.tar.gzip, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=GET size=317 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:fcb6f6d2c9986d9cd6a2ea3cc2936e5fc613e09f1af9042329011e43057f3265?ns=registry.k8s.io"
DEBU[0001] do request                                    digest="sha256:10f855b03c8aee4fb0b9b7031c333640d684bd9ee6045f11f9892c7fea394701" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="application/vnd.docker.image.rootfs.diff.tar.gzip, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=GET size=103735 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:10f855b03c8aee4fb0b9b7031c333640d684bd9ee6045f11f9892c7fea394701?ns=registry.k8s.io"
DEBU[0001] do request                                    digest="sha256:1e3d9b7d145208fa8fa3ee1c9612d0adaac7255f1bbc9ddea7e461e0b317805c" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="application/vnd.docker.image.rootfs.diff.tar.gzip, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=GET size=113 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:1e3d9b7d145208fa8fa3ee1c9612d0adaac7255f1bbc9ddea7e461e0b317805c?ns=registry.k8s.io"
DEBU[0001] do request                                    digest="sha256:fe5ca62666f04366c8e7f605aa82997d71320183e99962fa76b3209fdfbb8b58" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="application/vnd.docker.image.rootfs.diff.tar.gzip, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=GET size=21202 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:fe5ca62666f04366c8e7f605aa82997d71320183e99962fa76b3209fdfbb8b58?ns=registry.k8s.io"
DEBU[0001] do request                                    digest="sha256:e8c73c638ae9ec5ad70c49df7e484040d889cca6b4a9af056579c3d058ea93f0" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="application/vnd.docker.image.rootfs.diff.tar.gzip, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=GET size=198 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:e8c73c638ae9ec5ad70c49df7e484040d889cca6b4a9af056579c3d058ea93f0?ns=registry.k8s.io"
DEBU[0001] do request                                    digest="sha256:5627a970d25e752d971a501ec7e35d0d6fdcd4a3ce9e958715a686853024794a" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="application/vnd.docker.image.rootfs.diff.tar.gzip, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=GET size=130562 url="https://k8s.m.daocloud.io/v2/sig-storage/csi-provisioner/blobs/sha256:5627a970d25e752d971a501ec7e35d0d6fdcd4a3ce9e958715a686853024794a?ns=registry.k8s.io"
DEBU[0001] do request                                    digest="sha256:6a0243a6417a8582ce3ef1a15252bfabfd17c7ea69a0acd7fff4b9d6cd8c8f40" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="appregistry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
registry.k8s.io/sig-storage/csi-provisioner:v3.5.0:                               resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:d078dc174323407e8cc6f0f9abd4efaac5db27838f1564d0253d5e3233e3f17f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:555c963e93ba4469b1f3a5750305bac30dd6f1cdbc78788e0c083fcad24e1427: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:bf3c3249d9a3062d98e4890d1a81b660e18b706643e3633a13a3aaaf76bf00b8:   done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:fff4e558ad3a0d44f9ec0bf32771df4a102c6d93fce253dd3eb6657865b78a49:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:6a0243a6417a8582ce3ef1a15252bfabfd17c7ea69a0acd7fff4b9d6cd8c8f40:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:fcb6f6d2c9986d9cd6a2ea3cc2936e5fc613e09f1af9042329011e43057f3265:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:e8c73c638ae9ec5ad70c49df7e484040d889cca6b4a9af056579c3d058ea93f0:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:1e3d9b7d145208fa8fa3ee1c9612d0adaac7255f1bbc9ddea7e461e0b317805c:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:10f855b03c8aee4fb0b9b7031c333640d684bd9ee6045f11f9892c7fea394701:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:5627a970d25e752d971a501ec7e35d0d6fdcd4a3ce9e958715a686853024794a:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:7c881f9ab25e0d86562a123b5fb56aebf8aa0ddd7d48ef602faf8d1e7cf43d8c:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:4aa0ea1413d37a58615488592a0b827ea4b2e48fa5a77cf707d0e35f025e613f:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:fe5ca62666f04366c8e7f605aa82997d71320183e99962fa76b3209fdfbb8b58:    done           |++++++++++++++++++++++++++++++++++++++|
elapsed: 5.5 s                                                                    total:  27.0 M (4.9 MiB/s)
root@containerd:~#
root@containerd:~# nerdctl images
REPOSITORY                                     TAG       IMAGE ID        CREATED           PLATFORM       SIZE        BLOB SIZE
registry.k8s.io/sig-storage/csi-provisioner    v3.5.0    d078dc174323    21 seconds ago    linux/amd64    66.1 MiB    27.0 MiB
```



### 4.2. k8s.gcr.io镜像仓库验证

```shell
root@containerd:~# nerdctl --debug=true image pull k8s.gcr.io/kube-apiserver:v1.17.3
DEBU[0000] verifying process skipped
DEBU[0000] Found hosts dir "/etc/containerd/certs.d"
DEBU[0000] Ignoring hosts dir "/etc/docker/certs.d"      error="stat /etc/docker/certs.d: no such file or directory"
DEBU[0000] The image will be unpacked for platform {"amd64" "linux" "" [] ""}, snapshotter "overlayfs".
DEBU[0000] fetching                                      image="k8s.gcr.io/kube-apiserver:v1.17.3"
DEBU[0000] loading host directory                        dir=/etc/containerd/certs.d/k8s.gcr.io
DEBU[0000] resolving                                     host=k8s-gcr.m.daocloud.io
DEBU[0000] do request                                    host=k8s-gcr.m.daocloud.io request.header.accept="application/vnd.docker.distribution.manifest.v2+json, application/vnd.docker.distribution.manifest.list.v2+json, application/vnd.oci.image.manifest.v1+json, application/vnd.oci.image.index.v1+json, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=HEAD url="https://k8s-gcr.m.daocloud.io/v2/kube-apiserver/manifests/v1.17.3?ns=k8s.gcr.io"
k8s.gcr.io/kube-apiserver:v1.17.3: resolving      |--------------------------------------|
elapsed: 1.6 s                     total:   0.0 B (0.0 B/s)
DEBU[0001] fetch response received                       host=k8s-gcr.m.daocloud.io response.header.cache-status=MISS response.header.connection=keep-alive response.header.content-length=1665 response.header.content-type=application/vnd.docker.distribution.manifest.list.v2+json response.header.date="Fri, 28 Jul 2023 02:34:13 GMT" response.header.docker-content-digest="sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900" response.header.docker-distribution-api-version=registry/2.0 response.header.etag="\"sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900\"" response.header.server=nginx response.header.x-content-type-options=nosniff response.status="200 OK" url="https://k8s-gcr.m.daocloud.io/v2/kube-apiserver/manifests/v1.17.3?ns=k8s.gcr.io"
DEBU[0001] resolved                                      desc.digest="sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900" host=k8s-gcr.m.daocloud.io
DEBU[0001] loading host directory                        dir=/etc/containerd/certs.d/k8s.gcr.io
DEBU[0001] fetch                                         digest="sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900" mediatype=application/vnd.docker.distribution.manifest.list.v2+json size=1665
DEBU[0001] do request                                    digest="sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900" mediatype=application/vnd.docker.distribution.manifest.list.v2+json request.header.acck8s.gcr.io/kube-apiserver:v1.17.3:                                             resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900: downloading    |--------------------------------------|    0.0 B/1.6 KiB
elapsed: 2.0 s                                                                 total:   0.0 B (0.0 B/s)
DEBU[0002] fetch response received                       digest="sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900" mediatype=application/vnd.docker.distribution.manifest.list.v2+json response.header.cache-status=MISS response.header.connection=keep-alive response.header.content-length=1665 response.header.content-type=application/vnd.docker.distribution.manifest.list.v2+json response.header.date="Fri, 28 Jul 2023 02:34:14 GMT" response.header.docker-content-digest="sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900" response.header.docker-distribution-api-version=registry/2.0 response.header.etag="\"sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900\"" response.header.server=nginx response.header.x-content-type-options=nosniff response.status="200 OK" size=1665 url="https://k8s-gcr.m.daocloud.io/v2/kube-apiserver/manifests/sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900?ns=k8s.gcr.io"
DEBU[0002] fetch                                         digest="sha256:4ee4113bcce32ae436b15364b09a2439dee15f6a19bbcf43a470d3dc879b0c62" mediatype=application/vnd.docker.distribution.manifest.v2+json size=741
k8s.gcr.io/kube-apiserver:v1.17.3:                                                resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:4ee4113bcce32ae436b15364b09a2439dee15f6a19bbcf43a470d3dc879b0c62: downloading    |--------------------------------------|    0.0 B/741.0 B
elapsed: 2.6 s                                                                    total:  1.6 Ki (639.0 B/s)
DEBU[0002] fetch response received                       digest="sha256:4ee4113bcce32ae436b15364b09a2439dee15f6a19bbcf43a470d3dc879b0c62" mediatype=application/vnd.docker.distribution.manifest.v2+json response.header.cache-status=MISS response.header.connection=keep-alive response.header.content-length=741 response.header.content-type=application/vnd.docker.distribution.manifest.v2+json response.header.date="Fri, 28 Jul 2023 02:34:14 GMT" respok8s.gcr.io/kube-apiserver:v1.17.3:                                                resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:4ee4113bcce32ae436b15364b09a2439dee15f6a19bbcf43a470d3dc879b0c62: downloading    |--------------------------------------|    0.0 B/741.0 B
k8s.gcr.io/kube-apiserver:v1.17.3:                                                resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:4ee4113bcce32ae436b15364b09a2439dee15f6a19bbcf43a470d3dc879b0c62: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:90d27391b7808cde8d9a81cfa43b1e81de5c4912b4b52a7dccb19eb4fe3c236b:   downloading    |--------------------------------------|    0.0 B/1.7 KiB
elapsed: 3.2 s                                                                    total:  2.3 Ki (751.0 B/s)
DEBU[0003] fetch response received                       digest="sha256:90d27391b7808cde8d9a81cfa43b1e81de5c4912b4b52a7dccb19eb4fe3c236b" mediatype=application/vnd.docker.container.image.v1+json response.header.cache-status=MISS response.header.connection=keep-alive response.header.content-length=1767 response.header.content-type=text/html response.header.date="Fri, 28 Jul 2023 02:34:15 GMT" response.header.docker-content-digest="sha256:90d27391b7808cde8d9a81cfa43b1e81de5c4912b4b52a7dccb19eb4fe3c236b" response.header.docker-distribution-api-version=registry/2.0 response.header.etag="sha256:90d27391b7808cde8d9a81cfa43b1e81de5c4912b4b52a7dccb19eb4fe3c236b" response.header.server=nginx response.header.x-content-type-options=nosniff response.status="200 OK" size=1767 url="https://k8s-gcr.m.daocloud.io/v2/kube-apiserver/blobs/sha256:90d27391b7808cde8d9a81cfa43b1e81de5c4912b4b52a7dccb19eb4fe3c236b?ns=k8s.gcr.io"
DEBU[0003] fetch                                         digest="sha256:694976bfeffdb162655017f2c99283712340bd8c23e50c78e3e8d8aa002e9c95" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=29540037
DEBU[0003] fetch                                         digest="sha256:597de8ba0c30cdd0b372023aa2ea3ca9b3affbcba5ac8db922f57d6cb67db7c8" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip size=21089561
DEBU[0003] do request                                    digest="sha256:597de8ba0c30cdd0b372023aa2ea3ca9b3affbcba5ac8db922f57d6cb67db7c8" mediatype=application/vnd.docker.image.rootfs.diff.tar.gzip request.header.accept="appk8s.gcr.io/kube-apiserver:v1.17.3:                                                resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:4ee4113bcce32ae436b15364b09a2439dee15f6a19bbcf43a470d3dc879b0c62: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:90d27391b7808cde8d9a81cfa43b1e81de5c4912b4b52a7dccb19eb4fe3c236b:   done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:694976bfeffdb162655017f2c99283712340bd8c23e50c78e3e8d8aa002e9c95:    downloading    |--------------------------------------|    0.0 B/28.2 MiB
k8s.gcr.io/kube-apiserver:v1.17.3:                                                resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:4ee4113bcce32ae436b15364b09a2439dee15f6a19bbcf43a470d3dc879b0c62: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:90d27391b7808cde8d9a81cfa43b1e81de5c4912b4b52a7dccb19eb4fe3c236b:   done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:694976bfeffdb162655017f2c99283712340bd8c23e50c78e3e8d8aa002e9c95:    downloading    |--------------------------------------|    0.0 B/28.2 MiB
k8s.gcr.io/kube-apiserver:v1.17.3:                                                resolved       |++++++++++++++++++++++++++++++++++++++|
k8s.gcr.io/kube-apiserver:v1.17.3:                                                resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:33400ea29255bd20714b6b8092b22ebb045ae134030d6bf476bddfed9d33e900:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:4ee4113bcce32ae436b15364b09a2439dee15f6a19bbcf43a470d3dc879b0c62: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:90d27391b7808cde8d9a81cfa43b1e81de5c4912b4b52a7dccb19eb4fe3c236b:   done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:694976bfeffdb162655017f2c99283712340bd8c23e50c78e3e8d8aa002e9c95:    done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:597de8ba0c30cdd0b372023aa2ea3ca9b3affbcba5ac8db922f57d6cb67db7c8:    done           |++++++++++++++++++++++++++++++++++++++|
elapsed: 133.0s                                                                   total:  48.3 M (371.8 KiB/s)
root@containerd:~# nerdctl images
REPOSITORY                                     TAG        IMAGE ID        CREATED               PLATFORM       SIZE         BLOB SIZE
k8s.gcr.io/kube-apiserver                      v1.17.3    33400ea29255    About a minute ago    linux/amd64    167.3 MiB    48.3 MiB
registry.k8s.io/sig-storage/csi-provisioner    v3.5.0     d078dc174323    6 minutes ago         linux/amd64    66.1 MiB     27.0 MiB
```



### 4.3. docker.io镜像仓库验证

```shell
root@containerd:~# nerdctl --debug=true image pull docker.io/library/ubuntu:20.04
DEBU[0000] verifying process skipped
DEBU[0000] Found hosts dir "/etc/containerd/certs.d"
DEBU[0000] Ignoring hosts dir "/etc/docker/certs.d"      error="stat /etc/docker/certs.d: no such file or directory"
DEBU[0000] The image will be unpacked for platform {"amd64" "linux" "" [] ""}, snapshotter "overlayfs".
DEBU[0000] fetching                                      image="docker.io/library/ubuntu:20.04"
DEBU[0000] loading host directory                        dir=/etc/containerd/certs.d/docker.io
DEBU[0000] resolving                                     host=hub-mirror.c.163.com
DEBU[0000] do request                                    host=hub-mirror.c.163.com request.header.accept="application/vnd.docker.distribution.manifest.v2+json, application/vnd.docker.distribution.manifest.list.v2+json, application/vnd.oci.image.manifest.v1+json, application/vnd.oci.image.index.v1+json, */*" request.header.user-agent=containerd/1.7.1+unknown request.method=HEAD url="https://hub-mirror.c.163.com/v2/library/ubuntu/manifests/20.04?ns=docker.io"
docker.io/library/ubuntu:20.04: resolving      |--------------------------------------|
elapsed: 1.2 s                  total:   0.0 B (0.0 B/s)
DEBU[0001] fetch response received                       host=hub-mirror.c.163.com response.header.connection=keep-alive response.header.content-length=1201 response.header.content-type=application/vnd.docker.distribution.manifest.list.v2+json response.header.date="Fri, 28 Jul 2023 02:42:20 GMT" response.header.docker-content-digest="sha256:b872b0383a2149196c67d16279f051c3e36f2acb32d7eb04ef364c8863c6264f" response.header.docker-distribution-api-version=registry/2.0 response.header.etag="\"sha256:b872b0383a2149196c67d16279f051c3e36f2acb32d7eb04ef364c8863c6264f\"" response.header.server=nginx/1.10.1 response.status="200 OK" url="https://hub-mirror.c.163.com/v2/library/ubuntu/manifests/20.04?ns=docker.io"
DEBU[0001] resolved                                      desc.digest="sha256:b872b0383a2149196c67d16279f051c3e36f2acb32d7eb04ef364c8863c6264f" host=hub-mirror.c.163.com
DEBU[0001] loading host directory                        dir=/etc/containerd/certs.d/docker.io
DEBU[0001] fetch                                         digest="sha256:b872b0383a2149196c67d16279f051c3e36f2acb32d7eb04ef364c8863c6264f" mediatype=application/vnd.docker.distribution.manifest.list.v2+json size=1201
DEBU[0001] fetch                                         digest="sha256:8eb87f3d6c9f2feee114ff0eff93ea9dfd20b294df0a0353bd6a4abf403336fe" mediatype=application/vnd.docker.distribution.manifest.v2+json size=529
docker.io/library/ubuntu:20.04:                                                   resolved       |++++++++++++++++++++++++++++++++++++++|
docker.io/library/ubuntu:20.04:                                                   resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:b872b0383a2149196c67d16279f051c3e36f2acb32d7eb04ef364c8863c6264f:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:8eb87f3d6c9f2feee114ff0eff93ea9dfd20b294df0a0353bd6a4abf403336fe: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:d5447fc01ae62c20beffbfa50bc51b2797f9d7ebae031b8c2245b5be8ff1c75b:   done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:846c0b181fff0c667d9444f8378e8fcfa13116da8d308bf21673f7e4bea8d580:    done           |++++++++++++++++++++++++++++++++++++++|
elapsed: 2.6 s                                                                    total:  27.3 M (10.5 MiB/s)
root@containerd:~#
root@containerd:~#
root@containerd:~# nerdctl images
REPOSITORY                                     TAG        IMAGE ID        CREATED           PLATFORM       SIZE         BLOB SIZE
ubuntu                                         20.04      b872b0383a21    40 seconds ago    linux/amd64    75.8 MiB     27.3 MiB
k8s.gcr.io/kube-apiserver                      v1.17.3    33400ea29255    9 minutes ago     linux/amd64    167.3 MiB    48.3 MiB
registry.k8s.io/sig-storage/csi-provisioner    v3.5.0     d078dc174323    14 minutes ago    linux/amd64    66.1 MiB     27.0 MiB
```



## 5 参考

daocloud镜像仓库

https://github.com/containerd/containerd/blob/main/docs/hosts.md

https://github.com/containerd/containerd/blob/main/docs/cri/registry.md