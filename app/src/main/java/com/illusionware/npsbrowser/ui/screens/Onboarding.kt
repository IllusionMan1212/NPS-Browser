package com.illusionware.npsbrowser.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.ui.theme.ColorAccent
import com.illusionware.npsbrowser.ui.theme.Typography
import com.illusionware.npsbrowser.ui.theme.md_theme_dark_onSurface
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.illusionware.npsbrowser.data.SettingsPreferences
import com.illusionware.npsbrowser.model.ConsoleType
import com.illusionware.npsbrowser.model.PackageItemType
import com.illusionware.npsbrowser.ui.components.NPSIconButton
import com.illusionware.npsbrowser.viewmodels.OnboardingViewModel
import com.illusionware.npsbrowser.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

const val PAGE_COUNT = 3

val readexProFamily = FontFamily(
    Font(R.font.readex_pro_extra_light, FontWeight.ExtraLight),
    Font(R.font.readex_pro_light, FontWeight.Light),
    Font(R.font.readex_pro_regular, FontWeight.Normal),
    Font(R.font.readex_pro_medium, FontWeight.Medium),
    Font(R.font.readex_pro_semi_bold, FontWeight.SemiBold),
    Font(R.font.readex_pro_bold, FontWeight.Bold),
)

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun OnBoarding(
    navigateToHome: () -> Unit = {},
    darkTheme: Boolean = false,
    onboardingViewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory
    ),
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory
    ),
) {
    val systemUiController = rememberSystemUiController()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    )
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val settingsPrefs = settingsViewModel.uiState.collectAsStateWithLifecycle().value

    val darkIcons = when (pagerState.currentPage) {
        0 -> false
        else -> true
    }

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = darkIcons && !darkTheme
        )
        val activity = context as Activity
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = !darkTheme
            )
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    HorizontalPager(pageCount = PAGE_COUNT, state = pagerState) { page ->
        when (page) {
            0 -> OnBoardingInitial(
                incrementPage = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
            )
            1 -> OnBoardingSetupTSVs(
                page = page,
                incrementPage = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                skipToEnd = {
                    scope.launch { pagerState.animateScrollToPage(PAGE_COUNT - 1) }
                },
                settingsViewModel,
            )
//            2 -> OnBoardingStoragePermission(
//                page = page,
//                incrementPage = {
//                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
//                },
//                decrementPage = {
//                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
//                },
//                skipToEnd = {
//                    scope.launch { pagerState.animateScrollToPage(PAGE_COUNT - 1) }
//                },
//            )
            2 -> OnBoardingSetupSettings(
                page = page,
                incrementPage = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                decrementPage = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                },
                skipToEnd = {
                    scope.launch { pagerState.animateScrollToPage(PAGE_COUNT - 1) }
                },
                navigateToHome = navigateToHome,
                settingsViewModel,
                onboardingViewModel,
                settingsPrefs,
            )
        }
    }
}

@Composable
@Preview
fun OnBoardingInitial(incrementPage: () -> Unit = {}) {
    val density = LocalDensity.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = visible) {
        visible = true
    }

    Scaffold(containerColor = ColorAccent, contentColor = MaterialTheme.colorScheme.onBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically {
                        h -> h
                    } + fadeIn(
                        initialAlpha = 0.2f
                    ),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nps_fg_onboarding),
                        contentDescription = "App Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 64.dp)
                    )
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(tween(delayMillis = 100)) {
                        with(density) { 50.dp.roundToPx() }
                    } + fadeIn(
                        animationSpec = tween(delayMillis = 50),
                        initialAlpha = 0.0f,
                    ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ProvideTextStyle(value = Typography.displayMedium.copy(color = Color.White)) {
                                Text(text = "No Ads.")
                                Text(text = "No Waiting.")
                                Text(text = "No Bullshit.")
                            }
                        }
                        Text(
                            text = "Browse and download from a huge library of games, themes, and DLC to your heart's content.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            style = Typography.titleLarge.copy(color = md_theme_dark_onSurface),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(
                        animationSpec = tween(delayMillis = 200, durationMillis = 500),
                        initialAlpha = 0.0f,
                    ),
                ) {
                    TextButton(
                        onClick = incrementPage,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White, containerColor = Color(0xFF1A91EC)),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text(text = "Get Started")
                    }
                }
            }
        }
    }
}

