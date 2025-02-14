package com.woohyman.keyboard.base

import android.content.Context
import com.woohyman.keyboard.cheats.Cheat
import com.woohyman.keyboard.emulator.EmulatorException
import com.woohyman.keyboard.emulator.EmulatorRunner
import com.woohyman.keyboard.utils.EmuUtils
import com.woohyman.keyboard.utils.EmuUtils.emulator
import com.woohyman.keyboard.utils.FileUtils
import com.woohyman.keyboard.utils.NLog
import com.woohyman.mylibrary.R
import java.io.File
import java.util.Locale

open class Manager(context: Context) : EmulatorRunner(
    context
) {
    fun setFastForwardEnabled(enabled: Boolean) {
        emulator.setFastForwardEnabled(enabled)
    }

    fun setFastForwardFrameCount(frames: Int) {
        emulator.setFastForwardFrameCount(frames)
    }

    fun copyAutoSave(slot: Int?) {
        if (slot == null) {
            return
        }
        if (!emulator.isGameLoaded) {
            throw EmulatorException("game not loaded")
        }
        val md5: String? = emulator.loadedGame?.md5
        val base = EmulatorUtils.getBaseDir(context)
        val source = SlotUtils.getSlotPath(base, md5, 0)
        val target = SlotUtils.getSlotPath(base, md5, slot)
        val sourcePng = SlotUtils.getScreenshotPath(base, md5, 0)
        val targetPng = SlotUtils.getScreenshotPath(base, md5, slot)
        try {
            FileUtils.copyFile(File(source), File(target))
            FileUtils.copyFile(File(sourcePng), File(targetPng))
        } catch (e: Exception) {
            throw EmulatorException(R.string.act_emulator_save_state_failed)
        }
    }

    fun enableCheats(ctx: Context?): Int {
        var numCheats = 0
        for (cheatChars in Cheat.getAllEnableCheats(ctx, EmuUtils.fetchProxy.game.checksum)) {
            if (cheatChars.contains(":")) {
                if (emulator.info.supportsRawCheats()) {
                    var rawValues: IntArray? = null
                    rawValues = try {
                        Cheat.rawToValues(cheatChars)
                    } catch (e: Exception) {
                        throw EmulatorException(
                            R.string.act_emulator_invalid_cheat, cheatChars
                        )
                    }
                    enableRawCheat(rawValues?.get(0)!!, rawValues[1]!!, rawValues?.get(2)!!)
                } else {
                    throw EmulatorException(R.string.act_emulator_invalid_cheat, cheatChars)
                }
            } else {
                enableCheat(cheatChars.uppercase(Locale.getDefault()))
            }
            numCheats++
        }
        return numCheats
    }

    fun benchMark() {
        emulator.reset()
        val t1 = System.currentTimeMillis()
        for (i in 0..2999) {
            emulator.emulateFrame(0)
            try {
                Thread.sleep(2)
            } catch (ignored: Exception) {
            }
        }
        val t2 = System.currentTimeMillis()
        NLog.e("benchmark", "bechmark: " + (t2 - t1) / 1000f)
    }
}