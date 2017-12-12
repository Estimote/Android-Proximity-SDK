package com.estimote.proximityapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

/**
 * This is a simple custom view to show cute bubble animation :)
 * @author Estimote Inc. (contact@estimote.com)
 */
class BubbleView : View {

    internal data class DrawParams(var viewWidth: Int = 0,
                                   var viewHeight: Int = 0,
                                   var centerX: Double = 0.0,
                                   var centerY: Double = 0.0)

    private val DEFAULT_COLLAPSED_CIRCLE_RADIUS = 40.0f
    private val drawParams = DrawParams()

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var collapsedCircleRadius = DEFAULT_COLLAPSED_CIRCLE_RADIUS
    private var circleRadius = collapsedCircleRadius

    private val animator = ValueAnimator()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateDrawParams()
        canvas.drawCircle(drawParams.centerX.toFloat(), drawParams.centerY.toFloat(), circleRadius, paint)
    }

    fun setCollapsedCircleRadius(radius: Float) {
        collapsedCircleRadius = radius
    }

    fun setColor(color: Int) {
        paint.style = Paint.Style.FILL
        paint.color = context.resources.getColor(color)
    }

    fun reveal() {
        if (animator.isRunning) {
            animator.cancel()
        }
        animator.removeAllUpdateListeners()
        animator.addUpdateListener {
            circleRadius = it.animatedValue as Float
            invalidate()
        }
        animator.setFloatValues(0.0f, maxOf(drawParams.viewHeight, drawParams.viewWidth).toFloat())
        animator.interpolator = AccelerateInterpolator()
        animator.start()
    }

    fun collapse() {
        if (animator.isRunning) {
            animator.cancel()
        }
        animator.removeAllUpdateListeners()
        animator.addUpdateListener {
            circleRadius = it.animatedValue as Float
            invalidate()
        }
        animator.setFloatValues(maxOf(drawParams.viewHeight, drawParams.viewWidth).toFloat(), collapsedCircleRadius)
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    private fun updateDrawParams() {
        drawParams.viewWidth = width - (paddingLeft + paddingRight)
        drawParams.viewHeight = height - (paddingTop + paddingBottom)
        drawParams.centerX = paddingLeft + drawParams.viewWidth / 2.0
        drawParams.centerY = paddingTop + drawParams.viewHeight / 2.0
    }
}