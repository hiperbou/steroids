package resources

import com.soywiz.korau.sound.NativeSound
import com.soywiz.korau.sound.readSound
import com.soywiz.korge.atlas.Atlas
import com.soywiz.korge.atlas.readAtlas
import com.soywiz.korge.view.Views
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.readBitmapFont

import com.soywiz.korio.file.std.resourcesVfs
import gameplay.registerProcessSystem
import kotlin.native.concurrent.*

class Resources(private val views: Views) {
    @ThreadLocal
    companion object{
        lateinit var steroidsAtlas: Atlas

        lateinit var font: BitmapFont
        lateinit var tubo5Sound: NativeSound
        lateinit var tubo8Sound: NativeSound
        lateinit var fx33Sound: NativeSound
        lateinit var naveSound: NativeSound

        private var loaded = false
        private var loadedGfx = false
        private var loadedMusic = false
    }

    suspend fun loadAll() {
        if(loaded) return
        loaded = true

        loadGfx()
        loadMusic()
    }

    suspend fun loadGfx() {
        if(loadedGfx) return
        loadedGfx = true

        steroidsAtlas = resourcesVfs["fpg.atlas.json"].readAtlas(views)
        font = resourcesVfs["texts/I-pixel-u.fnt"].readBitmapFont()

        fx33Sound = resourcesVfs["fx33.wav"].readSound()
        tubo5Sound = resourcesVfs["tubo5.wav"].readSound()
        tubo8Sound = resourcesVfs["tubo8.wav"].readSound()
        naveSound = resourcesVfs["nave.wav"].readSound()


    }

    suspend fun loadMusic() {
        if(loadedMusic) return
        loadedMusic = true

        //music = resourcesVfs["music.mp3"].readNativeSound(true)
    }

    fun setLoaded() {
        loaded = true
    }
}

