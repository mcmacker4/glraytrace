package com.mcmacker4

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL11.*


fun main() {
    
    GLFWErrorCallback.createPrint(System.err).set()
    
    if (!glfwInit())
        throw IllegalStateException("Could not initialize GLFW.")
    
    val window = Window(Raytracer.width, Raytracer.height)
    
    glClearColor(0.3f, 0.6f, 0.9f, 1.0f)
    
    while (!window.shouldClose()) {
        glfwPollEvents()
        Raytracer.render()
        window.update()
    }
    
    window.destroy()
    
}