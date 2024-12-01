import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.topeng.ToDoItem

class MyDatabaseHelper(context: Context) {

    private val db: SQLiteDatabase = context.openOrCreateDatabase("todo_db", Context.MODE_PRIVATE, null)

    // 페이징된 할 일 목록 가져오기
    fun getPagedToDoItems(offset: Int, limit: Int): List<ToDoItem> {
        val cursor = db.rawQuery(
            "SELECT * FROM todo_items ORDER BY id ASC LIMIT ? OFFSET ?",
            arrayOf(limit.toString(), offset.toString())
        )
        val todoItems = mutableListOf<ToDoItem>()
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex("id"))
            val text = cursor.getString(cursor.getColumnIndex("text"))
            val isChecked = cursor.getInt(cursor.getColumnIndex("is_checked")) == 1
            todoItems.add(ToDoItem(id, text, isChecked))
        }
        cursor.close()
        return todoItems
    }


    // 할 일 항목 저장 또는 업데이트
    fun insertOrUpdateToDoItem(todoItem: ToDoItem) {
        val values = ContentValues().apply {
            put("id", todoItem.id)
            put("text", todoItem.text)
            put("is_checked", if (todoItem.isChecked) 1 else 0)
        }
        db.insertWithOnConflict("todo_items", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // 할 일 항목 삭제
    fun deleteToDoItemById(id: String) {
        db.delete("todo_items", "id = ?", arrayOf(id))
    }

    // 고유 ID 생성 (시간 기반)
    fun generateUniqueId(): String {
        return System.currentTimeMillis().toString() // 시간 기반으로 고유 ID 생성
    }
}
