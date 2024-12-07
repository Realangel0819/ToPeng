package com.example.topeng

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 드로어 레이아웃 및 네비게이션 뷰 초기화
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        // NavigationView의 헤더에 있는 이메일을 표시
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
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

        // Google Play Services 상태 확인
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            apiAvailability.getErrorDialog(this, resultCode, 1001)?.show()
            return // Google Play Services가 없으면 더 진행하지 않음
        }

        // Fragment 추가
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.weatherFragmentContainer, WeatherFragment())
                .commit()

            supportFragmentManager.beginTransaction()
                .replace(R.id.todoFragmentContainer, ToDoFragment())
                .commit()
        }

        // 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // 권한 부여된 경우 Toast만 표시
            Toast.makeText(this, "위치 권한이 이미 허용되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 네비게이션 메뉴 항목 클릭 이벤트 처리
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // 홈: 현재 액티비티 유지
                    Toast.makeText(this, "이미 홈이다펭.", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_mypage -> {
                    // 마이페이지 액티비티로 이동
                    val intent = Intent(this, MyPageActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers() // 드로어 닫기
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "위치 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}