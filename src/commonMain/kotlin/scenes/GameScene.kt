package scenes

import com.soywiz.klock.seconds
import com.soywiz.korge.view.*
import com.soywiz.korio.async.delay
import com.soywiz.korio.async.launchImmediately
import extensions.toBool
import gameplay.*
import input.*
import resources.Resources

class GameScene() : SceneBase() {

    lateinit var steroidsSounds: SteroidsSounds
    lateinit var pointsText : Text
    lateinit var levelText : Text

    override suspend fun Container.sceneInit() {
        Resources(views).loadAll()

        steroidsSounds = SteroidsSounds(Resources.tubo5Sound, Resources.tubo8Sound, Resources.fx33Sound, Resources.naveSound)


        val vida = listOf(nave_pequena(16,16),
            nave_pequena(48,16),
            nave_pequena(80,16))

        val t1 = text("STEROIDS Version 1.0", 15.0, font = Resources.font)
                .alignTopToTopOf(containerRoot)
                .centerXOn(containerRoot)
        text("(c) DIV GAMES STUDIO", 15.0, font = Resources.font)
                .alignTopToBottomOf(t1)
                .centerXOn(containerRoot)

        text("< >:rotar ^:avanzar SPC:disparo H:hiperespacio", 15.0, font = Resources.font)
                .alignBottomToBottomOf(containerRoot, padding = 4)
                .alignRightToRightOf(containerRoot)
        levelText = text("LEVEL ${currentGameState.nivel}", 15.0, font = Resources.font)
                .alignBottomToBottomOf(containerRoot, padding = 4)
                .alignLeftToLeftOf(containerRoot)
        pointsText = text("${currentGameState.puntuacion}", 15.0, font = Resources.font)
                .alignTopToTopOf(containerRoot)
                .alignRightToRightOf(containerRoot)

        space()

        fun startLevel() {
            currentGameState.resetCollisions()

            nave().position(320,240)                          // Inicia la nave

            // Inicia los asteroides, crea los procesos tipo asteroide
            (0..2 + currentGameState.nivel).forEach {
                asteroide(-16,-16,3);
            }
        }

        startLevel()

        launchImmediately {
            loop {
                if (currentGameState.muerte.toBool()) {                            // Si te matan

                    //signal(vida[--vidas], s_kill);       // Se borra un gráfico de las vidas
                    currentGameState.vidas--
                    if(currentGameState.vidas>0) vida[currentGameState.vidas-1].removeFromParent()
                    signalKill { it is asteroide }
                    signalKill { it is disparo_nave }

                    if (currentGameState.vidas == 0) {                    // Si no tienes vidas
                        //fade_off();                     // Acaba el juego
                        sceneContainer.changeTo<TitleScene>()
                        return@loop
                    }
                    currentGameState.muerte = 0


                    //fade(0, 0, 0, 8);                      // Hace apagado de pantalla
                    //WHILE(fading)                      // De un modo especial
                    //FRAME;
                    //END

                    startLevel()

                    //fade(100, 100, 100, 8);                // Enciende la pantalla
                } else {
                    if (children.firstOrNull { it is asteroide } == null) {       // Si no quedan asteroides
                        //fade(0,0,0,8);                      // Apaga pantalla
                        //WHILE (fading)                      // Espera
                        //    FRAME;
                        //END
                        delay(1.seconds)
                        currentGameState.nivel++                            // Cambia de nivel
                        updateLevelText()
                        signalKill { it is disparo_nave }
                        signalKill { it is nave }

                        startLevel()
                        //fade(100, 100, 100, 8);                // Enciende la pantalla
                    }
                }
                frame()
                //change_sound(id_sonido,volumen,200);    // Actualiza el sonido del motor
            }
        }
    }

    fun Container.updatePointsText(){
        pointsText.apply {
            text = "${currentGameState.puntuacion}"
            alignRightToRightOf(containerRoot)
        }
    }

