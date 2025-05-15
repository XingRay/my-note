### **解决方案**

#### **方法 1: 强制使用 SSH 协议克隆子模块**

```bash
# 全局替换 HTTPS 为 SSH
git config --global url."git@github.com:".insteadOf "https://github.com/"
```