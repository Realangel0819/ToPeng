package com.example.topeng

import MyDatabaseHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ToDoAdapter(
    private val todoList: MutableList<Triple<String, String, Boolean>>, // ID, 텍스트, 체크 상태
    private val onItemLongClick: (Int) -> Unit, // 길게 클릭 시 삭제 처리
    private val onItemClick: (Int) -> Unit // 한 번 클릭 시 수정 처리
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val (id, text, isChecked) = todoList[position]

        // 텍스트 설정
        holder.todoTextView.text = text

        // 체크박스 상태 설정
        holder.todoCheckBox.setOnCheckedChangeListener(null) // 리스너 제거
        holder.todoCheckBox.isChecked = isChecked

        // 체크박스 상태 변경 리스너
        holder.todoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // 리스트 업데이트
            todoList[position] = Triple(id, text, isChecked)

            // 데이터베이스 업데이트
            val dbHelper = MyDatabaseHelper(holder.itemView.context)
            dbHelper.insertOrUpdateText(id, text, isChecked)
        }

        // 아이템 클릭 처리 (수정 모드 활성화)
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }

        // 아이템 길게 클릭 처리 (삭제 확인)
        holder.itemView.setOnLongClickListener {
            onItemLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int = todoList.size

    class ToDoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val todoCheckBox: CheckBox = view.findViewById(R.id.todoCheckBox) // 체크박스 ID
        val todoTextView: TextView = view.findViewById(R.id.todoTextView) // 텍스트뷰 ID
        val todoEditText: EditText = view.findViewById(R.id.todoEditText) // 수정용 EditText
    }
}
