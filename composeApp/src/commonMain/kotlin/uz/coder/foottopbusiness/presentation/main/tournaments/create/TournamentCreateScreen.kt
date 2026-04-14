package uz.coder.foottopbusiness.presentation.main.tournaments.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsContract
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsViewModel

class TournamentCreateScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<TournamentsViewModel>()
        val state by viewModel.state.collectAsState()

        var name by remember { mutableStateOf("") }
        var startDate by remember { mutableStateOf("") }
        var endDate by remember { mutableStateOf("") }
        var maxTeams by remember { mutableStateOf("") }
        var entryFee by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }

        LaunchedEffect(state.isCreating) {
            // Ideally we'd have a success effect, but for now we check state change
            if (!state.isCreating && state.error == null && name.isNotEmpty()) {
                 navigator.pop() // Wait for manual confirmation or handle success effect
            }
        }

        Scaffold(
            topBar = {
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(Color(0xFF0F3D2E))
                        .padding(top = statusBarPadding + 16.dp, start = 8.dp, end = 24.dp, bottom = 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text(
                            "Turnir yaratish",
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
                    .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(LayoutDirection.Ltr), end = padding.calculateEndPadding(
                        LayoutDirection.Ltr))
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Turnir ma'lumotlari",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                TournamentInputField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Turnir nomi",
                    icon = Icons.Default.EmojiEvents
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TournamentInputField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = "Boshlanish (yyyy-MM-dd)",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                    TournamentInputField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = "Tugash (yyyy-MM-dd)",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TournamentInputField(
                        value = maxTeams,
                        onValueChange = { maxTeams = it.filter { c -> c.isDigit() } },
                        label = "Jamoalar soni",
                        icon = Icons.Default.Groups,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    TournamentInputField(
                        value = entryFee,
                        onValueChange = { entryFee = it.filter { c -> c.isDigit() || c == '.' } },
                        label = "To'lov (so'm)",
                        icon = Icons.Default.Money,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }

                TournamentInputField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Manzil",
                    icon = Icons.Default.LocationOn
                )

                if (state.error != null) {
                    Text(state.error!!, color = Color.Red, fontSize = 14.sp)
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.handleEvent(
                            TournamentsContract.Event.Create(
                                name,
                                startDate,
                                endDate,
                                maxTeams.toIntOrNull() ?: 0,
                                entryFee.toDoubleOrNull() ?: 0.0,
                                address.takeIf { it.isNotBlank() }
                            )
                        )
                        navigator.pop()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D2E)),
                    enabled = !state.isCreating && name.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Yaratish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    private fun TournamentInputField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        icon: ImageVector,
        modifier: Modifier = Modifier,
        keyboardType: KeyboardType = KeyboardType.Text
    ) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(icon, null, tint = Color(0xFF0F3D2E)) },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0F3D2E),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }
    }
}
