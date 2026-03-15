package uz.coder.foottopbusiness.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import uz.coder.foottopbusiness.core.ui.Primary
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpVoyager
import uz.coder.foottopbusiness.presentation.main.MainVoyager

object SplashVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<SplashViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    SplashNavigationEvent.NavigateToMain -> navigator.replace(MainVoyager)
                    SplashNavigationEvent.NavigateToLogin -> navigator.replace(SendOtpVoyager)
                }
            }
        }

        SplashScreenContent()
    }
}

@Composable
fun SplashScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder for Logo, replace with actual app icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Primary.copy(alpha = 0.1f), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text("⚽", fontSize = 60.sp)
            }

            val title = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) { append("Foot") }
                withStyle(SpanStyle(color = Primary, fontSize = 32.sp, fontWeight = FontWeight.Bold)) { append("Top") }
            }
            Text(title, modifier = Modifier.padding(top = 16.dp))
            Text(
                "Business",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
