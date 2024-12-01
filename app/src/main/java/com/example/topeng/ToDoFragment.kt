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

    private var offset = 0 // 페이징 시작점
    private val limit = 20 // 한 번에 로드할 데이터 수
    private var isLoading = false // 데이터를 불러오는 중인지 확인

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
        adapter = ToDoAdapter(todoList, { position ->
            showDeleteConfirmationDialog(position) // 삭제 처리
        }, { position ->
            enableEditMode(position) // 수정 처리
        })
        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 스크롤 이벤트 처리
        todoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItem >= totalItemCount - 1) {
                    // 추가 데이터 로드
                    loadVisibleData()
                }
            }
        })

        // 초기 데이터 로드
        loadVisibleData()

        // 할 일 추가 버튼 클릭 이벤트
        addTodoButton.setOnClickListener {
            addNewTodo()
        }
    }

    // SQLite에서 데이터 불러오기
    private fun loadVisibleData() {
        if (isLoading) return
        isLoading = true

        val dbHelper = MyDatabaseHelper(requireContext())
        val visibleData = dbHelper.getPagedTexts(offset, limit)

        if (visibleData.isNotEmpty()) {
            val startPosition = todoList.size
            todoList.addAll(visibleData)
            adapter.notifyItemRangeInserted(startPosition, visibleData.size)
            offset += limit
        }

        isLoading = false
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

        if (holder != null) {
            holder.todoTextView.visibility = View.GONE
            holder.todoEditText.visibility = View.VISIBLE
            holder.todoEditText.setText(currentTodo)
            holder.todoEditText.isSingleLine = true
            holder.todoEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            holder.todoEditText.inputType = InputType.TYPE_CLASS_TEXT
            holder.todoEditText.requestFocus()

            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(holder.todoEditText, InputMethodManager.SHOW_IMPLICIT)

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
                    hideKeyboard(v)
                    true
                } else {
                    false
                }
            }
            holder.todoEditText.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    holder.todoEditText.visibility = View.GONE
                    holder.todoTextView.visibility = View.VISIBLE
                    hideKeyboard(v)
                }
            }
        }
    }

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
                dbHelper.deleteTextById(id)
            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }
}
