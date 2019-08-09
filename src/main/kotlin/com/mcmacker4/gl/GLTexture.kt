package com.mcmacker4.gl

import com.mcmacker4.exceptions.STBLoadException
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_RGBA32F
import org.lwjgl.stb.STBImage.stbi_failure_reason
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_info_from_memory
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer


class GLTexture constructor(id: Int, val target: Int) : GLObject(id) {

    override fun bind() {
        glBindTexture(target, id)
    }

    override fun unbind() {
        glBindTexture(target, 0)
    }

    override fun delete() {
        glDeleteTextures(id)
    }

    companion object {
        
        private data class ImageData(val buffer: ByteBuffer, val width: Int, val height: Int)
        
        fun create2D(width: Int, height: Int) : GLTexture {
            val id = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, id)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, null as ByteBuffer?)
            glBindTexture(GL_TEXTURE_2D, 0)
            return GLTexture(id, GL_TEXTURE_2D)
        }
        
        fun load1D(path: String): GLTexture {
            val id = glGenTextures()
            glBindTexture(GL_TEXTURE_1D, id)

            glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

            val image = loadImage(path)
            
            glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, image.width, 0, GL_RGBA, GL_UNSIGNED_BYTE, image.buffer)
            
            stbi_image_free(image.buffer)
            return GLTexture(id, GL_TEXTURE_1D)
        }

        fun load2D(path: String): GLTexture {
            val id = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, id)

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            
            val image = loadImage(path)

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
                image.width, image.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image.buffer)
            
            stbi_image_free(image.buffer)
            return GLTexture(id, GL_TEXTURE_2D)
        }
        
        private fun loadImage(path: String) : ImageData {
            return GLTexture::class.java.getResourceAsStream("/textures/$path").use { file ->
                val fileArray = file.readBytes()
                val fileBuffer = MemoryUtil.memAlloc(fileArray.size)
                fileBuffer.put(fileArray).flip()

                if (!stbi_info_from_memory(fileBuffer, IntArray(1), IntArray(1), IntArray(1))) {
                    MemoryUtil.memFree(fileBuffer)
                    throw STBLoadException("STB Info error: ${stbi_failure_reason() ?: "unknown"}")
                } else {
                    return MemoryStack.stackPush().use { stack ->
                        val xbuff = stack.mallocInt(1)
                        val ybuff = stack.mallocInt(1)
                        val cbuff = stack.mallocInt(1)

                        val imageData = stbi_load_from_memory(fileBuffer, xbuff, ybuff, cbuff, 4)

                        if (imageData == null) {
                            MemoryUtil.memFree(fileBuffer)
                            throw STBLoadException("STB could not load image from memory.")
                        }
                        
                        MemoryUtil.memFree(fileBuffer)
                        return ImageData(imageData, xbuff.get(), ybuff.get())
                    }
                }
            }
        }
        
    }

}