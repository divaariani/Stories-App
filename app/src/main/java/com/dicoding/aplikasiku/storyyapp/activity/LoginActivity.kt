package com.dicoding.aplikasiku.storyyapp.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.aplikasiku.storyyapp.api.ApiConfig
import com.dicoding.aplikasiku.storyyapp.databinding.ActivityLoginBinding
import com.dicoding.aplikasiku.storyyapp.model.LoginResponse
import com.dicoding.aplikasiku.storyyapp.utils.Constant.KEY_EMAIL
import com.dicoding.aplikasiku.storyyapp.utils.Constant.KEY_IS_LOGIN
import com.dicoding.aplikasiku.storyyapp.utils.Constant.KEY_NAME
import com.dicoding.aplikasiku.storyyapp.utils.Constant.KEY_TOKEN
import com.dicoding.aplikasiku.storyyapp.utils.Constant.KEY_USER_ID
import com.dicoding.aplikasiku.storyyapp.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var sharedPref: SessionManager
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()
        sharedPref = SessionManager(this)

        binding.tvToRegister.setOnClickListener { toRegister() }

        binding.buttonLogin.setOnClickListener { checkLogin() }

        playAnimation()
    }

    // Property Animation here
    // Animation with Object Animator when a views appear
    private fun playAnimation() {
        val welcome = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 1f).setDuration(500)
        val desc = ObjectAnimator.ofFloat(binding.tvDesc, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.edLoginEmail, View.ALPHA, 1f).setDuration(500)
        val pw = ObjectAnimator.ofFloat(binding.edLoginPassword, View.ALPHA, 1f).setDuration(500)
        val button = ObjectAnimator.ofFloat(binding.buttonLogin, View.ALPHA, 1f).setDuration(500)
        val register = ObjectAnimator.ofFloat(binding.tvToRegister, View.ALPHA, 1f).setDuration(500)
        val together = AnimatorSet().apply {
            playTogether(email, pw)
        }

        AnimatorSet().apply {
            playSequentially(welcome, desc, together, button, register)
            start()
        }
    }

    private fun toRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        finish()
    }

    private fun checkLogin() {
        val email = binding.edLoginEmail.text.toString()
        val password = binding.edLoginPassword.text.toString()
        val toast = Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)

        if (!binding.edLoginPassword.error.isNullOrEmpty() || !binding.edLoginEmail.error.isNullOrEmpty()) {
            toast.show()
        } else {
            sendAPI(email, password)
        }
    }

    private fun sendAPI(email: String, password: String) {
        showLoading(true)
        val apiService = ApiConfig.getApiService().login(email, password)
        apiService.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val responseBody = response.body()!!.loginResult
                    sharedPref.apply {
                        setBooleanPref(KEY_IS_LOGIN, true)
                        setStringPref(KEY_TOKEN, responseBody.token)
                        setStringPref(KEY_USER_ID, responseBody.userId)
                        setStringPref(KEY_NAME, responseBody.name)
                        setStringPref(KEY_EMAIL, email)
                    }

                    val i = Intent(this@LoginActivity, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                    Toast.makeText(this@LoginActivity, response.body()?.message, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val loginFailed = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        LoginResponse::class.java
                    )
                    Toast.makeText(this@LoginActivity, loginFailed.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "onFailure: " + t.message)
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        val isLogin = sharedPref.isLogin
        if (isLogin) {
            val i = Intent(this@LoginActivity, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }
}