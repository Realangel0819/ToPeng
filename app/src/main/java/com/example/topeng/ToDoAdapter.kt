package com.example.topeng

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ToDoAdapter(
    private val todoList: MutableList<ToDoItem>,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val todoItem = todoList[position]
        holder.bind(todoItem)

        // 체크박스 상태 변경 시
        holder.todoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            todoItem.isChecked = isChecked

            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Firestore에서 ToDoItem을 업데이트
                db.collection("users")
                    .document(userId)
                    .collection("todos")
                    .document(todoItem.id)
                    .set(todoItem)  // Firestore에 저장
                    .addOnSuccessListener {
                        // 체크 상태 변경 후 메시지 표시
                        if (isChecked) {
                            Toast.makeText(holder.itemView.context, "고생했어 펭!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(holder.itemView.context, "데이터 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 짧은 클릭: 수정 모드로 진입
        holder.itemView.setOnClickListener {
            onEditClick(position)
        }

        // 길게 클릭: 삭제 확인 다이얼로그 표시
        holder.itemView.setOnLongClickListener {
            onDeleteClick(position)
            true // 길게 클릭했을 때 삭제 처리 후 true 리턴
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoTextView: TextView = itemView.findViewById(R.id.todoTextView)
        val todoCheckBox: CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val todoEditText: EditText = itemView.findViewById(R.id.todoEditText)

        fun bind(todoItem: ToDoItem) {
            todoTextView.text = todoItem.text
            todoCheckBox.isChecked = todoItem.isChecked  // 체크박스 상태 반영
            todoEditText.setText(todoItem.text)
        }
    }
}
