package com.example.topeng

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPageActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()  // Firestore 인스턴스


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // FirebaseAuth 초기화
        auth = FirebaseAuth.getInstance()

        // 드로어 레이아웃 및 네비게이션 뷰 초기화
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val headerView = navigationView.getHeaderView(0) // 헤더 레이아웃 참조
        val userEmailTextView = headerView.findViewById<TextView>(R.id.textViewEmail)
        // FirebaseAuth 인스턴스 초기화
        val auth = FirebaseAuth.getInstance()

        // Firebase에서 현재 로그인한 사용자 가져오기
        val user = auth.currentUser

        // 이메일이 존재하면 이메일을 텍스트뷰에 설정, 없으면 기본값 설정
        val userEmail = user?.email ?: "guest@example.com" // 현재 로그인된 사용자가 없다면 기본값 사용

        // 이메일 텍스트 설정
        userEmailTextView.text = userEmail
        // 이메일 텍스트 설정
        userEmailTextView.text = userEmail

        // 툴바 설정
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 햄버거 메뉴 토글 설정
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 네비게이션 메뉴 항목 클릭 이벤트 처리
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // 홈 액티비티로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_mypage -> {
                    // 마이페이지 :현재 액티비티 유지
                    Toast.makeText(this, "이미 마이페이지입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawers() // 드로어 닫기
            true
        }

        // 버튼 초기화 및 동작 설정
        val logoutButton = findViewById<Button>(R.id.buttonLogout)
        val deleteAccountButton = findViewById<Button>(R.id.buttonDeleteAccount)
        val changePasswordButton = findViewById<Button>(R.id.buttonChangePassword)

        // 로그아웃 버튼 클릭 시
        logoutButton.setOnClickListener {
            handleLogout()
        }

        // 계정 삭제 버튼 클릭 시
        deleteAccountButton.setOnClickListener {
            handleAccountDeletion()
        }

        // 비밀번호 변경 버튼 클릭 시
        changePasswordButton.setOnClickListener {
            navigateToChangePassword()
        }
    }

    // 로그아웃 처리
    private fun handleLogout() {
        // Firebase에서 로그아웃 처리
        FirebaseAuth.getInstance().signOut()

        // 로그아웃 후, LoginActivity로 이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }


    // 계정 삭제 처리
    private fun handleAccountDeletion() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            // Firestore에서 해당 사용자의 모든 todo 항목 삭제
            db.collection("users")
                .document(userId)
                .collection("todos")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("users")
                            .document(userId)
                            .collection("todos")
                            .document(document.id)
                            .delete()
                    }

                    // todo 항목 삭제 후 계정 삭제
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "계정과 모든 할 일이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "계정 삭제 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "할 일 삭제 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    // 비밀번호 변경 화면으로 이동
    private fun navigateToChangePassword() {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        // 아무 동작도 하지 않아서 뒤로 가기 버튼의 기본 동작을 막습니다.
    }
}
