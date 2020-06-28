package scenes

import com.soywiz.klock.seconds
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.scene.sleep
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.centerXBetween
import com.soywiz.korge.view.centerYBetween
import com.soywiz.korge.view.image
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.async.async
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.interpolation.Easing
import resources.Resources

class LoadingScene() : Scene() {

    override suspend fun Container.sceneInit() {
        views.clearColor = Colors.BLACK
    }

    override suspend fun sceneAfterInit() {
        super.sceneAfterInit()
        val splash = async { sceneView.splash() }

        sleep(1.0.seconds)
        Resources(views).loadAll()

        splash.await()
        sceneContainer.changeTo<TitleScene>()
    }

    suspend fun Container.splash() {
        val map = resourcesVfs["korge.png"].readBitmap().slice()
        val anim = async {
            logo(map)
        }
        sleep(1.seconds)
        anim.await()
    }

    suspend fun Container.logo(graph: BmpSlice) {
        val image = image(graph) {
            alpha = 0.0
            centerXBetween(0,640)
            centerYBetween(0,480)
        }

        image.tween(image::alpha[1], time = 1.seconds, easing = Easing.EASE_IN_OUT)
        sleep(1.seconds)
        image.tween(image::alpha[0], time = 1.seconds, easing = Easing.EASE_IN_OUT)
    }
}