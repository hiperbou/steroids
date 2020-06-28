package gameplay

import com.soywiz.korge.view.View

class Collisions<T:View> {

    val views = mutableListOf<T>()

    fun reset(){
        views.clear()
    }

    fun add(arrow:T){
        views.add(arrow)
    }

    fun remove(arrow:T){
        views.remove(arrow)
    }

    fun colidesWith(view: View, radius:Int = 20, offsetY:Int = 0):T? {
        return views.firstOrNull {
            checkCollision(view, radius, offsetY, it)
        }
    }

    private fun checkCollision(view: View, radius: Int, offsetY:Int, it: T): Boolean {
        if (view.x - radius > it.x) return false
        if (view.x + radius < it.x) return false
        if (view.y - radius > it.y + offsetY) return false
        if (view.y + radius < it.y + offsetY) return false

        return true
    }

}

interface ICollider {
    var alive:Boolean
    fun destroy()
}

class Collider<T: View>(private val collisions: Collisions<T>, private val container:T):ICollider
{
    override var alive = true
    init {
        collisions.add(container)
    }

    override fun destroy() {
        alive = false
        container.removeFromParent()
        collisions.remove(container)
    }
}
