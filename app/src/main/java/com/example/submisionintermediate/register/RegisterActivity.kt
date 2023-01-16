package com.example.submisionintermediate.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.submisionintermediate.R
import com.example.submisionintermediate.ViewModelFactory
import com.example.submisionintermediate.databinding.ActivityRegisterBinding
import com.example.submisionintermediate.login.LoginActivity
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.utils.Helper

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
        playAnimation()

        binding.tvDaftarLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f ).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(500)
        val nama = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(500)
        val namaet = ObjectAnimator.ofFloat(binding.editNameDaftar, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(500)
        val emailet = ObjectAnimator.ofFloat(binding.myEditEmailDaftar, View.ALPHA, 1f).setDuration(500)
        val pass = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(500)
        val passet = ObjectAnimator.ofFloat(binding.myEditPasswordDaftar, View.ALPHA, 1f).setDuration(500)
        val daftar = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(500)
        val desakun = ObjectAnimator.ofFloat(binding.tvDaftarLogin, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(title,nama,namaet,email,emailet,pass,passet,daftar,desakun)
            start()
        }
    }

    private fun setupViewModel() {
        registerViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[RegisterViewModel::class.java]
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
        binding.signupButton.setOnClickListener {
            val name = binding.editNameDaftar.text.toString()
            val email = binding.myEditEmailDaftar.text.toString()
            val password = binding.myEditPasswordDaftar.text.toString()
            when {
                name.isEmpty() -> {
                    binding.editNameDaftar.error = "Masukkan nama"
                }
                email.isEmpty() -> {
                    binding.myEditEmailDaftar.error = "Masukkan email"
                }
                password.isEmpty() -> {
                    binding.myEditPasswordDaftar.error = "Masukkan password"
                }
                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                    registerViewModel.register(name, email, password, object : Helper.ApiCallbackString {
                        override fun onResponse(success: Boolean, message: String) {
                            showAlert(success, message)
                        }
                    })
                }
            }
        }

    }

    private fun showAlert(boolean: Boolean, message: String) {
        if (boolean) {
            AlertDialog.Builder(this).apply {
                binding.progressBar.visibility = View.GONE
                        setTitle(getString(R.string.Yes))
                        setMessage(getString(R.string.createsukses))
                        setPositiveButton(getString(R.string.next)) { _, _ ->

                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        }
                        create()
                        show()
            }
        } else {
            AlertDialog.Builder(this).apply {
                binding.progressBar.visibility = View.GONE
                setTitle(getString(R.string.no))
                setMessage(getString(R.string.createfailed) + "$message")
                setPositiveButton(getString(R.string.again)) { _, _ ->
                }
                create()
                show()
            }
        }
    }
}