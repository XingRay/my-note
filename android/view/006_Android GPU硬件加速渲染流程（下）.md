![Android GPU硬件加速渲染流程（下）](D:\my-note\android\view\assets\v2-a153dcd9dee13ffd156be25d9741a516_1440w.png)

# Android GPU硬件加速渲染流程（下）

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l-1707456267235-224.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a-1707456267235-225.jpg)

同济大学 计算机系统结构硕士

关注

45 人赞同了该文章





目录

收起

3.3 硬件加速渲染的Display List构建过程分析

3.3.1 何为DisplayList

3.3.2 DisplayList构建表

3.3.3 DisplayList构建流程分析

3.3.4 TextureView构建流程分析

3.3.4.1 TextureView纹理创建

3.3.4.2 TextureView纹理更新

3.4硬件加速渲染的Display List渲染过程分析

3.4.1 渲染基本流程分析

3.4.2 将MainThread信息同步至RenderThread

3.4.2.1 mProperties、mDisplayListData同步

3.4.2.2 Bitmap同步

3.4.2.3 TextureView的同步过程

3.4.2.4 动画类型Layer的渲染信息同步

3.4.3 RenderThread渲染函数分析

3.4.3.1 基本流程分析

3.4.3.2 LayerRenderer渲染介绍

3.4.3.3 指令合并简介

4 Android 7.0（Nougat）对于hwui硬件绘制的更新

5 总结

6 参考

*本文希望通过两篇文章，详细介绍Android上层的**硬件加速渲染流程**，大约2万字，此为下篇。*

### 3.3 硬件加速渲染的Display List构建过程分析

