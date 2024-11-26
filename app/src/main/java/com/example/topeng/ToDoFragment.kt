package com.example.topeng

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ToDoFragment : Fragment() {

    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var addTodoButton: Button
    private val todoList = mutableListOf<String>() // To-Do 리스트 데이터
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

        // View 초기화
        todoRecyclerView = view.findViewById(R.id.todoRecyclerView)
        addTodoButton = view.findViewById(R.id.addTodoButton)

        // RecyclerView 어댑터 설정
        adapter = ToDoAdapter(todoList)
        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 할 일 추가 버튼 클릭 이벤트
        addTodoButton.setOnClickListener {
            addNewTodo()
        }
    }

    private fun addNewTodo() {
        val newTodo = "새로운 할 일 ${todoList.size + 1}"
        todoList.add(newTodo) // 데이터 추가
        adapter.notifyItemInserted(todoList.size - 1) // RecyclerView 갱신
        Toast.makeText(requireContext(), "할 일이 추가되었습니다.", Toast.LENGTH_SHORT).show()
    }
}
