package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "categories",
    foreignKeys = [ForeignKey(
        entity = CompanyDetails::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    val userId: Long,
    val categoryName: String,
    var isSelected : Int = 0,
    val isSynced: Int = 0,
) : Serializable
