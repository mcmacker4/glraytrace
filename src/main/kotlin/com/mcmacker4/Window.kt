package com.mcmacker4

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Callback
import org.lwjgl.system.MemoryUtil.NULL
import java.lang.RuntimeException


object Window {

    const val width = 1280
    const val height = 720
    
    private val glfwWindow: Long
    
    private val debugProc: Callback?
    
    init {
        if (!glfwInit())
            throw IllegalStateException("Could not initialize GLFW.")
        
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        
        
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6)
        
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)
        
        glfwWindow = glfwCreateWindow(width, height, "Raytracing", NULL, NULL)
        if (glfwWindow == NULL)
            throw RuntimeException("Could not create GLFW window.")
        
        glfwSetKeyCallback(glfwWindow) { _, key, _, action, _ ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(glfwWindow, true)
        }
        
        glfwMakeContextCurrent(glfwWindow)
        GL.createCapabilities()
        
        debugProc = GLUtil.setupDebugMessageCallback()

        glfwSwapInterval(0)
        
        println(glGetString(GL_VENDOR))
        println(glGetString(GL_VERSION))
        
        glfwShowWindow(glfwWindow)
    }
   
    fun update() {
        glfwSwapBuffers(glfwWindow)
    }
    
    fun destroy() {
        debugProc?.free()
        glfwDestroyWindow(glfwWindow)
    }
    
    fun shouldClose() : Boolean {
        return glfwWindowShouldClose(glfwWindow)
    }
    
}