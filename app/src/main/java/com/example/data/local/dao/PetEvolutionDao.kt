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

    @Query("UPDATE pet_evolution SET unlockedAccessoriesCsv = :unlockedCsv, activeAccessory = :accessory WHERE id = 1")
    suspend fun updateUnlockedAccessories(unlockedCsv: String, accessory: String)

    @Query("UPDATE pet_evolution SET coins = :coins, gems = :gems WHERE id = 1")
    suspend fun updateCurrencies(coins: Int, gems: Int)

    @Query("UPDATE pet_evolution SET exp = :exp, level = :level, expToNextLevel = :expToNextLevel, coins = :coins, gems = :gems WHERE id = 1")
    suspend fun updateProgression(exp: Int, level: Int, expToNextLevel: Int, coins: Int, gems: Int)

    @Query("UPDATE pet_evolution SET bloubShape = :shape WHERE id = 1")
    suspend fun setBloubShape(shape: String)

    @Query("UPDATE pet_evolution SET bloubSkinColor = :skinColor WHERE id = 1")
    suspend fun setBloubSkinColor(skinColor: String)

    @Query("UPDATE pet_evolution SET name = :name WHERE id = 1")
    suspend fun updatePetName(name: String)
}
