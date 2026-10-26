package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@JsonClass(generateAdapter = true)
data class SubTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

enum class TaskPriority(val displayName: String, val level: Int) {
    RENDAH("Rendah", 1),
    SEDANG("Sedang", 2),
    TINGGI("Tinggi", 3),
    MENDESAK("Mendesak", 4);

    companion object {
        fun fromString(value: String): TaskPriority {
            return entries.find { it.displayName.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: SEDANG
        }
    }
}

enum class TaskStatus(val displayName: String) {
    BELUM_DIMULAI("Belum Dimulai"),
    SEDANG_DIKERJAKAN("Sedang Dikerjakan"),
    SELESAI("Selesai");

    companion object {
        fun fromString(value: String): TaskStatus {
            return entries.find { it.displayName.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: BELUM_DIMULAI
        }
    }
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Umum",
    val startDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 86400000L,
    val dueTimeStr: String = "17:00",
    val estimatedDurationMinutes: Int = 60,
    val priority: String = TaskPriority.SEDANG.displayName,
    val status: String = TaskStatus.BELUM_DIMULAI.displayName,
    val progress: Int = 0,
    val subtasksJson: String = "[]",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isOverdue(): Boolean {
        if (status == TaskStatus.SELESAI.displayName) return false
        val now = System.currentTimeMillis()
        return dueDate < now
    }

    fun isDueToday(): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date(dueDate)) == sdf.format(Date())
    }

    fun formattedDueDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return "${sdf.format(Date(dueDate))}, $dueTimeStr"
    }
}
