package com.goose.player.extensions

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.widget.Toast

/**
 *Created by Gxxxse on 20.07.2019.
 */

fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Fragment.toast(message: CharSequence) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

fun Activity.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()