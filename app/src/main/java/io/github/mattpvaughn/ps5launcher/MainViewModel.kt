package io.github.mattpvaughn.ps5launcher

import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_LAUNCHER
import android.content.pm.ResolveInfo
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The [ViewModel] for the main launcher screen.
 *
 * TODO: refactor out [applicationContext] for testing. Provide the resources via calls to/from
 *       the MainActivity instead
 */
class MainViewModel(private val applicationContext: Context) : ViewModel() {

    // The most recently opened apps
    private var recentApps: List<String>? = null

    @Suppress("UNCHECKED_CAST")
    class Factory(private val applicationContext: Context) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(applicationContext) as T
            } else {
                throw IllegalArgumentException("Cannot instantiate $modelClass from FirstViewModel.Factory")
            }
        }
    }

    private val NO_APP = AppModel(null, "", "", null)

    private val _appIcons = MutableLiveData<List<AppModel>>(emptyList())
    val appIcons: LiveData<List<AppModel>>
        get() = _appIcons

    fun updateRecent(updated: List<String>) {
        recentApps = updated
        loadPackages()
    }

    /**
     * Load all installed apps on the device. Put apps matching [recentApps] at the start of the
     * list, retaining the same order as [recentApps]
     *
     * TODO: some perf could probably be gained by using the Java Stream API, or using mutable lists,
     *       there's a lot of lists being recreated
     */
    private fun loadPackages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // TODO- move applicationContext to the View layer for unit-testability
                val pm = applicationContext.packageManager
                val packages = applicationContext.packageManager.queryIntentActivities(
                    Intent(Intent.ACTION_MAIN).apply { this.addCategory(CATEGORY_LAUNCHER) },
                    0
                ).sortedBy { it.loadLabel(pm).toString() }

                // Order the apps list so it matches [recentApps]
                val editable = mutableListOf<ResolveInfo>()
                recentApps?.forEach { recentApp ->
                    packages.forEach {
                        val componentString =
                            pm.getLaunchIntentForPackage(it.activityInfo.packageName)?.component?.flattenToString()
                        if (recentApp == componentString) {
                            editable.add(it)
                        }
                    }
                }

                editable.addAll(packages.filter { !editable.contains(it) })
                val apps = editable.map {
                    AppModel(
                        it.loadIcon(applicationContext.packageManager),
                        it.loadLabel(applicationContext.packageManager) as String,
                        it.activityInfo.packageName,
                        pm.getLaunchIntentForPackage(it.activityInfo.packageName)
                    )
                }
                _appIcons.postValue(apps.padList())
            }
        }
    }

    /**
     * Pad the apps list with empty apps at the start and end because we want the first app
     * to start at the focused position, and so the last app can be brought to the focus position.
     *
     * TODO: this will vary depending on device size. Maybe move this behavior to the LayoutManager
     */
    private fun List<AppModel>.padList(): List<AppModel> {
        val ml = toMutableList()
        ml.add(0, NO_APP)
        ml.add(ml.size, NO_APP)
        ml.add(ml.size, NO_APP)
        ml.add(ml.size, NO_APP)
        ml.add(ml.size, NO_APP)
        return ml
    }

}
