# Assimp与模型渲染的故事：模型加载，骨骼蒙皮动画

想要开发一个好用的渲染引擎，加载/编辑模型和使用[骨骼动画](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=骨骼动画&zhida_source=entity)的功能是必不可少的，由于市面上的模型格式过多并且标准也互不统一，所以想要自己写出读取模型的算法是十分困难的，因此可以借助好用的模型加载库来完成这个工作，Assimp就是一个开源且支持格式较多的模型加载库，官方声称Assimp可以加载的模型格式有40种之多，但是实际上在加载一些模型格式时会时常出现错误，经本人测试部分[fbx](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=fbx&zhida_source=entity)，部分3ds和所有obj模型能够成功加载的，由于fbx拥有骨骼动画的功能，所以我基本以使用fbx为主（但想要加载全部fbx模型还是只能用FBX SDK完成）。

## Assimp架构

Assimp库使用的架构是典型的[树状结构](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=树状结构&zhida_source=entity)，而这个架构在模型加载中也是比较好用的，因为它足够清晰，也便于理清模型当中部件的父子关系，下图简单地呈现了Assimp的架构：

![img](./assets/v2-c42ae8837964840e8d34962a35c61c50_1440w.jpg)

Assimp架构（图片来自LearnOpenGL）

| Struct      | Content                                                      |
| ----------- | ------------------------------------------------------------ |
| aiScene     | mRootNode，mMeshes，mMaterials，mAnimations，mTextures，mLights，mCameras |
| aiNode      | mTransformation，mParent，mChildren，mMeshes(索引)           |
| aiMesh      | mVertices，mNormals，mTangents，mBitangents，mColors，mTextureCoords，mFaces，mBones，mAnimMeshes |
| aiFace      | mIndices                                                     |
| aiMaterial  | GetTextureCount(...)，GetTexture(...)                        |
| aiAnimation | mDuration，mTicksPerSecond，mChannels，mMeshChannels         |
| aiNodeAnim  | mNodeName，mPositionKeys，mRotationKeys，mScalingKeys，mPostState |

## Model类

Model类装载了加载模型的所有方法，后面我们的所有代码都会添加至这个类中：

```cpp
struct MaterialInfo {
	UINT diffuseMaps;
};

class Mesh {
public:
	struct RenderInfo {
		std::vector<Vertex> vertices;
		std::vector<UINT> indices;
	};

	std::vector<Vertex> vertices;
	std::vector<UINT> indices;
	UINT materialIndex;
	
	Mesh(std::vector<Vertex> vertices, std::vector<UINT> indices, UINT materialIndex) {
		this->vertices = vertices;
		this->indices = indices;
		this->materialIndex = materialIndex;
	}
};

class Model {
public:
	Model(std::string path);
	std::vector<Mesh> meshes;
	std::vector<MaterialInfo> materials;
	std::vector<Mesh::RenderInfo> renderInfo;
	std::vector<std::string> texturePath;

private:
	std::string directory;

	void ProcessNode(const aiScene* scene, aiNode* node);
	Mesh ProcessMesh(const aiScene* scene, aiMesh* mesh);
	UINT SetupMaterial(std::vector<UINT> diffuseMaps);
	void SetupRenderInfo();
	std::vector<UINT> LoadMaterialTextures(aiMaterial* mat, aiTextureType type);
};
```

### 获取Scene

在所有的工作开始之前，必然是使用Assimp将模型从本机读取出来，代码如下：

```cpp
Model::Model(std::string path) {
	Assimp::Importer importer;
	const aiScene* scene = importer.ReadFile(path, aiProcess_Triangulate | aiProcess_GenNormals | aiProcess_FlipUVs | aiProcess_CalcTangentSpace);
	directory = path.substr(0, path.find_last_of('\\')) + '\\';
   	ProcessNode(scene, scene->mRootNode);
	SetupRenderInfo();
}
```

ReadFile方法常用的几个参数意义如下：

| aiProcess                       | 意义                                                         |
| ------------------------------- | ------------------------------------------------------------ |
| aiProcess_Triangulate           | 以三角形图元读取模型网格                                     |
| aiProcess_GenNormals            | 自动生成法线                                                 |
| aiProcess_FlipUVs               | 翻转UV（是否使用视情况而定）                                 |
| aiProcess_CalcTangentSpace      | 自动计算[切线空间](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=切线空间&zhida_source=entity) |
| aiProcess_FlipWindingOrder      | 反转绕序                                                     |
| aiProcess_JoinIdenticalVertices | 合并相同顶点                                                 |

