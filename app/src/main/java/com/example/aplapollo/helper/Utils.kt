package com.example.aplapollo.helper

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.adapter.OutputDialogAdapter
import com.example.aplapollo.model.BomOutput
import com.example.aplapollo.view.LoginActivity
import com.example.apolloapl.R
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Utils {
    private var isLogoutDialogShowing = false
    fun toast(context: Context?, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun toastLong(context: Context?, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun setSharedPrefs(context: Context, key: String?, value: String?) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getSharedPrefs(context: Context, key: String?): String? {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getString(key, "")
    }

    fun getSharedPrefs(context: Context, key: String?, defValue: String?): String? {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getString(key, defValue)
    }

    fun setSharedPrefsInteger(context: Context, key: String?, value: Int) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getSharedPrefsInteger(context: Context, key: String?): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, 0)
    }

    fun getSharedPrefsDefaultIndex(context: Context, key: String?): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, -1)
    }

    fun getSharedPrefsInteger(context: Context, key: String?, defValue: Int): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, defValue)
    }


    fun setSharedPrefsLong(context: Context, key: String?, value: Long) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getSharedPrefsLong(context: Context, key: String?): Long {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getLong(key, 0)
    }

    fun setSharedPrefsBoolean(context: Context, key: String?, value: Boolean) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getSharedPrefsBoolean(context: Context, key: String?): Boolean {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getBoolean(key, true)
    }

    fun getSharedPrefsBoolean(context: Context, key: String?, defValue: Boolean): Boolean {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getBoolean(key, defValue)
    }

    fun removeSharedPrefs(context: Context, key: String?) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.remove(key)
        editor.apply()
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /* fun handleGeneralResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
         var errorMessage = ""
         if (response.isSuccessful) {
             response.body()?.let { generalResponse ->
                 return Resource.Success(generalResponse)
             }
         } else if (response.errorBody() != null) {
             val errorObject = response.errorBody()?.let {
                 JSONObject(it.charStream().readText())
             }
             errorObject?.let {
                 errorMessage = it.getString("errorMessage")
             }
         }
         return Resource.Error(errorMessage)
     }*/

    fun hasInternetConnection(application: Application): Boolean {
        val connectivityManager = application.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }
    fun showLogoutPopup(
        activity: Activity,
        sessionManager: SessionManager
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Session Expired")
            .setMessage("Your session has expired. Please login again.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                sessionManager.logoutKeepAdminConfig()


                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
                activity.finish()
            }
            .show()
    }
    val todayDate = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(Date())
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateTimeISO(): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        return java.time.LocalDateTime.now().format(formatter)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatGrnDate(dateStr: String): String {
        val dateTime = java.time.LocalDateTime.parse(dateStr)
        return dateTime.toString()
    }

//    private fun getTenant(): String {
//        return tenantCode.ifEmpty {
//            userDetail!!["defaultTenantCode"].toString()
//        }
//    }
fun showSearchableOutputDialog(
    activity: Activity,
    title: String = "Select Item",
    items: List<BomOutput>,
    onSelect: (BomOutput) -> Unit
) {

    val dialog = AlertDialog.Builder(activity).create()

    val view = activity.layoutInflater.inflate(
        R.layout.dialog_output_material,
        null
    )

    dialog.setView(view)

    val recycler = view.findViewById<RecyclerView>(R.id.recyclerOutput)
    val searchView =
        view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchOutput)
    val ivClose = view.findViewById<ImageView>(R.id.ivClose)

    searchView.isIconified = false
    searchView.requestFocus()

    recycler.layoutManager = LinearLayoutManager(activity)

    // Initial Adapter
    val adapter = OutputDialogAdapter(items.toMutableList()) { selected ->
        onSelect(selected)
        dialog.dismiss()
    }

    recycler.adapter = adapter


    ivClose.setOnClickListener {
        dialog.dismiss()
    }

    fun applyFilter(query: String) {

        val filteredList = if (query.isEmpty()) {
            items
        } else {
            items.filter {
                it.outputMaterial?.contains(query, true) == true ||
                        it.materialDescription?.contains(query, true) == true
            }
        }

        adapter.updateList(filteredList)
    }

    searchView.setOnQueryTextListener(
        object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                applyFilter(query.orEmpty().trim())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilter(newText.orEmpty().trim())
                return true
            }
        }
    )

    dialog.show()

    dialog.window?.apply {

        setBackgroundDrawableResource(android.R.color.transparent)

        val displayMetrics = activity.resources.displayMetrics

        val width = (displayMetrics.widthPixels * 0.95).toInt()
        val height = (displayMetrics.heightPixels * 0.70).toInt()

        setLayout(width, height)
    }
}


object WeightValidationUtils {

    fun validateWeight(
        motherWeight: Double,
        totalChildWeight: Double,
        scrapWeight: Double
    ): WeightResult {

        if (totalChildWeight <= 0) {
            return WeightResult.Error("Enter valid Child Weight")
        }

        if (scrapWeight <= 0) {
            return WeightResult.Error("Enter Scrap Weight")
        }

        if (totalChildWeight + scrapWeight > motherWeight) {
            return WeightResult.Error("Total weight exceeds Mother Coil")
        }

        val ironLoss =
            motherWeight - (totalChildWeight + scrapWeight)

        return WeightResult.Success(
            if (ironLoss < 0) 0.0 else ironLoss
        )
    }
}


    fun getWifiMacAddress(): String {
        return try {
            val nif = NetworkInterface.getByName("wlan0")
            val macBytes = nif?.hardwareAddress ?: return ""

            macBytes.joinToString(":") {
                "%02X".format(it)
            }
        } catch (e: Exception) {
            ""
        }
    }
    fun getMacAddress(): String {
        return try {
            val networkInterface =
                NetworkInterface.getByName("wlan0")

            val macBytes = networkInterface?.hardwareAddress
                ?: return ""

            macBytes.joinToString(":") {
                "%02X".format(it)
            }
        } catch (e: Exception) {
            ""
        }
    }
sealed class WeightResult {

    data class Success(val ironLoss: Double) : WeightResult()

    data class Error(val message: String) : WeightResult()
}
    fun formatDate(date: String?): String {

        return try {

            val inputFormat =
                java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    java.util.Locale.getDefault()
                )

            val outputFormat =
                java.text.SimpleDateFormat(
                    "dd-MM-yyyy hh:mm a",
                    java.util.Locale.getDefault()
                )

            val parsedDate = inputFormat.parse(date ?: "")

            outputFormat.format(parsedDate!!)

        } catch (e: Exception) {

            "-"
        }
    }
    fun showErrorDialog(activity: Activity, message: String) {

        if (activity.isFinishing || activity.isDestroyed) return

        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_error)

        dialog.setCancelable(true)

        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)
        val btnOk = dialog.findViewById<AppCompatButton>(R.id.btnOk)
        val ivClose = dialog.findViewById<ImageView>(R.id.ivClose)
        val ivIcon = dialog.findViewById<ImageView>(R.id.ivIcon)


        when {
            message.contains("success", true) -> {
                tvTitle.text = "Success"
                ivIcon.setImageResource(R.drawable.ic_check)
            }

            message.contains("warning", true) -> {
                tvTitle.text = "Warning"
                ivIcon.setImageResource(R.drawable.ic_warning)
            }

            else -> {
                tvTitle.text = "Oops!"
                ivIcon.setImageResource(R.drawable.ic_error)
            }
        }

        tvMessage.text = message

        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        ivClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.apply {

            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

            val width = (displayMetrics.widthPixels * 0.90).toInt()

            setLayout(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            setGravity(Gravity.CENTER)
        }

        dialog.show()
    }}