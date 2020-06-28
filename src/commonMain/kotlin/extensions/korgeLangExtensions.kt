package extensions

import com.soywiz.kds.IntArray2
import com.soywiz.kmem.umod

infix fun UByte.shr(other: Int): UInt = this.toUInt() shr other
infix fun UByte.shl(other: Int): UInt = this.toUInt() shl other
fun Boolean.toInt() = if (this) 1 else 0

inline fun Number.toBool() = this.toInt() != 0
inline fun UByte.toBool() = this.toInt() != 0
inline fun UShort.toBool() = this.toInt() != 0
inline fun UInt.toBool() = this.toInt() != 0
inline fun ULong.toBool() = this.toInt() != 0
fun Boolean.toBool() = this

fun IntArray2.getCyclic(x: Int, y: Int) = get(x umod width, y umod height)