在硬件加速渲染环境中，[Android](https://link.zhihu.com/?target=http%3A//lib.csdn.net/base/15)应用程序窗口的UI渲染是分两步进行的。第一步是构建Display List，发生在应用程序进程的Main Thread中；第二步是渲染Display List，发生在应用程序进程的Render Thread中。

### 3.3.1 何为DisplayList

在Android应用程序窗口中，每一个使用硬件加速的**View**都抽象为一个**Render Node**，而且如果一个View设置有Background，这个Background也被抽象为一个Render Node。这是由于在OpenGLRenderer库中，并没有View的概念，所有的一切可绘制的元素都抽象为一个Render Node。

![img](D:\my-note\android\view\assets\v2-f20c47e30be5f20f02cfa3c0b6da8a13_1440w.webp)

图11 Android应用程序窗口的Display List构建示意图

每一个Render Node都关联有一个Display List Renderer。这里又涉及到另外一个概念——Display List。注意，这个Display List不是Open GL里面的Display List，不过它们在概念上是差不多的。Display List是一个绘制命令缓冲区。也就是说，当View的成员函数onDraw被调用时，我们调用通过参数传递进来的Canvas的drawXXX成员函数绘制图形时，我们实际上只是将对应的绘制命令以及参数保存在一个Display List中，真正的渲染是调用Display List Replay。

```c
1. void DisplayListCanvas::drawLayer(DeferredLayerUpdater* layerHandle, float x, float y) {
2.  …
3.  addDrawOp(new (alloc()) DrawLayerOp(layerHandle->backingLayer(), x, y));
4. }
5. void DisplayListCanvas::drawBitmap(const SkBitmap* bitmap, const SkPaint* paint) {
6.  …
7.  addDrawOp(new (alloc()) DrawBitmapOp(bitmap, paint));
8. } 
```

注意，只有使用硬件加速渲染的View，才会关联有Render Node，也就才会使用到Display List。对于使用了软件方式来渲染的View，具体的做法是创建一个新的Canvas，这个Canvas的底层是一个Bitmap，也就是说，绘制都发生在这个Bitmap上。绘制完成之后，这个Bitmap再被记录在其Parent View的Display List中。而当Parent View的Display List的命令被执行时，记录在里面的Bitmap再通过Open GL命令来绘制。

另一方面，对于**TextureView**，它也不是通过Display List来绘制。由于它的底层实现直接就是一个Open GL纹理，因此就可以跳过Display List这一中间层，从而提高效率。这个Open GL纹理的绘制通过一个Layer来封装。Layer和Display List Renderer可以看作是同一级别的概念，它们都是通过Open GL命令来绘制UI元素的。只不过前者操作的是Open GL纹理，而后者操作的是Display List。

Android应用程序窗口的View是通过树形结构来组织的。这些View不管是通过硬件加速渲染还是软件渲染，或者是一个特殊的TextureView，在它们的成员函数onDraw被调用期间，它们都是将自己的UI绘制在Parent View的Display List中。其中，最顶层的Parent View是一个Root View，它关联的Root Node称为Root Render Node。也就是说，最终Root Render Node的Display List将会包含有一个窗口的所有绘制命令。在绘制窗口的下一帧时，Root Render Node的Display List都会通过一个Open GL Renderer真正地通过Open GL命令绘制在一个Graphic Buffer中。最后这个Graphic Buffer被交给SurfaceFlinger服务进行合成和显示。

引进Display List的概念有什么好处呢？主要是两个好处。第一个好处是在下一帧绘制中，如果一个View的内容不需要更新，那么就不用重建它的Display List，也就是不需要调用它的onDraw成员函数。第二个好处是在下一帧中，如果一个View仅仅是一些简单的属性发生变化，例如位置和Alpha值发生变化，那么也无需要重建它的Display List，只需要在上一次建立的Display List中修改一下对应的属性就可以了，这也意味着不需要调用它的onDraw成员函数。这两个好处使用在绘制应用程序窗口的一帧时，省去很多应用程序代码的执行，也就是大大地节省了CPU的执行时间。

### 3.3.2 DisplayList构建表

***DisplayListCanvas\***类有一个成员变量mDisplayListData，它指向的是一个DisplayListData对象，用来记录当前正在处理的DisplayListCanvas对应的Render Node的绘制命令。DisplayListData类通过三个向量来记录一个Render Node的绘制命令，如图12所示：

![img](D:\my-note\android\view\assets\v2-c2d5b5c25a712732e5891dd6857be902_1440w.webp)

图12 Display List Data结构示意图

这三个向量分别是一个Display List Op Vector、Chunk Vector和Draw Render Node Op Vector，其中：

> 1. Display List Op Vector包含了一个Render Node的所有绘制命令，每一个绘制命令用一个Display List Op来描述。
> 2. Draw Render Node Op Vector包含了一个Render Node的所有子Render Node，相当于包含了一个View的所有子View绘制命令，每一个子View绘制命令用一个Draw Render Node Op来描述。
> 3. Chunk Vector将一个Render Node的所有Display List Op和Draw Render Node Op划分成为Chunk来管理。一个Chunk通过一个begin op index和一个end op index来记录一组Display List Op，并且通过begin child index和end child index来记录一组Draw Render Node Op。

在渲染一个Render Node的时候，是按照Chunk Vector保存的Chunk顺序来渲染所有的Display List Op和Draw Render Node Op的。前面提到，Draw Render Node Op描述的是一个View的子View绘制命令。子View的Z轴坐标有可能是负的，这意味着子View要先于父View绘制。因此在渲染一个Chunk对应的Display List Op和Draw Render Node Op之前，需要对Draw Render Node Op按照其对应的子View的Z轴坐标由小到大进行排序。排序完成之后，先渲染Z轴坐标为负的Draw Render Node Op，接着再渲染Display List Op，最后渲染Z轴坐标为0或者正数的Draw Render Node Op。

从上面的分析就可以推断出，Chunk的存在意义就是将一个View自身的绘制命令及其子View绘制命令组织在一起。这样在渲染一个View的UI时，就可以很容易地处理子View的Z轴坐标为负数的情况。这同时也意味着在构建一个View的Display List的时候，记录的绘制命令有可能是乱序的。这就要求在渲染这些绘制命令的时候，需要对它们按照Z轴坐标进行排序。

### 3.3.3 DisplayList构建流程分析

```c
1. public class ThreadedRenderer extends HardwareRenderer {  
2.     ......  
3.  @Override 
4.  void draw(View view, AttachInfo attachInfo, HardwareDrawCallbacks callbacks) {  
5.         ......  
6.  
7.         updateRootDisplayList(view, callbacks);  //displayList构建过程
8.         ......    
9.         int syncResult = nSyncAndDrawFrame(mNativeProxy, frameTimeNanos,  
10.                 recordDuration, view.getResources().getDisplayMetrics().density); //displayList渲染过程 
11.     }  
12.     ......  
13. }  
```

本节主要分析DisplayList的构建过程：***updateRootDisplayList\***。这个mRootNode对应的就是RootRenderNode,这个View就是应用程序窗口的DecorView。当mRootNode.end调用完毕，一个应用程序窗口的Display List就构建完成。这个Display List通过递归的方式包含了所有子View的Display List。这样，最后我们通过渲染应用程序窗口的Root Render Node的Display List，就可以获得整个应用程序窗口的UI，整个流程如图13。

![img](D:\my-note\android\view\assets\v2-8dec4e4f3ed6615f8198e9616ed1ea58_1440w.png)

图13 DisplayList创建时序图

对于一个简单的文本窗口的displayList的构建函数调用如下(忽略background)：

**updateRootDisplayList –> updateViewTreeDisplayList**

**-> (\*Décor view\*) updateDisplayListIfDirty –> draw -> dispatchdraw -> draw(canvas, parent..)**

**->(\*Relativelayout\*) updateDisplayListIfDirty–> draw -> dispatchdraw ->draw(canvas,parent..)**

**->(\*Textview\*) updateDisplayListIfDirty–> draw -> ondraw**

对于View类的成员函数draw前面已有分析，当使用硬件渲染时，调用Canvas API相当是将API调用记录在一个Display List中，而当使用软件渲染时，调用Canvas API相当是将UI绘制在一个Bitmap中。然后通过RenderNode类的成员函数drawRenderNode将DisplayList绘制在父View关联的一个Render Node对应的DisplayListCanvas上。

![img](D:\my-note\android\view\assets\v2-fb740ff276756d027fdfe520399d6b05_1440w.png)

最终生成的DisplayList如下图14。

![img](D:\my-note\android\view\assets\v2-2dc10509a612a8945ffe63d951f080ac_1440w.png)

图14 DisplayList结构图

如第一章提到的TextView，具体DisplayList如下：

```text
1. Save 3
2. DrawPatch
3. Save 3
4. ClipRect 20.00, 4.00, 99.00, 44.00, 1
5. Translate 20.00, 12.00
6. DrawText 9, 18, 9, 0.00, 19.00, 0x17e898
7. Restore
8. RestoreToCount 0
```

### 3.3.4 TextureView构建流程分析

不同于任何其他View，TextureView直接使用OpenGL纹理，一般用于游戏或Camera。

### 3.3.4.1 TextureView纹理创建

TextureView的UI是通过一个HardwareLayer来描述的，该HardwareLayer可以通过TextureView类的成员函数getHardwareLayer获得，主要目的为创建一个关联的Layer（和SurfaceFlinger的Layer概念不同）以及为其生成一个OpenGL纹理，可见TextureView是通过Open GL纹理来实现的。

```c
1. Layer* LayerRenderer::createTextureLayer(RenderState& renderState) {  
2.     ......  
3.     Layer* layer = new Layer(Layer::kType_Texture, renderState, 0, 0); //对应kType_DisplayList
4.     ......  
5.     layer->generateTexture();  
6.     return layer;  
7. }  
```

其流程从TextureView到真正创建Layer经过一系列的JNI调用，如下： ThreadedRenderer类的成员函数nCreateTextureLayer是一个JNI函数，由Native层的函数android_view_ThreadedRenderer_createTextureLayer实现，如下所示，应用程序进程的Main Thread通过RenderProxy对象可以与Render Thread通信，DeferredLayerUpdater对象的创建在RenderThread中，以保证OpenGL环境的一致性，而且从名字可见layer渲染是延后执行的。

```java
1. static jlong android_view_ThreadedRenderer_createTextureLayer(JNIEnv* env, jobject clazz,  
2.         jlong proxyPtr) {  
3.     RenderProxy* proxy = reinterpret_cast<RenderProxy*>(proxyPtr);  
4.     DeferredLayerUpdater* layer = proxy->createTextureLayer();  
5.     return reinterpret_cast<jlong>(layer);  
6. }  
1. CREATE_BRIDGE2(createTextureLayer, RenderThread* thread, CanvasContext* context) {  
2.     Layer* layer = args->context->createTextureLayer();  
3.     if (!layer) return 0;  
4.     return new DeferredLayerUpdater(*args->thread, layer);  
5. }  
6.  
7. DeferredLayerUpdater* RenderProxy::createTextureLayer() {  
8.     SETUP_TASK(createTextureLayer);  
9.      args->context = mContext;  
10.     args->thread = &mRenderThread;  
11.     void* retval = postAndWait(task);  //需要在RenderThread中执行createTextureLayer
12.     DeferredLayerUpdater* layer = reinterpret_cast<DeferredLayerUpdater*>(retval);  
13.     return layer;  
14. }  
```

### 3.3.4.2 TextureView纹理更新

TextureView的使用者获得了这个SurfaceTexture之后，就可以向TextureView提供Open GL纹理的内容了。这是一种Producer-Consumer工作模式，TextureView的使用者是Producer，而TextureView是Consumer。当TextureView的使用者通过SurfaceTexture向TextureView提供了内容的时候，TextureView可以通过其成员变量mUpdateListener指向的一个OnFrameAvailableListener对象的成员函数onFrameAvailable(相当于queue)获得通知，这和SurfaceView也类似，如下所示：

```c
1. public class TextureView extends View {  
2.  private final SurfaceTexture.OnFrameAvailableListener mUpdateListener =  
3.  new SurfaceTexture.OnFrameAvailableListener() {  
4.  @Override 
5.  public void onFrameAvailable(SurfaceTexture surfaceTexture) {  
6.             updateLayer();  //标记Open GL纹理需要更新
7.             invalidate();  //从view继承的invalidate通知Main Thread需要重绘
8.         }  
9.     };  
10. }  
```

如果SurfaceTexture有更新，则在调用getHardwareLayer() -> applyUpdate()->updateSurfaceTexture来更新对应的Open GL纹理的大小、透明度和内容等分析。下面看一下updateSurfaceTexture的调用：

```c
1. public void updateSurfaceTexture() {  
2.     nUpdateSurfaceTexture(mFinalizer.get());  
3.     mRenderer.pushLayerUpdate(this);  
4. }  
```

（1） 从下面的代码可见，DeferredLayerUpdater类的成员函数updateTexImage并没有真正去更新当前正在处理的TextureView的Open GL纹理，而只是将DeferredLayerUpdater类的成员变量mUpdateTexImage设置为true，用来表示当前正在处理的TextureView的Open GL纹理需要进行更新。之所以要这样做，是因为纹理的更新要在Render Thread进行，而现在是在Main Thread执行。等到后面应用程序窗口的Display List被渲染时，TextureView的Open GL纹理才会被真正的更新：

```c
1.static void android_view_HardwareLayer_updateSurfaceTexture(JNIEnv* env, jobject clazz,  
2.         jlong layerUpdaterPtr) {  
3.     DeferredLayerUpdater* layer = reinterpret_cast<DeferredLayerUpdater*>(layerUpdaterPtr);
4.     layer->updateTexImage();  
5. }  

1.class DeferredLayerUpdater : public VirtualLightRefBase {  
2. public:  
3.     ANDROID_API void updateTexImage() {  
4.         mUpdateTexImage = true;  
5.     }  
6.     ......  
7. };  
```

（2）mRenderer调用pushLayerUpdate将当前正在处理的HardwareLayer保存在内部的一个待更新列表中，保存在这个列表中的DeferredLayerUpdater对象在渲染应用程序窗口的Display List的时候就会被处理，它的实现如下所示：

```c
1.      static void android_view_ThreadedRenderer_pushLayerUpdate(JNIEnv* env, jobject clazz,  
2.	        jlong proxyPtr, jlong layerPtr) {  
3.	    RenderProxy* proxy = reinterpret_cast<RenderProxy*>(proxyPtr);  
4.	    DeferredLayerUpdater* layer = reinterpret_cast<DeferredLayerUpdater*>(layerPtr);  
5.	    proxy->pushLayerUpdate(layer);  
6.	}  
1.	void RenderProxy::pushLayerUpdate(DeferredLayerUpdater* layer) {  
2.	    mDrawFrameTask.pushLayerUpdate(layer);  
3.	}
1.	void DrawFrameTask::pushLayerUpdate(DeferredLayerUpdater* layer) {  
2.	    ......  
3.	    mLayers.push_back(layer);  
4.	}  
```

### 3.4硬件加速渲染的Display List渲染过程分析

### 3.4.1 渲染基本流程分析

```c
1. public class ThreadedRenderer extends HardwareRenderer {  
2.     ......  
3.     @Override  
4.  void draw(View view, AttachInfo attachInfo, HardwareDrawCallbacks callbacks) {  
5.         ......  
6.  
7.  updateRootDisplayList(view, callbacks);  //displayList构建过程
8.         ......    
9.  int syncResult = nSyncAndDrawFrame(mNativeProxy, frameTimeNanos,  
10.                 recordDuration, view.getResources().getDisplayMetrics().density); //displayList渲染过程 
11.     }  
12.     ......  
13. }  
```

本节主要讲述Display List的渲染，即函数***nSyncAndDrawFrame\***，时序图如下

![img](D:\my-note\android\view\assets\v2-139cae43b1317dccf383f2fa02b1f161_1440w.png)

图 15 displayList 渲染时序图

DisplayList渲染发生在应用程序进程的Render Thread中，displayList渲染函数最终调用RenderProxy的syncAndDrawFrame。 DrawFrameTask的成员函数drawFrame最主要的操作就是调用另外一个成员函数postAndWait往Render Thread的Task Queue抛一个消息，并且进入睡眠状态，等待Render Thread在合适的时候唤醒。

```c
1. int RenderProxy::syncAndDrawFrame(nsecs_t frameTimeNanos, nsecs_t recordDurationNanos,  
2.  float density) {  
3.     mDrawFrameTask.setDensity(density);  
4.     return mDrawFrameTask.drawFrame(frameTimeNanos, recordDurationNanos);  
5. }  

1. int DrawFrameTask::drawFrame(nsecs_t frameTimeNanos, nsecs_t recordDurationNanos) {  
2.     ......  
3.     postAndWait();  
4.     ......  
5. }  
1. void DrawFrameTask::postAndWait() {  
2.     AutoMutex _lock(mLock);  
3.     mRenderThread->queue(this);  
4.     mSignal.wait(mLock);  // unblockUiThread唤醒
5. }  
6.  
7. void DrawFrameTask::unblockUiThread() {  
8.     AutoMutex _lock(mLock);  
9.     mSignal.signal();  
10. }  
11.  
```

真正执行渲染命令的在DrawFrameTask的run函数。

```c
1. void DrawFrameTask::run() {  
2.     ......  
3.  
4.     bool canUnblockUiThread;  
5.     bool canDrawThisFrame;  
6.     {  
7.         TreeInfo info(TreeInfo::MODE_FULL, mRenderThread->renderState());  
8.         canUnblockUiThread = syncFrameState(info);  //将应用程序窗口的Display List、Render Property以及Display List引用的Bitmap等信息从Main Thread同步到Render Thread中
9.         canDrawThisFrame = info.out.canDrawThisFrame;  
10.     }  
11.  
12.  // Grab a copy of everything we need 
13.     CanvasContext* context = mContext;  
14.  
15.  // From this point on anything in "this" is *UNSAFE TO ACCESS* 
16.    if (canUnblockUiThread) {  
17.         unblockUiThread();  //同步完成此时即可唤醒MainThread继续其他任务
18.     }  
19.  
20.    if (CC_LIKELY(canDrawThisFrame)) { //可以绘制，无需跳过
21.         context->draw();  //渲染
22.     }  
23.  
24.    if (!canUnblockUiThread) {  
25.         unblockUiThread();  //同步未完成，等待渲染完成后才能unblock
26.     }  
27. }  
```

其主要任务可分为2个：

1. 将Main Thread维护的Display List同步到Render Thread维护的Display List去，这个同步过程由Render Thread执行，但是Main Thread会被阻塞住。如果能够完全地将Main Thread维护的Display List同步到Render Thread维护的Display List去，那么Main Thread就会被唤醒，此后Main Thread和Render Thread就互不干扰，各自操作各自内部维护的Display List这意味着Render Thread在渲染应用程序窗口当前帧的Display List的同时，Main Thread可以去准备应用程序窗口下一帧的Display List，这样就使得应用程序窗口的UI更流畅。否则的话，Main Thread就会继续阻塞，直到Render Thread完成应用程序窗口当前帧的渲染为止；同步过程主要由syncFrameState函数实现
2. 对RootRenderNode的Display List进行渲染，就可以得到整个Android应用程序窗口的UI。主要由context->draw(); 完成，前提是当前帧能够进行绘制，什么时候当前帧不能够进行绘制呢？我们知道，应用程序进程绘制好一个窗口之后，得到的图形缓冲区要交给Surface Flinger进行合成，最后才能显示在屏幕上。Surface Flinger为每一个窗口都维护了一个图形缓冲区队列。当这个队列等待合成的图形缓冲区的个数大于等于2时，就表明Surface Flinger太忙了。因此这时候就最好不再向它提交图形缓冲区，这就意味着应用程序窗口的当前帧不能绘制了，也就是**丢帧**，这个判断机制也在syncFrameState函数。

```c
1. mNativeWindow->query(mNativeWindow.get(),  
2.      NATIVE_WINDOW_CONSUMER_RUNNING_BEHIND, &runningBehind);  
3. info.out.canDrawThisFrame = !runningBehind;  
```

### 3.4.2 将MainThread信息同步至RenderThread

DrawFrameTask类的成员函数syncFrameState的实现如下所示，其同步信息大约有4个方面的考虑：基本的properties和displayListData、Bitmap、TextureView、Layer。

```java
1. bool DrawFrameTask::syncFrameState(TreeInfo& info) {  
2.      ......  
3.     for (size_t i = 0; i < mLayers.size(); i++) {  
4.         mContext->processLayerUpdate(mLayers[i].get());  //处理deferlayer
5.     }  
6.     ......  
7.     mContext->prepareTree(info);  //返回值主要来自于此
8.   …..
9.  // If prepareTextures is false, we ran out of texture cache space 
10.  return info.prepareTextures;  
11. }  
```

### 3.4.2.1 mProperties、mDisplayListData同步

当Main Thread维护的Render Properties发生变化时，成员变量mDirtyPropertyFields的值就不等于0，其中不等于0的位就表示是哪一个具体的Property发生了变化，而当Main Thread维护的Display List Data发生变化时，成员变量mNeedsDisplayListDataSync的值就等于true，表示要从Main Thread同步到Render Thread。

```java
1. void RenderNode::prepareTreeImpl(TreeInfo& info) {  
2.     ......  
3.     if (info.mode == TreeInfo::MODE_FULL) {  //UI thread驱动，与之相对是RenderThread
4.          pushStagingPropertiesChanges(info);  //属性同步
5.     }  
6.     ......  
7.     if (info.mode == TreeInfo::MODE_FULL) {  
8.          pushStagingDisplayListChanges(info);  //displayList同步
9.     }  
10.    prepareSubTree(info, mDisplayListData);  //childNode->prepareTreeImpl(info, childFunctorsNeedLayer); 递归调用
11.    pushLayerUpdate(info);  //对于设置为Layer的类型同步更新
12.     ......  
13. }  
```

对于正常的View而言，将RenderNode类中有2个mProperties、mDisplayListData同步之后该过程就完成了，但是仍有额外3种情况需要考虑：即Bitmap、TextureView和将RenderNode设置为Layer的动画。

### 3.4.2.2 Bitmap同步

```java
1. void RenderNode::prepareSubTree(TreeInfo& info, DisplayListData* subtree) {  
2.  if (subtree) {  
3.         TextureCache& cache = Caches::getInstance().textureCache;  
4.         ......  
5.         if (subtree->ownedBitmapResources.size()) {  
6.             info.prepareTextures = false;  
7.         }  
8.         for (size_t i = 0; info.prepareTextures && i < subtree->bitmapResources.size(); i++) {             
              info.prepareTextures = cache.prefetchAndMarkInUse(subtree->bitmapResources[i]);  
9.         }  
10.        for (size_t i = 0; i < subtree->children().size(); i++) {  
11.             DrawRenderNodeOp* op = subtree->children()[i];  
12.             RenderNode* childNode = op->mRenderNode;  
13.             ......  
14.             childNode->prepareTreeImpl(info);  
15.             .....  
16.         }  
17.     }  
18. }  
```

Display List引用的Bitmap的同步方式与Display List和Render Property的同步方式有所不同。在同步Bitmap的时候，Bitmap将作为一个Open GL纹理上传到GPU去被Render Thread使用，因为Render Thread就通过已经上传到GPU的Open GL纹理来使用这些Bitmap

当这个TreeInfo对象的成员变量prepareTextures的值等于true时，表示应用程序窗口的Display List引用到的Bitmap均已作为Open GL纹理上传到了GPU。这意味着应用程序窗口的Display List引用到的Bitmap已全部同步完成。在这种情况下，Render Thread在渲染下一帧之前，就可以唤醒Main Thread。另一方面，如果上述TreeInfo对象的成员变量prepareTextures的值等于false，就意味着应用程序窗口的Display List引用到的某些Bitmap不能成功地作为Open GL纹理上传到GPU，这时候Render Thread在渲染下一帧之后，才可以唤醒Main Thread，防止这些未能作为Open GL纹理上传到GPU的Bitmap一边被Render Thread渲染，一边又被Main Thread修改。

Display List引用的Bitmap保存在它的成员变量ownedBitmapResources和bitmapResources的两个Vector中。

ownedBitmapResources：该列表中Bitmap的底层储存是由应用程序提供和管理的。这意味着很难维护该底层储存在Main Thread和Render Thread的一致性，这时候就需要将参数info指向的一个TreeInfo对象的成员变量prepareTextures的值设置为false，这样canUnblockUiThread就为false。

bitmapResources：该列表中Bitmap的底层储存不是由应用程序提供和管理的，因此就能够保证它不会被随意修改而又不通知Render Thread进行同步。对于这些Bitmap，就可以将它们作为Open GL纹理上传到GPU去。并不是所有的这些Bitmap都是能够作为Open GL纹理上传到GPU去的，有两个原因：

一是Bitmap太大，超出预先设定的最大Open GL纹理的大小。这种情况通过调用TextureCache类的成员函数canMakeTextureFromBitmap进行判断。

二是已经作为Open GL纹理上传到GPU的Bitmap太多，超出预先设定的最多可以上传到GPU的大小。

一旦某一个Bitmap不能作为Open GL纹理上传到GPU去，那么也是需要完全同步Main Thread和Render Thread渲染应用程序窗口的一帧的。这时候也需要将参数info指向的一个TreeInfo对象的成员变量prepareTextures的值设置为false，这样canUnblockUiThread也为false。

### 3.4.2.3 TextureView的同步过程

当TextureView有更新时，Native层会将一个与它关联的DeferredLayerUpdater对象保存在DrawFrameTask类的成员变量mLayers描述的一个vector中，保存在这个vector中的DeferredLayerUpdater对象，都是需要延后处理的。

mSurfaceTexture指向的一个是GLConsumer对象。调用成员其成员函数updateTexImage读出可用的图形缓冲区，并且将该图形缓冲区封装成一个Open GL纹理。这个Open GL纹理可以通过getCurrentTextureTarget()获得。最后将获得的Open GL纹理关联给成员变量mLayer描述的一个Layer对象，这样Layer就可以渲染了。

```java
1.	void CanvasContext::processLayerUpdate(DeferredLayerUpdater* layerUpdater) {  
2.	    bool success = layerUpdater->apply();  
3.	    ......  
4.	}  
1.	bool DeferredLayerUpdater::apply() {  
2.	    ......  
3.	    if (mUpdateTexImage) {  //displayList创建一节中设置该值，如果texture有更新则设置为true
4.	       mUpdateTexImage = false;  
5.	       doUpdateTexImage();  
6.	     }  
7.	    ......  
8.	}  
9.	void DeferredLayerUpdater::doUpdateTexImage() {  
10.	    if (mSurfaceTexture->updateTexImage() == NO_ERROR) {  //acquire buffer
11.	        ......  
12.	        GLenum renderTarget = mSurfaceTexture->getCurrentTextureTarget();  
13.	          LayerRenderer::updateTextureLayer(mLayer, mWidth, mHeight,  
14.	                !mBlend, forceFilter, renderTarget, transform);  
15.	    }  
16.	} 
```

### 3.4.2.4 动画类型Layer的渲染信息同步

对于TextureView来说经过上面的步骤其同步也已经完成，其最终在hwui中也会生成一个Layer，其View类型为LAYER_TYPE_HARDWARE，但是其RenderNode并不会设置为一个Layer。 当一个View的类型被设置为LAYER_TYPE_HARDWARE，且它的成员函数buildLayer被调用，那么与它关联的Render Node就会被设置为一个Layer。这意味着该View将会作为一个FBO（Frame Buffer Object）进行渲染。这样做主要是为了更流畅地显示一个View的动画。

动画设置代码：

```c
1. public class ViewPropertyAnimator {  
2.     ......  
3.     public ViewPropertyAnimator withLayer() {  
4.          mPendingSetupAction= new Runnable() {  //在动画开始显示之前执行
5.             @Override  
6.             public void run() {  
7.                 mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);  
8.                 if (mView.isAttachedToWindow()) {  
9.                       mView.buildLayer();  
10.                 }  
11.             }  
12.         };  
13.         final int currentLayerType = mView.getLayerType();  
14.         mPendingCleanupAction = new Runnable() {  //在动画结束显示之后执行,还原LayerType
15.             @Override  
16.             public void run() {  
17.                   mView.setLayerType(currentLayerType, null);  
18.             }  
19.         };  
20.         ......  
21.         return this;  
22.     }  
23. }  
```

与TextureView对比看一下hwui中Layer成员变量：

```c
24.   /**
25.  * Name of the FBO used to render the layer. If the name is 0
26.  * this layer is not backed by an FBO, but a simple texture.
27.  */
28.  GLuint fbo = 0; //TextureView只是一个简单的texture，fbo为0；上述场景不为0
29.  /**
30.  * If set to true (by default), the layer can be reused.
31.  */
32.  bool cacheable = true; //TextureView 中该值为false, 上述场景为true
33.  
34.  /**
35.  * Denotes whether the layer is a DisplayList, or Texture layer.
36.  */
37.  const Type type; // TextureView 中该值为Texture layer，上述场景为DisplayList
38.  …
39.  /**
40.  * Indicates the render target.
41.  */
42.  GLenum renderTarget = GL_TEXTURE_2D; // TextureView 中该值由上层传递下来（GL_TEXTURE_2D或GL_TEXTURE_EXTERNAL_OES，部分yuv使用后者），上述场景为默认值GL_TEXTURE_2D
43.  ……
44.  std::unique_ptr<OpenGLRenderer> renderer; //TextureView中该值为NULL，上述场景不为NULL
```

OpenGLRenderer类将需要进行更新处理的Layer对象保存在成员变量mLayerUpdates描述的一个Vector中，保存在这个Vector中的Layer对象在渲染应用程序窗口的Display List的时候，就是需要进行更新处理的。

```c
1. void RenderNode::pushLayerUpdate(TreeInfo& info) {  
2.     LayerType layerType = properties().layerProperties().type();  
3.     ......  
4.     if (CC_LIKELY(layerType != kLayerTypeRenderLayer) || CC_UNLIKELY(!isRenderable())) {  
5.        if (CC_UNLIKELY(mLayer)) {  
6.             LayerRenderer::destroyLayer(mLayer);  
7.             mLayer = NULL;  
8.         }  
9.         return;  
10.     }  
11.     ......  
12.  
13.      if (!mLayer) {  
14.         mLayer = LayerRenderer::createRenderLayer(info.renderState, getWidth(), getHeight());  //创建的Layer为kType_DisplayList，与textureView不同
15.         ......  
16.     } else if (mLayer->layer.getWidth() != getWidth() || mLayer->layer.getHeight() != getHeight()) {  
17.     if (!LayerRenderer::resizeLayer(mLayer, getWidth(), getHeight())) {  
18.             ......  
19.         }  
20.         ......  
21.     }  
22.     ......  
23.     if (dirty.intersect(0, 0, getWidth(), getHeight())) {  
24.         ......  
25.         mLayer->updateDeferred(this, dirty.fLeft, dirty.fTop, dirty.fRight, dirty.fBottom);
26.     }  
27.     ......  
28.     if (info.renderer && mLayer->deferredUpdateScheduled) {  
29.         info.renderer->pushLayerUpdate(mLayer);  
30.     }  
31.     ......  
32. }  
33. void Layer::updateDeferred(RenderNode* renderNode, int left, int top, int right, int bottom) {  
34.     requireRenderer();  //  renderer = new LayerRenderer(renderState, this);  
35.     this->renderNode = renderNode;  
36.     const Rect r(left, top, right, bottom);  
37.     dirtyRect.unionWith(r);  
38.     deferredUpdateScheduled = true;  //表示当前正在处理的Layer对象后面还需要执行真正的更新操作
39. }  
40. void OpenGLRenderer::pushLayerUpdate(Layer* layer) {  
41.     ......  
42.     mLayerUpdates.push_back(layer);  
43.     ......  
44. }  
45.  
```

这一步执行完成之后，应用程序窗口的Display List等信息就从Main Thread同步到Render Thread了，回到DrawFrameTask类的成员函数run中，接下来就可以调用CanvasContext类的成员函数draw渲染应用程序窗口的Display List了。

### 3.4.3 RenderThread渲染函数分析

### 3.4.3.1 基本流程分析

```c
1. void CanvasContext::draw() {  
2.     ......  
3.     SkRect dirty;  
4.     mDamageAccumulator.finish(&dirty);  
5.     ......  
6.     status_t status;  
7.     if (!dirty.isEmpty()) {  
8.         status = mCanvas->prepareDirty(dirty.fLeft, dirty.fTop,  
9.                 dirty.fRight, dirty.fBottom, mOpaque);  //执行一些初始化工作，取决于脏区域是不是空的，非Layer渲染下，基本不需要做多余工作
10.     } else {  
11.         status = mCanvas->prepare(mOpaque);  
12.     }  
13.     Rect outBounds;  
14.     status |= mCanvas->drawRenderNode(mRootRenderNode.get(), outBounds);  //渲染mRootRenderNode描述的应用程序窗口的Root Render Node的Display List
15.     ......  
16.     mCanvas->finish();  //清理工作
17.     ......  
18.     if (status & DrawGlInfo::kStatusDrew) {  
19.         swapBuffers();  //将前面已经绘制好的图形缓冲区提交给SurfaceFlinger合成和显示
20.     }  
21.     ......  
22. }  
```

在绘制指令不合并的情况下，其drawRenderNode如下：

```c
1.  status_t status;  
2.  // All the usual checks and setup operations (quickReject, setupDraw, etc.) 
3.  // will be performed by the display list itself 
4.  if (renderNode && renderNode->isRenderable()) {  
5.         // compute 3d ordering 
6.         renderNode->computeOrdering();  //重排那些Projected Node
7.         if (CC_UNLIKELY(mCaches.drawDeferDisabled)) {//禁止指令排序 
8.             status = startFrame();  //初始化
9.             ReplayStateStruct replayStruct(*this, dirty, replayFlags);  
10.            renderNode->replay(replayStruct, 0);  //真正渲染
11.            return status | replayStruct.mDrawGlStatus;  
12.         }
13.  }
```

在绘制指令可以合并的情况下，其drawRenderNode如下：

```c
13.  bool avoidOverdraw = !Properties::debugOverdraw;
14.  DeferredDisplayList deferredList(mState.currentClipRect(), avoidOverdraw);
15.  DeferStateStruct deferStruct(deferredList, *this, replayFlags);
16.  renderNode->defer(deferStruct, 0); //指令合并
17.  
18.  flushLayers(); //对于非Layer渲染下不需要
19.  startFrame();//初始化
20.  
21.  deferredList.flush(*this, dirty); //真正渲染
```

### 3.4.3.2 LayerRenderer渲染介绍

涉及到渲染UI的Renderer有两个，一个是LayerRenderer，另外一个是OpenGLRenderer。从前面的分析可以知道，LayerRenderer主要负责用来渲染类型为LAYER_TYPE_HARDWARE的View。这些View将会渲染在一个FBO上。OpenGLRenderer负责渲染应用程序窗口的Display List。这个Display List是直接渲染在Frame Buffer上的，也就是直接渲染在从Surface Flinger请求回来的图形缓冲区上。对于TextureView其实质仍然是OpenGLRenderer，并未生成LayerRenderer对象，只是有Layer生成而已，和当前的章节的讨论不同。

OpenGLRenderer类的成员函数getTargetFbo的返回值总是0，也就是说，OpenGLRenderer类总是直接将UI渲染在Frame Buffer上。LayerRenderer有一个重要的成员变量fbo。当它的值大于0的时候，就表示要将UI渲染在一个FBO上。

```c
1. class OpenGLRenderer : public StatefulBaseRenderer {  
2.   virtual GLuint getTargetFbo() const {  
3.     return 0;  
4.   }  
5. }  
6. GLuint LayerRenderer::getTargetFbo() const {  
7.  return mLayer->getFbo(); 
8. }  
```

如上一节所说，当一个View的类型被设置为LAYER_TYPE_HARDWARE，且它的成员函数buildLayer被调用，则LayerRenderer会被创建, buildLayer的实现如下所示：

```c
void CanvasContext::buildLayer(RenderNode* node) {
    ......
 
    TreeInfo info(TreeInfo::MODE_FULL, mRenderThread.renderState());
    ......
    node->prepareTree(info);
    ......
 
    mCanvas->flushLayerUpdates();
 
    ......
}
```

这里就可以看到CanvasContext类的成员函数buildLayer调用了RenderNode类的成员函数prepareTree，它用来将参数node描述的Render Node的Display List从Main Thread同步到Render Thread中，并且为该Render Node创建了一个Layer，但是这个Layer处理待更新状态。 接下来会继续调用成员变量mCanvas指向的一个OpenGLRenderer对象的成员函数flushLayerUpdates更新刚才创建的Layer（首次调用该函数replay绘制命令，之后通过draw），它的实现如下所示：

```c
1. void OpenGLRenderer::flushLayerUpdates() {  
2.     ......  
3.     updateLayers();  
4.     flushLayers();  
5.     ......  
6. }  
```

而layer在draw中的实现，同样调用了上述的2个函数：

```c
1. status_t OpenGLRenderer::prepareDirty(float left, float top,  
2.         float right, float bottom, bool opaque) {  
3.  
4.     setupFrameState(left, top, right, bottom, opaque);  
5.  
6.     ......  
7.     if (currentSnapshot()->fbo == 0) {  
8.         ......  
9.          updateLayers();  //对于Layer会进来两次，第一次为OpenGLRenderer
10.     } else {  
11.         return startFrame();  //第二次为LayerRenderer
12.     }  
13.     return DrawGlInfo::kStatusDone;  
14. }  

15. void OpenGLRenderer::drawRenderNode(RenderNode* renderNode, Rect& dirty, int32_t replayFlags){
16.   ….
17.   flushLayers(); 
18.   startFrame();//初始化
19.   deferredList.flush(*this, dirty); //真正渲染
20. }
```

1） updateLayers: 重排和合并所有设置了Layer的Render Node的Display List的绘制命令。

```c
1. bool OpenGLRenderer::updateLayer(Layer* layer, bool inFrame) {  
2.         ......  
3.         if (CC_UNLIKELY(inFrame || mCaches.drawDeferDisabled)) {  
4.             layer->render(*this);  //直接渲染至fbo，过程结束
5.         } else {  
6.             layer->defer(*this);  //重排合并，默认方式，需要flushLayers
7.         }  
8.   …..
9. }  
10. void Layer::defer(const OpenGLRenderer& rootRenderer) {  
11.     ......  
12.     deferredList = new DeferredDisplayList(dirtyRect);  
13.     DeferStateStruct deferredState(*deferredList, *renderer,  
14.             RenderNode::kReplayFlag_ClipChildren);  
15.     ......  
16.     renderNode->computeOrdering();  //重排
17.     renderNode->defer(deferredState, 0);  //合并
18.     deferredUpdateScheduled = false;  
19. }  
```

defer对应用程序窗口的Root Render Node及其子Render Node和Projected Node的的Display List的绘制命令进行合并操作。合并后得到的绘制命令，也就是DrawOp，就以Batch为单位保存 在本地变量deferredList描述的一个DeferredDisplayList对象的成员变量mBatches描述的一个Vector中。

2）flushLayers: *（如果绘制命令禁止合并，该函数是没有实际意义的）。*在执行绘制命令之前，还有一件事情需要做，就是先执行那些设置了Layer的子Render Node的绘制命令，以便得到一个对应的FBO。这些FBO就代表了那些设置了Layer的子Render Node的UI。这一步是通过调用OpenGLRenderer类的成员函数flush来完成的，对于保存在上述Vector中的每一个Layer，OpenGLRenderer类的成员函数flushLayers都会调用它的成员函数flush，目的就是执行这些Layer关联的Render Node的Display List经过重排和合并后的绘制命令。

```c
1. void OpenGLRenderer::flushLayers() {
2.  int count = mLayerUpdates.size();
3.  if (count > 0) {
4.   ….
5.   for (int i = 0; i < count; i++) {
6.      mLayerUpdates.itemAt(i)->flush();
7.    }
8.    mLayerUpdates.clear();
9.    mRenderState.bindFramebuffer(getTargetFbo());
10.   …..
11.  }
12. }
```

Layer类的成员函数flush的实现如下所示：

```c
13. void Layer::flush() {  
14.  // renderer is checked as layer may be destroyed/put in layer cache with flush scheduled 
15.  if (deferredList && renderer) {  
16.         //该renderer为LayerRenderer, 其prepareDirty会首先调用mRenderState.bindFramebuffer(mLayer->getFbo());
17.         renderer->prepareDirty(dirtyRect.left, dirtyRect.top, dirtyRect.right, dirtyRect.bottom,  !isBlend());  
18.         deferredList->flush(*renderer, dirtyRect);  
19.         ......  
20.     }  
21. }  
1. 
2. status_t DeferredDisplayList::flush(OpenGLRenderer& renderer, Rect& dirty) {  
3.     ......  
4.     status |= replayBatchList(mBatches, renderer, dirty);  
5.     ......  
6.     return status;  
7. }  
```

这时候所有设置了Layer的Render Noder及其子Render Node和Projected Node的Display List均已渲染到了自己的FBO之上，接下来就要将这些FBO以及其它没有设置Layer的Render Node的Display List渲染在从Surface Flinger请求回来的一个图形缓冲区之上。

由于前面每调用一个Layer对象的成员函数flush的时候，都会将一个FBO设置为当前的渲染对象，而接下来的渲染对象是Frame Buffer，因此就需要调用成员变量mRenderState描述的一个RenderState对象的成员函数bindFramebuffer将Frame Buffer设置为当前的渲染对象，这是在flushLayers函数的最后实现的。

3）回到OpenGLRenderer类的成员函数drawRenderNode中，这时候可以渲染应用程序窗口的**Root Render Nod**e的Display List了。在渲染之前，同样是先调用 startFrame执行一些诸如清理颜色绘冲区等基本操作，然后再调用的DeferredDisplayList类的成员函数flush来执行它们。这里同样是需要注意，这些绘制命令的执行是作用在FrameBuffer之上的。

```c
1. void OpenGLRenderer::drawRenderNode(RenderNode* renderNode, Rect& dirty, int32_t replayFlags){
2.   ….
3.  flushLayers(); 
4.  startFrame();//初始化
5.  deferredList.flush(*this, dirty); //真正渲染
6. }
```

### 3.4.3.3 指令合并简介

我们知道，Android应用程序窗口UI的视图是树形结构的。在渲染的时候，先绘制父视图的UI，再绘制子视图的UI。我们可以把这种绘制模式看作是分层的，即先绘制背后的层，再绘制前面的层，这是通过***defer\***函数实现的。

![img](D:\my-note\android\view\assets\v2-1feeecb325723825e3b7a516e22ba8db_1440w.webp)

图15 Android应用程序窗口UI分层绘制模式

图15显示的窗口由一个背景图、一个ActionBar、一个Button和一个应用程序图标组成，它们按照从前到后的顺序排列。其中，ActionBar和Button都是由背景和文字组成的，它们使用的背景图均为一个预加载的Drawable资源，并且是按照九宫图方式绘制。

按照我们前面描述的分层绘制模式，图15显示的窗口UI按照A、B、C、D、E和F的顺序进行绘制，每一次绘制对应的都是一个Open GL绘制命令。但是实际上，有些绘制命令是可以进行合并的。例如，ActionBar和Button的背景图，它们使用的都是预加载的Drawable资源，并且这些资源已经合成为一个地图集纹理上传到了GPU去了。如果可以将这两个背景图的绘制合并成一个Open GL命令，那么就可以减少Open GL渲染管线的状态切换次数，从而提高渲染效率。

注意，这种合并操作并不是总能执行的。例如，假设在图15中，介于ActionBar和Button之间应用程序图标不仅与ActionBar重叠，还与Button重叠，那么ActionBar和Button的背景就不可以进行合并绘制。这意味着两个绘制命令是否能够进行合并是由许多因素决定的。

后面我们在分析应用程序窗口的Display List构建和渲染过程就会看到，图15显示的A、B、C、D、E和F绘制操作都是对应一个Draw Op。我们可以将Draw Op看作是一个绘制命令，它们按照前后顺序保存在应用程序窗口的Display List中。为了能够实现合并，这些Draw Op不是马上被执行，而是先通过一个Deferred Display List进行重排，将可以合并的Draw Op先进行合并，然后再对它们进行合并。重排的算法就是依次将原来保存在应用程序窗口的Display List的Draw Op添加到Deferred Display List中去。在添加的过程中，如果发现后一个Draw Op可以与前一个Draw Op进行合并，那么就对它们进行合并。

在重排序和合并后，新的 deferred display list 就可以被绘制到屏幕上了，如下图16。

![img](D:\my-note\android\view\assets\v2-d7c1bb9fe8751d9fc316ef6d62bf3c64_1440w.png)

图16 flush调用流程

如第一章提到的TextView, 3.3.3所创建的displayList渲染所生成的OpenGL命令如下：

```c
1.   glUniformMatrix4fv(location = 2, count = 1, transpose = false, value = [1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0])
2.   glBindBuffer(target = GL_ARRAY_BUFFER, buffer = 0)
3.   glGenBuffers(n = 1, buffers = [3])
4.   glBindBuffer(target = GL_ELEMENT_ARRAY_BUFFER, buffer = 3)
5.   glBufferData(target = GL_ELEMENT_ARRAY_BUFFER, size = 24576, data = [ 24576 bytes ], usage = GL_STATIC_DRAW)
6.   glVertexAttribPointer(indx = 0, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0xbefdcf18)
7.   glVertexAttribPointer(indx = 1, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0xbefdcf20)
8.   glVertexAttribPointerData(indx = 0, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0x??, minIndex = 0, maxIndex = 48)
9.   glVertexAttribPointerData(indx = 1, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0x??, minIndex = 0, maxIndex = 48)
10.  glDrawElements(mode = GL_MAP_INVALIDATE_RANGE_BIT, count = 72, type = GL_UNSIGNED_SHORT, indices = 0x0)
11.  glBindBuffer(target = GL_ARRAY_BUFFER, buffer = 2)
12.  glBufferSubData(target = GL_ARRAY_BUFFER, offset = 768, size = 576, data = [ 576 bytes ])
13.  glDisable(cap = GL_BLEND)
14.  glUniformMatrix4fv(location = 2, count = 1, transpose = false, value = [1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 33.0, 0.0, 1.0])
15.  glVertexAttribPointer(indx = 0, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0x300)
16.  glVertexAttribPointer(indx = 1, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0x308)
17.  glDrawElements(mode = GL_MAP_INVALIDATE_RANGE_BIT, count = 54, type = GL_UNSIGNED_SHORT, indices = 0x0)
18.  eglSwapBuffers()
```

## 4 Android 7.0（Nougat）对于hwui硬件绘制的更新

Android 7.0（Nougat）中又对hwui进行了小规模重构，引入了BakedOpRenderer, FrameBuilder, LayerBuilder, RecordingCanvas等类，用宏HWUI_NEW_OPS管理。下面简单介绍下这些新成员：*[http://blog.csdn.net/jinzhuojun/article/details/54234354](https://link.zhihu.com/?target=http%3A//blog.csdn.net/jinzhuojun/article/details/54234354)*

**RecordingCanvas**: 之前Java层的DisplayListCanvas对应native层的DisplayListCanvas。引入RecordingCanvas后，其在native层的对应物就变成了RecordingCanvas。和DisplayListCanvas类似，画在RecordingCanvas上的内容都会被记录在RenderNode的DisplayList中。

**BakedOpRenderer**: 顾名思义，就是用于绘制batch/merge好的操作。用于替代之前的OpenGLRenderer。它是真正用GL绘制到on-screen surface上的。

**BakedOpDispatcher**: 提供一系列onXXX（如onBitmapOp）和onMergedXXX（如onMergedBitmapOps）静态函数供replay时调用。这些dispatch函数最后一般都会通过GlopBuilder来构造Glop然后通过BakedOpRenderer的renderGlop()函数来用OpenGL绘制。

**LayerBuilder**: 用于存储绘制某一层的操作和状态。替代了部分原DeferredDisplayList的工作。对于所有View通用，即如果View有render layer，它对应一个FBO；如果对于普通View，它对应的是SurfaceFlinger提供的surface。 其中的mBatches存储了当前层defer后（即batch/merge好）的绘制操作。

**FrameBuilder**: 管理某一帧的构建，用于处理，优化和存储从RenderNode和LayerUpdateQueue中来的渲染命令，同时它的replayBakedOps()方法还用于该帧的绘制命令重放。一帧中可能需要绘制多个层，每一层的上下文都会存在相应的LayerBuilder中。在FrameBuilder中通过mLayerBuilders和mLayerStack存储一个layer stack。它替代了原Snapshot类的一部分功能。

**OffscreenBuffer**: 用于替代Layer类，但是设计上更轻量，而且自带内存池（通过OffscreenBufferPool）。

**LayerUpdateQueue**：用于记录类型为硬件绘制层的RenderNode的更新操作。之后会通过FrameBuilder将该layer对应的RenderNode通过deferNodeOps()方法进行处理。

**RecordedOp**: 由RecordedCanvas将View中的绘制命令转化为RecordedOp。RecordedOp也是DisplayList中的基本元素，用于替代Android N之前的DisplayListOp。它有一坨各式各样的继承类代表各种各样的绘制操作。BakedOpState是RecordedOp和相应的状态的自包含封装（封装的过程称为bake）。

**BatchBase**: LayerBuilder中对DisplayList进行batch/merge处理后的结果以BatchBase形式保存在LayerBuilder的mBatches成员中。它有两个继承类分别为OpBatch和MergingOpBatch，分别用于不可合并和可合并操作。

修改后相关类图如下

![img](D:\my-note\android\view\assets\v2-b4fcd792060608100f17c4a35df585f0_1440w.webp)

图17 hwui硬件绘制类图

另外，和Android M相比，N中UI子系统中加入了不少对用户进行窗口resize的处理，主要应该是为了Android N新增加的多窗口分屏模式。比如当用户拖拽分屏窗口边缘时，onWindowDragResizeStart()被调用。它其中会创建BackdropFrameRenderer。BackdropFrameRenderer本身运行单独的线程，它负责在resize窗口而窗口绘制来不及的情况下填充背景。它会通过addRenderNode()加入专用的RenderNode。同时，Android N中将DecorView从PhoneWindow中分离成一个单独的文件，并实现新加的WindowCallbacks接口。它主要用于当用户变化窗口大小时ViewRootImpl对DecorView的回调。因为ViewRootImpl和WindowManagerService通信，它会被通知到窗口变化，然后回调到DecorView中。而DecorView中的相应回调会和BackupdropFrameRenderer交互。如updateContentDrawBounds()中最后会调用到了BackupdropFrmeRenderer的onContentDrawn()函数，其返回值代表在下面的内容绘制后是否需要再发起一次绘制。如果需要，之后会调用requestDrawWindow()。

## 5 总结

UI作为用户体验的核心之一，始终是Android每次升级中的重点，让我们看下hwui硬件加速的升级过程：

**Androd 3.0（Honeycomb）：**Android开始支持hwui（UI硬件加速）；

**Android 4.0（ICS）：**硬件加速被默认开启。同时ICS还引入了DisplayList的概念（不是OpenGL里的那个），它相当于是从View的绘制命令到GL命令之间的“中间语言”。它记录了绘制该View所需的全部信息，之后只要重放（replay）即可完成内容的绘制。这样如果View没有改动或只部分改动，便可重用或修改DisplayList，从而避免调用了一些上层代码，提高了效率。

**Android 4.3（JB）：**引入了DisplayList的defer操作，它主要用于对DisplayList中命令进行Batch（批次）和Merge（合并）。这样可以减少GL draw call和context切换以提高效率。

**Android 5.0（Lollipop）**：

1）引入了RenderNode（渲染节点）的概念，它是对DisplayList及一些View显示属性的进一步封装。代码上，一个View对应一个RenderNode（Native层对应同名类），其中管理着对应的DisplayList和OffscreenBuffer（如果该View为硬件绘制层）。每个向WindowManagerService注册的窗口对应一个RootRenderNode，通过它可以找到View层次结构中所有View的DisplayList信息。在Java层的DisplayListCanvas用于生成DisplayList，其在native层的对应类为RecordingCanvas（在Android N前为DisplayListCanvas）；

2）引入了RenderThread（渲染线程）。所有的GL命令执行都放到这个线程上。渲染线程在RenderNode中存有渲染帧的所有信息，且还监听VSync信号，因此可以独立做一些属性动画。这样即便主线程block也可以保证动画流畅。引入渲染线程后ThreadedRenderer替代了Gl20Renderer，作为proxy用于主线程（UI线程）把渲染任务交给渲染线程

**Android 7.0（Nougat）：**

1）对hwui进行了小规模重构，引入了BakedOpRenderer, FrameBuilder, LayerBuilder, RecordingCanvas等类，用宏HWUI_NEW_OPS管理；

2）针对多窗口做了一些优化。

可以看出一个View上的东西要绘制出来，要经过多步的转化，图18为Android7.0的优化后流程（除了类名，基本与Android6.0类似）：

![img](D:\my-note\android\view\assets\v2-993bb3e9afb725241d23a9889f1047d9_1440w.png)

图18 hwui硬件绘制流程

另一方面，我们也可以看到一些潜力可挖。比如当前可以合并的操作类型有限。另外主线程和渲染线程间的很多调用还是同步的，并行度或许可以进一步提高。另外Vulkan（高性能渲染，下一代图形API以及OpenGL的继承者*https://zhuanlan.zhihu.com/p/20712354*）的引入也可以帮助进一步榨干GPU的能力。



*在文章最后分享一个简单案例（修改硬件渲染参数）：
*

> 背景：
> 在某个项目中camera预览默认都是yuv420出图，通过GPU转成RGB送显，但由于ISP限制，特殊的分辨率下420出图会有概率性绿屏的问题，修改方案是把420出图修改为422出图，修改后实际的效果是预览时出现明显的锯齿（如下图键盘边沿、USB数据线等），拍照是OK的。所以怀疑是GPU转换的时候出现问题。
> 分析：
> 这个问题非常像是GL_NEAREST和GL_LINEAR在缩放时引起的。
> 前者表示“使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色”；后者表示“使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色”。前者只经过简单比较，需要运算较少，可能速度较快；后者需要经过加权平均计算，其中涉及除法运算，可能速度较慢（但如果有专门的处理硬件，也可能两者速度相同）。
> 从视觉效果上看，前者效果较差，在一些情况下锯齿现象明显，后者效果会较好（但如果纹理图象本身比较大，则两者在视觉效果上就会比较接近）。我在测试gl2_yuvtex时二者差距并不大，但在这个预览界面下，确实差距很大。

解决方法：

> diff --git a/libs/hwui/OpenGLRenderer.cpp b/libs/hwui/OpenGLRenderer.cpp
> index 35fc804…dfcedef 100644
> — a/libs/hwui/OpenGLRenderer.cpp
> +++ b/libs/hwui/OpenGLRenderer.cpp
> @@ -1115,7 +1115,8 @@ void OpenGLRenderer::drawTextureLayer(Layer* layer, const Rect& rect) {
> const float x = (int) floorf(rect.left + currentTransform().getTranslateX() + 0.5f);
> const float y = (int) floorf([rect.top](https://link.zhihu.com/?target=http%3A//rect.top) + currentTransform().getTranslateY() + 0.5f);
>
> --- layer->setFilter(GL_NEAREST);
> +++ //layer->setFilter(GL_NEAREST);
> +++ layer->setFilter(GL_LINEAR);
>
> setupDrawModelView(x, y, x + rect.getWidth(), y + rect.getHeight(), true);
> } else {
> layer->setFilter(GL_LINEAR);

## 6 参考

主要框架参照luoshengyang，Android7.0之后代码逻辑新增参考jinzhuojun，很多图片也直接引用了这2个博主，代码则以Android 6.0为准（比较麻烦的地方在于知乎对于代码不太友好，贴上去非常难看，而且无法加粗）。

*[http://blog.csdn.net/jinzhuojun/article/details/54234354](https://link.zhihu.com/?target=http%3A//blog.csdn.net/jinzhuojun/article/details/54234354)*

*[https://blog.csdn.net/luoshengyang/article/details/45601143](https://link.zhihu.com/?target=https%3A//blog.csdn.net/luoshengyang/article/details/45601143)*



编