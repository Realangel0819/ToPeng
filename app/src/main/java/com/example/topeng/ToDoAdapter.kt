package com.example.topeng

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


class ToDoAdapter(
    private val todoList: MutableList<String>,
    private val onItemLongClick: (Int) -> Unit, // 길게 클릭 시 삭제 처리
    private val onItemClick: (Int) -> Unit // 한 번 클릭 시 수정 처리
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val todo = todoList[position]
        holder.todoTextView.text = todo // 초기 텍스트 설정
        holder.todoCheckBox.isChecked = false // 체크박스 상태 초기화

        // 한 번 클릭 시 수정 처리
        holder.itemView.setOnClickListener {
            onItemClick(position) // 수정 처리
        }

        // 아이템 길게 클릭 시 삭제 여부 알림
        holder.itemView.setOnLongClickListener {
            onItemLongClick(position) // 삭제 처리
            true
        }
    }

    override fun getItemCount(): Int = todoList.size

    class ToDoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val todoCheckBox: CheckBox = view.findViewById(R.id.todoCheckBox)
        val todoTextView: TextView = view.findViewById(R.id.todoTextView)
        val todoEditText: EditText = view.findViewById(R.id.todoEditText) // EditText 추가
    }
}
