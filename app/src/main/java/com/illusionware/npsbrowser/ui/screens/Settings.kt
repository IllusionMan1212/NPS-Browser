package com.illusionware.npsbrowser.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.ui.components.NPSAlertDialog
import com.illusionware.npsbrowser.ui.components.NPSIconButton
import com.illusionware.npsbrowser.ui.components.NPSRadioButton
import com.illusionware.npsbrowser.ui.theme.ColorSecondaryLight
import com.illusionware.npsbrowser.ui.theme.Typography
import com.illusionware.npsbrowser.viewmodels.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.illusionware.npsbrowser.data.SettingsPreferences
import com.illusionware.npsbrowser.data.Theme
import com.illusionware.npsbrowser.model.ConsoleType
import com.illusionware.npsbrowser.model.PackageItemType

private val Themes = hashMapOf(
    Theme.LIGHT.ordinal to "Light",
    Theme.DARK.ordinal to "Dark",
    Theme.SYSTEM.ordinal to "Use System Theme"
)

@Composable
@Preview
fun SettingsScreen(
    navigationGoBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory
    )
) {
    val prefs = viewModel.uiState.collectAsStateWithLifecycle().value

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
                Appearance(viewModel, prefs)
                TsvFiles(viewModel, prefs)
                Updates(viewModel, prefs)
                Downloads(viewModel, prefs)
                Pkg2Zip(viewModel, prefs)
            }
        }

    }
}

@Composable
fun Appearance(viewModel: SettingsViewModel, prefs: SettingsPreferences) {
    var themeOpenDialog by remember { mutableStateOf(false) }

    SettingGroup(title = stringResource(id = R.string.appearance)) {
        DialogSetting(
            title = stringResource(id = R.string.theme),
            value = Themes[prefs.appTheme] ?: "None",
            icon = Icons.Outlined.Palette,
            onClick = { themeOpenDialog = true }
        )
    }
    if (themeOpenDialog) {
        ThemeDialog(
            prefs = prefs,
            onDismiss = { themeOpenDialog = false },
            onThemeChange = {
                viewModel.setTheme(it)
            }
        )
    }
}

@Composable
fun TsvFiles(viewModel: SettingsViewModel, prefs: SettingsPreferences) {
    var expanded by remember { mutableStateOf(false) }

    SettingGroup(title = "TSV Files") {
        TSVPicker(
            title = "PSV Games",
            value = Uri.decode(prefs.psvGames.ifEmpty { "Browse" }),
            icon = painterResource(id = R.drawable.vita_icon),
            onFilePick = {
                viewModel.setTSVFile(ConsoleType.PSVITA, PackageItemType.GAME, it)
            }
        )
        TSVPicker(
            title = "PSV DLC",
            value = Uri.decode(prefs.psvDlc.ifEmpty { "Browse" }),
            icon = null,
            onFilePick = {
                viewModel.setTSVFile(ConsoleType.PSVITA, PackageItemType.DLC, it)
            }
        )
        TSVPicker(
            title = "PSV Themes",
            value = Uri.decode(prefs.psvThemes.ifEmpty { "Browse" }),
            icon = null,
            onFilePick = {
                viewModel.setTSVFile(ConsoleType.PSVITA, PackageItemType.THEME, it)
            },
        )
        TSVPicker(
            title = "PS3 Games",
            value = Uri.decode(prefs.ps3Games).ifEmpty { "Browse" },
            icon = painterResource(id = R.drawable.ps3_icon),
            onFilePick = {
                viewModel.setTSVFile(ConsoleType.PS3, PackageItemType.GAME, it)
            }
        )
        if (!expanded) {
            DialogSetting(
                title = "Expand",
                value = "PS3 DLC, PSP Games, PSP DLC, PSX Games, PSM Games",
                icon = Icons.Outlined.ArrowDropDown,
                singleLine = true,
                onClick = { expanded = true },
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TSVPicker(
                    title = "PS3 DLC",
                    value = Uri.decode(prefs.ps3Dlc).ifEmpty { "Browse" },
                    icon = null,
                    onFilePick = {
                        viewModel.setTSVFile(ConsoleType.PS3, PackageItemType.DLC, it)
                    }
                )
                TSVPicker(
                    title = "PSP Games",
                    value = Uri.decode(prefs.pspGames).ifEmpty { "Browse" },
                    icon = painterResource(id = R.drawable.psp_icon),
                    onFilePick = {
                        viewModel.setTSVFile(ConsoleType.PSP, PackageItemType.GAME, it)
                    }
                )
                TSVPicker(
                    title = "PSP DLC",
                    value = Uri.decode(prefs.pspDlc).ifEmpty { "Browse" },
                    icon = null,
                    onFilePick = {
                        viewModel.setTSVFile(ConsoleType.PSP, PackageItemType.DLC, it)
                    }
                )
                TSVPicker(
                    title = "PSX Games",
                    value = Uri.decode(prefs.psxGames).ifEmpty { "Browse" },
                    icon = painterResource(id = R.drawable.psx_icon),
                    onFilePick = {
                        viewModel.setTSVFile(ConsoleType.PSX, PackageItemType.GAME, it)
                    }
                )
                TSVPicker(
                    title = "PSM Games",
                    value = Uri.decode(prefs.psmGames).ifEmpty { "Browse" },
                    icon = painterResource(id = R.drawable.psm_icon),
                    onFilePick = {
                        viewModel.setTSVFile(ConsoleType.PSM, PackageItemType.GAME, it)
                    }
                )
            }
        }
    }
}