### 遍历节点

从Scene中获取根节点后，就可以从根节点开始，不断递归遍历其下的子节点，最终做到处理完所有节点拥有的数据，代码如下：

```cpp
void Model::ProcessNode(const aiScene* scene, aiNode* node) {
	for (size_t i = 0; i < node->mNumMeshes; i++)
	{
		aiMesh* mesh = scene->mMeshes[node->mMeshes[i]];
		meshes.push_back(ProcessMesh(scene, mesh));
	}

	for (size_t i = 0; i < node->mNumChildren; i++)
	{
		ProcessNode(scene, node->mChildren[i]);
	}
}
```

如果在该节点下找到挂载的Mesh，就直接处理该Mesh数据并将其添加至meshes容器当中，需要注意的是aiNode仅仅保存Mesh对应的索引，所以需要真正获得aiMesh还需要到Scene中去查找。

### 处理网格

从Scene中获取到aiMesh后，就可以加载出aiMesh中的数据，包括模型的顶点，索引，法向量，切向量，[纹理坐标](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=纹理坐标&zhida_source=entity)，材质索引相关信息，代码如下：

```cpp
Mesh Model::ProcessMesh(const aiScene* scene, aiMesh* mesh) {
	std::vector<Vertex> vertices;
	std::vector<UINT> indices;

	for (size_t i = 0; i < mesh->mNumVertices; i++) {
		Vertex vertex;
		vertex.position.x = mesh->mVertices[i].x;
		vertex.position.y = mesh->mVertices[i].y;
		vertex.position.z = mesh->mVertices[i].z;

		vertex.normal.x = mesh->mNormals[i].x;
		vertex.normal.y = mesh->mNormals[i].y;
		vertex.normal.z = mesh->mNormals[i].z;

		vertex.tangent.x = mesh->mTangents[i].x;
		vertex.tangent.y = mesh->mTangents[i].y;
		vertex.tangent.z = mesh->mTangents[i].z;

		if (mesh->mTextureCoords[0]) {
			vertex.texCoord.x = mesh->mTextureCoords[0][i].x;
			vertex.texCoord.y = mesh->mTextureCoords[0][i].y;
		}
		else {
			vertex.texCoord = { 0.0f, 0.0f };
		}

		vertices.push_back(vertex);
	}

	for (size_t i = 0; i < mesh->mNumFaces; i++) {
		aiFace face = mesh->mFaces[i];
		for (size_t j = 0; j < face.mNumIndices; j++)
			indices.push_back(face.mIndices[j]);
	}

	std::vector<UINT> diffuseMaps;
	if (mesh->mMaterialIndex >= 0) {
		aiMaterial* material = scene->mMaterials[mesh->mMaterialIndex];
		diffuseMaps = LoadMaterialTextures(material, aiTextureType_DIFFUSE);
	}
	UINT materialIndex = SetupMaterial(diffuseMaps);

	return Mesh(vertices, indices, materialIndex);
}
```

需要注意的是，索引并非直接存储在aiMesh中，而是存储在aiMesh下的aiFace中，由于在导入模型时使用了aiProcess_Triangulate，这里的Face也就基本是三角形图元，拥有3个Indices，将它们读取出来添加进总的索引即可。

### 读取材质

每一个Mesh可以有唯一个mMaterialIndex材质索引，同样的，可以使用这个材质索引在Scene中查询到其对应的aiMaterial，里面存储了材质信息，代码如下：

```cpp
std::vector<UINT> Model::LoadMaterialTextures(aiMaterial* mat, aiTextureType type) {
	std::vector<UINT> textures;
	for (UINT i = 0; i < mat->GetTextureCount(type); i++)
	{
		aiString str;
		mat->GetTexture(type, 0, &str);
		bool skip = false;
		std::string texturePath = directory + str.C_Str();
		for (UINT j = 0; j < this->texturePath.size(); j++)
		{
			if (this->texturePath[j] == texturePath) {
				textures.push_back(j);
				skip = true;
				break;
			}
		}
		if (!skip) {
			textures.push_back(this->texturePath.size());
			this->texturePath.push_back(texturePath);
		}
	}
	return textures;
}
```

