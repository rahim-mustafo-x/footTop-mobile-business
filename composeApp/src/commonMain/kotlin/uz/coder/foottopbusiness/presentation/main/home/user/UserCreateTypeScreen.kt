package uz.coder.foottopbusiness.presentation.main.home.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.presentation.main.coaches.create.CoachCreateScreen

class UserCreateTypeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(top = statusBarPadding + 16.dp, start = 8.dp, end = 24.dp, bottom = 32.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text(
                            "Foydalanuvchi yaratish",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Qaysi turdagi foydalanuvchini yaratmoqchisiz?",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TypeCard(
                    title = "Yangi xodim",
                    subtitle = "Admin yoki Owner uchun tizimga kirish hisobini yaratish",
                    icon = Icons.Default.Person,
                    gradient = Brush.horizontalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3))),
                    onClick = { navigator.push(UserCreateScreen()) }
                )

                TypeCard(
                    title = "Coach profili",
                    subtitle = "Mavjud foydalanuvchiga coachlik vakolatlarini va ma'lumotlarini biriktirish",
                    icon = Icons.Default.SportsSoccer,
                    gradient = Brush.horizontalGradient(listOf(Color(0xFF03DAC6), Color(0xFF018786))),
                    onClick = { navigator.push(CoachCreateScreen()) }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TypeCard(
        title: String,
        subtitle: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        gradient: Brush,
        onClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.background(gradient).fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, modifier = Modifier.size(36.dp), tint = Color.White)
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text(subtitle, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
