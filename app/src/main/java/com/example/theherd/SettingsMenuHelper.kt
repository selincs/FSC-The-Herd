package com.example.theherd

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.PopupMenu

object SettingsMenuHelper {

    //Consolidates the settings menu code into one Helper class, so every page doesn't need these 40 extra lines
    fun showSettingsMenu(activity: Activity, anchor: View) {
        val popupMenu = PopupMenu(anchor.context, anchor)

        popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            println("Menu item clicked: ${item.itemId} via SettingsHelper")
            when (item.itemId) {

                R.id.menu_account_settings -> {
                    val intent = Intent(activity, AccountSettingsActivity::class.java)
                    activity.startActivity(intent)
                    true
                }

                R.id.menu_logout -> {
                    println("Logout clicked via SettingsHelper")
                    SessionManager.logout()

                    val intent = Intent(activity, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    activity.startActivity(intent)

                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }
}