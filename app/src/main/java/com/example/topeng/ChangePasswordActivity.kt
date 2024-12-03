package com.example.topeng

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val newPasswordInput = findViewById<EditText>(R.id.editTextNewPassword)
        val confirmPasswordInput = findViewById<EditText>(R.id.editTextConfirmPassword)
        val changePasswordButton = findViewById<Button>(R.id.buttonChangePassword)

        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 비밀번호 변경 후 이전 화면으로 돌아가기
            } else {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다. 다시 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
