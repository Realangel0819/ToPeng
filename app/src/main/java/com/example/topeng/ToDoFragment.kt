package com.example.topeng

import MyDatabaseHelper
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ToDoFragment : Fragment() {

    private lateinit var todoRecyclerView: RecyclerView
    private val todoList = mutableListOf<Triple<String, String, Boolean>>() // ID, 텍스트, 체크 상태
    private lateinit var adapter: ToDoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        todoRecyclerView = view.findViewById(R.id.todoRecyclerView)

        // RecyclerView 어댑터 설정
        adapter = ToDoAdapter(todoList, { position ->
            showDeleteConfirmationDialog(position) // 삭제 처리
        }, { position ->
            enableEditMode(position) // 수정 처리
        })
        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 초기 데이터 로드
        loadVisibleData()
    }

    fun addNewTodo() {
        val newTodo = "" // 빈 문자열로 새로운 할 일 추가
        val position = todoList.size
        val uniqueId = MyDatabaseHelper(requireContext()).generateUniqueId(position)

        todoList.add(Triple(uniqueId, newTodo, false)) // ID, 텍스트, 초기 체크 상태
        val dbHelper = MyDatabaseHelper(requireContext())
        dbHelper.insertOrUpdateText(uniqueId, newTodo, false) // 데이터베이스에 저장

        adapter.notifyItemInserted(position) // RecyclerView 갱신
        todoRecyclerView.scrollToPosition(position)

        // 추가된 아이템을 즉시 편집 모드로 전환
        todoRecyclerView.post {
            enableEditMode(position)
        }
    }

    private fun loadVisibleData() {
        val dbHelper = MyDatabaseHelper(requireContext())
        val visibleData = dbHelper.getPagedTexts(0, 20) // 필요한 만큼만 가져옴

        if (visibleData.isNotEmpty()) {
            todoList.addAll(visibleData)
            adapter.notifyDataSetChanged()
        }
    }

    private fun enableEditMode(position: Int) {
        val holder = todoRecyclerView.findViewHolderForAdapterPosition(position) as? ToDoAdapter.ToDoViewHolder
        val (id, currentTodo, isChecked) = todoList[position]

        if (holder != null) {
            holder.todoTextView.visibility = View.GONE
            holder.todoEditText.visibility = View.VISIBLE
            holder.todoEditText.setText(currentTodo)
            holder.todoEditText.requestFocus()

            holder.todoEditText.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val updatedText = v.text.toString().trim()

                    if (updatedText.isNotEmpty()) {
                        todoList[position] = Triple(id, updatedText, isChecked)
                        adapter.notifyItemChanged(position)
                        val dbHelper = MyDatabaseHelper(holder.itemView.context)
                        dbHelper.insertOrUpdateText(id, updatedText, isChecked)
                    } else {
                        todoList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        val dbHelper = MyDatabaseHelper(holder.itemView.context)
                        dbHelper.deleteTextById(id)
                    }

                    holder.todoEditText.visibility = View.GONE
                    holder.todoTextView.visibility = View.VISIBLE
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val (id, _, _) = todoList[position]
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("이 할 일을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                todoList.removeAt(position)
                adapter.notifyItemRemoved(position)

                val dbHelper = MyDatabaseHelper(requireContext())
                dbHelper.deleteTextById(id)
            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }
}
