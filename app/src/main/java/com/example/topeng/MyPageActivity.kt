package com.example.topeng

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MyPageActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // 드로어 레이아웃 및 네비게이션 뷰 초기화
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        // NavigationView의 헤더에 있는 이메일을 표시
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val headerView = navigationView.getHeaderView(0) // 헤더 레이아웃 참조

        val userEmailTextView = headerView.findViewById<TextView>(R.id.textViewEmail)

        // SharedPreferences에서 이메일 가져오기
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", "guest@example.com") // 기본값을 guest@example.com으로 설정

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
                    Toast.makeText(this, "이미 마이페이지다펭.", Toast.LENGTH_SHORT).show()
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

    private fun handleLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun handleAccountDeletion() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun navigateToChangePassword() {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(intent)
    }
    override fun onBackPressed() {
        // 아무 동작도 하지 않아서 뒤로 가기 버튼의 기본 동작을 막습니다.
    }
}
