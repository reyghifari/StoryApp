package com.example.submisionintermediate.login

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.submisionintermediate.R
import com.example.submisionintermediate.ViewModelFactory
import com.example.submisionintermediate.databinding.ActivityLoginBinding
import com.example.submisionintermediate.main.MainActivity
import com.example.submisionintermediate.model.UserModel
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.register.RegisterActivity
import com.example.submisionintermediate.utils.Helper

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var user: UserModel
    private lateinit var loginViewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
        playAnimation()

        loginViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        loginViewModel.toast.observe(this) {
            showToast(it)
        }
        binding.tvLoginDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageViewLogin, View.TRANSLATION_X, -30f, 30f ).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

    }

    private fun setupViewModel() {
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore),this)
        )[LoginViewModel::class.java]

        loginViewModel.getUser().observe(this) { user ->
            this.user = user
        }
    }



    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.myLoginEditEmail.text.toString()
            val password = binding.myLoginEditPassword.text.toString()
            when {
                email.isEmpty() -> {
                    binding.myLoginEditEmail.error = "Masukkan email"
                }
                password.isEmpty() -> {
                    binding.myLoginEditPassword.error = "Masukkan password"
                }
                else -> {
                    val user = UserModel(email = email, password = password)
                    loginViewModel.name.observe(this) {
                        user.name = it
                    }
                    loginViewModel.token.observe(this) {
                        user.token = it
                        token = it
                    }
                    loginViewModel.loginUser(user)
                    loginViewModel.toast.observe(this) {
                        if(it == "success") {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        var token = ""
    }

}