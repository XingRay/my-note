# Embed OpenGL inside Java AWT Canvas

This article shows how to use OpenGL calls inside Java AWT Canvas using JDK1.3's JAWT interface



[Download source and demo files - 6 Kb](https://www.codeproject.com/KB/openGL/opengl/opengl.zip)

![Sample Image - opengl.gif](D:\my-note\opengl\java\assets\opengl.gif)

## Introduction

This article provides a skeleton for adding OpenGL code to your Java Applications. The sample project shows how to run an OpenGL animation inside your AWT Canvas Widget. This is possible by the new JNi-based JAWT interface in the latest JDK1.3.

There are two major problems that needed to be solved to be able to do this.

Problem #1 - Get the HWND of a Java AWT Canvas.

Problem #2 - How to use OpenGL calls to draw on this HWND.

Problem #1 was solved by using a documented API present JDK 1.3. Here's the URL which explains JAWT interface and how to use it.

http://java.sun.com/j2se/1.3/docs/guide/awt/AWT_Native_Interface.html

Here's an extract from the project which shows the implementation.

Java

Shrink ▲  

```java
// Helper class for accessing JAWT Information.
class JAWT_Info 
{
private:
    JAWT awt;
    JAWT_DrawingSurface* ds;
    JAWT_DrawingSurfaceInfo* dsi;
    JAWT_Win32DrawingSurfaceInfo* dsi_win;
public:
    JAWT_Info(JNIEnv *env, jobject panel)
    {
        jboolean result;
        jint lock;

        // Get the AWT
        awt.version = JAWT_VERSION_1_3;
        result = JAWT_GetAWT(env, &awt);
        assert(result != JNI_FALSE);
        // Get the drawing surface
        ds = awt.GetDrawingSurface(env, panel);
        if(ds == NULL)
            return;
        // Lock the drawing surface
        lock = ds->Lock(ds);
        assert((lock & JAWT_LOCK_ERROR) == 0);

        // Get the drawing surface info
        dsi = ds->GetDrawingSurfaceInfo(ds);

        // Get the platform-specific drawing info
        dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
    }
    HWND getHWND()
    {
        if(dsi_win == NULL)
            return NULL;
        return dsi_win->hwnd;
    }
    HDC getHDC()
    {
        if(dsi_win == NULL)
            return NULL;
        return dsi_win->hdc;
    }
    virtual ~JAWT_Info()
    {
        if(ds != NULL)
        {
            // Free the drawing surface info
            ds->FreeDrawingSurfaceInfo(dsi);
            // Unlock the drawing surface
            ds->Unlock(ds);
            // Free the drawing surface
            awt.FreeDrawingSurface(ds);
        }
    }
};
```

Problem #2 was straight forward once you get the HWND. Here's an extract from the project which shows how to initiailize OpenGL.

Java

Shrink ▲  

```java
// Static variables for the OpenGL calls.
static HGLRC    hRC = NULL;
static HDC      hDC = NULL;

/*
 * Class:     MyWindow
 * Method:    initializeOpenGL
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_MyWindow_initializeOpenGL
  (JNIEnv *env, jobject panel)
{
    // get the window handle
    JAWT_Info info(env, panel);
    HWND hWnd = (HWND)info.getHWND();

    if(hWnd == NULL)
        return;

    PIXELFORMATDESCRIPTOR pfd;
    int iFormat;

    // get the device context (DC)
    HWND hwnd = info.getHWND();
    hDC = ::GetDC(hwnd);

    // set the pixel format for the DC
    ::ZeroMemory( &pfd, sizeof( pfd ) );
    pfd.nSize = sizeof( pfd );
    pfd.nVersion = 1;
    pfd.dwFlags = PFD_DRAW_TO_WINDOW | 
    PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER;
    pfd.iPixelType = PFD_TYPE_RGBA;
    pfd.cColorBits = 24;
    pfd.cDepthBits = 16;
    pfd.iLayerType = PFD_MAIN_PLANE;
    iFormat = ::ChoosePixelFormat( hDC, &pfd );
    ::SetPixelFormat( hDC, iFormat, &pfd );

    // create and enable the render context (RC)
    hRC = ::wglCreateContext( hDC );
    ::wglMakeCurrent( hDC, hRC );
}
```

Here's an extract from the project which shows how to paint using OpenGL.

Java

Shrink ▲  

```java
/*
 * Class:     MyWindow
 * Method:    paint
 * Signature: (Ljava/awt/Graphics;)V
 */
JNIEXPORT void JNICALL Java_MyWindow_paintOpenGL
  (JNIEnv *env, jobject panel)
{
    static float theta = 0.0f;
    // get the window handle
    JAWT_Info info(env, panel);
    HWND hWnd = (HWND)info.getHWND();

    if(hWnd == NULL)
        return;

    // OpenGL animation code goes here
    ::glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
    ::glClear( GL_COLOR_BUFFER_BIT );

    ::glPushMatrix();
    ::glRotatef( theta, 0.0f, 0.0f, 1.0f );
    ::glBegin( GL_TRIANGLES );
    ::glColor3f( 1.0f, 0.0f, 0.0f ); glVertex2f( 0.0f, 1.0f );
    ::glColor3f( 0.0f, 1.0f, 0.0f ); glVertex2f( 0.87f, -0.5f );
    ::glColor3f( 0.0f, 0.0f, 1.0f ); glVertex2f( -0.87f, -0.5f );
    ::glEnd();
    ::glPopMatrix();

    ::SwapBuffers( hDC );

    theta += 1.0f;
}
```

Once we are finished we here is the code for cleanup.

Java



```java
/*
 * Class:     MyWindow
 * Method:    cleanupOpenGL
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_MyWindow_cleanupOpenGL
(JNIEnv *env, jobject panel)
{
    // get the window handle
    JAWT_Info info(env, panel);
    HWND hWnd = (HWND)info.getHWND();
    if(hWnd == NULL)
        return;

    ::wglMakeCurrent( NULL, NULL );
    ::wglDeleteContext( hRC );
    ::ReleaseDC( hWnd, hDC );
}
```

This zip file above contains all the source code for this article, as well as a batch files to build and run the code.

**Resources**

- JNI FAQ at jGuru.com
  http://www.jguru.com/faq/JNI
- Java Tip 86: Support native rendering in JDK 1.3
  http://www.javaworld.com/javaworld/javatips/jw-javatip86.html
- Sun's introduction to the AWT Native Interface:
  [hhttp://java.sun.com/j2se/1.3/docs/guide/awt/AWT_Native_Interface.html](http://java.sun.com/j2se/1.3/docs/guide/awt/AWT_Native_Interface.html)
- Sun on AWT enhancements in the Java 2 SDK, version 1.3:
  http://java.sun.com/products/jdk/1.3/docs/guide/awt/enhancements.html
- Sun on native drawing JAWT interface improvements, post-Kestrel (free registration required):
  http://developer.java.sun.com/developer/bugParade/bugs/4281429.html
- "Enhance your Java application with Java Native Interface (JNI)," Tal Lyron (*JavaWorld,* October 1999):
  http://www.javaworld.com/javaworld/jw-10-1999/jw-10-jni.html
- "Java Tip 23: Write native methods," John D. Mitchell *(JavaWorld):*
  http://www.javaworld.com/javaworld/javatips/jw-javatip23.html

## License

This article has no explicit license attached to it but may contain usage terms in the article text or the download files themselves. If in doubt please contact the author via the discussion board below.

A list of licenses authors might use can be found [here](https://www.codeproject.com/info/Licenses.aspx)