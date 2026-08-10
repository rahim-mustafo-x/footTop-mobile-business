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

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign

import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.ui.GradientHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNotificationScreen(viewModel: SendNotificationViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val state by viewModel.state.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }
    val strings = Localization.current

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GradientHeader(
                title = strings.notifications,
                subtitle = strings.notifyUsers,
                titleFontSize = 22.sp,
                onBack = { navigator.pop() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        strings.msgType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TypeItem(
                            title = strings.booking,
                            icon = Icons.Default.CalendarMonth,
                            color = Color(0xFF4CAF50),
                            isSelected = state.type == "BOOKING",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.handleEvent(SendNotificationContract.Event.UpdateType("BOOKING"))
                        }
                        TypeItem(
                            title = strings.match,
                            icon = Icons.Default.SportsSoccer,
                            color = Color(0xFF2196F3),
                            isSelected = state.type == "MATCH",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.handleEvent(SendNotificationContract.Event.UpdateType("MATCH"))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TypeItem(
                            title = strings.tournament,
                            icon = Icons.Default.EmojiEvents,
                            color = Color(0xFFFF9800),
                            isSelected = state.type == "TOURNAMENT",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.handleEvent(SendNotificationContract.Event.UpdateType("TOURNAMENT"))
                        }
                        TypeItem(
                            title = strings.system,
                            icon = Icons.Default.Campaign,
                            color = Color(0xFF9C27B0),
                            isSelected = state.type == "SYSTEM",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.handleEvent(SendNotificationContract.Event.UpdateType("SYSTEM"))
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    LabelAndField(
                        label = strings.title,
                        value = state.title,
                        placeholder = strings.titleHint
                    ) {
                        viewModel.handleEvent(SendNotificationContract.Event.UpdateTitle(it))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LabelAndField(
                        label = strings.msgBody,
                        value = state.body,
                        placeholder = strings.bodyHint,
                        isMultiline = true
                    ) {
                        viewModel.handleEvent(SendNotificationContract.Event.UpdateBody(it))
                    }
                }
            }

            Button(
                onClick = { viewModel.handleEvent(SendNotificationContract.Event.SendToAll) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                enabled = !state.isLoading && state.title.isNotBlank() && state.body.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Icon(Icons.Default.Notifications, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(strings.sendToAll, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Text(
                strings.pushHint,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
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
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
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
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().let { if (isMultiline) it.height(140.dp) else it },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp) },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
