package com.mcmacker4

import com.mcmacker4.gl.*
import java.util.Random
import org.joml.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.*
import kotlin.math.*

object Raytracer {
    
    const val width = 1280
    const val height = 720
    
    private val texture = GLTexture.create2D(width, height)

    private val computeShader = GLProgram.create(
        GLShader.loadCompute("shader")
    )

    private val workGroupSize = MemoryStack.stackPush().use {
        val buff = it.mallocInt(3)
        glGetProgramiv(computeShader.id, GL_COMPUTE_WORK_GROUP_SIZE, buff)
        Vector2i(buff.get(0), buff.get(1))
    }
    
    private val drawShader = GLProgram.create(
        GLShader.loadVertex("shader"),
        GLShader.loadFragment("shader")
    )
    
    private val vao = VAO.create().apply {
        bind()
        bindAttribute(0, 2, GL_FLOAT, VBO.array(floatArrayOf(
            -1f, 1f,
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, -1f,
            1f, 1f
        )))
        unbind()
    }
    
    class Sphere(val position: Vector3f, val radius: Float, val color: Vector3f, val f: Float)
    class Camera(val position: Vector3f)
    
    private val rand = Random(2)
    private val spheres = (0 until 10).map {
        Sphere(
            Vector3f(rand.nextFloat() * 8f - 4f, rand.nextFloat() * 3 - 1.5f, -(rand.nextFloat() * 10 + 2)),
            rand.nextFloat() * 0.5f + 0.5f,
            Vector3f(rand.nextFloat() * 0.7f + 0.3f, rand.nextFloat() * 0.7f + 0.3f, rand.nextFloat() * 0.7f + 0.3f),
            rand.nextFloat() * 4f
        )
    }
    
    private val camera = Camera(Vector3f(0f))
    
    private fun nextPowerOfTwo(n: Int) : Int {
        var x = n - 1
        x = x or (x shr 1)
        x = x or (x shr 2)
        x = x or (x shr 4)
        x = x or (x shr 8)
        x = x or (x shr 16)
        return x + 1
    }
    
    fun render() {
        
        computeShader.bind()
        glBindImageTexture(0, texture.id, 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F)
        computeShader.uniformVec3("camera.position", camera.position)
        computeShader.uniform1f("camera.aspect", width.toFloat() / height)
        spheres.forEachIndexed { i, sphere ->
            sphere.position.x = sin(glfwGetTime().toFloat() + sphere.f) * 4f
            computeShader.uniformVec3("spheres[$i].position", sphere.position)
            computeShader.uniform1f("spheres[$i].radius", sphere.radius)
            computeShader.uniformVec3("spheres[$i].color", sphere.color)
        }
        glDispatchCompute(nextPowerOfTwo(width) / workGroupSize.x(),  nextPowerOfTwo(height) / workGroupSize.y(), 1)
        glBindImageTexture(0, 0, 0, false, 0, GL_READ_WRITE, GL_RGBA32F)
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
        computeShader.unbind()
        
        drawShader.bind()
        vao.bind()
        texture.bind()
        glDrawArrays(GL_TRIANGLES, 0, 6)
        texture.unbind()
        vao.unbind()
        drawShader.unbind()
        
    }
    
}