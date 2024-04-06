package com.zipper.egl_fbo

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface

class ImageEGLSurface {

    enum class EglState {
        // 无效
        Invalid,

        // 初始化
        Initialized,

        // 创建完成
        Created
    }

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY

    private var eglConfig: EGLConfig? = null

    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT

    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    private var render: Renderer? = null

    var eglState = EglState.Invalid
        private set

    init {
        createEGLEnv()
    }

    fun setRender(render: Renderer) {
        this.render = render
        createEGLEnv()
    }

    fun requestRender() {
        if (eglState == EglState.Invalid) {
            return
        }
        if (eglState == EglState.Initialized) {
            render?.surfaceCreated()
            eglState = EglState.Created
        }
        if (eglState == EglState.Created) {
            render?.onDrawFrame()
        }
    }

    fun destroy() {
        destroyEGLEnv()
    }

    // =============================== EGL =============================

    private fun createEGLEnv() {
        createEGLDisplay()
        createEGLConfig()
        createEGLContext()
        createEGLSurface()
        makeCurrent()
        if (eglDisplay == EGL14.EGL_NO_DISPLAY
            || eglConfig == null
            || eglContext == EGL14.EGL_NO_CONTEXT
            || eglSurface == EGL14.EGL_NO_SURFACE
        ) {
            eglState = EglState.Invalid
        }
    }

    private fun destroyEGLEnv() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            // 解绑显示
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            // 销毁EglSurface
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            // 销毁上下文
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            // 销毁显示
            EGL14.eglTerminate(eglDisplay)
        }
        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglConfig = null
        eglContext = EGL14.EGL_NO_CONTEXT
        eglSurface = EGL14.EGL_NO_SURFACE
        eglState = EglState.Invalid
    }

    /**
     * 1、创建EGLDisplay
     */
    private fun createEGLDisplay() {
        // 1、创建EGLDisplay
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val versions = IntArray(2)
        val success = EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1)
        if (!success) {
            eglDisplay = EGL14.EGL_NO_DISPLAY
        }
    }

    /**
     * 2、创建EGLConfig
     */
    private fun createEGLConfig() {
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            return
        }
        // 2、创建EGLConfig
        val configSpec = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )
        val numConfigs = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)
        EGL14.eglChooseConfig(eglDisplay, configSpec, 0, configs, 0, configs.size, numConfigs, 0)
        if (numConfigs[0] > 0) {
            eglConfig = configs[0]
        }
    }

    /**
     * 3、创建EGLContext
     */
    private fun createEGLContext() {
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            return
        }
        if (eglConfig == null) {
            return
        }
        // 3、创建EGLContext
        val contextSpec = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextSpec, 0)
    }

    /**
     * 4、创建EGLSurface
     */
    private fun createEGLSurface() {
        // 4、创建EGLSurface
        if (eglContext != EGL14.EGL_NO_CONTEXT) {
            val surfaceAttributes = intArrayOf(
                EGL14.EGL_WIDTH, EGL_SURFACE_WIDTH,
                EGL14.EGL_HEIGHT, EGL_SURFACE_HEIGHT,
                EGL14.EGL_NONE
            )
            eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttributes, 0)
        }
    }

    /**
     * 5、绑定EGLContext到当前线程显示设备
     */
    private fun makeCurrent() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
            eglState = EglState.Initialized
        }
    }


    // =============================== EGL =============================


    companion object{
        const val EGL_SURFACE_WIDTH = 1
        const val EGL_SURFACE_HEIGHT = 1
    }

    interface Renderer {

        fun surfaceCreated()

        fun onDrawFrame()
    }
}