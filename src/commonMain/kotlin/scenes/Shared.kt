package scenes

import com.soywiz.kmem.toIntFloor
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import gameplay.Process
import gameplay.getImage
import gameplay.rand

/*
fun Container.foto(graph:Int, x:Int, y:Int, size_x:Int, z:Int, flags:Int): Image {
    return image(getImage(graph)){
        position(x,y)
        anchor(0.5, 0.5)
        scale(size_x/100.0, size_x/100.0)
        smoothing = false
    }
}*/

fun Container.foto(graph:Int, x:Int, y:Int, size_x:Int, z:Int, flags:Int) = Foto(this, graph, x, y, size_x, z, flags)
class Foto(parent:Container, val initialGraph:Int, val xx:Int, val yy:Int, val size_x:Int, val z:Int, val flags:Int): Process(parent){
    override suspend fun main() {
        graph = initialGraph
        position(xx,yy)
        anchor(0.5, 0.5)
        scale(size_x/100.0, size_x/100.0)
        smoothing = false
    }
}

fun Container.space(){
    with(Bitmap32(640,480))
    {
        (0..499).forEach {
            this[rand(0,639).toIntFloor(), rand(0,479).toIntFloor()] = Colors.LIGHTGREY
        }
        image(this.slice())
    }
}