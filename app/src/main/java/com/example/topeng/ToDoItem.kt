package com.example.topeng

data class ToDoItem(
    val id: String,       // 항목 고유 ID
    val text: String,     // 할 일 텍스트
    var isChecked: Boolean // 체크 상태 (완료 여부)
)
