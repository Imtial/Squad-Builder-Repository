package com.example.squadbuilderrepository.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert (Player player);

    @Query("DELETE FROM playerTable WHERE pid = :pid")
    void delete (String pid);

    @Query("DELETE FROM playerTable")
    void deleteAll();

    @Query("SELECT * FROM playerTable ORDER BY name ASC")
    LiveData<List<Player>> getAlphabetizedPlayer();

    @Query("SELECT * FROM playerTable")
    List<Player> getStaticPlayerList();

    @Query("SELECT pid FROM playerTable")
    List<String> getStaticPlayerIDs();

}
