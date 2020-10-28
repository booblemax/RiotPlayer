package by.akella.riotplayer.db

import androidx.room.Database
import androidx.room.RoomDatabase
import by.akella.riotplayer.db.RiotDatabase.Companion.DATABASE_VERSION

@Database(entities = [SongEntity::class], version = DATABASE_VERSION)
abstract class RiotDatabase : RoomDatabase() {

    abstract val songsDao : SongDao

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "films_db"
    }
}