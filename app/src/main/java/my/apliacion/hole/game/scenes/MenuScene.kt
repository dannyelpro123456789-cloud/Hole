package my.apliacion.hole.game.scenes

import android.opengl.GLES20
import android.opengl.Matrix
import android.os.SystemClock
import my.apliacion.hole.core.graphics.HorrorBackground
import my.apliacion.hole.core.graphics.SimplePlane
import my.apliacion.hole.core.graphics.TextureUtils
import my.apliacion.hole.game.base.Scene
import my.apliacion.hole.game.manager.GameStateManager

class MenuScene : Scene {

    private val background = HorrorBackground()
    private val startButton = SimplePlane()
    private val bgMatrix = FloatArray(16)
    private val uiMatrix = FloatArray(16)
    
    private var pasilloTextureId: Int = -1
    private var textTextureId: Int = -1
    private val startTime = SystemClock.uptimeMillis()
    
    private var isStarting = false
    private var fadeAlpha = 1.0f

    override fun update(deltaTime: Float) {
        if (isStarting) {
            fadeAlpha -= deltaTime * 1.5f
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f
                GameStateManager.startGame()
            }
        }
    }

    override fun draw(viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        if (pasilloTextureId == -1) {
            pasilloTextureId = TextureUtils.loadTextureFromAssets("menu_horror.png")
        }
        if (textTextureId == -1) {
            textTextureId = TextureUtils.createTextTexture("START")
        }

        val currentTime = (SystemClock.uptimeMillis() - startTime) / 1000f
        
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // 1. Dibujar FONDO (Ocupa toda la pantalla)
        Matrix.setIdentityM(bgMatrix, 0)
        background.draw(pasilloTextureId, currentTime, bgMatrix, fadeAlpha)

        // 2. Dibujar TEXTO START (Centrado y Horizontal)
        Matrix.setIdentityM(uiMatrix, 0)
        
        // PARPADEO LENTO (Estilo VHS): Oscila entre 0.4 y 1.0
        val textBlink = (kotlin.math.sin(currentTime * 3.0f) * 0.3f + 0.7f)
        val finalAlpha = textBlink * fadeAlpha

        // ESCALA CORREGIDA: Reducimos el ancho para compensar el modo horizontal
        // y que las letras no se vean estiradas.
        Matrix.scaleM(uiMatrix, 0, 0.35f, 0.2f, 1.0f)
        
        startButton.draw(uiMatrix, textTextureId, finalAlpha)

        GLES20.glDisable(GLES20.GL_BLEND)
    }

    fun onStartTouched() {
        isStarting = true
    }

    override fun onPause() {}
    override fun onResume() {}
}
