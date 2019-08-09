package com.mcmacker4.gl

import org.lwjgl.opengl.GL15.*


class VBO private constructor(id: Int, private val target: Int) : GLObject(id) {
    
    init { assert(id != 0) }
    
    override fun bind() {
        glBindBuffer(target, id)
        boundBuffers[target] = id
    }
    
    fun write(data: IntArray) {
        //assert(boundBuffers[target] == id)
        bind()
        glBufferData(target, data, GL_STATIC_DRAW)
    }
    
    fun write(data: FloatArray) {
        //assert(boundBuffers[target] == id)
        bind()
        glBufferData(target, data, GL_STATIC_DRAW)
    }

    override fun unbind() {
        glBindBuffer(target, 0)
        boundBuffers[target] = 0
    }

    override fun delete() {
        glDeleteBuffers(id)
        vbos.remove(id)
    }

    companion object {
        
        private val vbos = arrayListOf<Int>()
        private val boundBuffers = hashMapOf<Int, Int>()
        
        fun array(data: FloatArray) =
            VBO(glGenBuffers(), GL_ARRAY_BUFFER).apply {
                bind()
                write(data)
                vbos.add(id)
            }
        
        fun indices(data: IntArray) =
            VBO(glGenBuffers(), GL_ELEMENT_ARRAY_BUFFER).apply {
                bind()
                write(data)
                vbos.add(id)
            }
        
        fun cleanup() {
            vbos.forEach { glDeleteBuffers(it) }
        }
        
    }
    
}