想要从aiMaterial中获取[纹理贴图](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=纹理贴图&zhida_source=entity)，首先必须明确自己想要的贴图种类，Assimp中用aiTextureType规定了一些常见的贴图种类，有漫反射贴图，镜面反射贴图，[法线贴图](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=法线贴图&zhida_source=entity)，光照贴图，高度图等等，如果只使用漫反射贴图的话指定aiTextureType_DIFFUSE即可。

用aiMaterial下的GetTextureCount方法获取纹理贴图数量后，使用GetTexture方法获取对应纹理贴图的**[相对路径](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=相对路径&zhida_source=entity)**，这里我为了避免相同贴图的重复加载，设定一个将[贴图路径](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=贴图路径&zhida_source=entity)与已经添加过的贴图路径进行比较的算法，做到所有贴图只会被添加一次。

返回的textures则代表了Mesh对应贴图在所有贴图中的索引位置，确保之后绘制Mesh的时候使用正确的纹理贴图。

SetupMaterial使用了类似的方法来确保不会有相同的Material被重复创建，代码如下：

```cpp
UINT Model::SetupMaterial(std::vector<UINT> diffuseMaps) {
	MaterialInfo material;
	if (diffuseMaps.size() > 0)
		material.diffuseMaps = diffuseMaps[0];
	else
		material.diffuseMaps = 0;

	UINT materialIndex;
	bool skip = false;
	for (UINT i = 0; i < materials.size(); i++){
		if (CompareMaterial(materials[i], material)) {
			materialIndex = i;
			skip = true;
			break;
		}
	}
	if (!skip) {
		materialIndex = materials.size();
		materials.push_back(material);
	}
	return materialIndex;
}

bool CompareMaterial(MaterialInfo dest, MaterialInfo source) {
	bool isSame = false;
	if (dest.diffuseMaps == source.diffuseMaps)
		isSame = true;
	else
		isSame = false;
	return isSame;
}
```

使用Material而非Texture在仅仅有一个漫反射贴图的时候似乎看不出意义，因为两者几乎等同，但当一个Mesh需要同时使用多个贴图时（例如同时使用漫反射贴图和法线贴图），使用Material来存储Texture就显得十分有必要，同时这也方便我们之后对于材质内容的扩展。

### 提取渲染数据

在之前，我们已经获取了所有Mesh的顶点和索引，接下来要做的就是将它们全部提取出来，便于之后使用DrawCall绘制：

```cpp
void Model::SetupRenderInfo() {
	renderInfo.resize(materials.size());

	for (UINT i = 0; i < meshes.size(); i++)
	{
		UINT index = meshes[i].materialIndex;
		UINT indexOffset = renderInfo[index].vertices.size();

		for (UINT j = 0; j < meshes[i].indices.size(); j++)
			renderInfo[index].indices.push_back(meshes[i].indices[j] + indexOffset);

		renderInfo[index].vertices.insert(renderInfo[index].vertices.end(), meshes[i].vertices.begin(), meshes[i].vertices.end());
	}
}
```

我们默认一个mesh使用一个材质，同时用材质作为区分模型部件的标准，也就是说，有多少个材质我们就使用多少个DrawCall，这确保了DrawCall数量的最小化，使得绘制效率尽可能高。

## SkinnedModel类

藉由上面的[Model类](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=4&q=Model类&zhida_source=entity)作为一个跳板，接下来我们就可以考虑添加骨骼与[蒙皮动画](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=蒙皮动画&zhida_source=entity)到我们的模型当中了，我将这个新的类命名为SkinnedModel类，当然这个新的类会比原来的庞大许多：

