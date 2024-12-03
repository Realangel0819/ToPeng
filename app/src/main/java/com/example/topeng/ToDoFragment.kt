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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



class ToDoFragment : Fragment() {


    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var addTodoButton: Button
    private lateinit var adapter: ToDoAdapter
    private val todoList = mutableListOf<ToDoItem>()
    private lateinit var dbHelper: MyDatabaseHelper

    private var offset = 0
    private val limit = 20
    private var isLoading = false
    private var isProcessingChanges = false

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
        dbHelper = MyDatabaseHelper(requireContext())

        // RecyclerView 어댑터 설정
        adapter = ToDoAdapter(todoList, { position ->
            // 짧은 클릭: 수정 모드로 진입
            val todoItem = todoList[position]
            enableEditMode(todoItem.id)
        }, { position ->
            // 길게 클릭: 삭제 확인 다이얼로그 띄우기
            val todoItem = todoList[position]
            showDeleteConfirmationDialog(todoItem.id)
        })

        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 초기 데이터 로드
        loadVisibleData()

        // 할 일 추가 버튼 클릭 이벤트
        addTodoButton.setOnClickListener {
            addNewTodo()
        }

        // 할 일 추가 버튼을 길게 눌렀을 때, 전체 삭제
        addTodoButton.setOnLongClickListener {
            showDeleteAllConfirmationDialog()
            true
        }
    }

    private fun loadVisibleData() {
        if (isLoading) return
        isLoading = true

        val visibleData = dbHelper.getPagedToDoItems(offset, limit)

        if (visibleData.isNotEmpty()) {
            val startPosition = todoList.size
            todoList.addAll(visibleData)
            adapter.notifyItemRangeInserted(startPosition, visibleData.size)
            offset += limit
        }

        isLoading = false
    }
    private fun addNewTodo() {
        if (isProcessingChanges) return
        isProcessingChanges = true

        val newTodo = ""
        val uniqueId = dbHelper.generateUniqueId()

        val newToDoItem = ToDoItem(uniqueId, newTodo, false)

        // 항목을 리스트의 맨 끝에 추가
        todoList.add(newToDoItem)

        // 어댑터에 새 항목이 마지막에 추가되었음을 알리기
        adapter.notifyItemInserted(todoList.size - 1)

        todoRecyclerView.post {
            enableEditMode(uniqueId)
        }

        isProcessingChanges = false
    }

    private fun enableEditMode(id: String) {
        val position = todoList.indexOfFirst { it.id == id }
        val holder =
            todoRecyclerView.findViewHolderForAdapterPosition(position) as? ToDoAdapter.ToDoViewHolder
        val currentTodoItem = todoList[position]

        holder?.let {
            // 키보드 숨기기
            hideKeyboard(it.itemView)

            // 기존 텍스트 보이기 / 편집 가능하게 만들기
            it.todoTextView.visibility = View.GONE
            it.todoEditText.visibility = View.VISIBLE
            it.todoEditText.setText(currentTodoItem.text)
            it.todoEditText.isSingleLine = true
            it.todoEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            it.todoEditText.inputType = InputType.TYPE_CLASS_TEXT
            it.todoEditText.requestFocus() // 포커스 주기

            // 키보드 자동 나타나게 하기
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(it.todoEditText, InputMethodManager.SHOW_IMPLICIT)

            // 편집 완료 시 처리
            it.todoEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveOrRemoveText(
                        position,
                        it.todoEditText.text.toString().trim(),
                        currentTodoItem.id,
                        currentTodoItem.isChecked
                    )
                    exitEditMode(it, position)
                    true
                } else {
                    false
                }
            }

            // 포커스가 바뀌면 저장/제거 처리
            it.todoEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    saveOrRemoveText(
                        position,
                        it.todoEditText.text.toString().trim(),
                        currentTodoItem.id,
                        currentTodoItem.isChecked
                    )
                    exitEditMode(it, position)
                }
            }
        }
    }


    private fun exitEditMode(holder: ToDoAdapter.ToDoViewHolder, position: Int) {
        holder.todoEditText.visibility = View.GONE
        holder.todoTextView.visibility = View.VISIBLE
    }

    private fun saveOrRemoveText(
        position: Int,
        updatedText: String,
        id: String,
        isChecked: Boolean
    ) {
        if (updatedText.isNotEmpty()) {
            todoList[position] = ToDoItem(id, updatedText, isChecked)
            adapter.notifyItemChanged(position)
            dbHelper.insertOrUpdateToDoItem(todoList[position])
            showCustomToast("할 일이 등록되었습니다.")
        } else {
            // 빈칸일 때 position 유효성 체크
            if (position >= 0 && position < todoList.size) {
                todoList.removeAt(position)
                adapter.notifyItemRemoved(position)
                dbHelper.deleteToDoItemById(id)
                showCustomToast("빈칸입니다.")
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showDeleteConfirmationDialog(id: String) {
        val position = todoList.indexOfFirst { it.id == id }

        if (position != -1) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("이 할 일을 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    // 할 일 삭제
                    todoList.removeAt(position)

                    // 삭제 후 항목이 밀리므로 notifyItemRemoved 호출
                    adapter.notifyItemRemoved(position)

                    // 삭제된 이후 항목들에 대해서 notifyItemChanged 호출
                    // position 이후 항목들이 밀리기 때문에, 그 항목들을 업데이트
                    if (position < todoList.size) {
                        adapter.notifyItemRangeChanged(position, todoList.size - position)
                    }

                    // 데이터베이스에서 삭제
                    dbHelper.deleteToDoItemById(id)
                    showCustomToast("할 일이 삭제되었습니다.") }
                .setNegativeButton("취소", null)
            builder.create().show()
        }
    }

    // 전체 삭제 확인 다이얼로그
    private fun showDeleteAllConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("모든 할 일을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                // 모든 할 일 삭제
                todoList.clear()
                adapter.notifyDataSetChanged()

                // 데이터베이스에서 모든 할 일 삭제
                dbHelper.deleteAllToDoItems()

                showCustomToast("모든 할일이 삭제되었습니다.")            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }
    // Toast를 짧게 표시하기 위한 커스터마이징
    fun showCustomToast(message: String) {
        val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        toast.show()

        // Toast 표시 시간 조정 (1초)
        val handler = android.os.Handler()
        handler.postDelayed({
            toast.cancel() // 1초 후에 Toast를 강제로 종료
        }, 1000) // 1000ms = 1초
    }
}
