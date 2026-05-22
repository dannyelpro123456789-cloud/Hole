package my.apliacion.hole.core.graphics

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class HorrorBackground {

    private val vertexShaderCode = """
        attribute vec4 a_Position;
        attribute vec2 a_TexCoord;
        varying vec2 v_TexCoord;
        void main() {
            gl_Position = a_Position;
            v_TexCoord = a_TexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform sampler2D u_Texture;
        uniform float u_Time;
        uniform float u_Alpha;
        varying vec2 v_TexCoord;

        // Función pseudo-aleatoria para el grano de película
        float noise(vec2 uv) {
            return fract(sin(dot(uv.xy + u_Time, vec2(12.9898, 78.233))) * 43758.5453);
        }

        void main() {
            vec4 color = texture2D(u_Texture, v_TexCoord);

            // 1. Grano de Película (Optimizado)
            float grain = fract(sin(dot(v_TexCoord + u_Time, vec2(12.9898, 78.233))) * 43758.5453) * 0.1;
            
            // 2. Scanlines sutiles
            float scanline = sin(v_TexCoord.y * 500.0) * 0.02;
            
            vec3 finalRGB = (color.rgb + grain) - scanline;
            
            // 3. Flicker y Alpha
            float flicker = 0.95 + 0.05 * sin(u_Time * 5.0);
            gl_FragColor = vec4(finalRGB * flicker, u_Alpha);
        }
    """.trimIndent()

    private var program: Int = 0
    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer

    init {
        val vertices = floatArrayOf(-1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices); position(0) }
        
        val texCoords = floatArrayOf(0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(texCoords); position(0) }

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(textureId: Int, time: Float, alpha: Float = 1.0f) {
        GLES20.glUseProgram(program)
        
        val posHandle = GLES20.glGetAttribLocation(program, "a_Position")
        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        val texHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
        GLES20.glEnableVertexAttribArray(texHandle)
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)

        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_Time"), time)
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_Alpha"), alpha)
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "u_Texture"), 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun loadShader(type: Int, code: String): Int = GLES20.glCreateShader(type).also {
        GLES20.glShaderSource(it, code)
        GLES20.glCompileShader(it)
    }
}