```cpp
class SkinnedMesh {
public:
	struct RenderInfo {
		std::vector<SkinnedVertex> vertices;
		std::vector<UINT> indices;
	};

	std::vector<SkinnedVertex> vertices;
	std::vector<UINT> indices;
	UINT materialIndex;

	SkinnedMesh(std::vector<SkinnedVertex> vertices, std::vector<UINT> indices, UINT materialIndex) {
		this->vertices = vertices;
		this->indices = indices;
		this->materialIndex = materialIndex;
	}
};

class SkinnedModel {
public:
	struct BoneData {
		UINT boneIndex[NUM_BONES_PER_VERTEX];
		float weights[NUM_BONES_PER_VERTEX];
		void Add(UINT boneID, float weight) {
			for (size_t i = 0; i < NUM_BONES_PER_VERTEX; i++) {
				if (weights[i] == 0.0f) {
					boneIndex[i] = boneID;
					weights[i] = weight;
					return;
				}
			}
			//insert error program
			MessageBox(NULL, L"bone index out of size", L"Error", NULL);
		}
	};
	struct BoneInfo {
		bool isSkinned = false;
		glm::mat4x4 boneOffset;
		glm::mat4x4 defaultOffset;
		int parentIndex;
	};

	SkinnedModel(std::string path);
	std::vector<SkinnedMesh> meshes;
	std::vector<MaterialInfo> materials;
	std::vector<SkinnedMesh::RenderInfo> renderInfo;
	std::vector<std::string> texturePath;

	void GetBoneMapping(std::unordered_map<std::string, UINT>& boneMapping) {
		boneMapping = this->boneMapping;
	}
	void GetBoneOffsets(std::vector<glm::mat4x4>& boneOffsets) {
		for (size_t i = 0; i < boneInfo.size(); i++)
			boneOffsets.push_back(boneInfo[i].boneOffset);
	}
	void GetNodeOffsets(std::vector<glm::mat4x4>& nodeOffsets) {
		for (size_t i = 0; i < boneInfo.size(); i++)
			nodeOffsets.push_back(boneInfo[i].defaultOffset);
	}
	void GetBoneHierarchy(std::vector<int>& boneHierarchy) {
		for (size_t i = 0; i < boneInfo.size(); i++)
			boneHierarchy.push_back(boneInfo[i].parentIndex);
	}
	void GetAnimations(std::unordered_map<std::string, AnimationClip>& animations) {
		animations.insert(this->animations.begin(), this->animations.end());
	}

private:
	std::string directory;

	void ProcessNode(const aiScene* scene, aiNode* node);
	SkinnedMesh ProcessMesh(const aiScene* scene, aiMesh* mesh);
	UINT SetupMaterial(std::vector<UINT> diffuseMaps);
	void SetupRenderInfo();
	std::vector<UINT> LoadMaterialTextures(aiMaterial* mat, aiTextureType type);

	//Bone/Animation Information
	std::vector<BoneInfo> boneInfo;
	std::unordered_map<std::string, UINT> boneMapping;
	std::unordered_map<std::string, AnimationClip> animations;

	void LoadBones(const aiMesh* mesh, std::vector<BoneData>& bones);
	void ReadNodeHierarchy(const aiNode* node, int parentIndex);
	void LoadAnimations(const aiScene* scene);
};

SkinnedModel::SkinnedModel(std::string path) {
	Assimp::Importer importer;
	const aiScene* scene = importer.ReadFile(path, aiProcess_Triangulate | aiProcess_GenNormals | aiProcess_FlipUVs | aiProcess_CalcTangentSpace);
	directory = path.substr(0, path.find_last_of('\\')) + '\\';
	ReadNodeHierarchy(scene->mRootNode, -1);
	ProcessNode(scene, scene->mRootNode);
	SetupRenderInfo();
	LoadAnimations(scene);
}
```

SkinnedModel类下有两个用于辅助骨骼加载的结构体，一个是BoneData，与顶点相对应，指定了每个顶点所对应的绑骨（骨骼索引，蒙皮权重），一个是BoneInfo，与骨骼相对应，指定了每个骨骼的父子关系和变换。boneMapping是一个用骨骼名字来查找骨骼索引的无序映射表，可以方便查找boneInfo。

### 梳理树状层级

在不使用骨骼蒙皮动画的时候，模型自身部件的父子关系显得可有可无，因为我们实际并不需要用到，但一旦使用骨骼蒙皮动画，由于父子关系会涉及到层级之间的（矩阵）变换关系，因此梳理一遍树状层级就显得不可缺少：

```cpp
void SkinnedModel::ReadNodeHierarchy(const aiNode* node, int parentIndex) {
	BoneInfo boneInfo;

	UINT boneIndex = this->boneInfo.size();
	boneMapping[node->mName.C_Str()] = boneIndex;

	boneInfo.boneOffset = glm::mat4(1.0f);
	boneInfo.parentIndex = parentIndex;
	boneInfo.defaultOffset = {
		node->mTransformation.a1, node->mTransformation.b1, node->mTransformation.c1, node->mTransformation.d1,
		node->mTransformation.a2, node->mTransformation.b2, node->mTransformation.c2, node->mTransformation.d2,
		node->mTransformation.a3, node->mTransformation.b3, node->mTransformation.c3, node->mTransformation.d3,
		node->mTransformation.a4, node->mTransformation.b4, node->mTransformation.c4, node->mTransformation.d4
	};
	this->boneInfo.push_back(boneInfo);

	for (size_t i = 0; i < node->mNumChildren; i++)
		ReadNodeHierarchy(node->mChildren[i], boneIndex);
}
```

