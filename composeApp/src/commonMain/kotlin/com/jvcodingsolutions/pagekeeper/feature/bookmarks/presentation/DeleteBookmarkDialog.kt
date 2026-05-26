package com.jvcodingsolutions.pagekeeper.feature.bookmarks.presentation

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
fun DeleteBookmarkDialog(
    onAction: (BookmarksAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(BookmarksAction.OnDialogCancel) },
        containerColor = BgMain,
        title = {
            Text(
                text = "Delete this bookmark?",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        },
        text = {
            Text(
                text = "This action will permanently remove the selected bookmark.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = { onAction(BookmarksAction.OnConfirmDelete) }) {
                Text("Delete", color = StateAlert)
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(BookmarksAction.OnDialogCancel) }) {
                Text("Cancel", color = TextPrimary)
            }
        },
    )
}
