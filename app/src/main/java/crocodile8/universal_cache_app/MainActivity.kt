package crocodile8.universal_cache_app

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import crocodile8.universal_cache.CachedSource
import crocodile8.universal_cache.CachedSourceNoParams
import crocodile8.universal_cache.FromCache
import crocodile8.universal_cache.getOrRequest
import crocodile8.universal_cache.observeAndRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * It's a sample application. Library code is in another module.
 */
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private val TAG = "UACachedRessource"

    private val taskInvocationCnt = AtomicInteger()
    private val longRunningTask =
        {
            Thread.sleep(1000)
            Log.i("test_", "test_ longRunningTask")
            taskInvocationCnt.incrementAndGet()
        }

    private val source1 = CachedSource<String, Int>(source = { params ->
        longRunningTask()
    })

    private lateinit var textView1: TextView
    private lateinit var textView1Bis: TextView
    private lateinit var textView2: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var clear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView1 = findViewById(R.id.textView_1)
        textView1Bis = findViewById(R.id.textView_1bis)

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
        observe()

    }

    private fun observe() {
        lifecycleScope.launch {
            source1.getAsync("1", FromCache.IF_HAVE).collectLatest {
                textView1Bis.text = "observe : $it"
                Log.i(TAG, "Et un passage dans observe !")
            }
        }
    }

    private fun refresh1() {
        CoroutineScope(Dispatchers.Main).launch {
            source1.getAsync("1", FromCache.NEVER)
                .collectLatest {
                textView1.text = "refresh1 : $it"
                Log.i(TAG, "Et un passage dans refresh1 !")
            }
        }
    }

    private fun refresh2() {
        lifecycleScope.launch {
            source1.getAsync("2", FromCache.NEVER)
                .collect {
                    textView2.text = "Ready, invocation count: $it"
                }
        }
    }
}
