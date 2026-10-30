package com.virasaya.ar

/** Reject small tracked planes (such as a hand) as arrow placement surfaces. */
const val MIN_ARROW_PLANE_EXTENT_METERS = 0.35f

fun isUsableArrowFloor(
    horizontalUpwardFacing: Boolean,
    tracking: Boolean,
    extentX: Float,
    extentZ: Float,
): Boolean = horizontalUpwardFacing && tracking &&
    extentX >= MIN_ARROW_PLANE_EXTENT_METERS && extentZ >= MIN_ARROW_PLANE_EXTENT_METERS
