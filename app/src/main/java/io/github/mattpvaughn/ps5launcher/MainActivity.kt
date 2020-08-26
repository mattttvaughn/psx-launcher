package io.github.mattpvaughn.ps5launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearSnapHelper
import io.github.mattpvaughn.ps5launcher.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        // The apps most recently opened. Quick thing in lieu of a real DB
        const val KEY_MOST_RECENT_APPS_OPEN = "KEY_MOST_RECENT_APPS_OPENED"
        const val MAX_RECENT_APP_COUNT = 10
        const val SEPARATOR = "*"
        const val APP_NAME = "PS5 Launcher"
    }

    private val adapter by lazy {
        AppListAdapter(object : AppClick {
            override fun click(app: AppModel) {
                if (app.launchIntent != null) {
                    updateRecentAppsList(app.launchIntent)
                    openApp(app)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        // Refresh the app list when the app resumes being visible. Will need to be moved to
        // onResume if the app gains uninstall/install ability
        lifecycleScope.launch {
            viewModel.updateRecent(fetchRecentApps())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(
            layoutInflater,
            findViewById(android.R.id.content),
            false
        )
        viewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(applicationContext)
        ).get(MainViewModel::class.java)

        binding.appPager.adapter = adapter
        binding.appPager.layoutManager = PSAppLayoutManager(this)

        val snapHelper = object : LinearSnapHelper() {
            // limit scroll distance
            override fun calculateScrollDistance(velocityX: Int, velocityY: Int): IntArray {
                return super.calculateScrollDistance(velocityX / 4, velocityY / 4)
            }
        }

        snapHelper.attachToRecyclerView(binding.appPager)
        adapter.submitList(emptyList())
        viewModel.appIcons.observe(this, Observer {
            adapter.submitList(it)
        })

//        Log.i("PA", "Adapter item count: ${adapter.itemCount}")
        setContentView(binding.root)

        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("K:mm", Locale.US)
        binding.clock.text = dateFormat.format(cal.time)
        lifecycleScope.launch {
            repeat(1000) {
                delay(5000)
                binding.clock.text = dateFormat.format(cal.time)
            }
        }
    }

    private fun updateRecentAppsList(launchIntent: Intent) {
        lifecycleScope.launch {
            val updatedApps = withContext(Dispatchers.IO) {
                val apps = fetchRecentApps()
                val component = launchIntent.component?.flattenToString() ?: ""
                val updated = apps.filter { it != component }.toMutableList()
                if (updated.size > MAX_RECENT_APP_COUNT) {
                    // remove old apps from list
                    updated.removeAt(MAX_RECENT_APP_COUNT)
                }
                if (updated.contains(component)) {
                    updated.remove(component)
                }
                updated.add(0, component)
                return@withContext updated
            }
            prefs.edit().putString(KEY_MOST_RECENT_APPS_OPEN, updatedApps.joinToString(SEPARATOR))
                .apply()
        }
    }

    private suspend fun fetchRecentApps(): List<String> {
        return withContext(Dispatchers.IO) {
            val recentApps = prefs.getString(KEY_MOST_RECENT_APPS_OPEN, "")
            return@withContext recentApps?.split(SEPARATOR) ?: emptyList()
        }
    }

    private fun openApp(app: AppModel) {
        startActivity(app.launchIntent)
    }
}