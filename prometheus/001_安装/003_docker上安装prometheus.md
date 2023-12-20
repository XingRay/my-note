## docker上安装prometheus

```bash
docker run -d --name prometheus --net-host -v /etc/prometheus:/etc/prometheus prom/prometheus
```

