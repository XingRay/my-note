# MNN createRuntime（二）

## 1、createRuntime

    根据 ScheduleConfig 创建运行时 Runtime 。RuntimeInfo 的定义见 2.1，其 first 用来存放根据 configs 创建的 Runtime（如 VulkanRuntime，CUDARuntime），它的 second 存放的是默认 Runtime，一般为 CPURuntime 。

![在这里插入图片描述](./assets/bbd67670b5dd80e9a7772d45439ddfba.png)

```
RuntimeInfo Interpreter::createRuntime(const std::vector<ScheduleConfig>& configs) {
    RuntimeInfo res;
    // 根据 configs 创建的 Runtime 存放在这里
    auto& mRuntimes = res.first;
    for (auto& config : configs) {
        Backend::Info compute;
        compute.type      = Schedule::getApprociateType(config);
        compute.numThread = config.numThread;
        if(config.type == MNN_FORWARD_AUTO) {
            if(compute.type == MNN_FORWARD_OPENCL || compute.type == MNN_FORWARD_METAL) {
                // AUTO set default gpu-mode MNN_GPU_TUNING_FAST
                compute.numThread = 16;
            }
        }
        compute.user      = config.backendConfig;
        if (mRuntimes.find(compute.type) == mRuntimes.end()) {
            auto newBn = RuntimeFactory::create(compute);
            if (nullptr == newBn) {
                MNN_ERROR("Can't create Runtime: %s\n", EnumNameForwardType((ForwardType)compute.type));
                continue;
            }
            mRuntimes[compute.type].reset(newBn);
        }
    }
    _getDefaultBackend(res);
    return res;
}
```



### 1.1 RuntimeInfo

```
typedef std::pair< std::map<MNNForwardType, std::shared_ptr<Runtime>>, \
					 std::shared_ptr<Runtime>> RuntimeInfo;

```



### 1.2 Schedule::getApprociateType

    // source/core/Schedule.cpp
    MNNForwardType Schedule::getApprociateType(const ScheduleConfig& config) {
        MNNForwardType type = config.type;
        // FIXME: Support Auto determine
        // MNN_FORWARD_AUTO 的处理逻辑
        if (MNN_FORWARD_AUTO == config.type) {
    	//Define Auto choose priority
            std::vector<MNNForwardType> priorityList;
            priorityList.push_back(MNN_FORWARD_USER_0); //HIAI
            priorityList.push_back(MNN_FORWARD_NN);     //CoreML
            priorityList.push_back(MNN_FORWARD_USER_1); //TensoRT
            priorityList.push_back(MNN_FORWARD_CUDA);   //CUDA
            priorityList.push_back(MNN_FORWARD_OPENCL); //OpenCL
            priorityList.push_back(MNN_FORWARD_METAL);  //METAL
            priorityList.push_back(MNN_FORWARD_VULKAN); //Vulkan
            priorityList.push_back(MNN_FORWARD_CPU);    //CPU
    
            for (auto bn : priorityList) {
                if (MNNGetExtraRuntimeCreator(bn) != nullptr) {
                    type = (MNNForwardType)bn;
                    break;
                }
            }
        }
        auto creator = MNNGetExtraRuntimeCreator(type);
        if (nullptr == creator) {
            MNN_PRINT("Can't Find type=%d backend, use %d instead\n", type, config.backupType);
            type = config.backupType;
        } else {
            // TODO : Not Limited to opencl
            if(type == MNN_FORWARD_OPENCL && config.backendConfig != nullptr) {
                if(config.backendConfig->power == BackendConfig::Power_Low) {
                    Backend::Info info;
                    info.type = type;
                    std::shared_ptr<Runtime> bn(creator->onCreate(info));
                    bool isSupportLowPower = bn->onGetRuntimeStatus(RuntimeStatus::STATUS_SUPPORT_POWER_LOW);
                    if(!isSupportLowPower) {
                        MNN_PRINT("type=%d backend don't Support Low Power, use %d instead\n", type, config.backupType);
                        type = config.backupType;
                    }
                }
            }
        }
        
    
        return type;
    }



#### 1.2.1 MNNGetExtraRuntimeCreator

	// source/core/Backend.cpp
	const RuntimeCreator* MNNGetExtraRuntimeCreator(MNNForwardType type) {
	    registerBackend();
	
		// 获取运行时创建器
		// （std::map<MNNForwardType, std::pair<const RuntimeCreator*, bool>>类型）
	    auto& gExtraCreator = GetExtraCreator();
	    // 根据推理类型查找运行时创建器
	    auto iter           = gExtraCreator.find(type);
	    if (iter == gExtraCreator.end()) {
	        return nullptr;
	    }
	    // iter->second 的类型为 std::pair<const RuntimeCreator* creator, bool needCheck>
	    if (!iter->second.second) {
	        return iter->second.first;
	    }
	    Backend::Info info;
	    info.type = type;
	    std::shared_ptr<Runtime> bn(iter->second.first->onCreate(info));
	    if (nullptr != bn.get()) {
	        return iter->second.first;
	    }
	    return nullptr;
	}


