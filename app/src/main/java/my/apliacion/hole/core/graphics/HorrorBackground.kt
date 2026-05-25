package my.apliacion.hole.core.graphics

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class HorrorBackground {

    private val vertexShaderCode = """
        uniform mat4 u_MVPMatrix;
        attribute vec4 a_Position;
        attribute vec2 a_TexCoord;
        varying vec2 v_TexCoord;
        void main() {
            gl_Position = u_MVPMatrix * a_Position;
            v_TexCoord = a_TexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform sampler2D u_Texture;
        uniform float u_Time;
        uniform float u_Alpha;
        varying vec2 v_TexCoord;

        void main() {
            vec4 texColor = texture2D(u_Texture, v_TexCoord);
            
            // Si la imagen no está, usamos un rojo sangre muy oscuro
            vec3 color = (texColor.a > 0.0) ? texColor.rgb : vec3(0.1, 0.0, 0.0);

            // 1. Lógica de Ojos: Se CIERRAN y ABREN cada 3 segundos
            // Usamos una onda seno lenta para que el cierre sea suave
            float eyeCycle = sin(u_Time * (3.14159 / 1.5)); // Ciclo completo cada 3s
            float eyeOpenAmount = smoothstep(-0.5, 0.5, eyeCycle); 
            
            // Detectar píxeles de ojos (puntos brillantes amarillentos)
            float distToCenter = distance(v_TexCoord, vec2(0.5, 0.5));
            bool isEyePixel = (texColor.r > 0.4 && texColor.g > 0.3 && texColor.b < 0.6);
            
            // Solo los ojos de los lados (distancia > 0.12) obedecen al ciclo
            if (isEyePixel && distToCenter > 0.12) {
                color *= eyeOpenAmount; 
            }

            // 2. Parpadeo de la LUZ (Flicker eléctrico)
            // Combinamos frecuencias para que sea errático
            float lightFlicker = 0.6 + 0.4 * step(0.1, sin(u_Time * 60.0) * cos(u_Time * 15.0));
            float distToLight = distance(v_TexCoord, vec2(0.5, 0.3));
            if (distToLight < 0.3) {
                color *= lightFlicker;
            }

            // 3. Post-procesado Lo-Fi (Grano y Scanlines)
            float grain = fract(sin(dot(v_TexCoord + u_Time, vec2(12.9898, 78.233))) * 43758.5453) * 0.08;
            float scanline = sin(v_TexCoord.y * 700.0) * 0.03;
            
            gl_FragColor = vec4((color + grain) - scanline, u_Alpha);
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

    fun draw(textureId: Int, time: Float, mvpMatrix: FloatArray, alpha: Float = 1.0f) {
        GLES20.glUseProgram(program)
        
        val mvpHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

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
