package my.apliacion.hole.core.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLUtils
import java.io.IOException
import java.lang.ref.WeakReference

object TextureUtils {
    private var contextRef: WeakReference<Context>? = null

    fun init(context: Context) {
        contextRef = WeakReference(context)
    }

    /**
     * Carga una textura desde la carpeta assets
     */
    fun loadTextureFromAssets(fileName: String): Int {
        val context = contextRef?.get() ?: return -1
        
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        if (textureIds[0] == 0) return 0

        val options = BitmapFactory.Options().apply { inScaled = false }
        
        try {
            val inputStream = context.assets.open(fileName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                android.util.Log.e("OpenGL", "Error: El archivo assets/$fileName no es una imagen válida")
                return 0
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()

        } catch (e: IOException) {
            android.util.Log.e("OpenGL", "ERROR CRÍTICO: No se encontró el archivo '$fileName' en la carpeta assets.")
            android.util.Log.e("OpenGL", "Asegúrate de copiar tu imagen a Hole/app/src/main/assets/")
            return 0
        }

        return textureIds[0]
    }

    fun createTextTexture(text: String): Int {
        val bitmap = Bitmap.createBitmap(512, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        bitmap.eraseColor(Color.TRANSPARENT)

        val paint = Paint().apply {
            color = Color.RED
            textSize = 100f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(10f, 0f, 0f, Color.BLACK)
        }

        canvas.drawText(text, 256f, 128f + (paint.textSize / 3), paint)

        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        return textureIds[0]
    }
}
