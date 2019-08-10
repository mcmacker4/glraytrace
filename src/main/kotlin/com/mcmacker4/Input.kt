package com.mcmacker4

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.*


object Input {
    
    private var mouseListener: ((Double, Double) -> Unit)? = null
    private var mxpos: Double = 0.0
    private var mypos: Double = 0.0
    
    init {
        MemoryStack.stackPush().use {
            val xbuff = it.mallocDouble(1)
            val ybuff = it.mallocDouble(1)
            glfwGetCursorPos(Window.glfwWindow, xbuff, ybuff)
            mxpos = xbuff.get()
            mypos = ybuff.get()
        }
    }
    
    fun isKeyDown(key: Int): Boolean {
        return glfwGetKey(Window.glfwWindow, key) == GLFW_PRESS
    }
    
    fun emitMouseEvent(xpos: Double, ypos: Double) {
        mouseListener?.let { listener ->
            val dx = xpos - mxpos
            mxpos = xpos
            val dy = ypos - mypos
            mypos = ypos
            listener(dx, dy)
        }
    }
    
    fun setMouseListener(listener: ((Double, Double) -> Unit)?) {
        mouseListener = listener
    }
    
}