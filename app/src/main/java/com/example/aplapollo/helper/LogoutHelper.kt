

package com.example.aplapollo.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.example.aplapollo.view.LoginActivity

object LogoutHelper {

    fun handleLogout(activity: Activity, session: SessionManager) {
        AlertDialog.Builder(activity)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                session.logoutKeepAdminConfig()
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
                activity.finish()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }
}
