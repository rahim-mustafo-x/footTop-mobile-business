package uz.coder.foottopbusiness.presentation.main.settings.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.ui.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNotificationScreen(viewModel: SendNotificationViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val state by viewModel.state.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SendNotificationContract.Effect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                SendNotificationContract.Effect.NavigateBack -> {
                    navigator.pop()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF0F3D2E))
                    .padding(top = statusBarPadding, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Xabarnoma yuborish",
                        color = Color.White,
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "XABAR TURI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeItem(
                    title = "Booking",
                    icon = Icons.Default.CalendarMonth,
                    color = Color(0xFF4CAF50),
                    isSelected = state.type == "BOOKING",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.handleEvent(SendNotificationContract.Event.UpdateType("BOOKING"))
                }
                TypeItem(
                    title = "Match",
                    icon = Icons.Default.SportsSoccer,
                    color = Color(0xFF2196F3),
                    isSelected = state.type == "MATCH",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.handleEvent(SendNotificationContract.Event.UpdateType("MATCH"))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeItem(
                    title = "Turnir",
                    icon = Icons.Default.EmojiEvents,
                    color = Color(0xFFFFB74D),
                    isSelected = state.type == "TOURNAMENT",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.handleEvent(SendNotificationContract.Event.UpdateType("TOURNAMENT"))
                }
                TypeItem(
                    title = "Sistema",
                    icon = Icons.Default.Campaign,
                    color = Color(0xFF757575),
                    isSelected = state.type == "SYSTEM",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.handleEvent(SendNotificationContract.Event.UpdateType("SYSTEM"))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LabelAndField(
                label = "SARLAVHA",
                value = state.title,
                placeholder = "Xabar sarlavhasini kiriting"
            ) {
                viewModel.handleEvent(SendNotificationContract.Event.UpdateTitle(it))
            }

            LabelAndField(
                label = "XABAR MATNI",
                value = state.body,
                placeholder = "Xabar matnini batafsil yozing",
                isMultiline = true
            ) {
                viewModel.handleEvent(SendNotificationContract.Event.UpdateBody(it))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.handleEvent(SendNotificationContract.Event.SendToAll) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D2E)),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Yuborish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TypeItem(
    title: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.1f) else Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) color.copy(alpha = 0.2f) else Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) color else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) color else Color.Black
        )
    }
}

@Composable
private fun LabelAndField(
    label: String,
    value: String,
    placeholder: String,
    isMultiline: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().let { if (isMultiline) it.height(120.dp) else it },
            placeholder = { Text(placeholder, color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray
            )
        )
    }
}
