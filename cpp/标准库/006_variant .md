# make_move_iterator

代码示例

```c++
std::vector<const char*> enabledExtensions = std::move(extensions);
std::vector<const char*> enabledLayers = std::move(layers);

for (const std::unique_ptr<DevicePlugin>& devicePlugin : devicePlugins) {
    devicePlugin->onCreateDevice(deviceCreateInfo);
    
    // 获取插件扩展（假设 getDeviceExtensions 返回临时对象）
    auto pluginDeviceExtensions = devicePlugin->getDeviceExtensions();
    enabledExtensions.insert(
        enabledExtensions.end(),
        std::make_move_iterator(pluginDeviceExtensions.begin()),
        std::make_move_iterator(pluginDeviceExtensions.end())
    );
    
    // 获取插件层（假设 getLayers 返回临时对象）
    auto pluginLayers = devicePlugin->getLayers();
    enabledLayers.insert(
        enabledLayers.end(),
        std::make_move_iterator(pluginLayers.begin()),
        std::make_move_iterator(pluginLayers.end())
    );
}
```