这里同样使用递归来遍历整个树状层次，并将aiNode下的mTransformation存储为了骨骼的默认变换以便之后使用。

### 加载骨骼蒙皮数据

在ProcessMesh中，我加入了以下方法以便获取aiMesh下的mBones数据：

```cpp
std::vector<BoneData> vertexBoneData(mesh->mNumVertices);
LoadBones(mesh, vertexBoneData);
```

LoadBones方法会为每个顶点生成所需用的骨骼数据，从而得到顶点与骨骼的对应关系，代码如下：

```cpp
void SkinnedModel::LoadBones(const aiMesh* mesh, std::vector<SkinnedModel::BoneData>& boneData) {
	for (size_t i = 0; i < mesh->mNumBones; i++) {
		UINT boneIndex = 0;
		std::string boneName(mesh->mBones[i]->mName.C_Str());

		if (boneMapping.find(boneName) == boneMapping.end()) {
			//insert error program
		}
		else
			boneIndex = boneMapping[boneName];

		boneMapping[boneName] = boneIndex;

		if (!boneInfo[boneIndex].isSkinned) {
			aiMatrix4x4 offsetMatrix = mesh->mBones[i]->mOffsetMatrix;
			boneInfo[boneIndex].boneOffset = {
				offsetMatrix.a1, offsetMatrix.b1, offsetMatrix.c1, offsetMatrix.d1,
				offsetMatrix.a2, offsetMatrix.b2, offsetMatrix.c2, offsetMatrix.d2,
				offsetMatrix.a3, offsetMatrix.b3, offsetMatrix.c3, offsetMatrix.d3,
				offsetMatrix.a4, offsetMatrix.b4, offsetMatrix.c4, offsetMatrix.d4
			};
			boneInfo[boneIndex].isSkinned = true;
		}

		for (size_t j = 0; j < mesh->mBones[i]->mNumWeights; j++) {
			UINT vertexID = mesh->mBones[i]->mWeights[j].mVertexId;
			float weight = mesh->mBones[i]->mWeights[j].mWeight;
			boneData[vertexID].Add(boneIndex, weight);
		}
	}
}
```

boneMapping已经在之前遍历树状层级的时候完成了构建，在这里我们只需要用boneName完成查找即可。aiBone下存有骨骼自身的偏移矩阵，将其存储在BoneInfo中；同样aiBone下还存有顶点蒙皮权重，暂时将其存储在BoneData中。

在读取顶点坐标/法向量/切向量的同时将蒙皮权重放进Vertex里，骨骼蒙皮权重规定了骨骼对一个顶点的造成的影响占比：

```cpp
vertex.boneWeights.x = vertexBoneData[i].weights[0];
vertex.boneWeights.y = vertexBoneData[i].weights[1];
vertex.boneWeights.z = vertexBoneData[i].weights[2];
```

### [顶点着色器](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=顶点着色器&zhida_source=entity)

修改顶点着色器的输入，在顶点数据中添加上骨骼蒙皮数据：

```cpp
struct VertexIn {
	float3 position;
	float2 texCoord;
	float3 normal;
	float3 tangent;

	float3 boneWeights;
	uint4 boneIndices;
};
```

由于需要考虑到骨骼蒙皮对顶点产生的变换，所以我们需要在顶点着色器中添加一些计算步骤，HLSL代码如下：

```cpp
//SkinnedVS.hlsl
float3 posL = float3(0.0f, 0.0f, 0.0f);    //Local Space
float3 normalL = float3(0.0f, 0.0f, 0.0f);
float3 tangentL = float3(0.0f, 0.0f, 0.0f);

/*Bone Animation*/
float weights[4] = { input.boneWeights.x, input.boneWeights.y, input.boneWeights.z, 1.0f - input.boneWeights.x - input.boneWeights.y - input.boneWeights.z };

for (int i = 0; i < 4; i++) {
	posL += weights[i] * mul(boneTransforms[input.boneIndices[i]], float4(input.position, 1.0f)).xyz;
	normalL += weights[i] * mul((float3x3)boneTransforms_inv_trans[input.boneIndices[i]], input.normal);
	normalL += weights[i] * mul((float3x3)boneTransforms[input.boneIndices[i]], input.normal);
}
```

