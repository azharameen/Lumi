package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PetEvolutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetEvolutionDao {
    @Query("SELECT * FROM pet_evolution WHERE id = 1")
    fun getPetEvolution(): Flow<PetEvolutionEntity?>

    @Query("SELECT * FROM pet_evolution WHERE id = 1")
    suspend fun getPetEvolutionDirect(): PetEvolutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(pet: PetEvolutionEntity)

    @Update
    suspend fun update(pet: PetEvolutionEntity)

    @Query("UPDATE pet_evolution SET activeAccessory = :accessory WHERE id = 1")
    suspend fun setActiveAccessory(accessory: String)
}
