package com.example.topeng

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // FirebaseAuth 초기화
        auth = FirebaseAuth.getInstance()

        val newPasswordEditText = findViewById<EditText>(R.id.editTextNewPassword)
        val changePasswordButton = findViewById<Button>(R.id.buttonChangePassword)

        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()

            if (newPassword.isNotEmpty()) {
                changePassword(newPassword)
            } else {
                Toast.makeText(this, "새 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 비밀번호 변경
    private fun changePassword(newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        finish() // 변경 후 액티비티 종료
                    } else {
                        Toast.makeText(this, "비밀번호 변경 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