这里的计算也并不复杂，分别将各个骨骼的蒙皮权重乘以骨骼变换矩阵再乘以初始坐标（向量），最后再逐个相加，就能得到需要的结果，这可以放在MVP矩阵变换之前执行。

## 骨骼动画

一些基本数据结构和功能，会在加载骨骼动画时使用到，贴在下面方便查看：

```cpp
struct VectorKey {
	float timePos;
	glm::vec3 value;
};

struct QuatKey {
	float timePos;
	glm::qua<float> value;
};

class BoneAnimation {
public:
	float GetStartTime()const;
	float GetEndTime()const;
	void Interpolate(float t, glm::mat4x4& M);
	
	std::vector<VectorKey> translation;
	std::vector<VectorKey> scale;
	std::vector<QuatKey> rotationQuat;

	glm::mat4x4 defaultTransform;

private:
	glm::vec3 LerpKeys(float t, const std::vector<VectorKey>& keys);
	glm::qua<float> LerpKeys(float t, const std::vector<QuatKey>& keys);
};

class AnimationClip {
public:
	float GetClipStartTime()const;
	float GetClipEndTime()const;
	void Interpolate(float t, std::vector<glm::mat4x4>& boneTransform);
	
	std::vector<BoneAnimation> boneAnimations;
};

class SkinnedData {
public:
	UINT GetBoneCount()const { return boneHierarchy.size(); }
	float GetClipStartTime(const std::string& clipName)const;
	float GetClipEndTime(const std::string& clipName)const;
	void Set(std::vector<int>& boneHierarchy,
		std::vector<glm::mat4x4>& boneOffsets,
		std::unordered_map<std::string, AnimationClip>& animations);
	void GetFinalTransform(const std::string& clipName, float timePos, std::vector<glm::mat4x4>& finalTransforms);

private:
	std::vector<int> boneHierarchy;
	std::vector<glm::mat4x4> boneOffsets;
	std::unordered_map<std::string, AnimationClip> animations;
};

float BoneAnimation::GetStartTime()const {
	float t0 = 0.0f;
	float t1 = 0.0f;
	float t2 = 0.0f;
	if (translation.size() != 0) t0 = translation.front().timePos;
	if (scale.size() != 0) t1 = scale.front().timePos;
	if (rotationQuat.size() != 0) t2 = rotationQuat.front().timePos;

	float timePos = t0 < t1 ? t0 : t1;
	timePos = timePos < t2 ? timePos : t2;
	return timePos;
}

float BoneAnimation::GetEndTime()const {
	float t0 = 0.0f;
	float t1 = 0.0f;
	float t2 = 0.0f;
	if (translation.size() != 0) t0 = translation.back().timePos;
	if (scale.size() != 0) t1 = scale.back().timePos;
	if (rotationQuat.size() != 0) t2 = rotationQuat.back().timePos;

	float timePos = t0 > t1 ? t0 : t1;
	timePos = timePos > t2 ? timePos : t2;
	return timePos;
}

float AnimationClip::GetClipStartTime()const {
	float t = FLT_MAX;
	for (UINT i = 0; i < boneAnimations.size(); i++)
		t = t < boneAnimations[i].GetStartTime() ? t : boneAnimations[i].GetStartTime();
	return t;
}

float AnimationClip::GetClipEndTime()const {
	float t = 0.0f;
	for (UINT i = 0; i < boneAnimations.size(); i++)
		t = t > boneAnimations[i].GetEndTime() ? t : boneAnimations[i].GetEndTime();
	return t;
}

void AnimationClip::Interpolate(float t, std::vector<glm::mat4x4>& boneTransform) {
	for (UINT i = 0; i < boneAnimations.size(); i++)
		boneAnimations[i].Interpolate(t, boneTransform[i]);
}

float SkinnedData::GetClipStartTime(const std::string& clipName)const {
	auto clip = animations.find(clipName);
	return clip->second.GetClipStartTime();
}

float SkinnedData::GetClipEndTime(const std::string& clipName)const {
	auto clip = animations.find(clipName);
	return clip->second.GetClipEndTime();
}

void SkinnedData::Set(std::vector<int>& boneHierarchy,
	std::vector<glm::mat4x4>& boneOffsets,
	std::unordered_map<std::string, AnimationClip>& animations) {
	this->boneHierarchy = boneHierarchy;
	this->boneOffsets = boneOffsets;
	this->animations = animations;
}
```

