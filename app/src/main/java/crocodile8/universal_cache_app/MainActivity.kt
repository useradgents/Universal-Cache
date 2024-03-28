package crocodile8.universal_cache_app

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import crocodile8.universal_cache.CachedSource
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * It's a sample application. Library code is in another module.
 */
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {


    val tasksByParams: MutableMap<String, AtomicInteger> = mutableMapOf()
    private val longRunningTask: (String) -> Int =
        {
            Thread.sleep(1000)
            Log.i("test_", "test_ $it longRunningTask")
            tasksByParams.getOrPut(it) {
                AtomicInteger()
            }.incrementAndGet()
        }

    private val source1 = CachedSource<String, Int>(source = { params -> longRunningTask(params) })
    private val collector1 = source1.newCollector()
    private val collector2 = source1.newCollector()
    private val collector3 = source1.newCollector()

    private lateinit var textView1: TextView
    private lateinit var textView2: TextView
    private lateinit var textView3: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var clear: Button

    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private lateinit var spinner3: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView1 = findViewById(R.id.textView_1)
        textView2 = findViewById(R.id.textView_2)
        textView3 = findViewById(R.id.textView_3)

        spinner1 = findViewById(R.id.collectorName1)
        spinner2 = findViewById(R.id.collectorName2)
        spinner3 = findViewById(R.id.collectorName3)

        spinner1.adapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1,
            nameList
        )
        spinner2.adapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1,
            nameList
        )
        spinner3.adapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1,
            nameList
        )
        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lifecycleScope.launch {
                    collector1.get(nameList.get(position), FromCache.IF_HAVE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lifecycleScope.launch {
                    collector2.get(nameList.get(position), FromCache.IF_HAVE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lifecycleScope.launch {
                    collector3.get(nameList.get(position), FromCache.IF_HAVE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }



        lifecycleScope.launch {
            collector1.collect {
                textView1.text = "Ready, invocation count: $it"
            }
        }

        lifecycleScope.launch {
            collector3.collect {
                textView3.text = "Ready, invocation count: $it"
            }
        }

        lifecycleScope.launch {
            collector2.collect {
                textView2.text = "Ready, invocation count:$it"
            }
        }


        button1 = findViewById(R.id.download_1)
        button1.setOnClickListener {
            refresh1()
        }
        button2 = findViewById(R.id.download_2)
        button2.setOnClickListener {
            refresh2()
        }

        button3 = findViewById(R.id.download_3)
        button3.setOnClickListener {
            refresh3()
        }
        clear = findViewById(R.id.clearCache)
        clear.setOnClickListener {
            lifecycleScope.launch { source1.clearCache() }
        }
    }

    private fun refresh1() {
        lifecycleScope.launch {
            collector1.get(
                nameList.get(spinner1.selectedItemPosition), FromCache.NEVER
            )
        }
    }


    private fun refresh2() {
        lifecycleScope.launch {
            collector2.get(
                nameList.get(spinner2.selectedItemPosition), FromCache.NEVER
            )
        }
    }

    private fun refresh3() {
        lifecycleScope.launch {
            collector3.get(
                nameList.get(spinner3.selectedItemPosition), FromCache.NEVER
            )
        }
    }

    companion object {
        val nameList = listOf("1", "2", "3")
    }
}
