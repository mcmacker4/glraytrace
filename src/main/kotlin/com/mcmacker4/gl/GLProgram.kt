package com.mcmacker4.gl

import com.mcmacker4.exceptions.ShaderLinkException
import com.mcmacker4.exceptions.ShaderValidateException
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL20.*
import java.nio.FloatBuffer


class GLProgram private constructor(id: Int) : GLObject(id) {
    
    private val locations = hashMapOf<String, Int>()
    
    fun uniform1i(name: String, i: Int) {
        val loc = getUniformLocation(name).takeIf { it != -1 } ?: return
        glUniform1i(loc, i)
    }

    fun uniform1f(name: String, f: Float) {
        val loc = getUniformLocation(name).takeIf { it != -1 } ?: return
        glUniform1f(loc, f)
    }

    fun uniformVec2(name: String, v: Vector2fc) {
        val loc = getUniformLocation(name).takeIf { it != -1 } ?: return
        glUniform2f(loc, v.x(), v.y())
    }
    
    fun uniformVec3(name: String, v: Vector3fc) {
        val loc = getUniformLocation(name).takeIf { it != -1 } ?: return
        glUniform3f(loc, v.x(), v.y(), v.z())
    }
    
    fun uniformMatrix(name: String, matrix: FloatBuffer) {
        val loc = getUniformLocation(name).takeIf { it != -1 } ?: return
        glUniformMatrix4fv(loc, false, matrix)
    }
    
    fun setTextureIndex(name: String, index: Int) {
        val loc = getUniformLocation(name).takeIf { it != -1 } ?: return
        glUniform1i(loc, index)
    }
    
    private fun getUniformLocation(name: String) : Int {
        return locations.getOrPut(name) {
            glGetUniformLocation(id, name)
        }
    }
    
    override fun bind() {
        glUseProgram(id)
    }

    override fun unbind() {
        glUseProgram(0)
    }

    override fun delete() {
        glDeleteProgram(id)
    }

    companion object {
        
        fun create(vararg shaders: GLShader) : GLProgram {
            val id = glCreateProgram()
            for (shader in shaders) {
                glAttachShader(id, shader.id)
            }
            
            glLinkProgram(id)
            if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE)
                throw ShaderLinkException(glGetProgramInfoLog(id))
            
            glValidateProgram(id)
            if (glGetProgrami(id, GL_VALIDATE_STATUS) == GL_FALSE)
                throw ShaderValidateException(glGetProgramInfoLog(id))
            
            return GLProgram(id)
        }

        fun load(name: String) : GLProgram {
            return create(
                GLShader.loadVertex(name),
                GLShader.loadFragment(name)
            )
        }
        
    }
    
}