### 加载模型动画

Assimp的模型动画存储在aiScene下的aiAnimation中，在我们加载Scene的同时就可以将模型动画同时加载进来，代码如下：

```cpp
void SkinnedModel::LoadAnimations(const aiScene* scene) {
	for (size_t i = 0; i < scene->mNumAnimations; i++) {
		AnimationClip animation;
		std::vector<BoneAnimation> boneAnims(boneInfo.size());
		aiAnimation* anim = scene->mAnimations[i];

		float ticksPerSecond = anim->mTicksPerSecond != 0 ? anim->mTicksPerSecond : 25.0f;
		float timeInTicks = 1.0f / ticksPerSecond;

		for (size_t j = 0; j < anim->mNumChannels; j++) {
			BoneAnimation boneAnim;
			aiNodeAnim* nodeAnim = anim->mChannels[j];

			for (size_t k = 0; k < nodeAnim->mNumPositionKeys; k++) {
				VectorKey keyframe;
				keyframe.timePos = nodeAnim->mPositionKeys[k].mTime * timeInTicks;
				keyframe.value.x = nodeAnim->mPositionKeys[k].mValue.x;
				keyframe.value.y = nodeAnim->mPositionKeys[k].mValue.y;
				keyframe.value.z = nodeAnim->mPositionKeys[k].mValue.z;
				boneAnim.translation.push_back(keyframe);
			}
			for (size_t k = 0; k < nodeAnim->mNumScalingKeys; k++) {
				VectorKey keyframe;
				keyframe.timePos = nodeAnim->mScalingKeys[k].mTime * timeInTicks;
				keyframe.value.x = nodeAnim->mScalingKeys[k].mValue.x;
				keyframe.value.y = nodeAnim->mScalingKeys[k].mValue.y;
				keyframe.value.z = nodeAnim->mScalingKeys[k].mValue.z;
				boneAnim.scale.push_back(keyframe);
			}
			for (size_t k = 0; k < nodeAnim->mNumRotationKeys; k++) {
				QuatKey keyframe;
				keyframe.timePos = nodeAnim->mRotationKeys[k].mTime * timeInTicks;
				keyframe.value.x = nodeAnim->mRotationKeys[k].mValue.x;
				keyframe.value.y = nodeAnim->mRotationKeys[k].mValue.y;
				keyframe.value.z = nodeAnim->mRotationKeys[k].mValue.z;
				keyframe.value.w = nodeAnim->mRotationKeys[k].mValue.w;
				boneAnim.rotationQuat.push_back(keyframe);
			}
			boneAnims[boneMapping[nodeAnim->mNodeName.C_Str()]] = boneAnim;
		}
		animation.boneAnimations = boneAnims;

		std::string animName(anim->mName.C_Str());
		animName = animName.substr(animName.find_last_of('|') + 1, animName.length() - 1);

		animations[animName] = animation;
	}
}
```

从aiAnimation下的mChannels就可以得到每个节点对应的骨骼动画，因此我们同样按照boneMapping的索引存储在boneAnims下，这样就不会变得混乱。

骨骼动画也是逐帧动画的一种呈现形式，所以依旧依赖关键帧来记录信息，Assimp中每个关键帧都由mPositionKeys（位置），mScalingKeys（缩放），mRotationKeys（旋转）三个形式记录变换，其中位置和缩放用向量记录，旋转用[四元数](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=四元数&zhida_source=entity)记录。

除了关键帧的变换我们需要知道以外，关键帧的时间点我们也需要知道，Assimp中用mTicksPerSecond来规定时间标准，求它的倒数得到timeInTicks（单位时间的长度），再用关键帧的mTime乘以timeInTicks得到标准化的关键帧时间点。

得知了关键帧的变换和时间点之后，就可以使用这些关键帧构建完整的骨骼动画了。

### 关键帧插值

