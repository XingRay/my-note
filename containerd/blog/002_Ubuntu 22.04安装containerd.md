# Ubuntu 22.04安装containerd

```shell
CONTAINERD_VERSION=1.7.2
wget https://ghproxy.com/https://github.com/containerd/containerd/releases/download/v${CONTAINERD_VERSION}/cri-containerd-cni-${CONTAINERD_VERSION}-linux-amd64.tar.gz
tar -xvzf cri-containerd-cni-*-linux-amd64.tar.gz -C /
rm -f cri-containerd-cni-*-linux-amd64.tar.gz

NERDCTL_VERSION=1.4.0
wget https://ghproxy.com/https://github.com/containerd/nerdctl/releases/download/v${NERDCTL_VERSION}/nerdctl-${NERDCTL_VERSION}-linux-amd64.tar.gz
mkdir nerdctl
tar -xf nerdctl-${NERDCTL_VERSION}-linux-amd64.tar.gz -C nerdctl/
mv nerdctl/nerdctl /usr/local/bin/
rm -rf nerdctl

# 覆盖containerd的runc工具
RUNC_VERSION=1.1.7
wget https://ghproxy.com/https://github.com/opencontainers/runc/releases/download/v${RUNC_VERSION}/runc.amd64
mv runc.amd64 runc && chmod +x runc && mv -f runc /usr/local/sbin/

# 创建默认配置文件
mkdir -p /etc/containerd
# 创建containerd默认配置文件
containerd config default | tee /etc/containerd/config.toml

# 修改Containerd的配置文件
sed -i "s#SystemdCgroup\ \=\ false#SystemdCgroup\ \=\ true#g" /etc/containerd/config.toml
cat /etc/containerd/config.toml | grep SystemdCgroup

sed -i "s#registry.k8s.io#m.daocloud.io/registry.k8s.io#g" /etc/containerd/config.toml
cat /etc/containerd/config.toml | grep sandbox_image

sed -i "s#config_path\ \=\ \"\"#config_path\ \=\ \"/etc/containerd/certs.d\"#g" /etc/containerd/config.toml
cat /etc/containerd/config.toml | grep certs.d

mkdir /etc/containerd/certs.d/docker.io -pv

# 配置加速器
cat > /etc/containerd/certs.d/docker.io/hosts.toml << EOF
server = "https://docker.io"
[host."https://hub-mirror.c.163.com"]
  capabilities = ["pull", "resolve"]
EOF

#生成配置文件
cat > /etc/crictl.yaml <<EOF
runtime-endpoint: unix:///run/containerd/containerd.sock
image-endpoint: unix:///run/containerd/containerd.sock
timeout: 10
debug: false
EOF


systemctl daemon-reload
systemctl enable --now containerd.service
systemctl restart  containerd.service

# 验证containerd是否安装成功
crictl info

# 验证是否可以下载镜像
ctr images pull docker.io/library/redis:alpine
```