@Composable
fun OnBoardingSetupTSVs(
    page: Int = 1,
    incrementPage: () -> Unit = {},
    skipToEnd: () -> Unit = {},
    settingsViewModel: SettingsViewModel,
) {
    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(vertical = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "Set up TSVs", style = Typography.displayMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "You can set up your TSV files on this screen",
                            style = Typography.titleMedium.copy(color = MaterialTheme.colorScheme.outline),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "You are expected to provide the TSV files yourself. NPS Browser will NOT provide you with these files",
                            style = Typography.titleSmall.copy(color = Color(0xFFD63A3A)),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .weight(1.0f, fill = false)
                        .padding(vertical = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        TSVGroup(title = "PS Vita") {
                            TSVButton(
                                "Games",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PSVITA, PackageItemType.GAME, it) }
                            )
                            TSVButton(
                                "Themes",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PSVITA, PackageItemType.THEME, it) }
                            )
                            TSVButton(
                                "DLC",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PSVITA, PackageItemType.DLC, it) }
                            )
                        }
                        TSVGroup(title = "PSP") {
                            TSVButton(
                                "Games",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PSP, PackageItemType.GAME, it) }
                            )
                            TSVButton(
                                "DLC",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PSP, PackageItemType.DLC, it) }
                            )
                            Box(modifier = Modifier.width(110.dp))
                        }
                        TSVGroup(title = "PS3") {
                            TSVButton(
                                "Games",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PS3, PackageItemType.GAME, it) }
                            )
                            TSVButton(
                                "DLC",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PS3, PackageItemType.DLC, it) }
                            )
                            Box(modifier = Modifier.width(110.dp))
                        }
                        TSVGroup(title = "PSX") {
                            TSVButton(
                                "Games",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PSX, PackageItemType.GAME, it) }
                            )
                        }
                        TSVGroup(title = "PSM") {
                            TSVButton(
                                "Games",
                                onFinish = { settingsViewModel.setTSVFile(ConsoleType.PSM, PackageItemType.GAME, it) }
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    OnBoardingBottomButtons(
                        page = page,
                        incrementPage = incrementPage,
                        skipToEnd = skipToEnd,
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnGroup(
    title: String,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            modifier = titleModifier,
            text = title,
            style = TextStyle.Default.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun TSVGroup(title: String, content: @Composable RowScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = TextStyle.Default.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            content = content
        )
    }
}

@Composable
fun TSVButton(title: String, onFinish: (Uri) -> Unit) {
    val context = LocalContext.current
    var hasSelected by rememberSaveable { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { fileUri ->
        if (fileUri != null) {
            context.contentResolver.takePersistableUriPermission(
                fileUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onFinish(fileUri)
            hasSelected = true
        }
    }
    FilledTonalButton(
        onClick = {
            filePicker.launch(arrayOf("text/tab-separated-values"))
        },
        modifier = Modifier
            .width(110.dp)
            .height(160.dp)
            .padding(vertical = 8.dp, horizontal = 0.dp)
            .graphicsLayer(clip = false),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = if (!hasSelected) MaterialTheme.colorScheme.secondaryContainer else Color(0x335EC03C)
        ),
    ) {
        Layout(
            modifier = Modifier.graphicsLayer(clip = false),
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.tsv_file),
                        contentDescription = "Add TSV",
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxWidth(),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(text = title, style = Typography.titleMedium)
                }
                Icon(
                    modifier = Modifier.layoutId("selected"),
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF5EC03C)
                )
            }
        ) { measurables, constraints ->
            val buttonPlaceable = measurables[0].measure(constraints)

            val iconPlaceable = measurables.first { it.layoutId == "selected" }.measure(constraints)

            layout(buttonPlaceable.width, buttonPlaceable.height) {
                if (hasSelected) {
                    iconPlaceable.placeRelative(
                        x = buttonPlaceable.width - iconPlaceable.width,
                        y = -8,
                    )
                }
                buttonPlaceable.placeRelative(0, 0)
            }
        }
    }
}

