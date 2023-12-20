## 在Windows上安装Docker

https://docs.docker.com/desktop/install/windows-install/

https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe



下载 Docker Desktop for Windows



设置:

DockerEngine 设置

```json
{
  "builder": {
    "features": {
      "buildkit": true
    },
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": ["https://jkzyghm3.mirror.aliyuncs.com"]
}
```

