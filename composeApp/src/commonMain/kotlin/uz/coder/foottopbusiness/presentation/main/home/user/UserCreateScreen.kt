package uz.coder.foottopbusiness.presentation.main.home.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.collectLatest
import uz.coder.foottopbusiness.core.ui.Primary

class UserCreateScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<UserCreateViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is UserCreateContract.Effect.ShowError -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    UserCreateContract.Effect.NavigateBack -> {
                        navigator.pop()
                    }
                }
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "Hisob yaratish",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), start = padding.calculateStartPadding(LayoutDirection.Ltr), end = padding.calculateEndPadding(
                        LayoutDirection.Ltr))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LabelAndField("TO'LIQ ISM", state.fullName, "Sardor Rahimov") {
                    viewModel.onEvent(UserCreateContract.Event.FullNameChanged(it))
                }

                LabelAndField("LOGIN (EMAIL)", "", "sardor@malaeb.uz") {
                    // TODO: Update login
                }

                LabelAndField("TELEFON", state.phone, "+998 90 123 45 67", KeyboardType.Phone) {
                    viewModel.onEvent(UserCreateContract.Event.PhoneChanged(it))
                }

                Column {
                    Text("PAROL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    var passwordVisible by remember { mutableStateOf(false) }
                    TextField(
                        value = "",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Parol kiriting", color = Color.Gray) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray)
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.LightGray,
                            unfocusedIndicatorColor = Color.LightGray
                        )
                    )
                }

                Column {
                    Text("ROL TANLANG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RoleItem("Admin", Icons.Default.Settings, "To'liq boshqaruv", state.role == "ADMIN", Modifier.weight(1f)) {
                            viewModel.onEvent(UserCreateContract.Event.RoleChanged("ADMIN"))
                        }
                        RoleItem("Stadion egasi", Icons.Outlined.Home, "Maydonlarni nazorat", state.role == "OWNER", Modifier.weight(1f)) {
                            viewModel.onEvent(UserCreateContract.Event.RoleChanged("OWNER"))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RoleItem("Coach", Icons.Outlined.Person, "Murabbiy kabineti", state.role == "COACH", Modifier.weight(1f)) {
                            viewModel.onEvent(UserCreateContract.Event.RoleChanged("COACH"))
                        }
                        RoleItem("Kuzatuvchi", Icons.Outlined.Visibility, "Faqat ko'rish", state.role == "VIEWER", Modifier.weight(1f)) {
                            viewModel.onEvent(UserCreateContract.Event.RoleChanged("VIEWER"))
                        }
                    }
                }

                LabelAndField("BIRIKTIRILGAN STADION", "", "Sport Arena A") {
                    // TODO: Update assigned stadium
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0F2F1))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Login va parol SMS orqali yuboriladi", color = Color(0xFF00695C), fontSize = 14.sp)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.onEvent(UserCreateContract.Event.CreateClicked) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D2E))
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Hisobni yaratish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelAndField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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

@Composable
private fun RoleItem(
    title: String,
    icon: ImageVector,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFE0F2F1) else Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = if (isSelected) Color(0xFF00695C) else Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(subtitle, fontSize = 10.sp, color = Color.Gray)
    }
}
