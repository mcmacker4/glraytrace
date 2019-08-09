package com.mcmacker4.gl


abstract class GLObject(val id: Int) {
    
    abstract fun bind()
    abstract fun unbind()
    
    abstract fun delete()
    
}