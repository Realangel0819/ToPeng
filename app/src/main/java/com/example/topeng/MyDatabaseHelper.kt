import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class MyDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AppDatabase.db"
        private const val DATABASE_VERSION = 2 // 데이터베이스 버전 증가
        const val TABLE_NAME = "MyData"
        const val COLUMN_ID = "id"
        const val COLUMN_TEXT = "text"
        const val COLUMN_IS_CHECKED = "isChecked" // 체크 상태
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TEXT TEXT NOT NULL,
                $COLUMN_IS_CHECKED INTEGER DEFAULT 0
            )
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_IS_CHECKED INTEGER DEFAULT 0")
        }
    }

    fun insertOrUpdateText(id: String, text: String, isChecked: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_TEXT, text)
            put(COLUMN_IS_CHECKED, if (isChecked) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getAllTexts(): List<Triple<String, String, Boolean>> {
        val texts = mutableListOf<Triple<String, String, Boolean>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT))
                val isChecked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_CHECKED)) == 1
                texts.add(Triple(id, text, isChecked))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return texts
    }

    fun deleteTextById(id: String) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id))
        db.close()
    }
    fun generateUniqueId(position: Int): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = dateFormat.format(Date()) // 현재 날짜를 yyyyMMdd 형식으로 포맷
        return "$date-$position" // 날짜와 리스트의 인덱스를 조합
    }

}
