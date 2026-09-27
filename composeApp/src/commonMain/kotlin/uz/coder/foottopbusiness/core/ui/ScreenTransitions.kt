package uz.coder.foottopbusiness.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.tab.TabNavigator

/** Tez boshlanib, yumshoq to'xtaydigan egri chiziq (iOS/Material "emphasized"ga yaqin). */
private val Emphasized = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private const val SCREEN_MS = 420

/**
 * [AnimatedScreens] bilan ishlatiladigan Navigator uchun: ekranlarni Voyager o'zi
 * darhol emas, animatsiya tugagach [AnimatedScreens] dispose qiladi.
 */
val AnimatedNavigatorDisposeBehavior = NavigatorDisposeBehavior(disposeSteps = false)

/**
 * `navigator.lastItem.Content()` o'rniga — ekranlar almashuvi animatsiyali:
 *  - push: yangi ekran o'ngdan suriladi, eskisi chapga siljib xiralashadi;
 *  - pop: yopilayotgan ekran o'ngga chiqib ketadi, ostidagisi qaytib keladi;
 *  - replace (login ↔ asosiy): yumshoq kattalashib paydo bo'ladi.
 *
 * Navigator [AnimatedNavigatorDisposeBehavior] bilan yaratilishi kerak, aks holda chiqib
 * ketayotgan ekranning ScreenModel'i animatsiya paytida qayta yaratilib qoladi.
 */
// clearEvent() — Voyager'ning o'z ScreenTransition'i ham shunday chaqiradi
@OptIn(InternalVoyagerApi::class)
@Composable
fun AnimatedScreens(navigator: Navigator, modifier: Modifier = Modifier) {
    // Stackdan chiqqan, lekin animatsiyasi hali tugamagan ekranlar
    val toDispose = remember { mutableStateOf(emptySet<Screen>()) }
    val currentScreens = navigator.items
    DisposableEffect(currentScreens) {
        onDispose {
            val newKeys = navigator.items.map { it.key }
            toDispose.value += currentScreens.filter { it.key !in newKeys }
        }
    }

    AnimatedContent(
        targetState = navigator.lastItem,
        modifier = modifier,
        transitionSpec = { screenTransition(navigator.lastEvent) },
        contentKey = { it.key },
        label = "screen"
    ) { screen ->
        if (transition.currentState == transition.targetState) {
            LaunchedEffect(Unit) {
                val aliveKeys = navigator.items.map { it.key }
                val dead = toDispose.value.filterNot { it.key in aliveKeys }
                if (dead.isNotEmpty()) {
                    dead.forEach { navigator.dispose(it) }
                    navigator.clearEvent()
                }
                toDispose.value = emptySet()
            }
        }
        // Fonsiz ekranlar surilayotganda ostidagi ekran ko'rinib qolmasin
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            navigator.saveableState("transition", screen) {
                screen.Content()
            }
        }
    }
}

private fun AnimatedContentTransitionScope<Screen>.screenTransition(event: StackEvent): ContentTransform =
    when (event) {
        // Ustidagi ekran shaffof bo'lmaydi — orqadagisi ko'rinib qolmasin
        StackEvent.Push -> slideInHorizontally(tween(SCREEN_MS, easing = Emphasized)) { it } togetherWith (
            slideOutHorizontally(tween(SCREEN_MS, easing = Emphasized)) { -it / 4 } +
                fadeOut(tween(SCREEN_MS), targetAlpha = 0.4f) +
                scaleOut(tween(SCREEN_MS, easing = Emphasized), targetScale = 0.96f)
            )

        StackEvent.Pop -> (
            slideInHorizontally(tween(SCREEN_MS, easing = Emphasized)) { -it / 4 } +
                fadeIn(tween(SCREEN_MS), initialAlpha = 0.4f) +
                scaleIn(tween(SCREEN_MS, easing = Emphasized), initialScale = 0.96f)
            ) togetherWith slideOutHorizontally(tween(SCREEN_MS, easing = Emphasized)) { it }

        else -> (
            fadeIn(tween(360, delayMillis = 90)) +
                scaleIn(tween(460, delayMillis = 90, easing = Emphasized), initialScale = 0.92f)
            ) togetherWith fadeOut(tween(120))
    }.apply {
        // Push'da yangi ekran, pop'da yopilayotgan ekran ustida turadi
        targetContentZIndex = if (event == StackEvent.Pop) -1f else 1f
    } using SizeTransform(clip = false)

/**
 * Tablar almashganda yangi tab yumshoq kattalashib paydo bo'ladi.
 * Avvalgidek faqat tanlangan tab chiziladi (tab holati saqlanmaydi).
 */
@Composable
fun AnimatedCurrentTab(tabNavigator: TabNavigator, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = tabNavigator.current,
        modifier = modifier,
        transitionSpec = {
            (
                fadeIn(tween(260, delayMillis = 80)) +
                    scaleIn(tween(360, delayMillis = 80, easing = Emphasized), initialScale = 0.96f)
                ) togetherWith fadeOut(tween(100)) using SizeTransform(clip = false)
        },
        contentKey = { it.key },
        label = "tab"
    ) { tab ->
        tab.Content()
    }
}
