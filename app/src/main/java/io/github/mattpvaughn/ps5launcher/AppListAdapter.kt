package io.github.mattpvaughn.ps5launcher

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.mattpvaughn.ps5launcher.databinding.ListItemAppIconBinding

class AppListAdapter(private val appClick: AppClick) :
    ListAdapter<AppModel, AppListAdapter.AppViewHolder>(AppDiffUtilCallback()) {

    companion object {
        /**
         * This is just a stopgap implementation for the MVP. This matches app package names to
         * custom icons provided by the app
         *
         * TODO: In the future, use an icon pack implementation
         */
        val PACKAGE_NAMES_TO_DRAWABLES = mapOf(
            "spotify" to R.drawable.spotify,
            "authenticator" to R.drawable.authenticator,
            "gearhead" to R.drawable.auto,
            "bloons" to R.drawable.bloons,
            "discord" to R.drawable.discord,
            "firefox" to R.drawable.firefox,
            "com.google.android.gm" to R.drawable.gmail,
            "com.android.mms" to R.drawable.messages,
            "plex" to R.drawable.plex,
            "vending" to R.drawable.play,
            "pocket" to R.drawable.pocketcasts,
            "flym" to R.drawable.rss,
            "snapchat" to R.drawable.snapchat
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder.from(parent, appClick)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppViewHolder private constructor(
        private val binding: ListItemAppIconBinding,
        private val appClick: AppClick
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: AppModel) {
            binding.root.setOnClickListener { appClick.click(app) }
            binding.appName.text = app.appName
            if (app.drawable == null) {
                binding.root.visibility = View.INVISIBLE
                binding.appIconImage.contentDescription = "Placeholder tile"
                binding.appIconImage.setImageBitmap(null)
            } else {
                binding.root.visibility = View.VISIBLE
                binding.appIconImage.contentDescription = app.appName
                var usedCustomIcon = false
                // TODO: refactor to use icon pack implementation
                for (appEntry in PACKAGE_NAMES_TO_DRAWABLES) {
                    if (app.packageName.toLowerCase().contains(appEntry.key)) {
                        binding.appIconImage.setImageResource(appEntry.value)
                        usedCustomIcon = true
                    }
                }
                if (!usedCustomIcon) {
                    try {
                        binding.appIconImage.setImageDrawable(app.drawable)
                    } catch (e: Resources.NotFoundException) {
                        Log.i("", "No resource found for app: ${app.appName}")
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, appClick: AppClick): AppViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAppIconBinding.inflate(layoutInflater, parent, false)
                return AppViewHolder(binding, appClick)
            }
        }
    }


    private class AppDiffUtilCallback : DiffUtil.ItemCallback<AppModel>() {
        override fun areItemsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AppModel, newItem: AppModel): Boolean {
            return oldItem == newItem
        }
    }
}


interface AppClick {
    fun click(app: AppModel)
}