通过关键帧插值推得[过渡帧](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=过渡帧&zhida_source=entity)的变换数据，关键帧插值分为向量[线性插值](https://zhida.zhihu.com/search?content_id=212496704&content_type=Article&match_order=1&q=线性插值&zhida_source=entity)和四元数球面线性插值，用当前时刻在两个关键帧之间的百分比位置进行线性插值，直接使用GLM库简化掉了计算的细节，这里不再详细讲解Lerp和Slerp的方法，代码如下：

```cpp
glm::vec3 BoneAnimation::LerpKeys(float t, const std::vector<VectorKey>& keys) {
	glm::vec3 res = glm::vec3(0.0f);
	if (t <= keys.front().timePos) res = keys.front().value;
	else if (t >= keys.back().timePos) res = keys.back().value;
	else {
		for (size_t i = 0; i < keys.size() - 1; i++){
			if (t >= keys[i].timePos && t <= keys[i + 1].timePos){
				float lerpPercent = (t - keys[i].timePos) / (keys[i + 1].timePos - keys[i].timePos);
				res = keys[i].value * lerpPercent + keys[i + 1].value * (1 - lerpPercent);
				break;
			}
		}
	}
	return res;
}

glm::qua<float> BoneAnimation::LerpKeys(float t, const std::vector<QuatKey>& keys) {
	glm::qua<float> res = glm::qua<float>();
	if (t <= keys.front().timePos) res = keys.front().value;
	else if (t >= keys.back().timePos) res = keys.back().value;
	else {
		for (size_t i = 0; i < keys.size() - 1; i++) {
			if (t >= keys[i].timePos && t <= keys[i + 1].timePos) {
				float lerpPercent = (t - keys[i].timePos) / (keys[i + 1].timePos - keys[i].timePos);
				res = glm::slerp(keys[i].value, keys[i + 1].value, lerpPercent);
				break;
			}
		}
	}
	return res;
}
```

将插值与矩阵变换综合在一起，得到当前帧的变换矩阵，代码如下：

```cpp
void BoneAnimation::Interpolate(float t, glm::mat4x4& M) {
	if (translation.size() == 0 && scale.size() == 0 && rotationQuat.size() == 0) {
		M = defaultTransform;
		return;
	}

	glm::vec3 T = glm::vec3(0.0f);
	glm::vec3 S = glm::vec3(0.0f);
	glm::qua<float> R = glm::qua<float>();

	if (translation.size() != 0) T = LerpKeys(t, translation);
	if (scale.size() != 0) S = LerpKeys(t, scale);
	if (rotationQuat.size() != 0) R = LerpKeys(t, rotationQuat);

	M = glm::translate(glm::mat4(1.0f), T) * glm::mat4_cast(R) * glm::scale(glm::mat4(1.0f), S);
}
```

### 最终变换矩阵

刚刚我们所作的都是对于一根骨头而言的，但是一个硕大的模型不可能只有一根骨头，而是拥有完整的骨架，并且骨骼之间相互具有父子关系，父物件的变换会对子物件造成影响，因此我们需要根据树状层级计算出一根骨头真正的变换矩阵，代码如下：

```cpp
void SkinnedData::GetFinalTransform(const std::string& clipName, float timePos, std::vector<glm::mat4x4>& finalTransforms) {
	UINT numBones = boneOffsets.size();
	std::vector<glm::mat4x4> toParentTransforms(numBones);

	auto clip = animations.find(clipName);
	clip->second.Interpolate(timePos, toParentTransforms);

	std::vector<glm::mat4x4> toRootTransforms(numBones);
	toRootTransforms[0] = toParentTransforms[0];

	for (UINT i = 1; i < numBones; i++){
		int parentIndex = boneHierarchy[i];
		toRootTransforms[i] = toRootTransforms[parentIndex] * toParentTransforms[i];
	}

	for (UINT i = 0; i < numBones; i++) {
		finalTransforms[i] = toRootTransforms[i] * boneOffsets[i];
	}
}
```

获得最终变换矩阵的原理非常简单，就是把自身的变换矩阵乘以父物体的变换矩阵，得到自身对于模型根部的变换矩阵，除此以外，还需要乘以boneOffset即骨骼自身的偏移。

在渲染循环的每一帧更新骨骼动画，并获取最终变换矩阵，由着色器读取完成计算：

```cpp
void UpdateSkinnedAnimation(float deltaTime) {
	timePos += deltaTime;

	//Loop
	if (timePos > skinnedInfo.GetClipEndTime(clipName))
		timePos = 0.0f;

	skinnedData.GetFinalTransform(clipName, timePos, finalTransforms);
}
```

