package com.example.topeng

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
        adapter = ToDoAdapter(todoList, { position -> // 길게 클릭 시 삭제 처리
            showDeleteConfirmationDialog(position)
        }, { position -> // 한 번 클릭 시 수정 처리
            enableEditMode(position)
        })
        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 할 일 추가 버튼 클릭 이벤트
        addTodoButton.setOnClickListener {
            addNewTodo()
        }
    }

    private fun addNewTodo() {
        val newTodo = "" // 빈 문자열로 새로운 할 일 추가
        todoList.add(newTodo) // 데이터 추가
        val position = todoList.size - 1
        adapter.notifyItemInserted(position) // RecyclerView 갱신

        // 새로운 아이템이 화면에 보이도록 스크롤
        todoRecyclerView.scrollToPosition(position)

        // 추가된 아이템을 즉시 편집 모드로 전환
        todoRecyclerView.post {
            enableEditMode(position)
        }
    }

    private fun enableEditMode(position: Int) {
        // 기존 코드
        val holder = todoRecyclerView.findViewHolderForAdapterPosition(position) as? ToDoAdapter.ToDoViewHolder
        val currentTodo = todoList[position]

        // 뷰 홀더가 null인지 확인
        if (holder != null) {
            // TextView를 숨기고 EditText를 보이게 함
            holder.todoTextView.visibility = View.GONE
            holder.todoEditText.visibility = View.VISIBLE
            holder.todoEditText.setText(currentTodo)

            // EditText 속성 설정
            holder.todoEditText.isSingleLine = true
            holder.todoEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            holder.todoEditText.inputType = InputType.TYPE_CLASS_TEXT

            // 포커스를 주고 키보드를 보이게 함
            holder.todoEditText.requestFocus()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(holder.todoEditText, InputMethodManager.SHOW_IMPLICIT)

            // Enter 키 눌렀을 때 처리
            holder.todoEditText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val updatedText = v.text.toString().trim()
                    if (updatedText.isNotEmpty()) {
                        todoList[position] = updatedText // 수정된 텍스트로 업데이트
                        adapter.notifyItemChanged(position) // RecyclerView 갱신
                    } else {
                        // 텍스트가 비어있다면 아이템 삭제
                        todoList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }

                    // EditText를 숨기고 TextView로 되돌림
                    holder.todoEditText.visibility = View.GONE
                    holder.todoTextView.visibility = View.VISIBLE

                    // 키보드 숨기기
                    hideKeyboard(v)
                    true
                } else {
                    false
                }
            }
        } else {
            // 뷰 홀더를 찾지 못한 경우 (예: 아이템이 화면에 보이지 않는 경우)
            todoRecyclerView.scrollToPosition(position)
            todoRecyclerView.post {
                enableEditMode(position)
            }
        }
    }


    // 키보드를 숨기는 함수
    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("이 할 일을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                todoList.removeAt(position) // 리스트에서 삭제
                adapter.notifyItemRemoved(position) // RecyclerView 갱신
            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }
}
