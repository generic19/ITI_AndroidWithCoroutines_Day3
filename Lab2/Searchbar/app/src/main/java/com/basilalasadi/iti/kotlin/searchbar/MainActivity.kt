package com.basilalasadi.iti.kotlin.searchbar

import android.os.Bundle
import android.view.WindowInsets.Type.systemBars
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.basilalasadi.iti.kotlin.searchbar.ui.theme.SearchbarTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SearchbarTheme {
                MainScreen()
            }
        }
    }
}

private val names = listOf(
    "Ahmed",
    "Abdullah",
    "Aya",
    "Basel",
    "Mahmoud",
    "Ziad",
)

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MainScreen() {
    val queryFlow = remember { MutableStateFlow("") }
    val query = queryFlow.collectAsStateWithLifecycle()
    val filteredList = rememberSaveable { mutableStateOf(names) }

    LaunchedEffect(queryFlow) {
        queryFlow
            .debounce(500)
            .collect { q ->
                filteredList.value = names.fastFilter {
                    it.contains(q, ignoreCase = true)
                }
            }
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TextField(
                value = query.value,
                onValueChange = {
                    queryFlow.value = it
                },
                label = { Text("Search") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(items = filteredList.value) { _, item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    SearchbarTheme {
        MainScreen()
    }
}