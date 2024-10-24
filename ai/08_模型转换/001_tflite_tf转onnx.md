# tflite/tf转onnx

https://github.com/onnx/tensorflow-onnx



## 安装

安装 anaconda

创建环境

tf2onnx 支持最新python版本为 3.10

```shell
conda create -n tf2onnx python=3.10
```

切换环境

```
conda activate tf2onnx
```

安装tensorflow

```
pip install tensorflow
```

安装onnx

```
pip install onnxruntime
```

安装tf2onnx

```
pip install -U tf2onnx
```



## 使用

### 转化tensorflow模型

```shell
python -m tf2onnx.convert --saved-model tensorflow-model-path --output model.onnx
```

可以设置操作集 [onnx-opset](https://github.com/onnx/onnx/blob/main/docs/Operators.md) [doc](https://onnx.ai/onnx/operators/index.html)

```shell
python -m tf2onnx.convert --saved-model tensorflow-model-path --opset 18 --output model.onnx
```



### 转化 checkpoint

```
python -m tf2onnx.convert --checkpoint  tensorflow-model-meta-file-path --output model.onnx --inputs input0:0,input1:0 --outputs output0:0
```



### 转化 graphdef

```shell
python -m tf2onnx.convert --graphdef  tensorflow-model-graphdef-file --output model.onnx --inputs input0:0,input1:0 --outputs output0:0
```



### 转化tflite

```shell
python -m tf2onnx.convert --opset 16 --tflite face_landmarks_detector.tflite --output face_landmarks_detector.onnx
```



## 其他部分



## CLI reference



```
python -m tf2onnx.convert
    --saved-model SOURCE_SAVED_MODEL_PATH |
    --checkpoint SOURCE_CHECKPOINT_METAFILE_PATH |
    --tflite TFLITE_MODEL_PATH |
    --tfjs TFJS_MODEL_PATH | 
    --input | --graphdef SOURCE_GRAPHDEF_PB
    --output TARGET_ONNX_MODEL
    [--inputs GRAPH_INPUTS]
    [--outputs GRAPH_OUTPUS]
    [--inputs-as-nchw inputs_provided_as_nchw]
    [--outputs-as-nchw outputs_provided_as_nchw]
    [--opset OPSET]
    [--dequantize]
    [--tag TAG]
    [--signature_def SIGNATURE_DEF]
    [--concrete_function CONCRETE_FUNCTION]
    [--target TARGET]
    [--extra_opset list-of-extra-opset]
    [--custom-ops list-of-custom-ops]
    [--load_op_libraries tensorflow_library_path]
    [--large_model]
    [--continue_on_error]
    [--verbose]
    [--output_frozen_graph]
```



### Parameters



#### --saved-model



TensorFlow model as saved_model. We expect the path to the saved_model directory.

#### --checkpoint



TensorFlow model as checkpoint. We expect the path to the .meta file.

#### --input or --graphdef



TensorFlow model as graphdef file.

#### --tfjs



Convert a tensorflow.js model by providing a path to the .tfjs file. Inputs/outputs do not need to be specified.

#### --tflite



Convert a tflite model by providing a path to the .tflite file. Inputs/outputs do not need to be specified.

#### --output



The target onnx file path.

#### --inputs, --outputs



TensorFlow model's input/output names, which can be found with [summarize graph tool](https://github.com/onnx/tensorflow-onnx#summarize_graph). Those names typically end with `:0`, for example `--inputs input0:0,input1:0`. Inputs and outputs are ***not*** needed for models in saved-model format. Some models specify placeholders with unknown ranks and dims which can not be mapped to onnx. In those cases one can add the shape after the input name inside `[]`, for example `--inputs X:0[1,28,28,3]`. Use -1 to indicate unknown dimensions.

#### --inputs-as-nchw



By default we preserve the image format of inputs (`nchw` or `nhwc`) as given in the TensorFlow model. If your hosts (for example windows) native format nchw and the model is written for nhwc, `--inputs-as-nchw` tensorflow-onnx will transpose the input. Doing so is convenient for the application and the converter in many cases can optimize the transpose away. For example `--inputs input0:0,input1:0 --inputs-as-nchw input0:0` assumes that images are passed into `input0:0` as nchw while the TensorFlow model given uses nhwc.

#### --outputs-as-nchw



Similar usage with `--inputs-as-nchw`. By default we preserve the format of outputs (`nchw` or `nhwc`) as shown in the TensorFlow model. If your hosts native format nchw and the model is written for nhwc, `--outputs-as-nchw` tensorflow-onnx will transpose the output and optimize the transpose away. For example `--outputs output0:0,output1:0 --outputs-as-nchw output0:0` will change the `output0:0` as nchw while the TensorFlow model given uses nhwc.

#### --ignore_default, --use_default



ONNX requires default values for graph inputs to be constant, while Tensorflow's PlaceholderWithDefault op accepts computed defaults. To convert such models, pass a comma-separated list of node names to the ignore_default and/or use_default flags. PlaceholderWithDefault nodes with matching names will be replaced with Placeholder or Identity ops, respectively.

#### --opset



By default we use the opset 15 to generate the graph. By specifying `--opset` the user can override the default to generate a graph with the desired opset. For example `--opset 17` would create a onnx graph that uses only ops available in opset 17. Because older opsets have in most cases fewer ops, some models might not convert on an older opset.

#### --dequantize



(This is experimental, only supported for tflite)

Produces a float32 model from a quantized tflite model. Detects ReLU and ReLU6 ops from quantization bounds.

#### --tag



Only valid with parameter `--saved_model`. Specifies the tag in the saved_model to be used. Typical value is 'serve'.

#### --signature_def



Only valid with parameter `--saved_model`. Specifies which signature to use within the specified --tag value. Typical value is 'serving_default'.

#### --concrete_function



(This is experimental, valid only for TF2.x models)

Only valid with parameter `--saved_model`. If a model contains a list of concrete functions, under the function name `__call__` (as can be viewed using the command `saved_model_cli show --all`), this parameter is a 0-based integer specifying which function in that list should be converted. This parameter takes priority over `--signature_def`, which will be ignored.

#### --target



Some models require special handling to run on some runtimes. In particular, the model may use unsupported data types. Workarounds are activated with `--target TARGET`. Currently supported values are listed on this [wiki](https://github.com/onnx/tensorflow-onnx/wiki/target). If your model will be run on Windows ML, you should specify the appropriate target value.

#### --extra_opset



If you want to convert a TF model using an existing custom op, this can specify the correspongding domain and version. The format is a comma-separated map of domain and version, for example: `ai.onnx.contrib:1`.

#### --custom-ops



If a model contains ops not recognized by onnx runtime, you can tag these ops with a custom op domain so that the runtime can still open the model. The format is a comma-separated map of tf op names to domains in the format OpName:domain. If only an op name is provided (no colon), the default domain of `ai.onnx.converters.tensorflow` will be used.

#### --load_op_libraries



Load the comma-separated list of tensorflow plugin/op libraries before conversion.

#### --large_model



(Can be used only for TF2.x models)

Only valid with parameter `--saved_model`. When set, creates a zip file containing the ONNX protobuf model and large tensor values stored externally. This allows for converting models whose size exceeds the 2 GB.

#### --continue_on_error



Continue to run conversion on error, ignore graph cycles so it can report all missing ops and errors.

#### --verbose



Verbose detailed output for diagnostic purposes.

#### --output_frozen_graph



Save the frozen and optimized tensorflow graph to a file for debug.

### Tool to get Graph Inputs & Outputs



To find the inputs and outputs for the TensorFlow graph the model developer will know or you can consult TensorFlow's [summarize_graph](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/tools/graph_transforms) tool, for example:

```
summarize_graph --in_graph=tests/models/fc-layers/frozen.pb
```