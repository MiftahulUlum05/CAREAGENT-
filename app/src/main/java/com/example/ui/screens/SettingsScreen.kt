package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.UserSettings
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    onSaveSettings: (UserSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var userName by remember { mutableStateOf(userSettings.userName) }
    var userRole by remember { mutableStateOf(userSettings.userRole) }
    var email by remember { mutableStateOf(userSettings.email) }
    var timeZone by remember { mutableStateOf(userSettings.timeZone) }
    var productiveStart by remember { mutableStateOf(userSettings.productiveStartHour) }
    var productiveEnd by remember { mutableStateOf(userSettings.productiveEndHour) }
    var pomodoroWork by remember { mutableIntStateOf(userSettings.pomodoroWorkDuration) }
    var pomodoroBreak by remember { mutableIntStateOf(userSettings.pomodoroBreakDuration) }
    var notificationsEnabled by remember { mutableStateOf(userSettings.notificationsEnabled) }
    var isDarkMode by remember { mutableStateOf(userSettings.isDarkMode) }
    var customApiKey by remember { mutableStateOf(userSettings.customApiKey) }

    val hasSystemKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Pengaturan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sesuaikan preferensi kerja, profil, dan kecerdasan buatan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Status Koneksi AI Gemini Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = PurpleAi)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Status Integrasi Gemini AI", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        val isConnected = hasSystemKey || customApiKey.isNotBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isConnected) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isConnected) "Terhubung" else "Mode Demo Cerdas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) PriorityGreen else PriorityAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Model aktif: gemini-3.5-flash. Memberikan analisis cerdas, pemecahan tugas, dan rekomendasi jadwal.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = { customApiKey = it },
                        label = { Text("Kunci API Gemini Kustom (Opsional)") },
                        placeholder = { Text("Masukkan API key jika ingin menggunakan akun sendiri...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Profil Pengguna Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Profil Pengguna", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = userRole,
                        onValueChange = { userRole = it },
                        label = { Text("Peran / Profesi") },
                        placeholder = { Text("Mahasiswa / Pekerja / Profesional") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Alamat Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Produktivitas & Jam Kerja
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Jam Produktif & Pomodoro", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = productiveStart,
                            onValueChange = { productiveStart = it },
                            label = { Text("Mulai Kerja") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = productiveEnd,
                            onValueChange = { productiveEnd = it },
                            label = { Text("Selesai Kerja") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = pomodoroWork.toString(),
                            onValueChange = { pomodoroWork = it.toIntOrNull() ?: 25 },
                            label = { Text("Fokus (menit)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pomodoroBreak.toString(),
                            onValueChange = { pomodoroBreak = it.toIntOrNull() ?: 5 },
                            label = { Text("Istirahat (menit)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = timeZone,
                        onValueChange = { timeZone = it },
                        label = { Text("Zona Waktu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Preferensi Tampilan & Notifikasi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Preferensi Aplikasi", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mode Gelap", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Tampilan gelap hemat daya & nyaman untuk mata", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Notifikasi Pengingat", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Peringatan untuk tugas terlambat & jadwal harian", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
            }
        }

        // Tombol Simpan Pengaturan
        item {
            Button(
                onClick = {
                    val updated = userSettings.copy(
                        userName = userName.trim(),
                        userRole = userRole.trim(),
                        email = email.trim(),
                        timeZone = timeZone.trim(),
                        productiveStartHour = productiveStart.trim(),
                        productiveEndHour = productiveEnd.trim(),
                        pomodoroWorkDuration = pomodoroWork,
                        pomodoroBreakDuration = pomodoroBreak,
                        notificationsEnabled = notificationsEnabled,
                        isDarkMode = isDarkMode,
                        customApiKey = customApiKey.trim()
                    )
                    onSaveSettings(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_save_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan Semua Pengaturan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