##### 1.2.1.1 registerBackend

```
static std::once_flag s_flag;
void registerBackend() {
    std::call_once(s_flag, [&]() {
#ifdef MNN_INTERNAL_ENABLED
        LogInit();
#endif
		// 注册 CPU 的运行时创建器和一些核心函数
        registerCPURuntimeCreator();
#ifndef MNN_BUILD_MINI
		// 维度计算初始化
        SizeComputerSuite::init();
        // 几何计算初始化
        GeometryComputer::init();
#endif
#if MNN_COREML_ENABLED
        registerCoreMLRuntimeCreator();
#endif
#ifdef MNN_NNAPI_ENABLED
        registerNNAPIRuntimeCreator();
#endif
#if MNN_OPENCL_ENABLED
        OpenCL::registerOpenCLRuntimeCreator();
#endif
#if MNN_METAL_ENABLED
        registerMetalRuntimeCreator();
#endif
    });
}
```



##### 1.2.1.2 GetExtraCreator

```
static std::map<MNNForwardType, std::pair<const RuntimeCreator*, bool>>& GetExtraCreator() {
    static std::once_flag gInitFlag;
    static std::map<MNNForwardType, std::pair<const RuntimeCreator*, bool>>* gExtraCreator;
    std::call_once(gInitFlag,
                   [&]() { gExtraCreator = new std::map<MNNForwardType, std::pair<const RuntimeCreator*, bool>>; });
    return *gExtraCreator;
}
```

    获取 RuntimeCreator ，然后根据类型创建对应的 Runtime。gExtraCreator 是一个 map 类型，其是通过函数 MNNInsertExtraRuntimeCreator 进行注册的。

```
// source/core/Backend.cpp
bool MNNInsertExtraRuntimeCreator(MNNForwardType type, const RuntimeCreator* creator, bool needCheck) {
    auto& gExtraCreator = GetExtraCreator();
    if (gExtraCreator.find(type) != gExtraCreator.end()) {
        MNN_ASSERT(false && "duplicate type");
        return false;
    }
    gExtraCreator.insert(std::make_pair(type, std::make_pair(creator, needCheck)));
    return true;
}
```

VULKAN 注册

```
// source/backend/vulkan/runtime/VulkanRuntime.cpp
static bool gResistor = []() {
    MNNInsertExtraRuntimeCreator(MNN_FORWARD_VULKAN, new VulkanRuntimeCreator, false);
    return false;
}();
```

CUDA 注册

```
// source/backend/cuda/Register.cpp
static const auto __cuda_global_initializer = []() {
    MNNInsertExtraRuntimeCreator(MNN_FORWARD_CUDA, new CUDARuntimeCreator, false);
    return true;
}();
```

OPENGL 注册

```
// source/backend/opengl/GLBackend.cpp
bool placeholder = []() {
    static std::once_flag createOnce;
    std::call_once(createOnce, []() {
        MNNInsertExtraRuntimeCreator(MNN_FORWARD_OPENGL, new GLRuntimeCreator, false);
    });
    return true;
}();
```

Metal 注册，在 registerBackend 中主动调用

```
// source/backend/metal/MetalBackend.mm
void registerMetalRuntimeCreator() {
    // according to
    // https://developer.apple.com/library/archive/documentation/DeviceInformation/Reference/iOSDeviceCompatibility/HardwareGPUInformation/HardwareGPUInformation.html
    // not all device with iOS 8+ supports metal.
    id<MTLDevice> device = MTLCreateSystemDefaultDevice();
    if (nil != device) {
        registerMetalOps();
#ifdef MNN_SUPPORT_RENDER
        registerMetalRenderOps();
#endif
        MNNInsertExtraRuntimeCreator(MNN_FORWARD_METAL, new MetalRuntimeCreator(device), false);
    } else {
        MNN_ERROR("Init Metal Error\n");
    }
}
```



### 1.3 RuntimeFactory::create

