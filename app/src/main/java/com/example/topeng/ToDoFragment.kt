package com.example.topeng

import MyDatabaseHelper
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ToDoFragment : Fragment() {

    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var addTodoButton: Button
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

        // View 초기화
        todoRecyclerView = view.findViewById(R.id.todoRecyclerView)
        addTodoButton = view.findViewById(R.id.addTodoButton)

        // SQLite에서 데이터 불러오기
        loadDataFromDatabase()

        // RecyclerView 어댑터 설정
        adapter = ToDoAdapter(todoList, { position ->
            showDeleteConfirmationDialog(position) // 삭제 처리
        }, { position ->
            enableEditMode(position) // 수정 처리
        })
        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 할 일 추가 버튼 클릭 이벤트
        addTodoButton.setOnClickListener {
            addNewTodo()
        }
    }

    // SQLite에서 데이터 불러오기
    private fun loadDataFromDatabase() {
        val dbHelper = MyDatabaseHelper(requireContext())
        val savedData = dbHelper.getAllTexts()

        // SQLite에서 불러온 데이터를 todoList에 추가
        todoList.clear()
        todoList.addAll(savedData) // Triple<ID, TEXT, isChecked> 추가
    }

    private fun addNewTodo() {
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

    private fun enableEditMode(position: Int) {
        val holder = todoRecyclerView.findViewHolderForAdapterPosition(position) as? ToDoAdapter.ToDoViewHolder
        val (id, currentTodo, isChecked) = todoList[position]

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
                        todoList[position] = Triple(id, updatedText, isChecked)
                        adapter.notifyItemChanged(position)

                        // 데이터베이스 업데이트
                        val dbHelper = MyDatabaseHelper(holder.itemView.context)
                        dbHelper.insertOrUpdateText(id, updatedText, isChecked)
                    } else {
                        todoList.removeAt(position)
                        adapter.notifyItemRemoved(position)

                        // 데이터베이스에서 삭제
                        val dbHelper = MyDatabaseHelper(holder.itemView.context)
                        dbHelper.deleteTextById(id)
                    }

                    holder.todoEditText.visibility = View.GONE
                    holder.todoTextView.visibility = View.VISIBLE
                    hideKeyboard(v)
                    true
                } else {
                    false
                }
            }
            // EditText의 포커스 변경 처리
            holder.todoEditText.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    // 포커스가 사라지면 기존 상태로 복원
                    holder.todoEditText.visibility = View.GONE
                    holder.todoTextView.visibility = View.VISIBLE
                    hideKeyboard(v)
                }
            }
        }
    }

    // 키보드를 숨기는 함수
    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val (id, _, _) = todoList[position]
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("이 할 일을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                todoList.removeAt(position)
                adapter.notifyItemRemoved(position)

                val dbHelper = MyDatabaseHelper(requireContext())
                dbHelper.deleteTextById(id) // 데이터베이스에서 삭제
            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }
}
