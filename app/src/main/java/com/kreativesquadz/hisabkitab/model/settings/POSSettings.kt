package com.kreativesquadz.hisabkitab.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kreativesquadz.hisabkitab.Config
import java.io.Serializable

@Entity(tableName = "pos_settings")
data class POSSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = Config.userId,
    val isEnableCashBalance: Boolean = false,
    val isBlockOutOfStock: Boolean = false,
    val isSynced: Int = 0
) : Serializable {

    fun isContentEqual(other: POSSettings): Boolean {
        return this.isEnableCashBalance == other.isEnableCashBalance &&
                this.isBlockOutOfStock == other.isBlockOutOfStock
    }
}