//@Composable
//fun OnBoardingStoragePermission(
//    page: Int = 2,
//    incrementPage: () -> Unit = {},
//    decrementPage: () -> Unit = {},
//    skipToEnd: () -> Unit = {},
//) {
//    val hasPermission = when (PackageManager.PERMISSION_GRANTED) {
//        ContextCompat.checkSelfPermission(
//            LocalContext.current,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) -> true
//        else -> false
//    }
//
//    val permissionLegacy = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission(),
//    ) {}
//
//    Scaffold { padding ->
//        Column(
//            Modifier
//                .padding(padding)
//                .padding(16.dp)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column(
//                Modifier
//                    .padding(vertical = 32.dp)
//                    .weight(1.0f)
//                    .fillMaxHeight(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.folder_bubbles),
//                    contentDescription = "Folder Image",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Text(
//                    text = "Storage".uppercase(),
//                    style = TextStyle.Default.copy(
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 28.sp,
//                        fontFamily = readexProFamily
//                    ),
//                )
//                Spacer(modifier = Modifier.height(24.dp))
//                Text(
//                    "Granting storage permission allows us to bypass newer Android limitations and choose any directory as your download directory",
//                    style = TextStyle.Default.copy(
//                        fontSize = 20.sp,
//                        fontFamily = readexProFamily
//                    ),
//                    textAlign = TextAlign.Center,
//                )
//                Spacer(modifier = Modifier
//                    .fillMaxHeight()
//                    .weight(1.0f))
//                Button(
//                    onClick = {
//                        permissionLegacy.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !hasPermission,
//                ) {
//                    Text(
//                        text = if (hasPermission) "Granted" else "Grant",
//                        style = TextStyle.Default.copy(
//                            fontSize = 18.sp,
//                            fontFamily = readexProFamily
//                        ),
//                    )
//                }
//            }
//            Column(Modifier.fillMaxWidth()) {
//                OnBoardingBottomButtons(
//                    page = page,
//                    incrementPage = incrementPage,
//                    decrementPage = decrementPage,
//                    skipToEnd = skipToEnd
//                )
//            }
//        }
//    }
//}