    fun Container.updateLevelText(){
        levelText.apply {
            text = "LEVEL ${currentGameState.nivel}"
            alignLeftToLeftOf(containerRoot)
        }
    }


    inner class hiperespacio(val xx: Number, val yy: Number) : Process(sceneView) {
        override suspend fun main() {
            position(xx, yy)
            steroidsSounds.playHiperespacio()
            graph = 10

            while (scale > 0) {
                scale -= 0.05
                frame()
            }
        }
    }

    inner class nave_pequena(val xx: Int, val yy: Int) : Process(sceneView) {
        override suspend fun main() {
            position(xx, yy)
            graph = 1
            scale = 0.75

            val rotationSpeed = PI/64
            loop {
                angle += rotationSpeed
                frame()
            }
        }
    }

    inner class pieza(val xx: Number, val yy: Number, val initialAngle:Number, val initialGraph:Int) : Process(sceneView) {
        override suspend fun main() {
            position(xx, yy)
            graph = initialGraph
            angle = initialAngle.toDouble()

            val angulo2 = rand(0.0, 2*PI)           // Reinicia variables al azar para los ángulos
            val incr_angulo = rand(-PI/32, PI/32)
            while(scale > 0) {
                angle += incr_angulo         // Gira los gráficos
                x += get_distx(angulo2,2)   // Los mueve
                y += get_disty(angulo2,2)
                scale -= 0.02                    // Y los re-escala (cada vez más pequenos)
                frame()
            }
            currentGameState.muerte = 1                       // Actualiza la variable para indicar que has muerto
        }
    }

    var disparoId = 0
    inner class disparo_nave(val xx: Number, val yy: Number, val initialAngle:Number) : Process(sceneView), ICollider {
        override val pname = "disparo_nave${disparoId++}"

        val collider = Collider(currentGameState.arrowCollisions,  this)
        override var alive = collider.alive
        override fun destroy() { collider.destroy() }

        override suspend fun main() {
            var cont = 20

            position(xx, yy)
            angle = initialAngle.toDouble()
            graph = 2

            steroidsSounds.playFuego()

            while(--cont > 0) {
                x+=get_distx(angle,16)     // Calcula las coordenadas respecto a la nave
                y+=get_disty(angle,16)

                // Comprueba si se ha salido de la pantalla y los arregla
                if (x<-16) x+=640+32
                if (y<-16) y+=480+32
                if (x>640+16) x-=640+32
                if (y>480+16) y-=480+32
                frame()
            }
            destroy()
        }
    }

