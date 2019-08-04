package com.goose.player.entity

import java.io.Serializable

/**
 *Created by Gxxxse on 17.07.2019.
 */
class Song(var path: String,
           var name: String,
           var album: String?,
           var artist: String,
           var duration: Int,
           var hours: String,
           var minutes: String,
           var seconds: String): Serializable