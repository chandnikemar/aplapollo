package com.example.aplapollo.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.login.LoginRequest
import com.example.aplapollo.repository.APLRepository
import com.example.aplapollo.viewmodel.login.LoginViewModel
import com.example.aplapollo.viewmodel.login.LoginViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityLoginBinding
import es.dmoral.toasty.Toasty


class LoginActivity : AppCompatActivity() {
lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session:   SessionManager
    private var baseUrl: String =""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private  var userDetails: HashMap<String, Any?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()
        session = SessionManager(this)
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        userDetails = session.getUserDetails()
        serverIpSharedPrefText = userDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"

        val aplRepository = APLRepository()
        val viewModelProviderFactory = LoginViewModelFactory(application, aplRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[LoginViewModel::class.java]


        if (Utils.getSharedPrefsBoolean(this@LoginActivity, Constants.KEY_ISLOGGEDIN, false)) {
            if (Utils.getSharedPrefsBoolean(this@LoginActivity, Constants.KEY_ISLOGGEDIN, true)) {
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            }
        }

        binding.buttonLogin.setOnClickListener {
            login()
            //startActivity(Intent(this@LoginActivity,VinRfidMappingActivity::class.java))
        }
        if (session.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }
        viewModel.loginMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {


                                session.createLoginSession(
                                    resultResponse.firstName,
                                    resultResponse.lastName,
                                    resultResponse.email,
                                    resultResponse.mobileNumber.toString(),
                                    resultResponse.isVerified.toString(),
                                    resultResponse.userName,
                                    resultResponse.jwtToken,
                                    resultResponse.refreshToken,
                                    resultResponse.defaultTenantCode,
                                    resultResponse.roleName,
                                )
                                Utils.setSharedPrefsBoolean(
                                    this@LoginActivity,
                                    Constants.LOGGEDIN,
                                    true
                                )
                                startActivity()  // Redirect to the HomeActivity if login is successful

                        } catch (e: Exception) {
                            Toasty.warning(
                                this@LoginActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@LoginActivity,
                            "Login failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()  // Show progress bar while API call is in progress
                }

                else -> {}
            }
        }
    }
    fun login() {
        try {
            // Fetching user credentials from input fields
            val username = binding.edUserName.text.toString().trim()
            val password = binding.edPassword.text.toString().trim()
            val loginUrl = "$baseUrl+login" // Base URL + login endpoint
            Log.d("LoginURL", "Login URL: $loginUrl")

            if (username == "admin" && password == "Pass@123") {
                startAdmin()  // Directly go to admin page if credentials match "admin" and "Pass@123"
            } else {
                // Validate user input
                val validationMessage = validateInput(username, password)
                if (validationMessage == null) {
                    val loginRequest = LoginRequest( username,password)
                    viewModel.login(baseUrl, loginRequest)  // Make the API call with the user credentials
                } else {
                    showErrorMessage(validationMessage)  // Show error message if input is invalid
                }
            }
        } catch (e: Exception) {
            showErrorMessage(e.printStackTrace().toString())
        }
    }

    fun startActivity() {
        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        finish()
    }
    fun startAdmin() {
        startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
    }
    private fun validateInput(userId: String, password: String): String? {
        return when {
            userId.isEmpty() || password.isEmpty() -> "Please enter valid credentials"
            userId.length < 5 -> "Please enter at least 5 characters for the username"
            password.length < 6 -> "Please enter a password with more than 6 characters"
            else -> null
        }
    }
    private fun showErrorMessage(message: String) {
        Toasty.warning(this@LoginActivity, message, Toasty.LENGTH_SHORT).show()
    }


    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }
}