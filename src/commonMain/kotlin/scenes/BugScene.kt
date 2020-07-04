package scenes

import com.soywiz.klock.Frequency
import com.soywiz.klock.milliseconds
import com.soywiz.korge.scene.sleep
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addFixedUpdater
import com.soywiz.korge.view.position
import com.soywiz.korio.async.Signal
import com.soywiz.korio.async.invoke
import com.soywiz.korio.async.launchImmediately
import gameplay.Process
import gameplay.SceneBase
import gameplay.loop
import resources.Resources
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BugScene() : SceneBase() {

    override suspend fun Container.sceneInit() {
        Resources(views).loadAll()
        val vida = mutableListOf(nave_pequena(16,16),
            nave_pequena(48,16),
            nave_pequena(80,16))

        var step = 0

        launchImmediately {
            while(true) {
                step++
                if (step==30) {
                    step = 0

                    if(vida.isNotEmpty()) {
                        vida.removeAt(0).removeFromParent()
                    }
                }
                sleep(30.milliseconds) //THIS FAILS!
                //frame() //THIS WORKS!
            }
        }
    }


    inner class nave_pequena(val xx: Int, val yy: Int) : Process(sceneView) {
        override suspend fun main() {
            position(xx, yy)
            graph = 1
            loop {
                println(xx)
                frame()
            }
        }
    }
}
