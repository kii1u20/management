package org.w1001.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenu(
    inMainMenu: MutableState<Boolean>,
    numberOfColumns: MutableState<String>,
    workTimeType: MutableState<Int>,
    columnNames: MutableList<MutableState<String>>
) {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { inMainMenu.value = false }) {
                Text("Exit Main Menu")
            }

            ScheduleSetupUI(numberOfColumns, workTimeType, columnNames)
        }
    }
}

@Composable
fun ScheduleSetupUI(
    columns: MutableState<String>,
    workTime: MutableState<Int>,
    columnNames: MutableList<MutableState<String>>
) {
    var isColumnValid by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter the number of columns (must be a positive integer):")
            OutlinedTextField(
                value = columns.value,
                onValueChange = { input ->
                    // Validate input and ensure it's a positive integer
                    isColumnValid = input.toIntOrNull()?.let { it > 0 } ?: false
                    columns.value = input
                    showError = !isColumnValid

                    if (isColumnValid) {
                        columnNames.clear()
                        repeat(input.toInt()) {
                            columnNames.add(mutableStateOf(""))
                        }
                    }
                },
                isError = showError,
                modifier = Modifier.padding(16.dp)
            )

            if (showError) {
                Text(
                    text = "Please enter a valid positive integer.",
                    color = MaterialTheme.colors.error
                )
            } else {
                ColumnNamesInput(columns.value.toInt(), columnNames)
            }



            Text("Select work time type:")
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = workTime.value == 1,
                    onClick = { workTime.value = 1 }
                )
                Text("Single Work Time")
                RadioButton(
                    selected = workTime.value == 2,
                    onClick = { workTime.value = 2 }
                )
                Text("Double Work Time")
            }

            Button(onClick = {
                if (isColumnValid) {
                    inMainMenu.value = false
                } else {
                    showError = true
                }
            }) {
                Text("Submit")
            }
        }
    }
}

@Composable
fun ColumnNamesInput(numberOfColumns: Int, columnNames: List<MutableState<String>>) {
    Column {
        for (i in 0 until numberOfColumns) {
            OutlinedTextField(
                value = columnNames[i].value,
                onValueChange = { columnNames[i].value = it },
                label = { Text("Column ${i + 1} Name") },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}