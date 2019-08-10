package com.mcmacker4

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc


operator fun Vector3fc.unaryMinus(): Vector3f = mul(-1f, Vector3f())

operator fun Vector3fc.plus(other: Vector3fc): Vector3f = add(other, Vector3f())

operator fun Vector3f.plusAssign(other: Vector3f) { add(other) }

operator fun Vector3fc.minus(other: Vector3fc): Vector3f = sub(other, Vector3f())

operator fun Vector3f.minusAssign(other: Vector3f) { sub(other) }

operator fun Vector3fc.times(other: Float): Vector3f = mul(other, Vector3f())

operator fun Vector3f.timesAssign(other: Float) { mul(other) }

operator fun Matrix4fc.times(other: Matrix4fc): Matrix4f = mul(other, Matrix4f())


fun Float.clamp(min: Float, max: Float) = when {
    this > max -> max
    this < min -> min
    else -> this
}