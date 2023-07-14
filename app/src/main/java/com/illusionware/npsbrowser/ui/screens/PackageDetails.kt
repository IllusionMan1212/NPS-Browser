package com.illusionware.npsbrowser.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.activities.PlaceholderColor
import com.illusionware.npsbrowser.data.ItemLayout
import com.illusionware.npsbrowser.data.SettingsPreferences
import com.illusionware.npsbrowser.data.Theme
import com.illusionware.npsbrowser.ui.components.NPSAlertDialog
import com.illusionware.npsbrowser.ui.components.NPSFloatingActionButton
import com.illusionware.npsbrowser.ui.components.NPSIconButton
import com.illusionware.npsbrowser.ui.theme.ColorSecondaryLight
import com.illusionware.npsbrowser.ui.theme.Typography
import com.illusionware.npsbrowser.viewmodels.PackageDetailsViewModel
import com.illusionware.npsbrowser.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageDetails(
    navigationGoBack: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory
    ),
    packageDetailsViewModel: PackageDetailsViewModel,
) {
    val clipboard = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val prefs = settingsViewModel.uiState.collectAsStateWithLifecycle().value

    val item = packageDetailsViewModel.uiState.collectAsStateWithLifecycle().value.item!!
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState)},
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            ) {
                NPSIconButton(tooltip = "Go Back", onClick = navigationGoBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back" )
                }
            }
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NPSFloatingActionButton(
                    onClick = {
                        if (!item.zRif.isNullOrEmpty()) {
                            clipboard.setText(AnnotatedString(item.zRif))
                        } else if (!item.rap.isNullOrEmpty()) {
                            clipboard.setText(AnnotatedString(item.rap))
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar("License key copied to clipboard")
                        }
                    },
                    enabled = !item.zRif.isNullOrEmpty() || !item.rap.isNullOrEmpty()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.key_copy_fill),
                        contentDescription = "Copy License Key"
                    )
                }
                NPSFloatingActionButton(
                    onClick = { /* TODO: */ },
                    onLongClick = { showBottomSheet = true },
                    enabled = !item.pkgUrl.isNullOrEmpty()
                ) {
                    Icon(imageVector = Icons.Filled.Download, contentDescription = "Download")
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("TODO")
                    .crossfade(true)
                    .error(R.drawable.default_game_icon)
                    .build(),
                placeholder = ColorPainter(PlaceholderColor),
                contentDescription = "Package Icon",
                modifier = Modifier
                    .aspectRatio(1f / 1f)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "[${item.titleId}] ${item.name}",
                    style = Typography.headlineSmall.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.Light,
                )
                if (!item.contentId.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = item.contentId,
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (item.region.isNotEmpty()) {
                        Info(
                            label = item.region,
                            iconDescription = "Region",
                            icon = Icons.Filled.Public
                        )
                    }
                    if (item.pkgSize.isNotEmpty()) {
                        Info(
                            label = item.pkgSize,
                            iconDescription = "Package",
                            icon = painterResource(id = R.drawable.package_ic),
                        )
                    }
                    if (!item.minFW.isNullOrEmpty()) {
                        Info(
                            label = item.minFW,
                            iconDescription = "Minimum Firmware",
                            icon = painterResource(id = R.drawable.gear),
                        )
                    }
                }
            }
        }
    }
    if (showBottomSheet) {
        OneTimeConfigBottomSheet(
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                }
            },
            sheetState = sheetState,
            prefs = prefs,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneTimeConfigBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    prefs: SettingsPreferences
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(top = 0.dp),
    ) {
        OneTimeConfigSheetContent(prefs)
    }
}

@Composable
fun Info(label: String, iconDescription: String, icon: Painter) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = iconDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            style = Typography.bodyMedium,
        )
    }
}

@Composable
fun Info(label: String, iconDescription: String, icon: ImageVector) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            style = Typography.bodyMedium,
        )
    }
}

@Composable
fun Option(
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, color = MaterialTheme.colorScheme.onBackground)
                Text(text = value, color = ColorSecondaryLight, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneTimeConfigSheetContent(prefs: SettingsPreferences) {
    var autoDecrypt by remember { mutableStateOf(prefs.pkg2zipAutoDecrypt) }
    var unpackInPlace by remember { mutableStateOf(prefs.unpackInDownload) }
    var deleteAfter by remember { mutableStateOf(prefs.deleteAfterUnpack) }
    var downloadDir by remember { mutableStateOf(Uri.decode(prefs.downloadDir)) }
    var decParamsDialog by remember { mutableStateOf(false) }
    var unpackDir by remember { mutableStateOf(Uri.decode(prefs.unpackDir)) }
    var pkg2zipParams by remember { mutableStateOf(prefs.pkg2zipParams) }
    var displayPkg2zipParams by remember { mutableStateOf(prefs.pkg2zipParams) }

    val downloadDirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { dirUri ->
        if (dirUri != null) {
            downloadDir = dirUri.toString()
        }
    }

    val unpackDirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { dirUri ->
        if (dirUri != null) {
            unpackDir = dirUri.toString()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Text(
            text = "One-time Configuration",
            textAlign = TextAlign.Center,
            style = Typography.titleLarge
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            FilterChip(
                label = { Text(text = "Decrypt automatically") },
                selected = autoDecrypt,
                onClick = { autoDecrypt = !autoDecrypt }
            )
            FilterChip(
                label = { Text(text = "Unpack in place") },
                selected = unpackInPlace,
                onClick = { unpackInPlace = !unpackInPlace },
            )
            FilterChip(
                label = { Text(text = "Delete after") },
                selected = deleteAfter,
                onClick = { deleteAfter = !deleteAfter },
            )
        }
        Column(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Column {
                Option(
                    title = "Download Directory",
                    value = Uri.decode(downloadDir).ifEmpty { "None" },
                    onClick = {
                        downloadDirPicker.launch(Uri.EMPTY)
                    }
                )
                AnimatedVisibility(
                    visible = !unpackInPlace
                ) {
                    Option(
                        title = "Unpack Directory",
                        value = Uri.decode(unpackDir).ifEmpty { "None" },
                        onClick = {
                            unpackDirPicker.launch(Uri.EMPTY)
                        }
                    )
                }
                AnimatedVisibility(
                    visible = autoDecrypt
                ) {
                    Option(
                        title = "Decryption Parameters",
                        value = pkg2zipParams,
                        onClick = { decParamsDialog = true }
                    )
                }
            }
            Button(
                onClick = { /*TODO: do the download*/ },
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.DownloadDone,
                    contentDescription = "Download",
                    Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Download")
            }
        }
    }
    if (decParamsDialog) {
        NPSAlertDialog(
            onDismiss = {
                decParamsDialog = false
                displayPkg2zipParams = pkg2zipParams
            },
            title = "Decryption Parameters",
            buttons = {
                TextButton(onClick = {
                    pkg2zipParams = displayPkg2zipParams
                    decParamsDialog = false
                }) {
                    Text(text = "OK")
                }
            }
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = displayPkg2zipParams,
                label = { Text(text = "Decryption Parameters") },
                onValueChange = {
                    displayPkg2zipParams = it
                },
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
fun BottomSheetPreview() {
    val prefs = SettingsPreferences(
        Theme.SYSTEM.ordinal,
        ItemLayout.LIST.ordinal,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        false,
        false,
        "",
        true,
    )

    Column {
        OneTimeConfigSheetContent(prefs = prefs)
    }
}
