package com.nebi.mongodb

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mongodb.stitch.android.core.Stitch
import com.mongodb.stitch.android.core.auth.StitchUser
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential

class MainActivity : AppCompatActivity() {

    lateinit var currentUser : StitchUser

    private var statusCode : Byte = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Login Screen"

        val client = Stitch.initializeAppClient(getString(R.string.my_app_id))

        client.auth.user?.let{

                currentUser = it

                if (!currentUser.isLoggedIn){

                    print("not logged in")

                    //      finish()

                } else if (currentUser.isLoggedIn){

                    print("logged in")

                    val intent = Intent(this, QueryActivity::class.java)

                    startActivity(intent)

                    finish()
                }

        }?: run{

            val emailPassClient = client.auth.getProviderClient(
                UserPasswordAuthProviderClient.factory)

            val email : EditText = findViewById(R.id.email_text)

            val password : EditText = findViewById(R.id.password_text)

            val registerButton : Button = findViewById(R.id.register_button)

            val spinner : ProgressBar = findViewById(R.id.main_progressBar)

            val resendConfirmation : Button = findViewById(R.id.resend_confirmation)

            spinner.visibility = View.GONE

            resendConfirmation.setOnClickListener{

                spinner.visibility = View.VISIBLE

                emailPassClient.resendConfirmationEmail(email.text.toString()).addOnCompleteListener{
                    if (it.isSuccessful){

                        val message = "A confirmation email was sent to email address: " + email.text.toString() +
                                "\nPlease check your email address and confirm in 30 minutes"

                        createDialog(message, "", "Confirmation Email", "Login for The First Time")

                        spinner.visibility = View.GONE

                    } else {

                        when(it.exception!!.message){
                            "user not found" ->{
                                createDialog("User not found", "Error", "Error")
                            }
                            "already confirmed" -> {
                                createDialog("User already confirmed", "Error", "Error")
                            }
                        }

                        spinner.visibility = View.GONE

                    }
                }
            }

            registerButton.setOnClickListener {

                spinner.visibility = View.VISIBLE

                emailPassClient.registerWithEmail(email.text.toString(), password.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {

                             val message = "A confirmation email was sent to email address: " + email.text.toString() +
                                        "\nPlease check your email address and confirm in 30 minutes"

                            createDialog(message, "", "Confirmation Email", "Login for The First Time")

                            spinner.visibility = View.GONE

                        } else {

                            spinner.visibility = View.GONE

                            when (it.exception!!.message) {
                                "name already in use" -> {
                                    createDialog(email.text.toString() + " is already in use", "Error", "Error")
                                }
                                "email invalid" -> {
                                    createDialog("Please enter a valid email address", "Error", "Error")
                                }
                                "password must be between 6 and 128 characters" -> {
                                    createDialog("Please enter a password with minimum 6 characters", "Error", "Error")
                                }
                            }

                        }

                    }
            }

            val loginButton : Button = findViewById(R.id.login_button)

            loginButton.setOnClickListener{

                spinner.visibility = View.VISIBLE

                if (email.text != null && password.text != null){
                val credential = UserPasswordCredential(email.text.toString(), password.text.toString())
                client.auth.loginWithCredential(credential).addOnCompleteListener {
                    //  task -> bu sekilde de kullanilabiliyor!
                    try {
                        if (it.isSuccessful) {

                            statusCode = 1
                            onPostExecute()
//
//                            Toast.makeText(this, it.result.id, Toast.LENGTH_LONG).show()

                            spinner.visibility = View.GONE
                        } else {

                            when (it.exception!!.message) {

                                "invalid username" -> {
                                    createDialog(email.text.toString() + " invalid user name", "Error", "Error")
                                }
                                "invalid username/password" -> {
                                    createDialog("Invalid username or password", "Error", "Error")
                                }
                                "invalid password" -> {
                                    createDialog("Invalid password", "Error", "Error")
                                }
                            }

                            spinner.visibility = View.GONE

                        }

                    } catch (e: Exception) {
                        spinner.visibility = View.GONE
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
            }
        }

    }

    private fun onPostExecute() {

        if (statusCode == 1.toByte()) {

            Log.d("stitch-auth", "Authentication Successful.")

            Toast.makeText(this, "Nebi welcomes you!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, QueryActivity::class.java)

            startActivity(intent)

            finish()

        } else {
            Log.e("stitch-auth", "Authentication Failed.")

            Toast.makeText(this, "Ugh...! Error in recognizing.", Toast.LENGTH_SHORT).show()

        }
    }

    private fun createDialog(message: String, type: String, dialogTitle: String, activityTitle: String = "MongoDB Test"){

        val builder = AlertDialog.Builder(this@MainActivity)

        builder.setTitle(dialogTitle)

        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialog, which -> }

        val dialog: AlertDialog = builder.create()

        dialog.show()

        val confirmationDiagButton: Button =
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        confirmationDiagButton.setOnClickListener{
            dialog.dismiss()
            title = activityTitle
        }

        if (type == "Error")
            confirmationDiagButton.setTextColor(Color.RED)
        else
            confirmationDiagButton.setTextColor(Color.GREEN)

    }

}
