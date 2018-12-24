package journal.gratitude.com.gratitudejournal.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import journal.gratitude.com.gratitudejournal.model.Entry

@Database(entities = [Entry::class], version = 1)
@TypeConverters(Converters::class)
abstract class EntryDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var INSTANCE: EntryDatabase? = null

        fun getDatabase(context: Context): EntryDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EntryDatabase::class.java,
                    "Entry_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}