```
Runtime* RuntimeFactory::create(const Backend::Info& info) {
    auto creator = MNNGetExtraRuntimeCreator(info.type);
    if (nullptr == creator) {
        MNN_PRINT("Create Runtime Failed because no creator for %d\n", info.type);
        return nullptr;
    }
    // 调用具体 RuntimeCreator，如 VulkanRuntimeCreator，MetalRuntimeCreator，GLRuntimeCreator
    auto runtime = creator->onCreate(info);
    if (nullptr == runtime) {
        MNN_PRINT("Create Runtime failed, the creator return nullptr, type = %d\n", info.type);
    }
    return runtime;
}
```



#### 1.3.1 VulkanRuntimeCreator

    若 RuntimeFactory::create 中 info.type 为 MNN_FORWARD_VULKAN，则 creator->onCreate(info) 实际调用的是 VulkanRuntimeCreator::onCreate 函数。

```
// source/backend/vulkan/runtime/VulkanRuntime.cpp
class VulkanRuntimeCreator : public RuntimeCreator {
public:
    virtual Runtime* onCreate(const Backend::Info& info) const {
    	// 初始化 Vulkan 库，获取相应的 API 函数
        if (InitVulkan()) {
            if (_testVulkan()) {
            // 创建 Vulkan 运行时
                return new VulkanRuntime(info);
            }
        }
        return nullptr;
    }
    virtual bool onValid(Backend::Info& info) const {
        return true;
    }
};

static bool gResistor = []() {
    MNNInsertExtraRuntimeCreator(MNN_FORWARD_VULKAN, new VulkanRuntimeCreator, false);
    return false;
}();
}
```



#### 1.3.2 VulkanRuntimeCreator

    若 RuntimeFactory::create 中 info.type 为 MNN_FORWARD_CPU，则 creator->onCreate(info) 实际调用的是 CPURuntimeCreator ::onCreate 函数。

```
class CPURuntimeCreator : public RuntimeCreator {
public:
    virtual Runtime* onCreate(const Backend::Info& info) const override {
        return new CPURuntime(info);
    }
};


#ifdef MNN_SUPPORT_BF16
extern void registerBF16Backend();
#endif
#ifdef ENABLE_ARMV82
extern void registerArm82RuntimeCreator();
#endif
void registerCPURuntimeCreator() {
    CPUBackend::initCreatorMap();
    registerCPUOps();
#ifdef MNN_SUPPORT_BF16
    registerBF16Backend();
#endif
#ifdef MNN_USE_ARMV82
    registerArm82RuntimeCreator();
#endif
    // TODO: Merge _initCoreFunction MNNFunctionInit and cpuinfo_arm_init
    MNNCoreFunctionInit();
    MNNInsertExtraRuntimeCreator(MNN_FORWARD_CPU, new CPURuntimeCreator);
};
```

### 1.4 ScheduleConfig

    // project/android/demo/app/includes/MNN/Interpreter.hpp
    /** session schedule config */
    struct ScheduleConfig {
        /** which tensor should be kept */
        std::vector<std::string> saveTensors;
        /** forward type */
        MNNForwardType type = MNN_FORWARD_CPU;
        /** number of threads in parallel */
        int numThread = 4;
    
        /** subpath to run */
        struct Path {
            std::vector<std::string> inputs;
            std::vector<std::string> outputs;
    
            enum Mode {
                /**
                 * Op Mode
                 * - inputs means the source op, can NOT be empty.
                 * - outputs means the sink op, can be empty.
                 * The path will start from source op, then flow when encounter the sink op.
                 * The sink op will not be compute in this path.
                 */
                Op = 0,
    
                /**
                 * Tensor Mode (NOT supported yet)
                 * - inputs means the inputs tensors, can NOT be empty.
                 * - outputs means the outputs tensors, can NOT be empty.
                 * It will find the pipeline that compute outputs from inputs.
                 */
                Tensor = 1
            };
    
            /** running mode */
            Mode mode = Op;
        };
        Path path;
    
        /** backup backend used to create execution when desinated backend do NOT support any op */
        MNNForwardType backupType = MNN_FORWARD_CPU;
    
        /** extra backend config */
        BackendConfig* backendConfig = nullptr;
    };


### 1.5 BackendConfig    

    // project/android/demo/app/includes/MNN/MNNForwardType.h
    struct BackendConfig {
        enum MemoryMode {
            Memory_Normal = 0,
            Memory_High,
            Memory_Low
        };
        
        MemoryMode memory = Memory_Normal;
        
        enum PowerMode {
            Power_Normal = 0,
            Power_High,
            Power_Low
        };
        
        PowerMode power = Power_Normal;
        
        enum PrecisionMode {
            Precision_Normal = 0,
            Precision_High,
            Precision_Low
        };
        
        PrecisionMode precision = Precision_Normal;
        
        /** user defined context */
        void* sharedContext = nullptr;
    };

