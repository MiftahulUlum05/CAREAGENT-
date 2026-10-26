package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val title: String,
    val category: String = "Aktivitas",
    val dateStr: String, // "yyyy-MM-dd"
    val startTimeStr: String, // "09:00"
    val endTimeStr: String, // "10:30"
    val notes: String = "",
    val isAIProposed: Boolean = false,
    val isApproved: Boolean = true,
    val isCompleted: Boolean = false
) {
    fun getStartMinutes(): Int {
        val parts = startTimeStr.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    fun getEndMinutes(): Int {
        val parts = endTimeStr.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    fun conflictsWith(other: ScheduleItemEntity): Boolean {
        if (this.id == other.id) return false
        if (this.dateStr != other.dateStr) return false
        val s1 = this.getStartMinutes()
        val e1 = this.getEndMinutes()
        val s2 = other.getStartMinutes()
        val e2 = other.getEndMinutes()
        return (s1 < e2 && e1 > s2)
    }
}
