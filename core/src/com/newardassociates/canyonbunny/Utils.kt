package com.newardassociates.canyonbunny

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2


class CameraHelper {
    companion object {
        val TAG = CameraHelper::class.java.simpleName

        const val MAX_ZOOM_IN = 0.25f
        const val MAX_ZOOM_OUT = 10.0f
    }

    val position: Vector2 = Vector2()
    var zoom: Float
        get() { return _zoom }
        set(value) {
            _zoom = MathUtils.clamp(value, MAX_ZOOM_IN, MAX_ZOOM_OUT)
        }
    private var _zoom: Float = 1.0f
    var target: Sprite? = null

    fun update(deltaTime: Float) {
       if (!hasTarget()) {
           return
       }

       position.x = target!!.x + target!!.originX
       position.y = target!!.y + target!!.originY
    }

    fun addZoom(amount: Float) {
        zoom = (_zoom + amount)
    }

    fun hasTarget(): Boolean {
        return target != null
    }

    fun hasTarget(target: Sprite): Boolean {
        return hasTarget() && this.target == target
    }

    fun applyTo(camera: OrthographicCamera) {
        camera.position.x = position.x
        camera.position.y = position.y
        camera.zoom = zoom
        camera.update()
    }
}