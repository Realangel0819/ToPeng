## ToPeng 🐧
![home](pic/no1.png)

## 프로젝트 소개
- 앱은 사용자가 로그인 후 위치 기반 날씨 정보와 투두 리스트를 함께 관리할 수 있는 앱이에요. 
- 날씨 API를 통해 실시간 날씨를 제공하고, 투두 리스트를 통해 할 일을 쉽게 추가하고 관리할 수 있어요. 
- 위치 추적을 통해 사용자의 정확한 날씨 정보를 제공해요.

## 1. 개발 환경
프론트엔드: Kotlin, Android Studio, XML
백엔드: OpenWeatherMap API (날씨 정보 제공), SQLite(todo저장)

## 2. 프로젝트 구조

```text
main
│
├── java/com/example/topeng
│   ├── ui/theme
│   ├── ChangePasswordActivity.kt
│   ├── LoginActivity.kt
│   ├── MainActivity.kt
│   ├── MyDatabaseHelper.kt
│   ├── MyPageActivity.kt
│   ├── RegisterActivity.kt
│   ├── ToDoAdapter.kt
│   ├── ToDoFragment.kt
│   ├── ToDoItem.kt
│   ├── WeatherApiService.kt
│   ├── WeatherFragment.kt
│   └── WeatherResponse.kt
│
└── res
    ├── AndroidManifest.xml
    ├── drawable
    ├── layout
    ├── values
    └── ...

test
│
└── java/com/example/topeng
    └── ExampleUnitTest.kt

.gitignore
build.gradle.kts
proguard-rules.pro
```

## 3. 기능소개
1. 회원가입 화면
회원가입 화면에서는 이메일, 비밀번호, 그리고 비밀번호 확인을 입력받습니다. 사용자가 입력한 정보를 기반으로 회원가입을 진행할 수 있으며, 비밀번호 확인 필드는 사용자가 입력한 비밀번호가 일치하는지 확인합니다.
![register](pic/register.png)
<br>
2. 로그인 화면
로그인 화면에서는 이메일과 비밀번호를 입력받고, 입력된 정보로 로그인할 수 있습니다. 사용자가 입력한 정보가 유효하면 로그인이 성공적으로 처리됩니다. 또한, 회원가입 버튼을 클릭하면 회원가입 화면으로 이동할 수 있습니다.
![login](pic/login.png)
<br>
3. 비밀번호 변경 화면
비밀번호 변경 화면에서는 사용자가 새 비밀번호와 새 비밀번호 확인을 입력하여 비밀번호를 변경할 수 있습니다. 변경 버튼을 클릭하면 비밀번호가 성공적으로 변경됩니다.
![passwordchange](pic/passwordchange.png)
<br>
4.메인 화면
메인 화면에서는 날씨 정보와 할 일 목록을 한눈에 볼 수 있습니다. 화면 상단에는 날씨 정보가 실시간으로 표시되고, 아래에는 할 일 추가하기 버튼과 체크리스트가 나타납니다. 사용자는 할 일을 추가하고, 완료된 항목을 체크하여 쉽게 관리할 수 있습니다.
![main](pic/main.png)
<br>
5. 마이 페이지
마이 페이지에서는 사용자의 날씨 정보와 할 일 목록을 관리할 수 있습니다. 화면 상단에는 현재 날짜, 위치, 온도, 날씨 상태가 표시되고, 하단에는 새로운 할 일 추가 버튼과 할 일 체크박스가 포함됩니다. 사용자는 할 일을 추가하거나 체크하여 관리할 수 있습니다.
![mypage](pic/mypage.png)
<br>
6. 네비게이션 드로어
네비게이션 드로어에서 사용자는 앱의 홈 화면, 마이 페이지 등 다양한 화면으로 이동할 수 있습니다. 화면 왼쪽 상단에 메뉴 아이콘을 클릭하면 드로어가 열리며, 사용자 정보와 함께 회원가입, 로그아웃, 설정 등 여러 메뉴를 선택할 수 있습니다.
![NavigationDrawer](pic/NavigationDrawer.png)
<br>

