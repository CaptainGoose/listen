package com.goose.player

/**
 *Created by Gxxxse on 27.07.2019.
 */

object Constants {
    interface ACTION {
        companion object {
            val MAIN_ACTION = "com.goose.player.action.main"
            val INIT_ACTION = "com.goose.player.action.init"
            val PREV_ACTION = "com.goose.player.action.prev"
            val PLAY_ACTION = "com.goose.player.action.play"
            val NEXT_ACTION = "com.goose.player.action.next"
            val STARTFOREGROUND_ACTION = "com.goose.player.action.startforeground"
            val STOPFOREGROUND_ACTION = "com.goose.player.action.stopforeground"
        }

    }

    interface NOTIFICATION_ID {
        companion object {
            val FOREGROUND_SERVICE = 10
        }
    }

}