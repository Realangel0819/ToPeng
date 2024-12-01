package com.example.topeng

import MyDatabaseHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ToDoAdapter(
    private val todoList: MutableList<ToDoItem>,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val todoItem = todoList[position]
        holder.bind(todoItem)

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
        val todoEditText: EditText = itemView.findViewById(R.id.todoEditText)

        fun bind(todoItem: ToDoItem) {
            todoTextView.text = todoItem.text
            todoEditText.setText(todoItem.text)
        }
    }
}