    var asteroideId = 0
    inner class asteroide(val xx: Number, val yy: Number, val initialGraph: Int) : Process(sceneView) {
        override val pname = "asteroideId${asteroideId++}"

        override suspend fun main() {
            var velocidad = 0                // Velocidad de los asteroides
            var id2 = 0                    // Identificador de uso general
            var incr_angulo = 0.0              // Velocidad de giro
            var angulo2 = 0.0                  // Dirección de avance

            position(xx, yy)
            graph = initialGraph

            angulo2 = rand(0.0, 2*PI)
            angle = angulo2     // Selecciona la dirección del asteroide
            incr_angulo = rand(-PI/32, PI/32) // Y la velocidad del giro
            velocidad = graph + currentGameState.nivel          // La velocidad depende de la fase del juego y del tamano del asteroide

            onCollision {
                if (it is disparo_nave) {
                    currentGameState.puntuacion += 25 * graph + (currentGameState.nivel - 1) * 25;  // Suma puntuación
                    updatePointsText()
                    it.removeFromParent() // Elimina el disparo
                    //sound(sonido_explosion, 30 * (6 - graph), 33 * graph);
                    steroidsSounds.playExplosion()
                    if (graph < 5)  {                        // Si el asteroide es muy grande
                        asteroide(x, y, graph + 1);         // Crea dos más pequenos
                        asteroide(x, y, graph + 1);
                    }
                    if(graph == 3) {                    // Si es el asteroide más grande
                        asteroide(x, y, graph + 1);         // Crea uno más (3 en total)
                    }
                    removeFromParent()//signal(ID, s_kill);                  // Elimina el asteroide actual
                }

                if (it is nave){
                    it.removeFromParent()
                    //sound(sonido_explosion, 200, 100)// Hace sonido de destrucción
                    steroidsSounds.playExplosion()
                    currentGameState.volumen = 0

                    pieza(it.x, it.y, it.angle, 6) // Destruye la nave en piezas
                    pieza(it.x, it.y, it.angle, 7)
                    pieza(it.x, it.y, it.angle, 8)
                    pieza(it.x, it.y, it.angle, 9)
                }
            }

            loop {
                x+=get_distx(angulo2,velocidad)    // Mueve los asteroides
                y+=get_disty(angulo2,velocidad)

                // Comprueba que si se ha salido de pantalla y actualiza la posición en consecuencia
                if (x<-16)  x += 640 + 32
                if (y<-16)  y += 480 + 32
                if (x>640+16) x -= 640 + 32
                if (y>480+16) y -= 480 + 32

                angle += incr_angulo                 // Gira el asteroide

                /*val it = currentGameState.arrowCollisions.colidesWith(this, 20)
                if(it != null) {
                    currentGameState.puntuacion += 25 * graph + (currentGameState.nivel - 1) * 25;  // Suma puntuación
                    updatePointsText()
                    it.destroy()
                    it.removeFromParent() // Elimina el disparo
                    //sound(sonido_explosion, 30 * (6 - graph), 33 * graph);
                    steroidsSounds.playExplosion()
                    if (graph < 5)  {                        // Si el asteroide es muy grande
                        asteroide(x, y, graph + 1);         // Crea dos más pequenos
                        asteroide(x, y, graph + 1);
                    }
                    if(graph == 3) {                    // Si es el asteroide más grande
                        asteroide(x, y, graph + 1);         // Crea uno más (3 en total)
                    }
                    removeFromParent()//signal(ID, s_kill);                  // Elimina el asteroide actual
                } else {
                    val it = currentGameState.playerCollision.colidesWith(this, 20)
                    if(it != null)
                    {
                        it.destroy()
                        it.removeFromParent()
                        //sound(sonido_explosion, 200, 100)// Hace sonido de destrucción
                        steroidsSounds.playExplosion()
                        currentGameState.volumen = 0

                        pieza(it.x, it.y, it.angle, 6) // Destruye la nave en piezas
                        pieza(it.x, it.y, it.angle, 7)
                        pieza(it.x, it.y, it.angle, 8)
                        pieza(it.x, it.y, it.angle, 9)
                    }
                }*/

                frame()
            }
        }
    }

