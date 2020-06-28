package gameplay

import com.soywiz.korau.sound.NativeSound


class SteroidsSounds (
        private val tubo5Sound: NativeSound,
        private val tubo8Sound: NativeSound,
        private val fx33Sound: NativeSound,
        private val naveSound: NativeSound
){
    var mute = false

    fun playExplosion(){
        if(mute) return
        tubo5Sound.play()
    }

    fun playFuego(){
        if(mute) return
        tubo8Sound.play()
    }

    fun playHiperespacio(){
        if(mute) return
        fx33Sound.play()
    }

    fun playAceleracion(){
        if(mute) return
        naveSound.play()
    }

    fun playMusic() {
        if(mute) return
     //   music.playForever()
    }
}