@Composable
fun OnBoardingSetupSettings(
    page: Int = 2,
    incrementPage: () -> Unit = {},
    decrementPage: () -> Unit = {},
    skipToEnd: () -> Unit = {},
    navigateToHome: () -> Unit = {},
    settingsViewModel: SettingsViewModel,
    onboardingViewModel: OnboardingViewModel,
    settingsPrefs: SettingsPreferences,
) {
    val clipboard = LocalClipboardManager.current

    var hmacKey by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

    val downloadDirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { dirUri ->
        if (dirUri != null) {
            settingsViewModel.setDownloadDir(dirUri.toString())
        }
    }

    val unpackDirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { dirUri ->
        if (dirUri != null) {
            settingsViewModel.setUnpackDir(dirUri.toString())
        }
    }

    fun setHmacKey(key: String) {
        if (key.isNotEmpty()) {
            settingsViewModel.setHMACKey(key)
        }
    }

    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(vertical = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Configure App", style = Typography.displayMedium)
                    Text(
                        text = "Set the app's settings to your liking and control how it behaves",
                        style = Typography.titleMedium.copy(color = MaterialTheme.colorScheme.outline),
                        textAlign = TextAlign.Center,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .weight(1.0f, fill = false)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    ColumnGroup(title = "HMAC Key", modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "The HMAC Key is used to decrypt the PS Vita game updates' urls. We cannot provide this key since it's the property of Sony",
                            style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
                        )
                        OutlinedTextField(
                            value = hmacKey,
                            onValueChange = {
                                hmacKey = it
                                setHmacKey(it.text)
                            },
                            label = { Text(text = "HMAC Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                NPSIconButton(
                                    tooltip = "Paste",
                                    onClick = {
                                        val text = clipboard.getText()?.text ?: ""
                                        hmacKey = TextFieldValue(text, selection = TextRange(text.length))
                                        setHmacKey(text)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentPaste,
                                        contentDescription = "Paste",
                                    )
                                }
                            }
                        )
                    }
                    ColumnGroup(title = "Downloads", titleModifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "Choose your desired directory to download and unpack packages",
                            style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
                        )
                        Column {
                            Toggleable(
                                title = "Unpack in download directory",
                                checked = settingsPrefs.unpackInDownload,
                                onCheckedChange = {
                                    settingsViewModel.setUnpackInDownload(it)
                                },
                            )
                            Toggleable(
                                title = "Delete package after unpacking",
                                checked = settingsPrefs.deleteAfterUnpack,
                                onCheckedChange = {
                                    settingsViewModel.setDeleteAfterUnpack(it)
                                },
                            )
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { downloadDirPicker.launch(Uri.EMPTY) }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Download Directory",
                                    )
                                    Text(
                                        text = Uri.decode(settingsPrefs.downloadDir).ifEmpty { "None" },
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { unpackDirPicker.launch(Uri.EMPTY) }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Unpack Directory",
                                    )
                                    Text(
                                        text = Uri.decode(settingsPrefs.unpackDir).ifEmpty { "None" },
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }
                        }
                    }
                    ColumnGroup(title = "PKG2ZIP", titleModifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "Modify the parameters and arguments of pkg2zip",
                            style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
                        )
                        Toggleable(
                            title = "Automatically decrypt downloaded content",
                            checked = settingsPrefs.pkg2zipAutoDecrypt,
                            onCheckedChange = {
                                settingsViewModel.setPkg2zipAutoDecrypt(it)
                            },
                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            value = settingsPrefs.pkg2zipParams,
                            onValueChange = {
                                settingsViewModel.setPkg2zipParams(it)
                            }, // TODO: validate args are legal ??
                            label = { Text(text = "Pkg2zip args") },
                            placeholder = { Text(text = "-x {pkgFile} \"{zRifKey}\"") },
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    OnBoardingBottomButtons(
                        page = page,
                        incrementPage = incrementPage,
                        decrementPage = decrementPage,
                        skipToEnd = skipToEnd,
                        navigateToHome = navigateToHome,
                        settingsViewModel = settingsViewModel,
                        onboardingViewModel = onboardingViewModel,
                    )
                }
            }
        }
    }
}

@Composable
fun Toggleable(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .clickable onCheckedChange@{
                onCheckedChange(!checked)
            }
            .padding(vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1.0f))
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}

@Composable
fun OnBoardingBottomButtons(
    page: Int,
    incrementPage: () -> Unit = {},
    decrementPage: () -> Unit = {},
    skipToEnd: () -> Unit = {},
    navigateToHome: () -> Unit = {},
    settingsViewModel: SettingsViewModel? = null,
    onboardingViewModel: OnboardingViewModel? = null
) {
    val finalPage = PAGE_COUNT - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f),
        horizontalArrangement = if (page != finalPage) Arrangement.SpaceBetween else Arrangement.End,
    ) {
        if (page != finalPage) {
            FilledTonalButton(
                onClick = skipToEnd,
                colors = ButtonDefaults.filledTonalButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 18.dp)
            ) {
                Text(text = "Skip")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (page != 1) {
                Button(onClick = decrementPage, contentPadding = PaddingValues(horizontal = 18.dp)) {
                    Text(text = "Prev")
                }
            }
            if (page == finalPage) {
                Button(
                    onClick = {
                        navigateToHome()
                        onboardingViewModel?.finishOnboarding(settingsViewModel!!.shouldCreateDefaultDir())
                    },
                    contentPadding = PaddingValues(horizontal = 18.dp)
                ) {
                    Text(text = "Finish")
                }
            } else {
                Button(
                    onClick = incrementPage,
                    contentPadding = PaddingValues(horizontal = 18.dp)
                ) {
                    Text(text = "Next")
                }
            }
        }
    }
}