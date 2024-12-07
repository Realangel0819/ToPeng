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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ToDoFragment : Fragment() {

    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var addTodoButton: Button
    private lateinit var adapter: ToDoAdapter
    private val todoList = mutableListOf<ToDoItem>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

        todoRecyclerView = view.findViewById(R.id.todoRecyclerView)
        addTodoButton = view.findViewById(R.id.addTodoButton)

        // RecyclerView 어댑터 설정
        adapter = ToDoAdapter(todoList, { position ->
            val todoItem = todoList[position]
            enableEditMode(todoItem.id)
        }, { position ->
            val todoItem = todoList[position]
            showDeleteConfirmationDialog(todoItem.id)
        })

        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadVisibleData()

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
        // Firebase에서 데이터를 가져와서 todoList에 반영하는 부분
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("todos")
                .get()
                .addOnSuccessListener { result ->
                    todoList.clear()
                    for (document in result) {
                        val todoItem = document.toObject(ToDoItem::class.java)
                        todoList.add(todoItem)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "데이터 로드 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun addNewTodo() {
        if (isProcessingChanges) return
        isProcessingChanges = true

        val newTodo = ""  // 새 할 일 내용
        val uniqueId = db.collection("users")
            .document(auth.currentUser?.uid ?: "") // 사용자 UID로 Firestore 경로 설정
            .collection("todos").document().id // Firestore에서 자동 생성된 ID

        val newToDoItem = ToDoItem(uniqueId, newTodo, false) // 할 일 객체 생성

        // Firestore에 새 항목 추가
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("todos")
                .document(uniqueId)
                .set(newToDoItem)
                .addOnSuccessListener {
                    // Firestore에 데이터가 성공적으로 추가되면 UI 갱신
                    todoList.add(newToDoItem)
                    adapter.notifyItemInserted(todoList.size - 1)
                    todoRecyclerView.scrollToPosition(todoList.size - 1)

                    todoRecyclerView.post {
                        enableEditMode(uniqueId) // 새로 추가된 아이템 편집 모드로 전환
                    }

                    isProcessingChanges = false
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "데이터 추가 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                    isProcessingChanges = false
                }
        }
    }


    private fun enableEditMode(id: String) {
        val position = todoList.indexOfFirst { it.id == id }
        val holder = todoRecyclerView.findViewHolderForAdapterPosition(position) as? ToDoAdapter.ToDoViewHolder
        val currentTodoItem = todoList[position]

        holder?.let {
            hideKeyboard(it.itemView)

            it.todoTextView.visibility = View.GONE
            it.todoEditText.visibility = View.VISIBLE
            it.todoEditText.setText(currentTodoItem.text)
            it.todoEditText.isSingleLine = true
            it.todoEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            it.todoEditText.inputType = InputType.TYPE_CLASS_TEXT
            it.todoEditText.requestFocus()

            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(it.todoEditText, InputMethodManager.SHOW_IMPLICIT)

            it.todoEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveOrRemoveText(position, it.todoEditText.text.toString().trim(), currentTodoItem.id, currentTodoItem.isChecked)
                    exitEditMode(it, position)
                    true
                } else {
                    false
                }
            }

            it.todoEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    saveOrRemoveText(position, it.todoEditText.text.toString().trim(), currentTodoItem.id, currentTodoItem.isChecked)
                    exitEditMode(it, position)
                }
            }
        }
    }

    private fun exitEditMode(holder: ToDoAdapter.ToDoViewHolder, position: Int) {
        holder.todoEditText.visibility = View.GONE
        holder.todoTextView.visibility = View.VISIBLE
    }

    private fun saveOrRemoveText(position: Int, updatedText: String, id: String, isChecked: Boolean) {
        if (updatedText.isNotEmpty()) {
            todoList[position] = ToDoItem(id, updatedText, isChecked)
            adapter.notifyItemChanged(position)

            // Firestore에 데이터 업데이트
            val userId = auth.currentUser?.uid
            if (userId != null) {
                db.collection("users")
                    .document(userId)
                    .collection("todos")
                    .document(id)
                    .set(todoList[position])
            }

            showCustomToast("할 일이 등록되었습니다.")
        } else {
            // 빈칸일 때
            if (position >= 0 && position < todoList.size) {
                todoList.removeAt(position)
                adapter.notifyItemRemoved(position)

                val userId = auth.currentUser?.uid
                if (userId != null) {
                    db.collection("users")
                        .document(userId)
                        .collection("todos")
                        .document(id)
                        .delete()
                }

                showCustomToast("빈칸입니다.")
            }
        }
    }

    private fun showDeleteConfirmationDialog(id: String) {
        val position = todoList.indexOfFirst { it.id == id }
        if (position != -1) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("이 할 일을 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    todoList.removeAt(position)
                    adapter.notifyItemRemoved(position)

                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        db.collection("users")
                            .document(userId)
                            .collection("todos")
                            .document(id)
                            .delete()
                    }

                    showCustomToast("할 일이 삭제되었습니다.")
                }
                .setNegativeButton("취소", null)
            builder.create().show()
        }
    }

    private fun showDeleteAllConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("모든 할 일을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                todoList.clear()
                adapter.notifyDataSetChanged()

                val userId = auth.currentUser?.uid
                if (userId != null) {
                    db.collection("users")
                        .document(userId)
                        .collection("todos")
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                db.collection("users")
                                    .document(userId)
                                    .collection("todos")
                                    .document(document.id)
                                    .delete()
                            }
                        }
                }

                showCustomToast("모든 할일이 삭제되었습니다.")
            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }

    private fun showCustomToast(message: String) {
        val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        toast.show()

        val handler = android.os.Handler()
        handler.postDelayed({
            toast.cancel()
        }, 1000)
    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
