package io.github.mattpvaughn.ps5launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
        const val KEY_RECENT_APPS = "KEY_MOST_RECENT_APPS_OPENED"
        const val MAX_RECENT_APP_COUNT = 10
        const val SEPARATOR = "*"
        const val APP_NAME = "PS5 Launcher"
    }

    private val adapter by lazy {
        AppListAdapter(object : AppClick {
            override fun click(app: AppModel) {
                openApp(app)
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
            // limit scroll distance, this causes scrolling to look more controller-like and less
            // app-like. Kind of annoying, but maybe some kind of "quick scroll" could be added
            override fun calculateScrollDistance(velocityX: Int, velocityY: Int): IntArray {
                return super.calculateScrollDistance(velocityX / 4, velocityY / 4)
            }
        }

        snapHelper.attachToRecyclerView(binding.appPager)
        adapter.submitList(emptyList())
        viewModel.appIcons.observe(this, Observer {
            adapter.submitList(it)
        })

        setContentView(binding.root)

        // Update the clock every 5 seconds
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

    /**
     * Adds a new app, uniquely identified by it's launch [Intent] to the start of the list of apps
     * most recently opened, accessible via [getSharedPreferences(APP_NAME, MODE_PRIVATE)] at
     * [KEY_RECENT_APPS]
     */
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
            prefs.edit().putString(KEY_RECENT_APPS, updatedApps.joinToString(SEPARATOR))
                .apply()
        }
    }

    /**
     * Retrieve the list of launch [Intent]s for the most recently opened apps, sorted by recency
     */
    private suspend fun fetchRecentApps(): List<String> {
        return withContext(Dispatchers.IO) {
            val recentApps = prefs.getString(KEY_RECENT_APPS, "")
            return@withContext recentApps?.split(SEPARATOR) ?: emptyList()
        }
    }

    /** Opens an app, updates the recent apps list */
    private fun openApp(app: AppModel) {
        if (app.launchIntent != null) {
            updateRecentAppsList(app.launchIntent)
            startActivity(app.launchIntent)
        }
    }
}