    var naveID = 0
    inner class nave() : Process(sceneView), ICollider {

        override val pname = "nave${naveID++}"

        val collider = Collider(currentGameState.playerCollision,  this)
        override var alive = collider.alive
        override fun destroy() { collider.destroy() }

        override suspend fun main() {
            var disparo = 1              // 1=disparo permitido, 0=no permitido
            var hiper = 1                // 1=hiperespacio permitido, 0=no permitido
            var velocidad_x = 0.0          // incremento x
            var velocidad_y = 0.0          // incremento y

            graph = 1

            val _left = BUTTON_LEFT
            val _right = BUTTON_RIGHT
            val _up = BUTTON_UP
            val _down = BUTTON_DOWN

            val _control = BUTTON_A
            val _space = BUTTON_A

            val _h = BUTTON_B

            var shoot = 0

            loop {
            // Lee teclas y actualiza el ángulo de la nave
                if (true || key(_right)) angle -= PI/16
                if (key(_left)) angle += PI/16

                if (key(_up)) {                     // Calcula el avance con formula
                    velocidad_x += get_distx(angle, 10)
                    velocidad_y += get_disty(angle, 10)
                    // Incrementa el volumen de la nave
                    with(currentGameState) {
                        volumen += 30
                        if (volumen > 256) {
                            volumen = 256
                        } else {
                            // Decrementa el volumen
                            volumen -= 10
                        }
                        if (volumen < 0) volumen = 0
                    }
                }

                x += velocidad_x/10
                y += velocidad_y/10

                // Comprueba si se ha salido de la pantalla y lo soluciona
                if (x<-16) x+=640+32
                if (y<-16)  y+=480+32
                if (x>640+16) x-=640+32
                if (y>480+16) y-=480+32

                shoot++
                if (shoot>3 || key(_space) || key (_control)) {    // Comprueba la tecla de disparo
                    shoot = 0
                    if (disparo.toBool()) {                        // Y si se puede dispara
                        disparo = 0
                        disparo_nave(x, y, angle)        // Dispara, creando un proceso tipo disparo nave
                    }
                } else {
                    disparo = 1                          // Hace que los disparos salgan de uno en uno
                }

                if (key(_h)) {                    // Comprueba la tecla del hiperespacio
                    if (hiper.toBool()) {                         // Y si se puede se hace
                        hiper = 0
                        hiperespacio(x, y)
                        x = rand(0, 640)                  // Pon la nave en una posición aleatoria
                        y = rand(0, 480)
                    }
                } else {
                    hiper = 1                            // Hace que los hiperespacios salgan de uno en uno
                }

                frame()
            }
        }
    }
}

    /*

PROCESS nave(x,y);

PRIVATE
    disparo=1;              // 1=disparo permitido, 0=no permitido
    hiper=1;                // 1=hiperespacio permitido, 0=no permitido
    velocidad_x=0;          // incremento x
    velocidad_y=0;          // incremento y

BEGIN
    graph=1;
	file=fichero;
    LOOP
        // Lee teclas y actualiza el ángulo de la nave
        IF (key(_right)) angle-=pi/16; END
        IF (key(_left)) angle+=pi/16; END

        IF (key(_up))                       // Calcula el avance con formula
            velocidad_x+=get_distx(angle,10);
            velocidad_y+=get_disty(angle,10);
            // Incrementa el volumen de la nave
            IF ((volumen+=30) > 256) volumen=256; END
        ELSE
            // Decrementa el volumen
            volumen-=10;
            IF (volumen<0) volumen=0; END
        END

        x+=velocidad_x/10;
        y+=velocidad_y/10;

        // Comprueba si se ha salido de la pantalla y lo soluciona
        IF (x<-16) x+=640+32; END
        IF (y<-16)  y+=480+32; END
        IF (x>640+16) x-=640+32; END
        IF (y>480+16) y-=480+32; END

        IF (key(_space) OR key (_control))      // Comprueba la tecla de disparo
            IF (disparo)                        // Y si se puede dispara
                disparo=0;
                disparo_nave(x,y,angle);        // Dispara, creando un proceso tipo disparo nave
            END
        ELSE
            disparo=1;                          // Hace que los disparos salgan de uno en uno
        END

        IF (key(_h))                            // Comprueba la tecla del hiperespacio
            IF (hiper)                          // Y si se puede se hace
                hiper=0;
                hiperespacio(x,y);
                x=rand(0,640);                  // Pon la nave en una posición aleatoria
                y=rand(0,480);
            END
        ELSE
            hiper=1;                            // Hace que los hiperespacios salgan de uno en uno
        END

        FRAME;
    END
END




PROCESS asteroide(x,y,graph);

PRIVATE
    velocidad;                // Velocidad de los asteroides
    id2=0;                    // Identificador de uso general
    incr_ángulo;              // Velocidad de giro
    ángulo2;                  // Dirección de avance

BEGIN
    angle=ángulo2=rand(0,2*pi);     // Selecciona la dirección del asteroide
    incr_ángulo=rand(-pi/32,pi/32); // Y la velocidad del giro
    velocidad=graph+nivel;          // La velocidad depende de la fase del juego y del tamano del asteroide
	file=fichero;

    LOOP

        // Comprueba si se ha chocado con un disparo
        id2=collision(TYPE disparo_nave);
        IF (id2)
            puntuacion+=25*graph+(nivel-1)*25;  // Suma puntuación
            signal(id2,s_kill);                 // Elimina el disparo
            sound(sonido_explosion,30*(6-graph),33*graph);
            IF (graph<5)                        // Si el asteroide es muy grande
                asteroide(x,y,graph+1);         // Crea dos más pequenos
                asteroide(x,y,graph+1);
            END
            IF (graph==3)                       // Si es el asteroide más grande
                asteroide(x,y,graph+1);         // Crea uno más (3 en total)
            END
            signal(ID,s_kill);                  // Elimina el asteroide actual
        END

        // Comprueba si se ha chocado con la nave
        id2=collision(TYPE nave);
        IF (id2)
            signal(id2,s_kill);             // Elimina el proceso de la nave
            sound(sonido_explosion,200,100);// Hace sonido de destrucción
            volumen=0;

            pieza(id2.x,id2.y,id2.angle,6); // Destruye la nave en piezas
            pieza(id2.x,id2.y,id2.angle,7);
            pieza(id2.x,id2.y,id2.angle,8);
            pieza(id2.x,id2.y,id2.angle,9);
        END

        x+=get_distx(ángulo2,velocidad);    // Mueve los asteroides
        y+=get_disty(ángulo2,velocidad);

        // Comprueba que si se ha salido de pantalla y actualiza la posición en consecuencia
        IF (x<-16)  x+=640+32;   END
        IF (y<-16)  y+=480+32;   END
        IF (x>640+16) x-=640+32; END
        IF (y>480+16) y-=480+32; END

        angle+=incr_ángulo;                 // Gira el asteroide
        FRAME;
    END
END



    PROCESS disparo_nave(x,y,angle);

PRIVATE
    cont=20;                        // Contador de uso general

BEGIN
    sound(sonido_fuego,100,100);    // Sonido de disparo
    graph=2;                        // Selecciona el gráfico
	file=fichero;
    REPEAT
        x+=get_distx(angle,16);     // Calcula las coordenadas respecto a la nave
        y+=get_disty(angle,16);

        // Comprueba si se ha salido de la pantalla y los arregla
        IF (x<-16) x+=640+32; END
        IF (y<-16) y+=480+32; END
        IF (x>640+16) x-=640+32; END
        IF (y>480+16) y-=480+32; END

        FRAME;
    UNTIL (--cont==0)               // Avanza 20 pasos antes de desaparecer
END

    PROCESS pieza(x,y,angle,graph);

PRIVATE
    ángulo2;                        // Angulo aleatorio
    incr_ángulo;                    // Velocidad de giro

BEGIN
    ángulo2=rand(0,2*pi);           // Reinicia variables al azar para los ángulos
    incr_ángulo=rand(-pi/32,pi/32);
	file=fichero;

    REPEAT
        angle+=incr_ángulo;         // Gira los gráficos
        x+=get_distx(ángulo2,2);    // Los mueve
        y+=get_disty(ángulo2,2);
        size-=2;                    // Y los re-escala (cada vez más pequenos)
        FRAME;
    UNTIL (size<=0)

    muerte=1;                       // Actualiza la variable para indicar que has muerto
END


    PROCESS nave_pequena(x,y);

BEGIN
    graph=1;                    // Elige el gráfico
	file=fichero;
    size=75;                    // Hace que sea más pequeno que con la nave que juegas

    LOOP
        angle+=pi/64;           // Gira el gráfico
        FRAME;
    END

END

PROCESS hiperespacio(x,y);

BEGIN
    sound(sonido_hiperespacio,180,400); // Realiza el sonido
    graph=10;                           // Selecciona el gráfico
	file=fichero;
    WHILE (size>0)                      // Repite hasta que desaparezca
        size-=5;                        // Hace que el gráfico sea más pequeno
        FRAME;
    END
END*/
