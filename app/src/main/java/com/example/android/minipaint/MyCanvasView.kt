package com.example.android.minipaint

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat

private const val STROKE_WIDTH = 12f // has to be float
// to display what you will draw in MyCanvasView, you have to set it as the content view of the MainActivity.
class MyCanvasView(context: Context) : View(context) {
    // these are your bitmap and canvas for caching what has been drawn before.
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    // Set up the paint with which to draw. bcz In order to draw, you need a Paint object that specifies how things are styled when drawn, and a Path that specifies what is being drawn.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER and specifies how lines and curve segments join on a stroked path.
        strokeCap = Paint.Cap.ROUND // default: BUTT, and it sets the shape of the end of the line to be a cap.
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    // Using a path, there is no need to draw every pixel and each time request a refresh of the display. Instead, you can (and will) interpolate a path between points for much better performance.
    private var path = Path() // add a variable path and initialize it with a Path obj to store the path that is being drawn when following user's touch on screen

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    // After the user stops moving and lifts their touch, these are the starting point for the next path.
    private var currentX = 0f
    private var currentY = 0f

    // scaledTouchSlop returns the distance in pixels a touch can wander before the system thinks the user is scrolling.
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private lateinit var frame: Rect
    //This callback method is called by the Android system with the changed screen dimensions
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        // create an instance of Bitmap with the new width and height, which are the screen size and assign ti to extraBitmap
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
        // Calculate a rectangular frame around the picture.
        val inset = 40
        frame = Rect(inset, inset, width - inset, height - inset)
    }

    // Draw the contents of the cached extraBitmap on the canvas associated with the view. in drawBitmap() method you provide the bitmap, the x and y coordinates(in pixels) of the top left corner and null for the Paint.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        // Draw a frame around the canvas.
        canvas.drawRect(frame, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            motionTouchEventX = event.x
            motionTouchEventY = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> touchStart()
                MotionEvent.ACTION_MOVE -> touchMove()
                MotionEvent.ACTION_UP -> touchUp()
            } }
            return true
    }

    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    //Define the touchMove() method. Calculate the traveled distance (dx, dy), create a curve between the two points and store it in path,
    // update the running currentX and currentY tally, and draw the path. Then call invalidate() to force redrawing of the screen with the updated path
    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it.
            extraCanvas.drawPath(path, paint)
        }
        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again. When the user lifts their touch, all that is needed is to reset the path so it does not get drawn again. Nothing is drawn, so no invalidation is needed.
        path.reset()
    }
}