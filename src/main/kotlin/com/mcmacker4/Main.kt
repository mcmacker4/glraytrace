package com.mcmacker4

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL11.*


fun main() {
    
    GLFWErrorCallback.createPrint(System.err).set()
    
    Window
    
    glClearColor(0.3f, 0.6f, 0.9f, 1.0f)
    
    var last = glfwGetTime()
    var count = 0
    
    while (!Window.shouldClose()) {
        
        val now = glfwGetTime()
        if (now - last > 1) {
            println(count)
            count = 0
            last = now
        }
        
        glfwPollEvents()
        Raytracer.render()
        Window.update()
        
        count++
        
    }
    
    Window.destroy()
    
}