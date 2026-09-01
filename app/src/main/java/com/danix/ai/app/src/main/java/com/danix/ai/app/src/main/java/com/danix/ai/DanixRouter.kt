package com.danix.ai

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs

class DanixRouter(private val context: Context) {
    private val mathEngine = MathEngine()

    fun processCommand(input: String): String {
        val q = input.trim().lowercase()
        return when {
            q.contains("battery") || q.contains("storage") || q.contains("health") -> {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val stat = StatFs(Environment.getDataDirectory().path)
                val freeGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
                val totalGb = (stat.blockCountLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
                "Battery is at $battery%. Storage has $freeGb GB free of $totalGb GB."
            }
            q.contains("flashlight") -> {
                val turnOn = !q.contains("off")
                try {
                    val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    cm.setTorchMode(cm.cameraIdList[0], turnOn)
                    if (turnOn) "Flashlight turned on." else "Flashlight turned off."
                } catch (e: Exception) {
                    "Could not control flashlight: ${e.message}"
                }
            }
            q.matches(Regex(".*(\\d+\\s*[+\\-*/×÷]\\s*\\d+).*")) || q.contains("times") || q.contains("plus") -> {
                val parsed = q.replace("times", "*").replace("plus", "+").replace("minus", "-").replace("divided by", "/")
                    .replace("[^0-9+\\-*/.]".toRegex(), "")
                "The result is: ${mathEngine.calculate(parsed)}"
            }
            else -> "DANIX received: \"$input\"."
        }
    }
}
