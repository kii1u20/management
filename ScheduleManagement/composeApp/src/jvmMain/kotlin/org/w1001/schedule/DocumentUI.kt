package org.w1001.schedule

import androidx.compose.runtime.Composable
import org.w1001.schedule.mainViews.ScheduleSpreadsheetUI

@Composable
fun DocumentUI(viewModel: AppViewModel) {
    when (viewModel.documentState.value) {
        is DocumentState.ScheduleState -> ScheduleSpreadsheetUI(viewModel)
        DocumentState.Empty -> { /* Show empty state or loading */ }
    }
}