package crocodile8.universal_cache_app

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import crocodile8.universal_cache.CachedSource
import crocodile8.universal_cache.CachedSourceNoParams
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * It's a sample application. Library code is in another module.
 */
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val taskInvocationCnt = AtomicInteger()
    private val longRunningTask =
        {
            Thread.sleep(1000)
            Log.i("test_", "test_ longRunningTask")
            taskInvocationCnt.incrementAndGet()
        }

    private val source1 = CachedSource<String, Int>(source = { params -> longRunningTask() })

    private lateinit var textView1: TextView
    private lateinit var textView2: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var clear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView1 = findViewById(R.id.textView_1)

        textView2 = findViewById(R.id.textView_2)
        button1 = findViewById(R.id.download_1)
        button1.setOnClickListener {
            refresh1()
        }
        button2 = findViewById(R.id.download_2)
        button2.setOnClickListener {
            refresh2()
        }
        clear = findViewById(R.id.clearCache)
        clear.setOnClickListener {
            lifecycleScope.launch { source1.clearCache() }
        }
    }

    private fun refresh1() {
        lifecycleScope.launch {
            source1.get("1", FromCache.NEVER).collect {
                textView1.text = "Ready, invocation count: $it"
            }
        }
    }

    private fun refresh2() {
        lifecycleScope.launch {
            source1.get("2", FromCache.NEVER)
                .collect {
                    textView2.text = "Ready, invocation count: $it"
                }
        }
    }
}
