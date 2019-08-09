package com.mcmacker4.gl

import org.lwjgl.opengl.GL30.*


class VAO private constructor(id: Int) : GLObject(id) {
    
    val attributes = hashMapOf<Int, VBO>()
    
    override fun bind() {
        if (id == 0) throw Exception("Binding VAO with id 0!")
        glBindVertexArray(id)
    }

    override fun unbind() {
        glBindVertexArray(0)
    }
    
    fun bindAttribute(index: Int, size: Int, type: Int, buffer: VBO) {
        if (id == 0) throw Exception("Binding attribute to VAO with id 0!")
        buffer.bind()
        glVertexAttribPointer(index, size, type, false, 0, 0)
        glEnableVertexAttribArray(index)
        attributes[index] = buffer
    }
    
    override fun delete() {
        if (id == 0) throw Exception("Deleting VAO with id 0!")
        glDeleteVertexArrays(id)
        vaos.remove(id)
    }

    companion object {
        
        val EMPTY = VAO(0)
        
        const val POSITIONS = 0
        const val NORMALS = 1
        const val TEXTURE_UVS = 2
        
        private val vaos = arrayListOf<Int>()
        
        fun create() : VAO = glGenVertexArrays().let {
            vaos.add(it)
            return VAO(it)
        }
        
        fun cleanup() {
            vaos.forEach { glDeleteVertexArrays(it) }
        }
        
    }
    
}