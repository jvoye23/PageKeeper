package com.jvcodingsolutions.pagekeeper.feature.globalbookmarks.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.jvcodingsolutions.pagekeeper.designsystem.theme.BgMain
import com.jvcodingsolutions.pagekeeper.designsystem.theme.StateAlert
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextPrimary
import com.jvcodingsolutions.pagekeeper.designsystem.theme.TextSecondary

@Composable
fun DeleteAllBookmarksDialog(
    onAction: (GlobalBookmarksAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(GlobalBookmarksAction.OnDismissDialog) },
        containerColor = BgMain,
        title = {
            Text(
                text = "Delete all bookmarks?",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        },
        text = {
            Text(
                text = "All bookmarks for this book will be permanently removed.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = { onAction(GlobalBookmarksAction.OnConfirmDeleteAll) }) {
                Text("Delete", color = StateAlert)
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(GlobalBookmarksAction.OnDismissDialog) }) {
                Text("Cancel", color = TextPrimary)
            }
        },
    )
}
