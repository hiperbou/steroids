package input

import com.soywiz.korev.Key
import com.soywiz.korev.KeyEvent


val BUTTON_UP: Int = 1
val BUTTON_DOWN: Int = 2
val BUTTON_LEFT: Int = 4
val BUTTON_RIGHT: Int = 8
val BUTTON_A: Int = 16
val BUTTON_B: Int = 32
val BUTTON_C: Int = 64
val BUTTON_START: Int = 128

fun getButtonPressed(it: KeyEvent) = when (it.key) {
    Key.A, Key.LEFT -> BUTTON_LEFT
    Key.D,  Key.RIGHT -> BUTTON_RIGHT
    Key.W, Key.UP -> BUTTON_UP
    Key.S, Key.DOWN -> BUTTON_DOWN

    Key.SPACE -> BUTTON_A
    Key.N  -> BUTTON_A
    Key.M  -> BUTTON_A
    Key.Z  -> BUTTON_A
    Key.X  -> BUTTON_A

    Key.H  -> BUTTON_B


    Key.ENTER -> BUTTON_START

    else -> 0
}

