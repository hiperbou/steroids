package scenes

import com.soywiz.klock.seconds
import com.soywiz.kmem.toIntFloor
import com.soywiz.kmem.unsetBits
import com.soywiz.korge.input.onKeyDown
import com.soywiz.korge.input.onKeyUp
import com.soywiz.korge.time.delay
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import extensions.toBool
import gameplay.*
import input.*
import resources.Resources


class TitleScene() : SceneBase() {

    lateinit var pafSounds:SteroidsSounds


    override suspend fun Container.sceneInit() {
        Resources(views).loadAll()
        inicio()
    }

    inner class inicio:Process(sceneView) {
        override suspend fun main() {
            with(foto(1,160,119,100,100,0)) { //llamada para crear imagen del fondo
                launchAsap {
                    loop {
                        scaleX = 1.0
                        delay(0.25.seconds)
                        scaleX = -1.0
                        delay(0.25.seconds)
                    }
                }
            }

            space()


            val t1 = text("STEROIDS Version 1.0", 15.0, font = Resources.font)
                    .alignTopToTopOf(containerRoot)
                    .centerXOn(containerRoot)
            text("(c) DIV GAMES STUDIO", 15.0, font = Resources.font)
                    .alignTopToBottomOf(t1)
                    .centerXOn(containerRoot)
            text("PRESIONE UNA TECLA PARA JUGAR", 15.0, font = Resources.font)
                    .centerOn(containerRoot)
            text("< >:rotar ^:avanzar SPC:disparo H:hiperespacio", 15.0, font = Resources.font)
                    .alignBottomToBottomOf(containerRoot, padding = 4)
                    .alignRightToRightOf(containerRoot)
            text("LEVEL ${currentGameState.nivel}", 15.0, font = Resources.font)
                    .alignBottomToBottomOf(containerRoot, padding = 4)
                    .alignLeftToLeftOf(containerRoot)
            text("${currentGameState.puntuacion}", 15.0, font = Resources.font)
                    .alignTopToTopOf(containerRoot)
                    .alignRightToRightOf(containerRoot)

            delay(1.seconds)

            var key = 0

            //onKeyDown { key = key.setBits(getButtonPressed(it)) }
            onKeyDown { key = getButtonPressed(it) }
            onKeyUp { key = key.unsetBits(getButtonPressed(it)) }

            loop {
                if (key != 0) {
                    currentGameState = GameState()
                    sceneContainer.changeTo<GameScene>()
                }

                frame()
            }
        }
    }
}