@Composable
fun TSVPicker(
    title: String,
    value: String,
    icon: Painter? = null,
    onFilePick: (Uri) -> Unit,
) {
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { fileUri ->
        if (fileUri != null) {
            context.contentResolver.takePersistableUriPermission(
                fileUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onFilePick(fileUri)
        }
    }

    DialogSetting(
        title = title,
        value = value,
        icon = icon,
        onClick = {
            filePicker.launch(arrayOf("text/tab-separated-values"))
        }
    )}

@Composable
fun Updates(viewModel: SettingsViewModel, prefs: SettingsPreferences) {
    val clipboard = LocalClipboardManager.current

    var hmacOpenDialog by remember { mutableStateOf(false) }
    var dialogHmacKey by remember { mutableStateOf("") }

    SideEffect {
        dialogHmacKey = prefs.hmacKey
    }

    SettingGroup(title = "Updates") {
        DialogSetting(
            title = "HMAC Key",
            value = prefs.hmacKey.ifEmpty { "None" },
            icon = Icons.Outlined.Key,
            onClick = { hmacOpenDialog = true }
        )
    }
    if (hmacOpenDialog) {
        NPSAlertDialog(
            onDismiss = { hmacOpenDialog = false },
            title = "HMAC Key",
            buttons = {
                TextButton(onClick = { hmacOpenDialog = false }) {
                    Text(text = "Cancel")
                }
                TextButton(onClick = {
                    viewModel.setHMACKey(dialogHmacKey)
                    hmacOpenDialog = false
                }) {
                    Text(text = "OK")
                }
            }
        ) {
            OutlinedTextField(
                value = dialogHmacKey,
                onValueChange = { dialogHmacKey = it },
                label = { Text(text = "HMAC Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    NPSIconButton(
                        tooltip = "Paste",
                        onClick = { dialogHmacKey = clipboard.getText()?.text ?: "" }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentPaste,
                            contentDescription = "Paste",
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun Downloads(viewModel: SettingsViewModel, prefs: SettingsPreferences) {
    val downloadDirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { dirUri ->
        if (dirUri != null) {
            viewModel.setDownloadDir(dirUri.toString())
        }
    }

    val unpackDirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { dirUri ->
        if (dirUri != null) {
            viewModel.setUnpackDir(dirUri.toString())
        }
    }

    SettingGroup(title = "Downloads") {
        ToggleSetting(
            title = "Unpack in Download Directory",
            checked = prefs.unpackInDownload,
            onClick = {
                viewModel.setUnpackInDownload(it)
            }
        )
        ToggleSetting(
            title = "Delete package after unpacking",
            checked = prefs.deleteAfterUnpack,
            onClick = {
                viewModel.setDeleteAfterUnpack(it)
            }
        )
        DialogSetting(
            title = "Download Directory",
            value = Uri.decode(prefs.downloadDir).ifEmpty { "None" },
            icon = Icons.Outlined.FileDownload,
            onClick = {
                downloadDirPicker.launch(Uri.EMPTY)
            }
        )
        DialogSetting(
            title = "Unpack Directory",
            value = Uri.decode(prefs.unpackDir).ifEmpty { "None" },
            icon = Icons.Outlined.Unarchive,
            onClick = {
                unpackDirPicker.launch(Uri.EMPTY)
            }
        )
    }
}

@Composable
fun Pkg2Zip(viewModel: SettingsViewModel, prefs: SettingsPreferences) {
    var pkg2zipOpenDialog by remember { mutableStateOf(false) }
    var dialogPkg2zipParams by remember { mutableStateOf("") }

    SideEffect {
        dialogPkg2zipParams = prefs.pkg2zipParams
    }

    SettingGroup(title = "Pkg2Zip") {
        ToggleSetting(
            title = "Automatically decrypt downloaded content",
            checked = prefs.pkg2zipAutoDecrypt,
            onClick = {
                viewModel.setPkg2zipAutoDecrypt(it)
            }
        )
        DialogSetting(
            title = "Decryption params",
            value = prefs.pkg2zipParams.ifEmpty { "None" },
            icon = painterResource(
                id = R.drawable.lock_open
            ),
            onClick = { pkg2zipOpenDialog = true }
        )
    }
    if (pkg2zipOpenDialog) {
        NPSAlertDialog(
            onDismiss = { pkg2zipOpenDialog = false },
            title = "PKG2ZIP Args",
            buttons = {
                TextButton(onClick = { pkg2zipOpenDialog = false }) {
                    Text(text = "Cancel")
                }
                TextButton(onClick = {
                    viewModel.setPkg2zipParams(dialogPkg2zipParams)
                    pkg2zipOpenDialog = false
                }) {
                    Text(text = "OK")
                }
            }
        ) {
            OutlinedTextField(
                value = dialogPkg2zipParams,
                onValueChange = { dialogPkg2zipParams = it },
                label = { Text(text = "PKG2ZIP Args") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

@Composable
fun ThemeDialog(
    prefs: SettingsPreferences,
    onDismiss: () -> Unit,
    onThemeChange: (v: Theme) -> Unit
) {
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
                onClick = {
                    onThemeChange(Theme.LIGHT)
                },
                selected = prefs.appTheme == Theme.LIGHT.ordinal,
            )
            NPSRadioButton(
                text = "Dark",
                onClick = {
                    onThemeChange(Theme.DARK)
                },
                selected = prefs.appTheme == Theme.DARK.ordinal,
            )
            NPSRadioButton(
                text = "Use System Theme",
                onClick = {
                    onThemeChange(Theme.SYSTEM)
                },
                selected = prefs.appTheme == Theme.SYSTEM.ordinal,
            )
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

@Composable
fun Header(title: String) {
    Row(Modifier.padding(start = 56.dp)) {
        Text(text = title, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DialogSetting(title: String, value: String, icon: Painter?, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            if (icon != null) {
                Icon(painter = icon, contentDescription = null, modifier = Modifier.width(56.dp))
            } else {
                Box(modifier = Modifier.width(56.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, color = MaterialTheme.colorScheme.onBackground)
                Text(text = value, color = ColorSecondaryLight, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun DialogSetting(
    title: String,
    value: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    singleLine : Boolean = false
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.width(56.dp))
            } else {
                Box(modifier = Modifier.width(56.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = value,
                    color = ColorSecondaryLight,
                    fontSize = 14.sp,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    }
}

@Composable
fun ToggleSetting(title: String, icon: ImageVector? = null, checked: Boolean, onClick: (Boolean) -> Unit) {
    Box(
        modifier = Modifier.clickable onClick@{
            onClick(!checked)
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(top = 8.dp, end = 24.dp, bottom = 8.dp),
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, Modifier.width(56.dp))
            } else {
                Box(modifier = Modifier.width(56.dp))
            }
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1.0f))
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}
