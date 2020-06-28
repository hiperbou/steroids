package gameplay


import scenes.GameScene


class GameState() {
    var paused = false
    //var pauseBalls = false
    //var restarting = false

    var nivel = 1
    var volumen = 0
    var muerte = 0
    var puntuacion = 0

    var vidas = 4


    //val arrowCollisions = Collisions<GameScene.disparo>()
    //val ballCollision = Collisions<GameScene.bola>()
    //val playerCollision = Collisions<Player>()

    fun resetCollisions(){
        //arrowCollisions.reset()
        //ballCollision.reset()
    }
}

var currentGameState = GameState()
