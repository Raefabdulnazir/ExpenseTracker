package com.example.expensetracker.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.sharp.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import org.w3c.dom.Text

/*
* Settings
*
* Preferences - currency , budget management
* Appearances   -   dark mode , light mode
* Notifications - budget notifications , nightly reminder
* Security  -   passcode or fingerprint
* Help & Support
* About & App Info
* */

@Composable
fun SettingsScreen(){

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(19.dp)
    ) {
        //Settings Header
        item { 
            Text(
                text = "Settings" ,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        //Preferences Section
        item {
            SettingsMenuRow(
                icon = Icons.Default.Settings,
                title = "Preferences",
                description = "Currency , Budget , Management",
                onClick = {onMenuClick("Preferences")}
            )
        }

        //Appearance Section
        item{
            SettingsMenuRow(
                icon = Icons.Default.Star,  //need to change
                title = "Appearance",
                description = "Dark mode / Light mode",
                onClick = { onMenuClick("Appearance")
                }
            )
        }

        // Notifications Section
        item {
            SettingsMenuRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Manage reminders and alerts",
                onClick = { onMenuClick("Notifications") }
            )
        }

        // Security Section
        item {
            SettingsMenuRow(
                icon = Icons.Default.Lock,
                title = "Security",
                description = "Passcode or Fingerprint",
                onClick = { onMenuClick("Security") }
            )
        }

        // Help & Support Section
        item {
            SettingsMenuRow(
                icon = Icons.Rounded.Call,  //need to change
                title = "Help & Support",
                onClick = { onMenuClick("Help & Support") }
            )
        }

        // About & App info
        item {
            SettingsMenuRow(
                icon = Icons.Filled.Info,
                title = "About & App Info",
                onClick = { onMenuClick("About & App Info") }
            )
        }

    }
}

@Composable
fun SettingsMenuRow(icon: ImageVector , title: String , description: String? = null , onClick: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title , style = MaterialTheme.typography.bodyLarge)
            if(description != null){
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun onMenuClick(title: String){

}