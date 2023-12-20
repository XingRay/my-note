# Kubernetes的RBAC

### 1 创建账号

serviceaccount-default.yaml

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: default
  namespace: springcloud-k8s
```



### 2 创建角色

role-app.yaml

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: role-app
  namespace: springcloud-k8s
rules:
- apiGroups: [""]
  resources: ["endpoints"]
  verbs: ["get", "list", "watch"]
- apiGroups: [""]
  resources: ["services"]
  verbs: ["get", "list", "watch"]
```



### 3 创建 账号-角色 绑定关系

rolebinding-default-role-app.yaml

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: rolebinding-default-role-app
  namespace: springcloud-k8s
subjects:
- kind: ServiceAccount
  name: default
  namespace: springcloud-k8s
roleRef:
  kind: Role
  name: role-app
  apiGroup: rbac.authorization.k8s.io
```



### 4 查看资源及操作

```bash
kubectl api-resources -owide
```

```bash
NAME                              SHORTNAMES         APIVERSION                             NAMESPACED   KIND                             VERBS
bindings                                             v1                                     true         Binding                          [create]
componentstatuses                 cs                 v1                                     false        ComponentStatus                  [get list]
configmaps                        cm                 v1                                     true         ConfigMap                        [create delete deletecollection get list patch update watch]
endpoints                         ep                 v1                                     true         Endpoints                        [create delete deletecollection get list patch update watch]
events                            ev                 v1                                     true         Event                            [create delete deletecollection get list patch update watch]
limitranges                       limits             v1                                     true         LimitRange                       [create delete deletecollection get list patch update watch]
namespaces                        ns                 v1                                     false        Namespace                        [create delete get list patch update watch]
nodes                             no                 v1                                     false        Node                             [create delete deletecollection get list patch update watch]
persistentvolumeclaims            pvc                v1                                     true         PersistentVolumeClaim            [create delete deletecollection get list patch update watch]
persistentvolumes                 pv                 v1                                     false        PersistentVolume                 [create delete deletecollection get list patch update watch]
pods                              po                 v1                                     true         Pod                              [create delete deletecollection get list patch update watch]
podtemplates                                         v1                                     true         PodTemplate                      [create delete deletecollection get list patch update watch]
replicationcontrollers            rc                 v1                                     true         ReplicationController            [create delete deletecollection get list patch update watch]
resourcequotas                    quota              v1                                     true         ResourceQuota                    [create delete deletecollection get list patch update watch]
secrets                                              v1                                     true         Secret                           [create delete deletecollection get list patch update watch]
serviceaccounts                   sa                 v1                                     true         ServiceAccount                   [create delete deletecollection get list patch update watch]
services                          svc                v1                                     true         Service                          [create delete deletecollection get list patch update watch]
mutatingwebhookconfigurations                        admissionregistration.k8s.io/v1        false        MutatingWebhookConfiguration     [create delete deletecollection get list patch update watch]
validatingwebhookconfigurations                      admissionregistration.k8s.io/v1        false        ValidatingWebhookConfiguration   [create delete deletecollection get list patch update watch]
customresourcedefinitions         crd,crds           apiextensions.k8s.io/v1                false        CustomResourceDefinition         [create delete deletecollection get list patch update watch]
apiservices                                          apiregistration.k8s.io/v1              false        APIService                       [create delete deletecollection get list patch update watch]
applications                      app                app.k8s.io/v1beta1                     true         Application                      [delete deletecollection get list patch create update watch]
helmapplications                  happ               application.kubesphere.io/v1alpha1     false        HelmApplication                  [delete deletecollection get list patch create update watch]
helmapplicationversions           happver            application.kubesphere.io/v1alpha1     false        HelmApplicationVersion           [delete deletecollection get list patch create update watch]
helmcategories                    hctg               application.kubesphere.io/v1alpha1     false        HelmCategory                     [delete deletecollection get list patch create update watch]
helmreleases                      hrls               application.kubesphere.io/v1alpha1     false        HelmRelease                      [delete deletecollection get list patch create update watch]
helmrepos                         hrepo              application.kubesphere.io/v1alpha1     false        HelmRepo                         [delete deletecollection get list patch create update watch]
controllerrevisions                                  apps/v1                                true         ControllerRevision               [create delete deletecollection get list patch update watch]
daemonsets                        ds                 apps/v1                                true         DaemonSet                        [create delete deletecollection get list patch update watch]
deployments                       deploy             apps/v1                                true         Deployment                       [create delete deletecollection get list patch update watch]
replicasets                       rs                 apps/v1                                true         ReplicaSet                       [create delete deletecollection get list patch update watch]
statefulsets                      sts                apps/v1                                true         StatefulSet                      [create delete deletecollection get list patch update watch]
applications                      app,apps           argoproj.io/v1alpha1                   true         Application                      [delete deletecollection get list patch create update watch]
applicationsets                   appset,appsets     argoproj.io/v1alpha1                   true         ApplicationSet                   [delete deletecollection get list patch create update watch]
appprojects                       appproj,appprojs   argoproj.io/v1alpha1                   true         AppProject                       [delete deletecollection get list patch create update watch]
argocdextensions                                     argoproj.io/v1alpha1                   true         ArgoCDExtension                  [delete deletecollection get list patch create update watch]
tokenreviews                                         authentication.k8s.io/v1               false        TokenReview                      [create]
localsubjectaccessreviews                            authorization.k8s.io/v1                true         LocalSubjectAccessReview         [create]
selfsubjectaccessreviews                             authorization.k8s.io/v1                false        SelfSubjectAccessReview          [create]
selfsubjectrulesreviews                              authorization.k8s.io/v1                false        SelfSubjectRulesReview           [create]
subjectaccessreviews                                 authorization.k8s.io/v1                false        SubjectAccessReview              [create]
horizontalpodautoscalers          hpa                autoscaling/v2                         true         HorizontalPodAutoscaler          [create delete deletecollection get list patch update watch]
cronjobs                          cj                 batch/v1                               true         CronJob                          [create delete deletecollection get list patch update watch]
jobs                                                 batch/v1                               true         Job                              [create delete deletecollection get list patch update watch]
certificatesigningrequests        csr                certificates.k8s.io/v1                 false        CertificateSigningRequest        [create delete deletecollection get list patch update watch]
clusters                                             cluster.kubesphere.io/v1alpha1         false        Cluster                          [delete deletecollection get list patch create update watch]
leases                                               coordination.k8s.io/v1                 true         Lease                            [create delete deletecollection get list patch update watch]
bgpconfigurations                                    crd.projectcalico.org/v1               false        BGPConfiguration                 [delete deletecollection get list patch create update watch]
bgppeers                                             crd.projectcalico.org/v1               false        BGPPeer                          [delete deletecollection get list patch create update watch]
blockaffinities                                      crd.projectcalico.org/v1               false        BlockAffinity                    [delete deletecollection get list patch create update watch]
caliconodestatuses                                   crd.projectcalico.org/v1               false        CalicoNodeStatus                 [delete deletecollection get list patch create update watch]
clusterinformations                                  crd.projectcalico.org/v1               false        ClusterInformation               [delete deletecollection get list patch create update watch]
felixconfigurations                                  crd.projectcalico.org/v1               false        FelixConfiguration               [delete deletecollection get list patch create update watch]
globalnetworkpolicies                                crd.projectcalico.org/v1               false        GlobalNetworkPolicy              [delete deletecollection get list patch create update watch]
globalnetworksets                                    crd.projectcalico.org/v1               false        GlobalNetworkSet                 [delete deletecollection get list patch create update watch]
hostendpoints                                        crd.projectcalico.org/v1               false        HostEndpoint                     [delete deletecollection get list patch create update watch]
ipamblocks                                           crd.projectcalico.org/v1               false        IPAMBlock                        [delete deletecollection get list patch create update watch]
ipamconfigs                                          crd.projectcalico.org/v1               false        IPAMConfig                       [delete deletecollection get list patch create update watch]
ipamhandles                                          crd.projectcalico.org/v1               false        IPAMHandle                       [delete deletecollection get list patch create update watch]
ippools                                              crd.projectcalico.org/v1               false        IPPool                           [delete deletecollection get list patch create update watch]
ipreservations                                       crd.projectcalico.org/v1               false        IPReservation                    [delete deletecollection get list patch create update watch]
kubecontrollersconfigurations                        crd.projectcalico.org/v1               false        KubeControllersConfiguration     [delete deletecollection get list patch create update watch]
networkpolicies                                      crd.projectcalico.org/v1               true         NetworkPolicy                    [delete deletecollection get list patch create update watch]
networksets                                          crd.projectcalico.org/v1               true         NetworkSet                       [delete deletecollection get list patch create update watch]
clustertemplates                                     devops.kubesphere.io/v1alpha3          false        ClusterTemplate                  [delete deletecollection get list patch create update watch]
devopsprojects                                       devops.kubesphere.io/v1alpha3          false        DevOpsProject                    [delete deletecollection get list patch create update watch]
gitrepositories                                      devops.kubesphere.io/v1alpha3          true         GitRepository                    [delete deletecollection get list patch create update watch]
pipelineruns                      pr                 devops.kubesphere.io/v1alpha3          true         PipelineRun                      [delete deletecollection get list patch create update watch]
pipelines                         pip                devops.kubesphere.io/v1alpha3          true         Pipeline                         [delete deletecollection get list patch create update watch]
s2ibinaries                                          devops.kubesphere.io/v1alpha1          true         S2iBinary                        [delete deletecollection get list patch create update watch]
s2ibuilders                       s2ib               devops.kubesphere.io/v1alpha1          true         S2iBuilder                       [delete deletecollection get list patch create update watch]
s2ibuildertemplates               s2ibt              devops.kubesphere.io/v1alpha1          false        S2iBuilderTemplate               [delete deletecollection get list patch create update watch]
s2iruns                           s2ir               devops.kubesphere.io/v1alpha1          true         S2iRun                           [delete deletecollection get list patch create update watch]
templates                                            devops.kubesphere.io/v1alpha3          true         Template                         [delete deletecollection get list patch create update watch]
endpointslices                                       discovery.k8s.io/v1                    true         EndpointSlice                    [create delete deletecollection get list patch update watch]
events                            ev                 events.k8s.io/v1                       true         Event                            [create delete deletecollection get list patch update watch]
flowschemas                                          flowcontrol.apiserver.k8s.io/v1beta2   false        FlowSchema                       [create delete deletecollection get list patch update watch]
prioritylevelconfigurations                          flowcontrol.apiserver.k8s.io/v1beta2   false        PriorityLevelConfiguration       [create delete deletecollection get list patch update watch]
gateways                                             gateway.kubesphere.io/v1alpha1         true         Gateway                          [delete deletecollection get list patch create update watch]
nginxes                                              gateway.kubesphere.io/v1alpha1         true         Nginx                            [delete deletecollection get list patch create update watch]
applications                                         gitops.kubesphere.io/v1alpha1          true         Application                      [delete deletecollection get list patch create update watch]
federatedrolebindings                                iam.kubesphere.io/v1alpha2             true         FederatedRoleBinding             [delete deletecollection get list patch create update watch]
federatedroles                                       iam.kubesphere.io/v1alpha2             true         FederatedRole                    [delete deletecollection get list patch create update watch]
federatedusers                                       iam.kubesphere.io/v1alpha2             true         FederatedUser                    [delete deletecollection get list patch create update watch]
globalrolebindings                                   iam.kubesphere.io/v1alpha2             false        GlobalRoleBinding                [delete deletecollection get list patch create update watch]
globalroles                                          iam.kubesphere.io/v1alpha2             false        GlobalRole                       [delete deletecollection get list patch create update watch]
groupbindings                                        iam.kubesphere.io/v1alpha2             false        GroupBinding                     [delete deletecollection get list patch create update watch]
groups                                               iam.kubesphere.io/v1alpha2             false        Group                            [delete deletecollection get list patch create update watch]
loginrecords                                         iam.kubesphere.io/v1alpha2             false        LoginRecord                      [delete deletecollection get list patch create update watch]
rolebases                                            iam.kubesphere.io/v1alpha2             false        RoleBase                         [delete deletecollection get list patch create update watch]
users                                                iam.kubesphere.io/v1alpha2             false        User                             [delete deletecollection get list patch create update watch]
workspacerolebindings                                iam.kubesphere.io/v1alpha2             false        WorkspaceRoleBinding             [delete deletecollection get list patch create update watch]
workspaceroles                                       iam.kubesphere.io/v1alpha2             false        WorkspaceRole                    [delete deletecollection get list patch create update watch]
clusterconfigurations             cc                 installer.kubesphere.io/v1alpha1       true         ClusterConfiguration             [delete deletecollection get list patch create update watch]
alertmanagerconfigs                                  monitoring.coreos.com/v1alpha1         true         AlertmanagerConfig               [delete deletecollection get list patch create update watch]
alertmanagers                                        monitoring.coreos.com/v1               true         Alertmanager                     [delete deletecollection get list patch create update watch]
podmonitors                                          monitoring.coreos.com/v1               true         PodMonitor                       [delete deletecollection get list patch create update watch]
probes                                               monitoring.coreos.com/v1               true         Probe                            [delete deletecollection get list patch create update watch]
prometheuses                                         monitoring.coreos.com/v1               true         Prometheus                       [delete deletecollection get list patch create update watch]
prometheusrules                                      monitoring.coreos.com/v1               true         PrometheusRule                   [delete deletecollection get list patch create update watch]
servicemonitors                                      monitoring.coreos.com/v1               true         ServiceMonitor                   [delete deletecollection get list patch create update watch]
thanosrulers                                         monitoring.coreos.com/v1               true         ThanosRuler                      [delete deletecollection get list patch create update watch]
clusterdashboards                                    monitoring.kubesphere.io/v1alpha2      false        ClusterDashboard                 [delete deletecollection get list patch create update watch]
dashboards                                           monitoring.kubesphere.io/v1alpha2      true         Dashboard                        [delete deletecollection get list patch create update watch]
ipamblocks                                           network.kubesphere.io/v1alpha1         false        IPAMBlock                        [delete deletecollection get list patch create update watch]
ipamhandles                                          network.kubesphere.io/v1alpha1         false        IPAMHandle                       [delete deletecollection get list patch create update watch]
ippools                                              network.kubesphere.io/v1alpha1         false        IPPool                           [delete deletecollection get list patch create update watch]
namespacenetworkpolicies          nsnp               network.kubesphere.io/v1alpha1         true         NamespaceNetworkPolicy           [delete deletecollection get list patch create update watch]
ingressclasses                                       networking.k8s.io/v1                   false        IngressClass                     [create delete deletecollection get list patch update watch]
ingresses                         ing                networking.k8s.io/v1                   true         Ingress                          [create delete deletecollection get list patch update watch]
networkpolicies                   netpol             networking.k8s.io/v1                   true         NetworkPolicy                    [create delete deletecollection get list patch update watch]
runtimeclasses                                       node.k8s.io/v1                         false        RuntimeClass                     [create delete deletecollection get list patch update watch]
configs                           nc                 notification.kubesphere.io/v2beta2     false        Config                           [delete deletecollection get list patch create update watch]
notificationmanagers              nm                 notification.kubesphere.io/v2beta2     false        NotificationManager              [delete deletecollection get list patch create update watch]
receivers                         nr                 notification.kubesphere.io/v2beta2     false        Receiver                         [delete deletecollection get list patch create update watch]
poddisruptionbudgets              pdb                policy/v1                              true         PodDisruptionBudget              [create delete deletecollection get list patch update watch]
podsecuritypolicies               psp                policy/v1beta1                         false        PodSecurityPolicy                [create delete deletecollection get list patch update watch]
resourcequotas                                       quota.kubesphere.io/v1alpha2           false        ResourceQuota                    [delete deletecollection get list patch create update watch]
clusterrolebindings                                  rbac.authorization.k8s.io/v1           false        ClusterRoleBinding               [create delete deletecollection get list patch update watch]
clusterroles                                         rbac.authorization.k8s.io/v1           false        ClusterRole                      [create delete deletecollection get list patch update watch]
rolebindings                                         rbac.authorization.k8s.io/v1           true         RoleBinding                      [create delete deletecollection get list patch update watch]
roles                                                rbac.authorization.k8s.io/v1           true         Role                             [create delete deletecollection get list patch update watch]
priorityclasses                   pc                 scheduling.k8s.io/v1                   false        PriorityClass                    [create delete deletecollection get list patch update watch]
servicepolicies                                      servicemesh.kubesphere.io/v1alpha2     true         ServicePolicy                    [delete deletecollection get list patch create update watch]
strategies                                           servicemesh.kubesphere.io/v1alpha2     true         Strategy                         [delete deletecollection get list patch create update watch]
volumesnapshotclasses                                snapshot.storage.k8s.io/v1             false        VolumeSnapshotClass              [delete deletecollection get list patch create update watch]
volumesnapshotcontents                               snapshot.storage.k8s.io/v1             false        VolumeSnapshotContent            [delete deletecollection get list patch create update watch]
volumesnapshots                                      snapshot.storage.k8s.io/v1             true         VolumeSnapshot                   [delete deletecollection get list patch create update watch]
csidrivers                                           storage.k8s.io/v1                      false        CSIDriver                        [create delete deletecollection get list patch update watch]
csinodes                                             storage.k8s.io/v1                      false        CSINode                          [create delete deletecollection get list patch update watch]
csistoragecapacities                                 storage.k8s.io/v1                      true         CSIStorageCapacity               [create delete deletecollection get list patch update watch]
storageclasses                    sc                 storage.k8s.io/v1                      false        StorageClass                     [create delete deletecollection get list patch update watch]
volumeattachments                                    storage.k8s.io/v1                      false        VolumeAttachment                 [create delete deletecollection get list patch update watch]
accessors                                            storage.kubesphere.io/v1alpha1         false        Accessor                         [delete deletecollection get list patch create update watch]
workspaces                                           tenant.kubesphere.io/v1alpha1          false        Workspace                        [delete deletecollection get list patch create update watch]
workspacetemplates                                   tenant.kubesphere.io/v1alpha2          false        WorkspaceTemplate                [delete deletecollection get list patch create update watch]
```

