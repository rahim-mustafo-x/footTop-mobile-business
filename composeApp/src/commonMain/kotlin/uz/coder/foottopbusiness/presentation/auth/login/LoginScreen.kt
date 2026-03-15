package uz.coder.foottopbusiness.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import uz.coder.foottopbusiness.core.ui.Border
import uz.coder.foottopbusiness.core.ui.BorderFocused
import uz.coder.foottopbusiness.core.ui.Info
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.core.ui.UniversalClickableText

@Composable
fun LoginScreen(
    navigateToMain: () -> Unit,
    navigateBack: () -> Unit,
    showToast: (String) -> Unit,
    viewModel: LoginViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // countdown timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            viewModel.handleEvent(LoginContract.Event.TimerTick)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginContract.Effect.NavigateToMain -> navigateToMain()
                is LoginContract.Effect.ShowToast -> showToast(effect.message)
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)) {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }

            Spacer(Modifier.height(16.dp))

            // FootTop title
            val title = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)) {
                    append("Foot")
                }
                withStyle(SpanStyle(color = Primary, fontSize = 30.sp, fontWeight = FontWeight.Bold)) {
                    append("Top")
                }
            }
            Text(title)

            Spacer(Modifier.height(32.dp))

            // Subtitle
            Text(
                "Enter the code sent to your phone ${state.phoneNumber}",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 30.dp)
            )

            Spacer(Modifier.height(24.dp))

            // OTP boxes
            OtpCode(state.otpCode) {
                viewModel.handleEvent(LoginContract.Event.OtpCode(it))
            }

            Spacer(Modifier.height(20.dp))

            // Timer
            val minutes = state.secondsLeft / 60
            val seconds = state.secondsLeft % 60
            Text(
                "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')} vaqt qoldi",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            // Help text
            UniversalClickableText(
                textParts = listOf(
                    "Qandaydir muammo yuzaga kelgan bo'lsa " to null,
                    "Yordam xizmati" to HELP_CENTER,
                    "ga murojaat qiling." to null
                ),
                styles = mapOf(
                    HELP_CENTER to SpanStyle(color = Info, fontSize = 14.sp)
                ),
                modifier = Modifier.fillMaxWidth()
            ) { tag->
                when(tag){
                    HELP_CENTER->{
                        //todo add linker when it said
                        print("help center clicked")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Resend button
            Button(
                onClick = { viewModel.handleEvent(LoginContract.Event.ResendCode) },
                enabled = state.canResend,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFEEEEEE),
                    disabledContentColor = Color.Gray
                )
            ) {
                Text("Resend Code", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun OtpCode(otpCode: String, onOtpChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
        BasicTextField(
            otpCode,
            onValueChange = onOtpChange,
            textStyle = TextStyle(color = Color.Transparent),
            modifier = Modifier.size(1.dp).focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            modifier = Modifier.fillMaxWidth().clickable { focusRequester.requestFocus() },
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            repeat(6) {
                val digit = otpCode.getOrNull(it) ?: ""
                val isFocused = otpCode.length == it
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.Transparent, RoundedCornerShape(10.dp))
                        .border(
                            width = 2.dp,
                            color = when {
                                isFocused -> BorderFocused
                                digit.toString().isNotEmpty() -> BorderFocused
                                else -> Border
                            },
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(digit.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
//tags
const val HELP_CENTER = "HELP_CENTER"