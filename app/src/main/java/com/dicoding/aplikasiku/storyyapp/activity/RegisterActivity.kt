package com.dicoding.aplikasiku.storyyapp.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.aplikasiku.storyyapp.R
import com.dicoding.aplikasiku.storyyapp.api.ApiConfig
import com.dicoding.aplikasiku.storyyapp.databinding.ActivityRegisterBinding
import com.dicoding.aplikasiku.storyyapp.model.RegisterResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RegisterActivity"
    }

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        binding.tvToLogin.setOnClickListener { toLogin() }

        binding.buttonRegister.setOnClickListener { checkRegister() }

        playAnimation()
    }

    // Property Animation here
    // Animation with Object Animator when a views appear
    private fun playAnimation() {
        val welcome = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 1f).setDuration(500)
        val desc = ObjectAnimator.ofFloat(binding.tvDesc, View.ALPHA, 1f).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.edRegisterName, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.edRegisterEmail, View.ALPHA, 1f).setDuration(500)
        val pw = ObjectAnimator.ofFloat(binding.edRegisterPassword, View.ALPHA, 1f).setDuration(500)
        val button = ObjectAnimator.ofFloat(binding.buttonRegister, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.tvToLogin, View.ALPHA, 1f).setDuration(500)
        val together = AnimatorSet().apply {
            playTogether(name, email, pw)
        }

        AnimatorSet().apply {
            playSequentially(welcome, desc, together, button, login)
            start()
        }
    }

    private fun toLogin() {
        val i = Intent(this, LoginActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        finish()
    }

    private fun checkRegister() {
        val name = binding.edRegisterName.text.toString()
        val email = binding.edRegisterEmail.text.toString()
        val password = binding.edRegisterPassword.text.toString()
        val toast = Toast.makeText(applicationContext, R.string.password_less_than_8_char, Toast.LENGTH_SHORT)

        if (!binding.edRegisterName.error.isNullOrEmpty() && !binding.edRegisterPassword.error.isNullOrEmpty() && !binding.edRegisterEmail.error.isNullOrEmpty()) {
            "error"
        } else if (password.length < 8) {
            toast.show()
        } else {
            sendAPI(name, email, password)
        }
    }

    private fun sendAPI(name: String, email: String, password: String) {
        showLoading(true)
        val apiService = ApiConfig.getApiService().register(name, email, password)
        apiService.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                showLoading(false)
                if (response.isSuccessful) {
                    val i = Intent(this@RegisterActivity, LoginActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                    Toast.makeText(
                        this@RegisterActivity,
                        response.body()?.message,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    val loginFailed = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        RegisterResponse::class.java
                    )
                    Toast.makeText(this@RegisterActivity, loginFailed.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
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
}