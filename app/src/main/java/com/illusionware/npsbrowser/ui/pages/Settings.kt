package com.illusionware.npsbrowser.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.illusionware.npsbrowser.Preferences
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.ui.components.NPSAlertDialog
import com.illusionware.npsbrowser.ui.components.NPSIconButton
import com.illusionware.npsbrowser.ui.components.NPSRadioButton
import com.illusionware.npsbrowser.ui.theme.ColorSecondaryLight
import com.illusionware.npsbrowser.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@Preview
fun SettingsScreen(navigationGoBack: () -> Unit = {}, onThemeChange: (v: Int) -> Unit = {}) {
    Scaffold (
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            ) {
                NPSIconButton(tooltip = "Go Back", onClick = { navigationGoBack() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back" )
                }
                Text(text = stringResource(id = R.string.title_activity_settings), style = Typography.titleLarge)
            }
        },
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(vertical = 16.dp)) {
                Appearance(onThemeChange)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Header("Updates")
                    Spacer(modifier = Modifier.size(2.dp))
                    DialogSetting(title = "HMAC Key", value = "None", icon = Icons.Outlined.Key)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Header("Downloads")
                    Spacer(modifier = Modifier.size(2.dp))
                    ToggleSetting(title = "Unpack in Download Directory")
                    DialogSetting(title = "Download Directory", value = "sdcard/Downloads", icon = Icons.Outlined.FileDownload)
                    DialogSetting(title = "Unpack Directory", value = "sdcard/unpack", icon = Icons.Outlined.Unarchive)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Header("Pkg2zip")
                    Spacer(modifier = Modifier.size(2.dp))
                    ToggleSetting(title = "Automatically decrypt downloaded content")
                    DialogSetting(title = "Decryption params", value = "-x {pkgFile} \"{zRifKey}\"", icon = painterResource(
                        id = R.drawable.lock_open
                    ))
                }
            }
        }

    }
}

@Composable
fun SettingGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Header(title)
        Spacer(modifier = Modifier.size(2.dp))
        content()
    }
}

val Themes = hashMapOf(
    0 to "Light",
    1 to "Dark",
    2 to "Use System Theme"
)

@Composable
fun Appearance(onThemeChange: (v: Int) -> Unit) {
    val prefs = Preferences(LocalContext.current)
    val theme = prefs.theme.collectAsState(initial = 0)

    var themeOpenDialog by remember { mutableStateOf(false) }

    SettingGroup(title = stringResource(id = R.string.appearance)) {
        DialogSetting(
            title = stringResource(id = R.string.theme),
            value = Themes[theme.value] ?: "None",
            icon = Icons.Outlined.Palette,
            onClick = { themeOpenDialog = true }
        )
    }
    if (themeOpenDialog) {
        ThemeDialog(
            onDismiss = { themeOpenDialog = false },
            onThemeChange = onThemeChange
        )
    }
}

@Composable
fun ThemeDialog(onDismiss: () -> Unit, onThemeChange: (v: Int) -> Unit) {
    val prefs = Preferences(LocalContext.current)
    val theme = prefs.theme.collectAsState(initial = "")

    NPSAlertDialog(
        onDismiss = onDismiss,
        title = stringResource(id = R.string.theme),
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            NPSRadioButton(
                text = "Light",
                onClick = { CoroutineScope(Dispatchers.IO).launch {
                    onThemeChange(0)
                    prefs.changeTheme(0) }
                },
                selected = theme.value == 0,
            )
            NPSRadioButton(
                text = "Dark",
                onClick = { CoroutineScope(Dispatchers.IO).launch {
                    onThemeChange(1)
                    prefs.changeTheme(1) }
                },
                selected = theme.value == 1,
            )
            NPSRadioButton(
                text = "Use System Theme",
                onClick = { CoroutineScope(Dispatchers.IO).launch {
                    onThemeChange(2)
                    prefs.changeTheme(2) }
                },
                selected = theme.value == 2,
            )
        }
    }
}

@Composable
fun Header(title: String) {
    Row(Modifier.padding(start = 56.dp)) {
        Text(text = title, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
// TODO: remove the default value for onClick
fun DialogSetting(title: String, value: String, icon: Painter, onClick: () -> Unit = {}) {
    Box(
         Modifier
             .fillMaxWidth()
             .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(painter = icon, contentDescription = "TODO:", modifier = Modifier.width(56.dp))
            Column {
                Text(text = title, color = MaterialTheme.colorScheme.onBackground)
                Text(text = value, color = ColorSecondaryLight)
            }
        }
    }
}

@Composable
// TODO: remove the default value for onClick
fun DialogSetting(title: String, value: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = "TODO:", modifier = Modifier.width(56.dp))
            Column {
                Text(text = title, color = MaterialTheme.colorScheme.onBackground)
                Text(text = value, color = ColorSecondaryLight)
            }
        }
    }
}

@Composable
fun ToggleSetting(title: String, icon: ImageVector? = null) {
    Box(
        modifier = Modifier.clickable { /* TODO: */ },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(top = 8.dp, end = 24.dp, bottom = 8.dp),
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = "TODO:", Modifier.width(56.dp))
            } else {
                Box(modifier = Modifier.width(56.dp))
            }
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1.0f))
            Switch(checked = true, onCheckedChange = { /* TODO: */ })
        }
    }
}
