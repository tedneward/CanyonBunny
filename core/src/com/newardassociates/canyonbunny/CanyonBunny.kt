package com.newardassociates.canyonbunny

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Disposable

class Constants {
    companion object {
        const val VIEWPORT_WIDTH = 5.0f
        const val VIEWPORT_HEIGHT = 5.0f
    }
}

class WorldController : InputAdapter() {
    companion object {
        val TAG : String = WorldController::class.java.simpleName
    }

    var testSprites : Array<Sprite?> = arrayOfNulls(5)
    var selectedSprite : Int = 0
    var cameraHelper : CameraHelper = CameraHelper()

    init {
        Gdx.input.inputProcessor = this

        init()
    }

    private fun init() {
        val width = 32
        val height = 32

        val pixmap = createProceduralPixmap(width, height)
        val texture = Texture(pixmap)

        testSprites.forEachIndexed { i, _ ->
            val spr = Sprite(texture)
            spr.setSize(1.0f, 1.0f)
            spr.setOrigin(spr.width / 2.0f, spr.height / 2.0f)
            val randomX = MathUtils.random(-2.0f, 2.0f)
            val randomY = MathUtils.random(-2.0f, 2.0f)
            spr.setPosition(randomX, randomY)
            testSprites[i] = spr
        }
        selectedSprite = 0
    }

    private fun createProceduralPixmap(width: Int, height: Int): Pixmap {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 0.5f)
        pixmap.fill()

        pixmap.setColor(Color.YELLOW)
        pixmap.drawLine(0,0, width, height)
        pixmap.drawLine(width, 0, 0, height)

        pixmap.setColor(Color.CYAN)
        pixmap.drawRectangle(0, 0, width, height)
        return pixmap
    }

    fun update(deltaTime: Float) {
        handleDebugInput(deltaTime)
        updateTestObjects(deltaTime)
        cameraHelper.update(deltaTime)
    }
    private fun updateTestObjects(deltaTime: Float) {
        var rotation = testSprites[selectedSprite]!!.rotation
        rotation += 90 * deltaTime
        rotation %= 360
        testSprites[selectedSprite]!!.rotation = rotation
    }
    private fun handleDebugInput(deltaTime: Float) {
        if (Gdx.app.type != Application.ApplicationType.Desktop)
            return

        // Selected-sprite controls
        val spriteMoveSpeed = 5 * deltaTime
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            moveSelectedSprite(-spriteMoveSpeed, 0.0f)
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            moveSelectedSprite(spriteMoveSpeed, 0.0f)
        if (Gdx.input.isKeyPressed(Input.Keys.W))
            moveSelectedSprite(0.0f, spriteMoveSpeed)
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            moveSelectedSprite(0.0f, -spriteMoveSpeed)

        // Camera controls
        var cameraMoveSpeed = 5.0f * deltaTime
        val cameraMoveSpeedAccelerationFactor = 5.0f
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            cameraMoveSpeed *= cameraMoveSpeedAccelerationFactor
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveCamera(-cameraMoveSpeed, 0.0f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveCamera(cameraMoveSpeed, 0.0f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveCamera(0.0f, -cameraMoveSpeed)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveCamera(0.0f, cameraMoveSpeed)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE)) {
            cameraHelper.position.x = 0.0f
            cameraHelper.position.y = 0.0f
        }

        // Camera zoom controls
        var cameraZoomSpeed = 1 * deltaTime
        val cameraZoomSpeedAccelerationFactor = 5.0f
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            cameraZoomSpeed *= cameraZoomSpeedAccelerationFactor
        }
        if (Gdx.input.isKeyPressed(Input.Keys.COMMA)) {
            cameraHelper.addZoom(cameraZoomSpeed)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) {
            cameraHelper.addZoom(-cameraZoomSpeed)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SLASH)) {
            cameraHelper.zoom = 1.0f
        }
    }
    private fun moveSelectedSprite(x: Float, y: Float) {
        testSprites[selectedSprite]!!.translate(x, y)
    }
    private fun moveCamera(x: Float, y: Float) {
        val newX = x + cameraHelper.position.x
        val newY = y + cameraHelper.position.y
        cameraHelper.position.x = newX
        cameraHelper.position.y = newY
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.R -> {
                Gdx.app.debug(TAG, "Reset game world")
                init()
            }
            Input.Keys.SPACE -> {
                selectedSprite = (selectedSprite + 1) % testSprites.size
                if (cameraHelper.hasTarget()) {
                    cameraHelper.target = testSprites[selectedSprite]
                }
                Gdx.app.debug(TAG, "Sprite ${selectedSprite} selected")
            }
            Input.Keys.ENTER -> {
                cameraHelper.target = if (cameraHelper.hasTarget()) null else testSprites[selectedSprite];
                Gdx.app.debug(TAG, "Camera follow enabled on $cameraHelper.hasTarget()")
            }
        }
        return false
    }
}

class WorldRenderer(private val worldController: WorldController) : Disposable {
    companion object {
        val TAG = WorldRenderer::class.java.simpleName
    }

    private var batch : SpriteBatch = SpriteBatch()
    private var camera : OrthographicCamera = OrthographicCamera(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
    init {
        camera.position.set(0.0f, 0.0f, 0.0f)
        camera.update()
    }

    fun render() {
        renderTestObjects()
    }
    private fun renderTestObjects() {
        worldController.cameraHelper.applyTo(camera)
        batch.projectionMatrix = camera.combined
        batch.begin()
        for (spr in worldController.testSprites)
            spr!!.draw(batch)
        batch.end()
    }

    fun resize(width: Int, height: Int) {
        camera.viewportWidth = (Constants.VIEWPORT_HEIGHT / height) * width
        camera.update()
    }

    override fun dispose() {
        batch.dispose()
    }
}

class CanyonBunnyMain : ApplicationListener {
    companion object {
        val TAG : String = CanyonBunnyMain::class.java.simpleName
        val CORNFLOWER_BLUE = Color(0x64/255.0f, 0x95/255.0f, 0xed/255.0f, 0xff/255.0f)
    }

    private lateinit var worldController : WorldController
    private lateinit var worldRenderer : WorldRenderer
    private var paused = false

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        Gdx.app.debug(TAG, "create()")

        // Let's assume create() was called to re-create everything from scratch
        worldController = WorldController()
        worldRenderer = WorldRenderer(worldController)

        paused = false
    }

    override fun resize(width: Int, height: Int) {
        Gdx.app.debug(TAG, "resize()")
        worldRenderer.resize(width, height)
    }

    override fun render() {
        if (!paused) {
            worldController.update(Gdx.graphics.deltaTime)
        }

        Gdx.gl.glClearColor(CORNFLOWER_BLUE.r, CORNFLOWER_BLUE.g, CORNFLOWER_BLUE.b, CORNFLOWER_BLUE.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        worldRenderer.render()
    }

    override fun pause() {
        Gdx.app.debug(TAG, "pause()")
        paused = true
    }

    override fun resume() {
        Gdx.app.debug(TAG, "resume()")
        paused = false
    }

    override fun dispose() {
        Gdx.app.debug(TAG, "dispose()")
        worldRenderer.dispose()
    }

}