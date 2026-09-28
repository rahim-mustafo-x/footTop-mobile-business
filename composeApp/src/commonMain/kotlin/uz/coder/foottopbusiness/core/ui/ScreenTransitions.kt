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
 * darhol emas, kontenti kompozitsiyadan chiqqach [AnimatedScreens] dispose qiladi.
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
@OptIn(InternalVoyagerApi::class)
@Composable
fun AnimatedScreens(navigator: Navigator, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = navigator.lastItem,
        modifier = modifier,
        transitionSpec = { screenTransition(navigator.lastEvent) },
        contentKey = { it.key },
        label = "screen"
    ) { screen ->
        // Ekran kontenti kompozitsiyadan chiqqanda (chiqish animatsiyasi tugagach)
        // u stackda qolmagan bo'lsa, dispose qilinadi. Bu effekt kontentdan oldin
        // e'lon qilingan — onDispose teskari tartibda chaqiriladi, ya'ni ekranning
        // o'z lifecycle effektlari (onStop) avval ishlaydi. Aks holda Voyager allaqachon
        // DESTROYED bo'lgan lifecycle'ni to'xtatmoqchi bo'lib, ilova yiqiladi.
        DisposableEffect(screen.key) {
            onDispose {
                if (navigator.items.none { it.key == screen.key }) {
                    navigator.dispose(screen)
                }
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
                fadeOut(tween(SCREEN_MS), targetAlpha = 0.4f)
            )

        StackEvent.Pop -> (
            slideInHorizontally(tween(SCREEN_MS, easing = Emphasized)) { -it / 4 } +
                fadeIn(tween(SCREEN_MS), initialAlpha = 0.4f)
            ) togetherWith slideOutHorizontally(tween(SCREEN_MS, easing = Emphasized)) { it }

        else -> fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(120))
    }.apply {
        // Push'da yangi ekran, pop'da yopilayotgan ekran ustida turadi
        targetContentZIndex = if (event == StackEvent.Pop) -1f else 1f
    } using null // Ekranlar doim to'liq o'lchamda — o'lcham animatsiyasi keraksiz

/**
 * Tablar almashganda yangi tab yumshoq paydo bo'ladi.
 * Har bir tabning ichki navigator stack'i saqlanadi — qaytib kelganda ekranlar va
 * ularning ScreenModel'lari qayta yaratilmaydi (aks holda eski stack'dagi ekranlar
 * hech qachon dispose bo'lmay, xotirada qolib ketardi).
 */
@Composable
fun AnimatedCurrentTab(tabNavigator: TabNavigator, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = tabNavigator.current,
        modifier = modifier,
        transitionSpec = {
            fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(100)) using null
        },
        contentKey = { it.key },
        label = "tab"
    ) { tab ->
        tabNavigator.saveableState("currentTab", tab) {
            tab.Content()
        }
    }
}
