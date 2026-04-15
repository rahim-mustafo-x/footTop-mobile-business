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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.coder.foottopbusiness.core.ui.Info
import uz.coder.foottopbusiness.core.ui.UniversalClickableText
import uz.coder.foottopbusiness.core.visualTransformation.PhoneTransformation

@Composable
fun SendOtpScreen(
    navigateToLogin: (String) -> Unit,
    showToast: (String) -> Unit,
    viewModel: SendOtpViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoading = state.isLoading
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues), horizontalAlignment = Alignment.CenterHorizontally){
            val appName = buildAnnotatedString {
                withStyle(SpanStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )){
                    append("Foot")
                }
                withStyle(SpanStyle(MaterialTheme.colorScheme.primary,
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
            UniversalClickableText(textParts = listOf(
                "Ro'yhatdan o'tish bilan siz " to null,
                "Foydalanish shartlari" to USER_TERMS,
                "ga rozilik bildirasiz" to null
            ),
            styles = mapOf(
                USER_TERMS to SpanStyle(color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
            ), modifier = Modifier.fillMaxWidth()){ tag->
                when(tag){
                    USER_TERMS->{
                        //todo add linker when it said
                        print("user terms clicked")
                    }
                }
            }
            Spacer(modifier = Modifier.height(7.dp))
            UniversalClickableText(textParts = listOf(
                "Qandaydir muammo yuzaga kelgan bo'lsa " to null,
                "Yordam xizmati" to HELP_CENTER,
                "ga murojaat qiling" to null
            ), styles = mapOf(
                HELP_CENTER to SpanStyle(color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
            ), modifier = Modifier.fillMaxWidth()){tag->
                when(tag){
                    HELP_CENTER->{
                        //todo add linker when it said
                        print("help center clicked")
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    viewModel.handleEvent(SendOtpContract.Event.NavigateToLogin)
                },
                enabled = state.phoneNumber.length == 9 && !isLoading,
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)
            ) {
                if (isLoading)
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp).width(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                else
                    Text("Continue", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }

    }
    LaunchedEffect(Unit){
        viewModel.effect.collect {effect->
            when(effect){
                is SendOtpContract.Effect.NavigateToLogin -> {
                    navigateToLogin(effect.phoneNumber)
                }
                is SendOtpContract.Effect.ShowToast -> {
                    showToast(effect.message)
                }
                is SendOtpContract.Effect.Error -> {
                    showToast(effect.message?:"")
                }
                else -> {}
            }
        }
    }
    LaunchedEffect(state.phoneNumber) {
        print("phoneNumber='${state.phoneNumber}'")
    }
}
//tags
const val USER_TERMS = "USER_TERMS"
const val HELP_CENTER = "HELP_CENTER"