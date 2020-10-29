package by.akella.riotplayer.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg songs: SongEntity)

    @Delete
    fun remove(vararg songs: SongEntity)

    @Update
    fun update(song: SongEntity)

    @Query("SELECT * from songs order by id ASC")
    fun getSongs(): List<SongEntity>

    @Query("DELETE from songs")
    fun removeAllSongs()
}