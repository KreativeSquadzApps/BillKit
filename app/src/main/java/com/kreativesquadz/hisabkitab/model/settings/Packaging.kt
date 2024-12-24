package com.kreativesquadz.hisabkitab.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kreativesquadz.hisabkitab.Config
import java.io.Serializable

@Entity(tableName = "packaging")
data class Packaging(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = Config.userId,
    val packagingAmount: Double,
    val isSynced: Int = 0
) : Serializable {

    fun isContentEqual(other: Packaging): Boolean {
        return this.packagingAmount == other.packagingAmount
    }
}
