package com.mcmacker4

import com.mcmacker4.gl.*
import java.util.Random
import org.joml.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.*
import kotlin.math.*

object Raytracer {
    
    private val samples = Vector2i(4, 4)
    
    private val ssTexture = GLTexture.create2D(Window.width * samples.x, Window.height * samples.y)
    private val avgTexture = GLTexture.create2D(Window.width, Window.height)
    private val environment = GLTexture.load2D("environ.png")

    private val marcherShader = GLProgram.create(GLShader.loadCompute("marcher"))
    private val averageShader = GLProgram.create(GLShader.loadCompute("average"))

    private val marcherWorkGroupSize = MemoryStack.stackPush().use {
        val buff = it.mallocInt(3)
        glGetProgramiv(marcherShader.id, GL_COMPUTE_WORK_GROUP_SIZE, buff)
        Vector2i(buff.get(0), buff.get(1))
    }
    
    private val averageWorkGroupSize = MemoryStack.stackPush().use {
        val buff = it.mallocInt(3)
        glGetProgramiv(averageShader.id, GL_COMPUTE_WORK_GROUP_SIZE, buff)
        Vector2i(buff.get(0), buff.get(1))
    }
    
    private val drawShader = GLProgram.create(
        GLShader.loadVertex("quad"),
        GLShader.loadFragment("quad")
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
    
    private val rand = Random()
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
        
        marcherShader.bind()
        glActiveTexture(GL_TEXTURE0)
        glBindImageTexture(0, ssTexture.id, 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F)
        glActiveTexture(GL_TEXTURE1)
        environment.bind()
        marcherShader.uniformVec3("camera.position", camera.position)
        marcherShader.uniform1f("camera.aspect", Window.width.toFloat() / Window.height)
        spheres.forEachIndexed { i, sphere ->
            sphere.position.x = sin(glfwGetTime().toFloat() + sphere.f) * 4f
            marcherShader.uniformVec3("spheres[$i].position", sphere.position)
            marcherShader.uniform1f("spheres[$i].radius", sphere.radius)
            marcherShader.uniformVec3("spheres[$i].color", sphere.color)
        }
        glDispatchCompute(nextPowerOfTwo(Window.width * samples.x) / marcherWorkGroupSize.x(),  nextPowerOfTwo(Window.height * samples.y) / marcherWorkGroupSize.y(), 1)
        environment.unbind()
        glActiveTexture(GL_TEXTURE0)
        glBindImageTexture(0, 0, 0, false, 0, GL_READ_WRITE, GL_RGBA32F)
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
        marcherShader.unbind()
        
        averageShader.bind()
        glBindImageTexture(0, ssTexture.id, 0, false, 0, GL_READ_ONLY, GL_RGBA32F)
        glBindImageTexture(1, avgTexture.id, 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F)
        averageShader.uniformVec2i("samples", samples)
        glDispatchCompute(nextPowerOfTwo(Window.width) / averageWorkGroupSize.x(),  nextPowerOfTwo(Window.height) / averageWorkGroupSize.y(), 1)
        glBindImageTexture(1, 0, 0, false, 0, GL_READ_WRITE, GL_RGBA32F)
        glBindImageTexture(0, 0, 0, false, 0, GL_READ_WRITE, GL_RGBA32F)
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
        averageShader.unbind()
        
        drawShader.bind()
        vao.bind()
        glActiveTexture(GL_TEXTURE0)
        avgTexture.bind()
        glDrawArrays(GL_TRIANGLES, 0, 6)
        avgTexture.unbind()
        vao.unbind()
        drawShader.unbind()
        
    }
    
}