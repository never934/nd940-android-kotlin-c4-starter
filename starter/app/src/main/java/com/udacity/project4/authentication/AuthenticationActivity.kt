package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
import org.koin.android.ext.android.inject


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    val viewModel: AuthenticationViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        viewModel.authenticationState.observe(this, Observer { authState ->
            when (authState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    startRemindersActivity()
                }
                else -> {}
            }
        })
        loginBtn.setOnClickListener {
            launchSignInFlow()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                startRemindersActivity()
            }
        }
    }

    private fun startRemindersActivity() {
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

    private fun launchSignInFlow() {
        val customLayout = AuthMethodPickerLayout.Builder(R.layout.layout_auth)
            .setGoogleButtonId(R.id.googleBtn)
            .setEmailButtonId(R.id.emailBtn)
            .build()
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    companion object {
        const val SIGN_IN_RESULT_CODE = 1001
    }
}
