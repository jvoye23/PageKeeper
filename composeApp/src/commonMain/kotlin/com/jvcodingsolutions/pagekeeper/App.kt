package com.jvcodingsolutions.pagekeeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jvcodingsolutions.pagekeeper.app.MainViewModel
import com.jvcodingsolutions.pagekeeper.app.NavigationRoot
import com.jvcodingsolutions.pagekeeper.designsystem.theme.PageKeeperTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {

    val viewModel: MainViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    PageKeeperTheme {
        if(!state.isChecking) {
            NavigationRoot()
        }
    }
}