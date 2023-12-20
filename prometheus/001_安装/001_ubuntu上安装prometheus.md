## Ubuntu上安装Prometheus

### 1 下载安装包

https://prometheus.io/download/

下载prometheus的linux版本,当前最新版本为

https://github.com/prometheus/prometheus/releases/download/v2.45.0/prometheus-2.45.0.linux-amd64.tar.gz



2 安装

将安装包下载后上传至服务器

解压到指定目录

```bash
mkdir -p /root/develop/prometheus
```

```bash
tar -zxvf prometheus-2.45.0.linux-amd64.tar.gz -C /root/develop/prometheus/
```

进入解压后的目录

```bash
cd /root/develop/prometheus/prometheus-2.45.0.linux-amd64
```

列出文件

```bash
ls
```

```bash
console_libraries  consoles  LICENSE  NOTICE  prometheus  prometheus.yml  promtool
```



修改配置 

```
vi prometheus.yml
```

```yaml
# my global config
global:
  scrape_interval: 15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

# Alertmanager configuration
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          # - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: "prometheus"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
      - targets: ["localhost:9090"]
```

global: 全局配置, 如 scrape_interval 抓取监控数据的间隔  evaluation_interval 计算规则间隔

alerting: 警报配置,

rule_files: 规则文件配置

scrape_configs: 抓取监控数据的配置, 默认抓取的目标只有 prometheus 自身, prometheus自身也有一些监控信息可以抓取

配置的文档:

https://prometheus.io/docs/prometheus/latest/configuration/configuration/





启动

```bash
prometheus --config.file=prometheus.yml
```

正常启动之后可以通过浏览器访问 http://192.168.0.140:9090/ 进入 prometheus 的web页面



