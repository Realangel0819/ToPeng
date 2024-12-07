package com.example.topeng

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MyDatabaseHelper(private val context: Context) {  // context를 생성자에서 전달받음

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Firestore에서 할 일 목록을 가져오는 메서드
    suspend fun getPagedToDoItems(offset: Int, limit: Int): List<ToDoItem> {
        val todoItems = mutableListOf<ToDoItem>()

        // 현재 로그인된 사용자의 ID를 가져옵니다.
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("로그인이 필요합니다.")
            return todoItems
        }

        // Firestore에서 사용자의 할 일 목록을 가져옵니다.
        val snapshot = db.collection("users")
            .document(userId)
            .collection("todos")
            .orderBy("id") // id 필드를 기준으로 정렬
            .limit(limit.toLong())
            .startAfter(offset.toLong()) // offset 구현
            .get()
            .await()

        // 가져온 데이터를 todoItems에 추가
        for (document in snapshot) {
            val todoItem = document.toObject(ToDoItem::class.java)
            todoItems.add(todoItem)
        }

        return todoItems
    }

    // 할 일 항목 저장 또는 업데이트
    suspend fun insertOrUpdateToDoItem(todoItem: ToDoItem) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("로그인이 필요합니다.")
            return
        }

        val todoRef = db.collection("users")
            .document(userId)
            .collection("todos")
            .document(todoItem.id)

        // Firestore에 저장
        todoRef.set(todoItem)
            .addOnSuccessListener {
                showToast("할 일이 저장되었습니다.")
            }
            .addOnFailureListener { e ->
                showToast("할 일 저장 실패: ${e.message}")
            }
    }

    // 할 일 항목 삭제
    suspend fun deleteToDoItemById(id: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("로그인이 필요합니다.")
            return
        }

        val todoRef = db.collection("users")
            .document(userId)
            .collection("todos")
            .document(id)

        todoRef.delete()
            .addOnSuccessListener {
                showToast("할 일이 삭제되었습니다.")
            }
            .addOnFailureListener { e ->
                showToast("할 일 삭제 실패: ${e.message}")
            }
    }

    // 고유 ID 생성 (시간 기반)
    fun generateUniqueId(): String {
        return System.currentTimeMillis().toString() // 시간 기반으로 고유 ID 생성
    }

    // 모든 할 일 삭제
    suspend fun deleteAllToDoItems() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("로그인이 필요합니다.")
            return
        }

        val todosRef = db.collection("users")
            .document(userId)
            .collection("todos")

        // 모든 할 일 삭제
        todosRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                todosRef.document(document.id).delete()
            }
        }
        showToast("모든 할 일이 삭제되었습니다.")
    }

    // Toast 메시지 출력
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
