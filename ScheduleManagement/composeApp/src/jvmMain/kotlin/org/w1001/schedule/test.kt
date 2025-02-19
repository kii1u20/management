package org.w1001.schedule

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.w1001.schedule.components.recomposeHighlighter

@Composable
fun test(

) {
    var size = remember { mutableStateOf(1000.dp) }
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Text("Hello")
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column(
            modifier = Modifier.horizontalScroll(rememberScrollState()).verticalScroll(rememberScrollState())
        ) {
            (0..40).forEach {
                LazyRow(
                    userScrollEnabled = false,
                    modifier = Modifier.width(size.value)
                ) {
                    items(200) {
                        Text("Hello", modifier = Modifier.recomposeHighlighter())
                    }
                }
            }

            Button(onClick = { size.value = 2000.dp }) {
                Text("Hello")
            }
        }
    }
}
