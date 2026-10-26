package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.GeminiClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CareAgentRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val taskDao = database.taskDao()
    private val scheduleDao = database.scheduleDao()
    private val noteDao = database.noteDao()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val subtaskListType = Types.newParameterizedType(List::class.java, SubTask::class.java)
    private val subtaskAdapter = moshi.adapter<List<SubTask>>(subtaskListType)

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    fun getAllSchedules(): Flow<List<ScheduleItemEntity>> = scheduleDao.getAllSchedules()
    fun getSchedulesForDate(dateStr: String): Flow<List<ScheduleItemEntity>> = scheduleDao.getSchedulesForDate(dateStr)
    suspend fun insertSchedule(item: ScheduleItemEntity): Long = scheduleDao.insertSchedule(item)
    suspend fun insertSchedules(items: List<ScheduleItemEntity>): List<Long> = scheduleDao.insertSchedules(items)
    suspend fun updateSchedule(item: ScheduleItemEntity) = scheduleDao.updateSchedule(item)
    suspend fun deleteSchedule(item: ScheduleItemEntity) = scheduleDao.deleteSchedule(item)
    suspend fun deleteScheduleById(id: Long) = scheduleDao.deleteScheduleById(id)
    suspend fun clearPendingProposals() = scheduleDao.clearPendingProposals()

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    fun parseSubtasks(json: String): List<SubTask> {
        return try {
            if (json.isBlank()) emptyList() else subtaskAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeSubtasks(subtasks: List<SubTask>): String {
        return try {
            subtaskAdapter.toJson(subtasks)
        } catch (e: Exception) {
            "[]"
        }
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val count = taskDao.getTaskCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdfDate.format(Date(now))
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowStr = sdfDate.format(cal.time)

            val initialSubtasks1 = listOf(
                SubTask(UUID.randomUUID().toString(), "Analisis dataset dan pra-pemrosesan", true),
                SubTask(UUID.randomUUID().toString(), "Pelatihan model transformer", true),
                SubTask(UUID.randomUUID().toString(), "Evaluasi matriks akurasi & F1-score", false),
                SubTask(UUID.randomUUID().toString(), "Menyusun bab kesimpulan laporan", false)
            )

            val initialSubtasks2 = listOf(
                SubTask(UUID.randomUUID().toString(), "Review materi kuliah bab 1-5", true),
                SubTask(UUID.randomUUID().toString(), "Mengerjakan latihan soal tahun lalu", false),
                SubTask(UUID.randomUUID().toString(), "Diskusi kelompok studi kasus", false)
            )

            val initialSubtasks3 = listOf(
                SubTask(UUID.randomUUID().toString(), "Rekap capaian sprint mingguan", true),
                SubTask(UUID.randomUUID().toString(), "Presentasi demo fitur baru", true)
            )

            val sampleTasks = listOf(
                TaskEntity(
                    title = "Penyusunan Laporan Akhir Proyek AI",
                    description = "Menyelesaikan draft akhir eksperimen model pembelajaran mesin dan dokumentasi API.",
                    category = "Akademik",
                    startDate = now - 86400000L * 2,
                    dueDate = now + 86400000L, // besok
                    dueTimeStr = "17:00",
                    estimatedDurationMinutes = 120,
                    priority = TaskPriority.TINGGI.displayName,
                    status = TaskStatus.SEDANG_DIKERJAKAN.displayName,
                    progress = 50,
                    subtasksJson = serializeSubtasks(initialSubtasks1),
                    notes = "Konsultasikan hasil metrik dengan dosen pembimbing."
                ),
                TaskEntity(
                    title = "Persiapan Ujian Akhir Semester (UAS)",
                    description = "Belajar mandiri untuk mata kuliah Sistem Terdistribusi dan Basis Data Lanjut.",
                    category = "Akademik",
                    startDate = now,
                    dueDate = now + 86400000L * 3,
                    dueTimeStr = "20:00",
                    estimatedDurationMinutes = 180,
                    priority = TaskPriority.MENDESAK.displayName,
                    status = TaskStatus.BELUM_DIMULAI.displayName,
                    progress = 30,
                    subtasksJson = serializeSubtasks(initialSubtasks2),
                    notes = "Fokus pada algoritma konsensus Raft dan Paxos."
                ),
                TaskEntity(
                    title = "Rapat Evaluasi Sprint Mingguan",
                    description = "Sinkronisasi progress fitur SaaS CAREAGENT dengan tim pengembang.",
                    category = "Pekerjaan",
                    startDate = now - 3600000L * 2,
                    dueDate = now, // hari ini
                    dueTimeStr = "14:00",
                    estimatedDurationMinutes = 60,
                    priority = TaskPriority.SEDANG.displayName,
                    status = TaskStatus.SELESAI.displayName,
                    progress = 100,
                    subtasksJson = serializeSubtasks(initialSubtasks3),
                    notes = "Notula rapat disimpan di Google Drive."
                ),
                TaskEntity(
                    title = "Olahraga Kardio & Jogging Sore",
                    description = "Lari 5km di taman kota untuk menjaga stamina dan fokus belajar.",
                    category = "Kesehatan",
                    startDate = now,
                    dueDate = now,
                    dueTimeStr = "16:30",
                    estimatedDurationMinutes = 45,
                    priority = TaskPriority.RENDAH.displayName,
                    status = TaskStatus.BELUM_DIMULAI.displayName,
                    progress = 0,
                    subtasksJson = "[]",
                    notes = "Bawa botol minum dan headset."
                ),
                TaskEntity(
                    title = "Review Proposal Hibah Penelitian",
                    description = "Memeriksa kesesuaian anggaran biaya dan jadwal kegiatan penelitian.",
                    category = "Proyek",
                    startDate = now - 86400000L * 3,
                    dueDate = now - 86400000L, // terlambat
                    dueTimeStr = "10:00",
                    estimatedDurationMinutes = 90,
                    priority = TaskPriority.MENDESAK.displayName,
                    status = TaskStatus.SEDANG_DIKERJAKAN.displayName,
                    progress = 70,
                    subtasksJson = "[]",
                    notes = "Perlu revisi pada bagian luaran wajib."
                )
            )
            taskDao.insertTasks(sampleTasks)

            val sampleSchedules = listOf(
                ScheduleItemEntity(
                    title = "Rapat Evaluasi Sprint Mingguan",
                    category = "Pekerjaan",
                    dateStr = todayStr,
                    startTimeStr = "14:00",
                    endTimeStr = "15:00",
                    notes = "Google Meet bersama tim produk.",
                    isApproved = true,
                    isCompleted = true
                ),
                ScheduleItemEntity(
                    title = "Sesi Fokus: Laporan Akhir AI",
                    category = "Akademik",
                    dateStr = todayStr,
                    startTimeStr = "15:30",
                    endTimeStr = "17:30",
                    notes = "Fokus penulisan bab metodologi & hasil.",
                    isApproved = true,
                    isCompleted = false
                ),
                ScheduleItemEntity(
                    title = "Jogging Sore & Relaksasi",
                    category = "Kesehatan",
                    dateStr = todayStr,
                    startTimeStr = "17:45",
                    endTimeStr = "18:30",
                    notes = "Target 5km santai.",
                    isApproved = true,
                    isCompleted = false
                ),
                ScheduleItemEntity(
                    title = "Belajar Mandiri UAS Sistem Terdistribusi",
                    category = "Akademik",
                    dateStr = tomorrowStr,
                    startTimeStr = "09:00",
                    endTimeStr = "11:30",
                    notes = "Review slide kuliah pertemuan 1-7.",
                    isApproved = true,
                    isCompleted = false
                )
            )
            scheduleDao.insertSchedules(sampleSchedules)

            val sampleNotes = listOf(
                NoteEntity(
                    title = "Ide Fitur Auto-Reschedule CAREAGENT",
                    content = "Menambahkan kemampuan deteksi otomatis jika ada tugas terlambat, lalu AI otomatis menyarankan slot waktu kosong baru.",
                    category = "Ide",
                    colorHex = "#1E293B"
                ),
                NoteEntity(
                    title = "Rumus Pengingat Pomodoro 50/10",
                    content = "Untuk tugas kompleks seperti koding atau menulis ilmiah, rasio 50 menit kerja fokus dan 10 menit istirahat terbukti meningkatkan daya tahan kognitif.",
                    category = "Produktivitas",
                    colorHex = "#1E293B"
                ),
                NoteEntity(
                    title = "Daftar Bacaan Jurnal NLP & LLM",
                    content = "1. Attention is All You Need\n2. Chain of Thought Prompting\n3. Retrieval-Augmented Generation for Knowledge-Intensive NLP",
                    category = "Belajar",
                    colorHex = "#1E293B"
                )
            )
            noteDao.insertNotes(sampleNotes)
        }
    }

    suspend fun callGeminiAi(
        prompt: String,
        customKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customKey.isNullOrBlank() -> customKey
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        // Fetch current context from database
        val currentTasks = taskDao.getAllTasks().first()
        val taskContext = currentTasks.take(10).joinToString("\n") {
            "- [${it.status}] ${it.title} (Prioritas: ${it.priority}, Tenggat: ${it.formattedDueDate()}, Progres: ${it.progress}%)"
        }

        val systemPrompt = """
            Anda adalah CAREAGENT AI — Asisten Cerdas Pengelola Aktivitas dan Tugas untuk pengguna di Indonesia.
            Anda berbicara dalam Bahasa Indonesia yang ramah, profesional, solutif, dan terstruktur.
            Gunakan konteks tugas pengguna saat ini berikut untuk menjawab:
            $taskContext
            
            Prinsip CAREAGENT:
            1. Realistis dalam penjadwalan (hindari kelelahan/burnout, perhatikan jam istirahat).
            2. Tentukan prioritas berdasarkan Matriks Eisenhower (Urgensi vs Kepentingan).
            3. Jika memecah tugas, berikan subtask yang konkret dan dapat langsung dieksekusi.
            4. Berikan saran produktivitas berbasis sains (Teknik Pomodoro, Time-blocking).
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext generateSmartFallbackResponse(prompt, currentTasks)
        }

        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                )
            )
            val response = GeminiClient.service.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!reply.isNullOrBlank()) {
                reply
            } else {
                generateSmartFallbackResponse(prompt, currentTasks)
            }
        } catch (e: Exception) {
            // Graceful intelligent fallback if network or key is invalid
            generateSmartFallbackResponse(prompt, currentTasks, e.localizedMessage)
        }
    }

    private fun generateSmartFallbackResponse(prompt: String, tasks: List<TaskEntity>, errorMsg: String? = null): String {
        val lower = prompt.lowercase(Locale.ROOT)
        val overdueTasks = tasks.filter { it.isOverdue() }
        val highPriority = tasks.filter { it.priority == TaskPriority.TINGGI.displayName || it.priority == TaskPriority.MENDESAK.displayName && it.status != TaskStatus.SELESAI.displayName }
        val pendingCount = tasks.count { it.status != TaskStatus.SELESAI.displayName }

        return when {
            lower.contains("prioritas") || lower.contains("terlebih dahulu") || lower.contains("dikerjakan") -> {
                buildString {
                    appendLine("📌 **Rekomendasi Prioritas CAREAGENT AI:**\n")
                    if (overdueTasks.isNotEmpty()) {
                        appendLine("⚠️ **Perhatian Segera (Terlambat):**")
                        overdueTasks.forEach { appendLine("• **${it.title}** (Tenggat: ${it.formattedDueDate()}) — Selesaikan bagian kritis hari ini!") }
                        appendLine()
                    }
                    if (highPriority.isNotEmpty()) {
                        appendLine("🔥 **Tugas Prioritas Tinggi / Mendesak:**")
                        highPriority.take(3).forEach {
                            appendLine("• **${it.title}** (Est. ${it.estimatedDurationMinutes} mnt, Progres: ${it.progress}%)")
                        }
                        appendLine()
                    }
                    appendLine("💡 **Saran:** Mulai dengan metode *Eat That Frog* pada tugas prioritas tertinggi pada jam produktif pagi/siang Anda sebelum beralih ke tugas rutin.")
                }
            }
            lower.contains("pecah") || lower.contains("langkah") || lower.contains("subtask") || lower.contains("proposal") || lower.contains("laporan") -> {
                buildString {
                    appendLine("📋 **Pecahan Langkah Kerja Cerdas (Task Breakdown):**\n")
                    appendLine("Berikut rekomendasi subtask agar tugas besar lebih mudah diselesaikan tanpa rasa kewalahan:")
                    appendLine("1. **Riset & Pengumpulan Bahan** (Estimasi 45 mnt) — Kumpulkan referensi utama dan buat outline.")
                    appendLine("2. **Penulisan Kerangka Utama** (Estimasi 60 mnt) — Buat draf bab pengantar dan metodologi.")
                    appendLine("3. **Pengolahan Data & Isi Inti** (Estimasi 90 mnt) — Analisis data dan rangkum temuan.")
                    appendLine("4. **Review & Proofreading** (Estimasi 30 mnt) — Koreksi tata bahasa dan kelengkapan lampiran.")
                    appendLine("\n✨ *Anda dapat langsung menambahkan langkah-langkah di atas sebagai Subtask di menu Manajemen Tugas!*")
                }
            }
            lower.contains("jadwal") || lower.contains("uas") || lower.contains("belajar") || lower.contains("atur ulang") -> {
                buildString {
                    appendLine("🗓️ **Usulan Jadwal Belajar & Produktivitas Realistis:**\n")
                    appendLine("Berdasarkan waktu luang dan jam produktif Anda (08:00 - 17:00):")
                    appendLine("• **Sesi 1 (08:30 - 10:30):** Deep Work — Materi konseptual tersulit (Fokus tinggi)")
                    appendLine("• **Istirahat (10:30 - 10:45):** Peregangan ringan & hidrasi air putih")
                    appendLine("• **Sesi 2 (10:45 - 12:00):** Latihan soal mandiri & studi kasus")
                    appendLine("• **Istirahat Siang (12:00 - 13:30):** Makan siang & relaksasi")
                    appendLine("• **Sesi 3 (13:30 - 15:30):** Review proyek kelompok & koding praktis")
                    appendLine("• **Sesi 4 (16:00 - 17:00):** Olahraga ringan atau evaluasi harian")
                    appendLine("\n💡 *Gunakan tombol 'Buat Jadwal dengan AI' pada Smart Scheduler untuk langsung mengaplikasikan ke kalender.*")
                }
            }
            lower.contains("analisis") || lower.contains("produktivitas") -> {
                val completedCount = tasks.count { it.status == TaskStatus.SELESAI.displayName }
                val total = tasks.size.coerceAtLeast(1)
                val rate = (completedCount * 100) / total
                buildString {
                    appendLine("📊 **Analitik & Evaluasi Produktivitas Anda:**\n")
                    appendLine("• **Total Tugas Terdaftar:** $total tugas")
                    appendLine("• **Tingkat Penyelesaian:** $rate% ($completedCount selesai)")
                    appendLine("• **Tugas Aktif:** $pendingCount tugas dalam antrean")
                    if (overdueTasks.isNotEmpty()) {
                        appendLine("• **Peringatan:** Terdapat ${overdueTasks.size} tugas terlambat yang membutuhkan perhatian.")
                    }
                    appendLine("\n🎯 **Rekomendasi CAREAGENT:**")
                    appendLine("Pertahankan ritme kerja dengan teknik Time-Blocking. Jangan biarkan progres stagnan di bawah 30% pada tugas prioritas tinggi.")
                }
            }
            else -> {
                buildString {
                    appendLine("Halo! Saya **CAREAGENT AI**, asisten cerdas pengelola tugas dan jadwal Anda.")
                    appendLine("\nSaat ini Anda memiliki **$pendingCount tugas aktif**.")
                    if (highPriority.isNotEmpty()) {
                        appendLine("Tugas paling mendesak: **${highPriority.first().title}**.")
                    }
                    appendLine("\nAnda dapat meminta saya untuk:")
                    appendLine("1. \"Tentukan tugas yang harus saya kerjakan terlebih dahulu.\"")
                    appendLine("2. \"Pecah tugas proposal/laporan menjadi subtask kecil.\"")
                    appendLine("3. \"Buat jadwal belajar persiapan UAS 7 hari ke depan.\"")
                    appendLine("4. \"Analisis produktivitas saya minggu ini.\"")
                    if (errorMsg != null) {
                        appendLine("\n*(Catatan: Menggunakan respons terintegrasi lokal. Kunci API Gemini Anda dapat dicek pada menu Pengaturan)*")
                    }
                }
            }
        }
    }
}
