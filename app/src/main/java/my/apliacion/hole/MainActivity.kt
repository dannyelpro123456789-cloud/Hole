package my.apliacion.hole

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import my.apliacion.hole.core.graphics.TextureUtils
import my.apliacion.hole.game.manager.GameState
import my.apliacion.hole.game.manager.GameStateManager

class MainActivity : AppCompatActivity() {

    private lateinit var gLView: GLSurfaceView
    private var previousX: Float = 0f
    private var previousY: Float = 0f
    
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Forzar orientación horizontal por código
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // 1. Inicializar utilidades
        TextureUtils.init(this)

        // 2. Iniciar música de fondo
        setupBackgroundMusic()

        // 3. Configurar motor gráfico OpenGL
        gLView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(MyGameRenderer())
        }

        // 4. Usar solo el motor gráfico como contenido (Eliminamos el botón nativo duplicado)
        setContentView(gLView)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Float = event.x
        val y: Float = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Si estamos en el menú, cualquier toque inicia el juego
                if (GameStateManager.currentState == GameState.MENU_INICIO) {
                    gLView.queueEvent {
                        // Bridge seguro al motor gráfico
                        GameStateManager.onMenuTouch()
                    }
                    // Detenemos la música aquí también
                    stopBackgroundMusic()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // Si ya estamos en el juego, el movimiento rota la cámara
                if (GameStateManager.currentState != GameState.MENU_INICIO) {
                    val deltaX = x - previousX
                    val deltaY = y - previousY

                    gLView.queueEvent {
                        GameStateManager.onInputRotation(deltaX, deltaY)
                    }
                }
            }
        }

        previousX = x
        previousY = y
        return true
    }

    override fun onResume() {
        super.onResume()
        gLView.onResume()
        if (GameStateManager.currentState == GameState.MENU_INICIO) {
            mediaPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        gLView.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    private fun setupBackgroundMusic() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.horror_menu).apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopBackgroundMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
