package com.candstech.cacoustic

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private external fun nativeDspStatus(): String
    private external fun nativeRms(samples: FloatArray): Float
    private external fun nativeEstimateLag(reference: FloatArray, measured: FloatArray, maxLag: Int): Int
    private external fun nativeFftMagnitude(samples: FloatArray): FloatArray

    companion object {
        init { System.loadLibrary("cacoustic_dsp") }
    }

    private val bg = Color.rgb(5, 14, 25)
    private val panel = Color.rgb(12, 27, 42)
    private val cyan = Color.rgb(53, 215, 239)
    private val muted = Color.rgb(151, 177, 196)
    private var selected = "RTA"
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildShell()
        renderPage()
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(16, 14, 16, 12)
        }
        val brand = TextView(this).apply {
            text = "C ACOUSTIC"
            textSize = 27f
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            setTextColor(cyan)
        }
        val sub = TextView(this).apply {
            text = "CANDSTECH  •  AUDIO ANALYSIS & SYSTEM TUNING"
            textSize = 10f
            setTextColor(muted)
            setPadding(0, 2, 0, 14)
        }
        root.addView(brand)
        root.addView(sub)

        val tabsScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("RTA", "Transfer", "Impulse", "SPL", "Audio I/O", "Auto Phase").forEach { tab ->
            val b = Button(this).apply {
                text = tab
                textSize = 10f
                isAllCaps = false
                setTextColor(if (tab == selected) cyan else Color.WHITE)
                setBackgroundColor(if (tab == selected) Color.rgb(20, 55, 69) else panel)
                setOnClickListener {
                    selected = tab
                    buildShell()
                    renderPage()
                }
            }
            tabs.addView(b, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = 6 })
        }
        tabsScroll.addView(tabs)
        root.addView(tabsScroll)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 14, 0, 0)
        }
        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun renderPage() {
        content.removeAllViews()
        when (selected) {
            "RTA" -> {
                addHeading("REAL-TIME ANALYZER", "Spectrum engine scaffold")
                addMetric("FFT size", "4096", "Averaging: 8")
                addGraphPlaceholder("RTA SPECTRUM", "FFT DSP primitive tersedia; live capture belum terhubung.")
                addAction("Run DSP self-check") {
                    val data = FloatArray(1024) { i -> kotlin.math.sin(2.0 * Math.PI * 32 * i / 1024).toFloat() }
                    val mags = nativeFftMagnitude(data)
                    val peak = mags.indices.maxByOrNull { mags[it] } ?: 0
                    Toast.makeText(this, "FFT self-check selesai · peak bin $peak", Toast.LENGTH_LONG).show()
                }
            }
            "Transfer" -> {
                addHeading("TRANSFER FUNCTION", "Magnitude • Phase • Coherence")
                addMetric("Reference", "Input 1", "Measurement: Input 2")
                addGraphPlaceholder("MAGNITUDE / PHASE", "Belum ada sinkronisasi dual-channel atau transfer function live.")
                addGraphPlaceholder("COHERENCE", "Coherence akan ditampilkan setelah capture koheren tersedia.")
            }
            "Impulse" -> {
                addHeading("IMPULSE RESPONSE", "Arrival time & delay analysis")
                addMetric("Capture", "Not connected", "IR window: configurable")
                addGraphPlaceholder("IMPULSE RESPONSE", "Capture IR dan time-windowing belum terhubung ke perangkat audio.")
            }
            "SPL" -> {
                addHeading("SPL METER", "A-weighting / C-weighting planned")
                addMetric("Level", "Uncalibrated", "Tidak menampilkan dB SPL palsu")
                addGraphPlaceholder("CALIBRATION", "Profil sensitivitas mikrofon dan calibrator belum tersedia.")
            }
            "Audio I/O" -> {
                addHeading("AUDIO DEVICE & ROUTING", "USB interface • external OTG hub")
                addMetric("Input route", "Not connected", "Android USB audio support varies")
                addGraphPlaceholder("CHANNEL MAP", "Pemetaan reference / measurement memerlukan enumerasi perangkat dan capture engine.")
                addAction("Check native DSP status") {
                    Toast.makeText(this, nativeDspStatus(), Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                addHeading("AUTO PHASE", "Delay recommendation prototype")
                addMetric("Mode", "Analysis only", "No external DSP control")
                addGraphPlaceholder("MAIN + SUB ALIGNMENT", "Capture TF1 dan TF2 diperlukan. Korelasi saja tidak menjamin alignment optimal di seluruh crossover.")
                addAction("Run synthetic lag test") {
                    val ref = FloatArray(512) { i -> if (i == 100) 1f else 0f }
                    val measured = FloatArray(512) { i -> if (i == 112) 1f else 0f }
                    val lag = nativeEstimateLag(ref, measured, 64)
                    Toast.makeText(this, "Synthetic lag estimate: $lag samples", Toast.LENGTH_LONG).show()
                }
            }
        }
        val foot = TextView(this).apply {
            text = "C ACOUSTIC  •  PROTOTYPE 0.2  •  DSP VALIDATION REQUIRED"
            textSize = 9f
            setTextColor(muted)
            setPadding(0, 20, 0, 8)
        }
        content.addView(foot)
    }

    private fun addHeading(title: String, detail: String) {
        val t = TextView(this).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }
        val d = TextView(this).apply {
            text = detail
            textSize = 11f
            setTextColor(muted)
            setPadding(0, 3, 0, 12)
        }
        content.addView(t); content.addView(d)
    }

    private fun addMetric(label: String, value: String, detail: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(panel)
            setPadding(12, 12, 12, 12)
        }
        box.addView(TextView(this).apply { text = label.uppercase(); textSize = 10f; setTextColor(muted) })
        box.addView(TextView(this).apply { text = value; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; setTextColor(cyan); setPadding(0, 4, 0, 2) })
        box.addView(TextView(this).apply { text = detail; textSize = 11f; setTextColor(Color.WHITE) })
        content.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 10 })
    }

    private fun addGraphPlaceholder(title: String, detail: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(panel)
            setPadding(12, 12, 12, 16)
        }
        box.addView(TextView(this).apply { text = title; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE) })
        val graph = View(this).apply {
            setBackgroundColor(Color.rgb(7, 19, 31))
            minimumHeight = 120
        }
        box.addView(graph, LinearLayout.LayoutParams(-1, 120).apply { topMargin = 10; bottomMargin = 8 })
        box.addView(TextView(this).apply { text = detail; textSize = 11f; setTextColor(muted) })
        content.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 10 })
    }

    private fun addAction(label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(cyan)
            setOnClickListener { action() }
        }
        content.addView(button, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
    }
}
