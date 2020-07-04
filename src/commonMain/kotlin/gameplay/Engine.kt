package gameplay


import com.soywiz.kds.atomic.kdsIsFrozen
import com.soywiz.klock.Frequency
import com.soywiz.kmem.setBits
import com.soywiz.kmem.unsetBits
import com.soywiz.korge.component.StageComponent
import com.soywiz.korge.component.registerStageComponent
import com.soywiz.korge.input.onKeyDown
import com.soywiz.korge.input.onKeyUp
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.bitmap.slice
import com.soywiz.korio.async.*
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.cos
import com.soywiz.korma.geom.sin
import extensions.toBool
import input.getButtonPressed
import kotlinx.coroutines.*
import resources.Resources
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import kotlin.reflect.*

val PI = kotlin.math.PI

fun rand(from:Double, to:Double) = Random.Default.nextDouble(from, to)
fun rand(from:Int, to:Int) = Random.Default.nextDouble(from.toDouble(), to.toDouble())
//fun rand(from:Int, to:Int) = Random.Default.nextInt(from, to)

fun get_distx(angle:Double, dist:Number) = dist.toDouble() * cos(Angle.Companion.fromRadians(-angle))
fun get_disty(angle:Double, dist:Number) = dist.toDouble() * sin(Angle.Companion.fromRadians(-angle))



/*fun fget_dist(const int &x0, const int &y0, const int &x1,const int &y1)
{
    static int dx;
    static int dy;
    static int min;
    dx = abs(x1-x0);
    dy = abs(y1-y0);
    min = (dx<dy) ? dx : dy;

    return (dx+dy-(min >> 1)-(min >> 2)+(min>>3)+(min>> 4));
}*/

private val imageCache = mutableMapOf<Int, BmpSlice>()
fun getImage(graph:Int): BmpSlice {
    return imageCache.getOrPut(graph) {
        return if(graph==0) Process.emptyImage.slice() else
        Resources.steroidsAtlas["${graph.toString().padStart(3, '0')}.png"]//.texture
    }
}

private lateinit var currentScene:Scene

abstract class SceneBase:Scene()
{
    private val frameReady = Signal<Unit>()
    private var frameListenerInitialized = false

    suspend fun Container.frame() {
        if(!frameListenerInitialized) {
            frameListenerInitialized = true
            addFixedUpdater(Frequency(24.0)) {
                frameReady.invoke()
            }
        }

        frameReady.waitOneBase()
    }

    init {
        currentScene = this
    }

    fun Container.signalKill(predicate:(View)->Boolean) {
        children.filter(predicate).forEach { it.removeFromParent() }
    }

    fun Container.letMeAlone(self:View) {
        signalKill { it != self }
    }
}


abstract class Process(parent: Container) : Image(emptyImage) {
    companion object {
        val emptyImage = Bitmap32(1,1)
    }

    open val pname:String
        get() = this::class.simpleName ?: "process"

    private var _graph = 0
    var graph:Int
        get() =  _graph
        set(value) {
            _graph = value
            texture = getImage(value)
        }

    private var _angle = 0.0
    var angle:Double
        get() =  _angle
        set(value) {
            _angle = value
            rotationRadians = -value
        }

    init {
        lateinit var job: Job

        addComponent(object : StageComponent {
            override val view: View = this@Process

            override fun added(views: Views) {
                job = launchAsap {
                    //main()
                    //removeFromParent()
                    var action = ::main
                    while (true) {
                        try {
                            action()
                            removeFromParent()
                            break
                        } catch (e: ChangeActionException) {
                            action = e.action
                        }
                    }
                }
            }

            override fun removed(views: Views) {
                job.cancel()
            }
        })

        parent.addChild(this)
        anchor(0.5, 0.5)
        smoothing = false
    }

    abstract suspend fun main()

    inline fun loop(block:()->Unit) {
        while(true) {
            block()
        }
    }

    class ChangeActionException(val action: KSuspendFunction0<Unit>) : Exception()

    private var key = 0
    private var keyListener = false


    fun key(k:Int):Boolean {
        if (!keyListener) {
            keyListener = true
            onKeyDown { key = key.setBits(getButtonPressed(it)) }
            onKeyUp { key = key.unsetBits(getButtonPressed(it)) }
        }
        return (key and k).toBool()
    }

    fun launch(callback: suspend () -> Unit) = currentScene.launch(callback)
    fun launchImmediately(callback: suspend () -> Unit) = currentScene.launchImmediately(callback)
    fun launchAsap(callback: suspend () -> Unit) = currentScene.launchAsap(callback)

    fun <T>async(callback: suspend () -> T) = currentScene.async(callback)
    fun <T>asyncImmediately(callback: suspend () -> T) = currentScene.asyncImmediately(callback)
    fun <T>asyncAsap(callback: suspend () -> T) = currentScene.asyncAsap(callback)
}


fun Views.registerProcessSystem() {
    registerStageComponent()
}



fun Scene.loop(block:suspend ()->Unit){
    launchImmediately {
        while(true) {
            block()
        }
    }
}


