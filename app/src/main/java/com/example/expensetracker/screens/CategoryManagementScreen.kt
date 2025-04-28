package com.example.expensetracker.screens

import android.annotation.SuppressLint
import android.graphics.drawable.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.model.Category
import com.example.expensetracker.viewmodel.CategoryViewModel
import org.w3c.dom.Text

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CategoryManagementScreen(viewModel: CategoryViewModel){

    var isDialogueVisible by remember {
        mutableStateOf(false)
    }

    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()

    Scaffold(
        floatingActionButton = {
           FloatingActionButton(onClick = { isDialogueVisible = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
           }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item{ Text(text = "Income", style = MaterialTheme.typography.headlineMedium)}
            items(incomeCategories) { category ->
                CategoryItem(
                    category = category,
                    onDelete = {viewModel.deleteCategory(category)}
                )
            }

            item{ Spacer(modifier = Modifier.height(16.dp)) }

            item { Text(text = "Expense", style = MaterialTheme.typography.headlineMedium) }
            items(expenseCategories){ category ->
                CategoryItem(
                    category = category,
                    onDelete = {viewModel.deleteCategory(category)}
                )
            }
        }
        if(isDialogueVisible){
            AddCategoryDialog(
                onDismiss = { isDialogueVisible = false },
                onAdd = { name , type ->
                    viewModel.addCategory(name, type)
                    isDialogueVisible = false
                },
                existingCategories = incomeCategories + expenseCategories
            )
        }
    }

}

@Composable
fun CategoryItem(category: Category , onDelete: () -> Unit){

    var showDialog by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                //.clickable { }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = category.name , style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = { showDialog = true} ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete category")
            }
        }
    }

    if (showDialog){
        DeleteCategoryDialog(
            categoryName = category.name,
            onConfirm = {
                onDelete()
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun DeleteCategoryDialog(categoryName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Category") },
        text = {
            Text("Deleting \"$categoryName\" will also remove all its transactions, budget, and analysis data. Are you sure?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Yes, Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
    existingCategories: List<Category>) {

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Income") }
    var expanded by remember { mutableStateOf(false) }

    var showErrorDialog by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Category") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Type")
                Box {
                    Text(
                        text = type,
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(8.dp)
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            onClick = { type = "Income"; expanded = false },
                            text = { Text("Income") }
                        )
                        DropdownMenuItem(
                            onClick = { type = "Expense"; expanded = false },
                            text = { Text("Expense") }
                        )
                    }

                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmedName = name.trim()
                val categoryExists = existingCategories.any {
                    it.name.equals(trimmedName, ignoreCase = true) && it.type == type
                }

                if (trimmedName.isEmpty()) {
                    showErrorDialog = true
                } else if (categoryExists) {
                    showErrorDialog = true
                } else {
                    onAdd(trimmedName, type)
                    onDismiss()
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )

    if (showErrorDialog) {
        val trimmedName = name.trim()
        val duplicateExists = existingCategories.any {
            it.name.equals(trimmedName, ignoreCase = true) && it.type == type
        }

        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = {
                if (trimmedName.isEmpty()) {
                    Text("Category name cannot be empty.")
                } else if (duplicateExists) {
                    Text("A \"$trimmedName\" category already exists in \"$type\".")
                }
            },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

}