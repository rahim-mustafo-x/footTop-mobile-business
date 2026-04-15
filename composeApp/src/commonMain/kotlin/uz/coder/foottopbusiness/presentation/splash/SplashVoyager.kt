package uz.coder.foottopbusiness.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
    val infiniteTransition = rememberInfiniteTransition()
    
    // Aylanish animatsiyasi (koptok dumalayotgandek)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Sakrash animatsiyasi
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = bounce.dp)
                    .rotate(rotation),
                tint = MaterialTheme.colorScheme.primary
            )

            val title = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) { append("Foot") }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontSize = 32.sp, fontWeight = FontWeight.Bold)) { append("Top") }
            }
            Text(title, modifier = Modifier.padding(top = 16.dp))
            Text(
                "Business",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
    }
}
