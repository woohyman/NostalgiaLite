package com.woohyman.keyboard.keyboard

import com.woohyman.keyboard.controllers.KeyAction

class KeyboardControllerKeys {
    companion object {
        const val PLAYER2_OFFSET = 100000
        const val KEY_XPERIA_CIRCLE = 2068987562
        const val KEY_MENU = 902
        const val KEY_BACK = 900
        const val KEY_RESET = 901
        const val KEY_FAST_FORWARD = 903
        const val KEY_SAVE_SLOT_0 = 904
        const val KEY_LOAD_SLOT_0 = 905
        const val KEY_SAVE_SLOT_1 = 906
        const val KEY_LOAD_SLOT_1 = 907
        const val KEY_SAVE_SLOT_2 = 908
        const val KEY_LOAD_SLOT_2 = 909

        @JvmField
        var KEYS_RIGHT_AND_UP =
            keysToMultiCode(KeyAction.KEY_RIGHT.key, KeyAction.KEY_UP.key)

        @JvmField
        var KEYS_RIGHT_AND_DOWN =
            keysToMultiCode(KeyAction.KEY_RIGHT.key, KeyAction.KEY_DOWN.key)

        @JvmField
        var KEYS_LEFT_AND_DOWN =
            keysToMultiCode(KeyAction.KEY_LEFT.key, KeyAction.KEY_DOWN.key)

        @JvmField
        var KEYS_LEFT_AND_UP =
            keysToMultiCode(KeyAction.KEY_LEFT.key, KeyAction.KEY_UP.key)

        private fun keysToMultiCode(key1: Int, key2: Int): Int {
            return key1 * 1000 + key2 + 10000
        }

        fun isMulti(mapValue: Int): Boolean {
            return mapValue >= 10000
        }
    }
}