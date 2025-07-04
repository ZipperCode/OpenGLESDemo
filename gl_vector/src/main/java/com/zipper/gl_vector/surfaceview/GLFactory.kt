package com.zipper.gl_vector.surfaceview

import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

class GLFactory(
    private val targetEglVersion: Int = 2
) : GLSurfaceView.EGLContextFactory, GLSurfaceView.EGLWindowSurfaceFactory {

    var eglContext: EGLContext = EGL10.EGL_NO_CONTEXT

    var eglSurface: EGLSurface = EGL10.EGL_NO_SURFACE

    override fun createContext(egl: EGL10?, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext? {
        try {
            checkEglError("before createContext", egl)
            val attribLIst = intArrayOf(
                EGL_CONTEXT_CLIENT_VERSION, targetEglVersion,
                EGL10.EGL_NONE
            )
            val context = egl?.eglCreateContext(
                display,
                eglConfig,
                EGL10.EGL_NO_CONTEXT,
                attribLIst
            ) ?: EGL10.EGL_NO_CONTEXT
            checkEglError("after createContext version = $targetEglVersion", egl)
            eglContext = context

        } catch (e: Exception) {
            Log.e("GLFactory", "createContext error: ${Log.getStackTraceString(e)}")
        }
        return eglContext
    }

    override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
        try {
            if (egl?.eglDestroyContext(display, context) == false) {
                Log.e("DefaultContextFactory", "display:$display context: $context")
                Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().id)
            }
        } catch (e: Exception) {
            Log.e("GLFactory", "destroyContext error: ${Log.getStackTraceString(e)}")
        }
        eglContext = EGL10.EGL_NO_CONTEXT
    }

    override fun createWindowSurface(egl: EGL10?, display: EGLDisplay?, config: EGLConfig?, nativeWindow: Any?): EGLSurface? {
        try {
            eglSurface = egl?.eglCreateWindowSurface(display, config, nativeWindow, null) ?: EGL10.EGL_NO_SURFACE
        } catch (e: Exception) {
            Log.e("GLFactory", "createWindowSurface error: ${Log.getStackTraceString(e)}")
        }
        return eglSurface
    }

    override fun destroySurface(egl: EGL10?, display: EGLDisplay?, surface: EGLSurface?) {
        try {
            egl?.eglDestroySurface(display, surface)
        } catch (e: Exception) {
            Log.e("GLFactory", "destroySurface error: ${Log.getStackTraceString(e)}")
        }
        eglSurface = EGL10.EGL_NO_SURFACE
    }


    companion object {

        private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098

        fun checkEglError(prompt: String, egl: EGL10?) {
            var error = egl?.eglGetError()
            if (error != EGL10.EGL_SUCCESS) {
                Log.w("EglError", "$prompt: 0x${error}")
            }
        }
    }
}