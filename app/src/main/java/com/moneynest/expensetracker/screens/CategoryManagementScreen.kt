package com.moneynest.expensetracker.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneynest.expensetracker.model.Category
import com.moneynest.expensetracker.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    //onBackClick: () -> Unit
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Manage Categories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            /*navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },*/
            actions = {
                IconButton(onClick = { isDialogVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Category",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // INCOME CATEGORIES Section
            if (incomeCategories.isNotEmpty()) {
                SectionHeaderCategory(title = "INCOME CATEGORIES")
                Spacer(modifier = Modifier.height(8.dp))

                incomeCategories.forEach { category ->
                    CategoryCard(
                        category = category,
                        onDelete = { viewModel.deleteCategory(category) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // EXPENSE CATEGORIES Section
            if (expenseCategories.isNotEmpty()) {
                SectionHeaderCategory(title = "EXPENSE CATEGORIES")
                Spacer(modifier = Modifier.height(8.dp))

                expenseCategories.forEach { category ->
                    CategoryCard(
                        category = category,
                        onDelete = { viewModel.deleteCategory(category) }
                    )
                }
            }

            // Empty state
            if (incomeCategories.isEmpty() && expenseCategories.isEmpty()) {
                EmptyStateMessage()
            }
        }
    }

    // Add Category Dialog
    if (isDialogVisible) {
        AddCategoryDialog(
            onDismiss = { isDialogVisible = false },
            onAdd = { name, type ->
                viewModel.addCategory(name, type)
                isDialogVisible = false
            },
            existingCategories = incomeCategories + expenseCategories
        )
    }
}

@Composable
fun SectionHeaderCategory(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun CategoryCard(
    category: Category,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${category.type} Category",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${category.name}",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteCategoryDialog(
            categoryName = category.name,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun EmptyStateMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Categories Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to create your first category",
            fontSize = 14.sp,
            color = Color.Gray.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DeleteCategoryDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Category",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Deleting \"$categoryName\" will also remove all its transactions, budget, and analysis data. Are you sure?",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
    existingCategories: List<Category>
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Income") }
    var expanded by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Category",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Category Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Type Selection
                Text(
                    text = "Category Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Type Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { type = "Income" },
                        label = { Text("Income") },
                        selected = type == "Income",
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { type = "Expense" },
                        label = { Text("Expense") },
                        selected = type == "Expense",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val categoryExists = existingCategories.any {
                        it.name.equals(trimmedName, ignoreCase = true) && it.type == type
                    }

                    if (trimmedName.isEmpty() || categoryExists) {
                        showErrorDialog = true
                    } else {
                        onAdd(trimmedName, type)
                        onDismiss()
                    }
                }
            ) {
                Text("Add Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )

    // Error Dialog
    if (showErrorDialog) {
        val trimmedName = name.trim()
        val duplicateExists = existingCategories.any {
            it.name.equals(trimmedName, ignoreCase = true) && it.type == type
        }

        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Invalid Input") },
            text = {
                Text(
                    if (trimmedName.isEmpty()) {
                        "Category name cannot be empty."
                    } else if (duplicateExists) {
                        "A \"$trimmedName\" category already exists in \"$type\"."
                    } else {
                        "Please check your input and try again."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}