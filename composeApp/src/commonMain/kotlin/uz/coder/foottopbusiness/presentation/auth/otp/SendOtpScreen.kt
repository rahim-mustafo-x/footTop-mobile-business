package uz.coder.foottopbusiness.presentation.auth.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import uz.coder.foottopbusiness.core.ui.Info
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.core.visualTransformation.PhoneTransformation

@Composable
fun SendOtpScreen(
    navigateToLogin: () -> Unit,
    showToast: (String) -> Unit,
    viewModel: SendOtpViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isLoading by remember { mutableStateOf(false) }
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues), horizontalAlignment = Alignment.CenterHorizontally){
            val appName = buildAnnotatedString {
                withStyle(SpanStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )){
                    append("Foot")
                }
                withStyle(SpanStyle(Primary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold)){
                    append("Top")
                }
                append("\n")
                withStyle(SpanStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraLight)){
                    append("Business")
                }
            }
            Text(appName, modifier = Modifier.padding(vertical = 40.dp))

            Text("Welcome!", fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
            Text("Enter your phone number to continue", fontSize = 17.sp, modifier = Modifier.padding(bottom = 40.dp))

            Row(modifier = Modifier.padding(30.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("+998", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                TextField(state.phoneNumber, onValueChange = {text->
                    viewModel.handleEvent(
                        SendOtpContract.Event.TypePhoneNumber(text)
                    )
                }, textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    placeholder = { Text("90 123 45 67", fontSize = 20.sp) },
                    visualTransformation = PhoneTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )

                )
            }
            val youAgreeTerms = buildAnnotatedString {
                withStyle(SpanStyle(
                    fontSize = 14.sp)){
                    append("Ro'yhatdan o'tish bilan siz ")
                }
                withStyle(SpanStyle(color = Info,
                    fontSize = 14.sp)){
                    append("Foydalanish shartlari")
                }
                withStyle(SpanStyle(
                    fontSize = 14.sp)){
                    append("ga rozilik bildirasiz")
                }
            }

            Text(youAgreeTerms, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(7.dp))
            val callHelp = buildAnnotatedString {
                withStyle(SpanStyle(
                    fontSize = 14.sp)){
                    append("Qandaydir muammo yuzaga kelgan bo'lsa ")
                }
                withStyle(SpanStyle(color = Info,
                    fontSize = 14.sp)){
                    append("Yordam xizmati")
                }
                withStyle(SpanStyle(
                    fontSize = 14.sp)){
                    append("ga murojaat qiling")
                }
            }
            Text(callHelp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(10.dp))

            Button(onClick = {
                viewModel.handleEvent(SendOtpContract.Event.NavigateToLogin)
            }, shape = RoundedCornerShape(5.dp), contentPadding = PaddingValues(10.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)){
                if (isLoading)
                    CircularProgressIndicator()
                else
                    Text("Continue", fontSize = 20.sp, color = Color.White)
            }
        }

    }
    LaunchedEffect(Unit){
        viewModel.effect.collect {
            when(it){
                SendOtpContract.Effect.NavigateToLogin -> {
                    isLoading = false
                    navigateToLogin()
                }
                is SendOtpContract.Effect.ShowToast -> {
                    isLoading = false
                    showToast(it.message)
                }
                is SendOtpContract.Effect.Error -> {
                    isLoading = false
                    showToast(it.message?:"")
                }
                SendOtpContract.Effect.Loading -> {
                    isLoading = true
                }
            }
        }
    }
    LaunchedEffect(state.phoneNumber) {
        print("phoneNumber='${state.phoneNumber}'")
    }
}