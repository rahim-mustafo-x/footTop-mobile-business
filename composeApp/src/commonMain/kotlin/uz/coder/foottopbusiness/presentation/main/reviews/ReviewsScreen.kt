package uz.coder.foottopbusiness.presentation.main.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import uz.coder.foottopbusiness.core.formatAsDate
import uz.coder.foottopbusiness.core.localization.ErrorMapper
import uz.coder.foottopbusiness.core.localization.Language
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.core.ui.Warning
import uz.coder.foottopbusiness.data.network.dto.owner.StaffRatingDto

/** Sharhlar ekrani (bosh sahifa va sozlamalardan ochiladi). */
class ReviewsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<ReviewsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow
        val strings = Localization.current
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                snackbarHostState.showSnackbar(
                    when (effect) {
                        ReviewsContract.Effect.ReplySaved -> strings.replySaved
                        ReviewsContract.Effect.ReplyDeleted -> strings.replyDeleted
                        is ReviewsContract.Effect.ShowError ->
                            effect.message.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it, strings) }
                                ?: strings.unknownNetworkError
                    }
                )
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(strings.reviews) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.average?.let { avg ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    oneDecimal(avg),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Stars(avg)
                                Text(
                                    "${strings.averageRating} · ${state.ratings.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !state.onlyUnanswered,
                            onClick = { viewModel.handleEvent(ReviewsContract.Event.SetOnlyUnanswered(false)) },
                            label = { Text(strings.all) }
                        )
                        val count = state.unanswered.size
                        FilterChip(
                            selected = state.onlyUnanswered,
                            onClick = { viewModel.handleEvent(ReviewsContract.Event.SetOnlyUnanswered(true)) },
                            label = { Text(if (count > 0) "${strings.unanswered} ($count)" else strings.unanswered) }
                        )
                    }
                }
                when {
                    state.isLoading && state.ratings.isEmpty() -> item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null && state.ratings.isEmpty() -> item {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudOff, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                state.error?.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it, strings) } ?: strings.loadFailed,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { viewModel.handleEvent(ReviewsContract.Event.Load) }) { Text(strings.retry) }
                        }
                    }
                    state.shown.isEmpty() -> item {
                        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.StarOutline, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(strings.noReviews, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    else -> items(state.shown, key = { it.id }) { r ->
                        ReviewCard(r, strings) { viewModel.handleEvent(ReviewsContract.Event.Edit(r)) }
                    }
                }
            }
        }

        ReplyDialog(state, strings, viewModel::handleEvent)
    }
}

private fun oneDecimal(v: Double): String {
    val r = kotlin.math.round(v * 10) / 10
    return r.toString().replace('.', ',')
}

@Composable
private fun Stars(value: Double) {
    Row {
        (1..5).forEach { i ->
            Icon(
                if (i <= kotlin.math.round(value)) Icons.Default.Star else Icons.Outlined.StarOutline,
                null,
                Modifier.size(16.dp),
                tint = Warning
            )
        }
    }
}

@Composable
private fun ReviewCard(r: StaffRatingDto, strings: Language, onReply: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (r.userFullName ?: "?").take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        r.userFullName?.takeIf { it.isNotBlank() } ?: strings.anonymousPlayer,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val date = r.createdAt.toLocalDateTimeSafe()?.formatAsDate()
                    Text(
                        listOfNotNull(r.stadiumName, date).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Stars((r.rating ?: 0).toDouble())
            }
            r.comment?.takeIf { it.isNotBlank() }?.let { Text(it) }
            r.ownerReply?.takeIf { it.isNotBlank() }?.let { reply ->
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(strings.yourReply, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(reply, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            TextButton(onClick = onReply) {
                val hasReply = !r.ownerReply.isNullOrBlank()
                Icon(if (hasReply) Icons.Default.Edit else Icons.AutoMirrored.Filled.Reply, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (hasReply) strings.editReply else strings.replyAction)
            }
        }
    }
}

@Composable
private fun ReplyDialog(state: ReviewsContract.State, strings: Language, onEvent: (ReviewsContract.Event) -> Unit) {
    val rating = state.editing ?: return
    val hasReply = !rating.ownerReply.isNullOrBlank()
    val tooLong = state.replyText.length > MAX_REPLY
    AlertDialog(
        onDismissRequest = { onEvent(ReviewsContract.Event.DismissEdit) },
        title = { Text(if (hasReply) strings.editReply else strings.replyAction) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rating.comment?.takeIf { it.isNotBlank() }?.let {
                    Text("“$it”", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
                OutlinedTextField(
                    value = state.replyText,
                    onValueChange = { onEvent(ReviewsContract.Event.UpdateReply(it)) },
                    label = { Text(strings.yourReply) },
                    placeholder = { Text(strings.replyPlaceholder) },
                    minLines = 4,
                    isError = tooLong,
                    supportingText = { Text("${state.replyText.length}/$MAX_REPLY") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(ReviewsContract.Event.SaveReply) },
                enabled = !state.isSaving && state.replyText.isNotBlank() && !tooLong
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(strings.save)
                }
            }
        },
        dismissButton = {
            if (hasReply) {
                OutlinedButton(
                    onClick = { onEvent(ReviewsContract.Event.DeleteReply) },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(strings.deleteReply) }
            } else {
                OutlinedButton(onClick = { onEvent(ReviewsContract.Event.DismissEdit) }) { Text(strings.cancel) }
            }
        }
    )
}
