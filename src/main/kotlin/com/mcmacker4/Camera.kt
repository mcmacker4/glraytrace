package com.mcmacker4

import org.joml.*
import org.lwjgl.glfw.*
import org.lwjgl.system.*
import java.nio.*
import kotlin.math.*


class Camera(val position: Vector3f, val rotation: Vector3f) {

    private val speed = 5f
    private val sensitivity = 0.003f
    private val vmBuffer = MemoryUtil.memAllocFloat(4*4)
    
    init {
        
        Input.setMouseListener { x, y ->
            rotation.apply {
                rotation.y -= x.toFloat() * sensitivity
                rotation.x -= y.toFloat() * sensitivity
                rotation.x = rotation.x.clamp(-PI.toFloat() / 2, PI.toFloat() / 2)
            }
        }
        
    }
    
    fun update(delta: Float) {
        val direction = Vector3f()
        if (Input.isKeyDown(GLFW.GLFW_KEY_A) && !Input.isKeyDown(GLFW.GLFW_KEY_D))
            direction.x = -1f
        else if (Input.isKeyDown(GLFW.GLFW_KEY_D) && !Input.isKeyDown(GLFW.GLFW_KEY_A))
            direction.x = 1f
        if (Input.isKeyDown(GLFW.GLFW_KEY_W) && !Input.isKeyDown(GLFW.GLFW_KEY_S))
            direction.z = -1f
        else if (Input.isKeyDown(GLFW.GLFW_KEY_S) && !Input.isKeyDown(GLFW.GLFW_KEY_W))
            direction.z = 1f
        if (Input.isKeyDown(GLFW.GLFW_KEY_SPACE) && !Input.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
            direction.y = 1f
        else if (Input.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && !Input.isKeyDown(GLFW.GLFW_KEY_SPACE))
            direction.y = -1f

        position += direction.rotateY(rotation.y) * speed * delta
    } 
    
    fun getViewMatrix(): FloatBuffer {
        return Matrix4f().apply {
            identity()
            rotateY(rotation.y)
            rotateX(rotation.x)
        }.get(vmBuffer)
    }


}