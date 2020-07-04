import com.soywiz.korge.Korge
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Views
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.SizeInt
import gameplay.registerProcessSystem
import scenes.*
import kotlin.reflect.KClass


suspend fun main() = Korge(Korge.Config(module = SteroidsGameModule))

object SteroidsGameModule : Module() {
	override val title = "Steroids"
	override val size = SizeInt(640,480)
	//override val windowSize = SizeInt(800, 600)
	override val windowSize = SizeInt(640, 480)
	override val targetFps = 24.0

	override val bgcolor = Colors.BLACK
	override val mainScene: KClass<out Scene> = GameScene::class

	override suspend fun init(injector: AsyncInjector): Unit = injector.run {
		//mapInstance(GameState())
		get<Views>().registerProcessSystem()

		mapPrototype { LoadingScene(/*get()*/) }
		mapPrototype { TitleScene(/*get()*/) }
		mapPrototype { GameScene(/*get()*/) }

	}
}
