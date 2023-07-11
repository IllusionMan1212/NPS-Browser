package com.illusionware.npsbrowser.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.data.ItemLayout
import com.illusionware.npsbrowser.data.SettingsPreferences
import com.illusionware.npsbrowser.model.ConsoleType
import com.illusionware.npsbrowser.model.PackageItem
import com.illusionware.npsbrowser.ui.components.*
import com.illusionware.npsbrowser.ui.pages.OnBoarding
import com.illusionware.npsbrowser.ui.pages.SettingsScreen
import com.illusionware.npsbrowser.ui.theme.ColorAccent
import com.illusionware.npsbrowser.ui.theme.ColorOnPrimaryLight
import com.illusionware.npsbrowser.ui.theme.NPSBrowserTheme
import com.illusionware.npsbrowser.ui.theme.Typography
import com.illusionware.npsbrowser.viewmodels.OnboardingViewModel
import com.illusionware.npsbrowser.viewmodels.PackageListViewModel
import com.illusionware.npsbrowser.viewmodels.SettingsViewModel
import kotlinx.coroutines.runBlocking

val PlaceholderColor = Color(0x1F888888)
val redHatDisplayFamily = FontFamily(
    Font(R.font.red_hat_display_bold, FontWeight.Bold),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory
            )
            val onboardingViewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModel.Factory
            )

            val onboardingPrefs = onboardingViewModel.uiState.collectAsStateWithLifecycle().value
            val appTheme = runBlocking { settingsViewModel.getAppTheme() }
            val settingsPrefs = settingsViewModel.uiState.collectAsStateWithLifecycle().value
            val navController = rememberNavController()

            val darkTheme = when (appTheme) {
                0 -> false
                1 -> true
                else -> isSystemInDarkTheme()
            }

            NPSBrowserTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (onboardingPrefs.seenOnboarding) MaterialTheme.colorScheme.background else ColorAccent
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if (onboardingPrefs.seenOnboarding) "Home" else "Onboarding",
                    ) {
                        composable(route = "Home") {
                            HomePage(
                                navigateToSettings = { navController.navigate("Settings") },
                                settingsPrefs = settingsPrefs,
                                settingsViewModel = settingsViewModel,
                            )
                        }
                        composable(route = "Settings") {
                            SettingsScreen(
                                navigationGoBack = { navController.popBackStack() },
                            )
                        }
                        composable(route = "Onboarding") {
                            OnBoarding(
                                navigateToHome = {
                                    navController.popBackStack()
                                    navController.navigate("Home")
                                },
                                darkTheme = darkTheme,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    navigateToSettings: () -> Unit = {},
    settingsPrefs: SettingsPreferences,
    settingsViewModel: SettingsViewModel,
) {
    var isSearchOpen by rememberSaveable { mutableStateOf(false)}
    var searchQuery by rememberSaveable { mutableStateOf("")}
    val searchBar = remember { FocusRequester() }
    var layoutDialogOpen by remember { mutableStateOf(false) }

    val tsvs = settingsViewModel.availableTsvs()
    var selectedBarItem by remember { mutableStateOf(ConsoleType.PSVITA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isSearchOpen) {
                        Text(text = stringResource(id = R.string.app_name))
                    }
                },
                actions = {
                    if (isSearchOpen) {
                        NPSIconButton(
                            tooltip = "Close Search",
                            onClick = { isSearchOpen = false }
                        ){
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Close Search")
                        }
                        Spacer(Modifier.width(12.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = {str -> searchQuery = str},
                            Modifier
                                .weight(1.0f)
                                .focusRequester(searchBar),
                            placeholder = {
                                Text(text = stringResource(id = R.string.search), style = Typography.bodyLarge)
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                        )
                        Spacer(Modifier.width(12.dp))
                        if (searchQuery.isNotEmpty()) {
                            NPSIconButton(
                                tooltip = "Clear Search",
                                onClick = { searchQuery = "" }
                            ){
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear Search")
                            }
                        }

                        LaunchedEffect(Unit) {
                            searchBar.requestFocus()
                        }
                    }
                    if (!isSearchOpen) {
                        NPSIconButton(
                            tooltip = stringResource(id = R.string.search_tooltip),
                            onClick = { isSearchOpen = true }
                        ){
                            Icon(imageVector = Icons.Filled.Search, contentDescription = stringResource(
                                id = R.string.search_tooltip
                            ))
                        }
                    }
                    NPSIconButton(
                        tooltip = stringResource(id = R.string.layout),
                        onClick = { layoutDialogOpen = true }
                    ) {
                        Icon(imageVector = if (settingsPrefs.layout == ItemLayout.GRID.ordinal) Icons.Filled.GridView else Icons.Filled.ViewList, contentDescription = stringResource(
                            id = R.string.layout
                        ))
                    }
                    NPSIconButton(
                        tooltip = stringResource(id = R.string.title_activity_settings),
                        onClick = { navigateToSettings() }
                    ){
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = stringResource(
                            id = R.string.title_activity_settings
                        ))
                    }
                }
            )
        },
        bottomBar = {
            if (tsvs.isNotEmpty()) {
                NavBar(
                    selectedTab = selectedBarItem,
                    onTabSelected = { selectedBarItem = it },
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (tsvs.isEmpty()) {
                NoTsvs(navigateToSettings = navigateToSettings)
            } else {
                PackageList(selectedBarItem, settingsPrefs = settingsPrefs)
            }
        }

        if (layoutDialogOpen) {
            LayoutDialog(
                onDismiss = { layoutDialogOpen = false },
                prefs = settingsPrefs,
                viewModel = settingsViewModel,
            )
        }
    }
}

@Composable
fun NavBar(selectedTab: ConsoleType, onTabSelected: (ConsoleType) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == ConsoleType.PSVITA,
            icon = { Icon(painterResource(id = R.drawable.vita_icon), contentDescription = "PS Vita") },
            label = { Text(text = "PS Vita") },
            onClick = {
                onTabSelected(ConsoleType.PSVITA)
            }
        )
        NavigationBarItem(
            selected = selectedTab == ConsoleType.PS3,
            icon = { Icon(painterResource(id = R.drawable.ps3_icon), contentDescription = "PS3") },
            label = { Text(text = "PS3") },
            onClick = {
                onTabSelected(ConsoleType.PS3)
            }
        )
        NavigationBarItem(
            selected = selectedTab == ConsoleType.PSP,
            icon = { Icon(painterResource(id = R.drawable.psp_icon), contentDescription = "PSP") },
            label = { Text(text = "PSP") },
            onClick = {
                onTabSelected(ConsoleType.PSP)
            }
        )
        NavigationBarItem(
            selected = selectedTab == ConsoleType.PSX,
            icon = { Icon(painterResource(id = R.drawable.psx_icon), contentDescription = "PSX") },
            label = { Text(text = "PSX") },
            onClick = {
                onTabSelected(ConsoleType.PSX)
            }
        )
        NavigationBarItem(
            selected = selectedTab == ConsoleType.PSM,
            icon = { Icon(painterResource(id = R.drawable.psm_icon), contentDescription = "PSM") },
            label = { Text(text = "PSM") },
            onClick = {
                onTabSelected(ConsoleType.PSM)
            }
        )
    }
}

@Composable
@Preview
fun NoTsvs(padding: PaddingValues = PaddingValues(0.dp), navigateToSettings: () -> Unit = {}) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ph_info),
            contentDescription = "Info",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(92.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "You haven't added any TSVs",
            color = MaterialTheme.colorScheme.outline,
            style = Typography.bodyLarge
        )
        Row {
            Text(
                text = "Add your TSV files from the ",
                color = MaterialTheme.colorScheme.outline,
                style = Typography.bodyLarge
            )
            Text(
                text = "settings",
                color = ColorAccent,
                style = Typography.bodyLarge,
                modifier = Modifier.clickable {
                    navigateToSettings()
                }
            )
        }
    }
}

@Composable
fun PackageList(
    type: ConsoleType = ConsoleType.PSVITA,
    viewModel: PackageListViewModel = viewModel(
        factory = PackageListViewModel.Factory
    ),
    settingsPrefs: SettingsPreferences,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val packages = when (type) {
        ConsoleType.PSVITA -> {
            uiState.packages.filter { it.consoleType == ConsoleType.PSVITA }
        }
        ConsoleType.PS3 -> {
            uiState.packages.filter { it.consoleType == ConsoleType.PS3 }
        }
        ConsoleType.PSP -> {
            uiState.packages.filter { it.consoleType == ConsoleType.PSP }
        }
        ConsoleType.PSX -> {
            uiState.packages.filter { it.consoleType == ConsoleType.PSX }
        }
        ConsoleType.PSM -> {
            uiState.packages.filter { it.consoleType == ConsoleType.PSM }
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (packages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "You haven't added any TSVs for this console yet",
                        style = Typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                if (settingsPrefs.layout == ItemLayout.LIST.ordinal) {
                    LazyColumn {
                        items(packages) {
                            PackageListItem(it)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(140.dp),
                        contentPadding = PaddingValues(8.dp),
                    ) {
                        items(packages) {
                            PackageGridItem(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackageListItem(item: PackageItem) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { /* TODO: */ }) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1.0f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("TODO:")
                        .crossfade(true)
                        .error(R.drawable.default_game_icon)
                        .build(),
                    placeholder = ColorPainter(PlaceholderColor),
                    contentDescription = "Package Icon",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.name,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = Typography.bodyLarge,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .weight(1.0f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ProvideTextStyle(value = Typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)) {
                            Text(text = item.region)
                            if (item.minFW.isNotEmpty()) {
                                Text(text = String.format("%.2f", item.minFW.toFloat()))
                            }
                            if (item.pkgSize.isNotEmpty()) {
                                Text(text = item.pkgSize)
                            }
                            Tag(title = item.dataType.name, small = true)
                        }
                    }
                }
            }
            Column {
                if (item.pkgUrl.isEmpty() || item.pkgUrl == "MISSING") {
                    Icon(
                        painter = painterResource(id = R.drawable.no_url_icon),
                        contentDescription = "No PKG URL",
                        tint = Color(0xFFCB5A5A)
                    )
                }
                if (item.consoleType != ConsoleType.PSX && (item.zRif.isEmpty() && item.rap.isEmpty())) {
                    Icon(
                        painter = painterResource(id = R.drawable.no_key_icon),
                        contentDescription = "No License Key",
                        tint = Color(0xFFCB5A5A)
                    )
                }
            }
        }
    }
}

@Composable
fun Tag(title: String, small: Boolean = false) {
    Box(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFEAF654))
            .layoutId("tag")
            .padding(horizontal = if (small) 6.dp else 8.dp, vertical = if (small) 2.dp else 4.dp)
    ) {
        Text(
            text = title.uppercase(),
            color = ColorOnPrimaryLight,
            style = Typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = redHatDisplayFamily,
                fontSize = if (small) 8.sp else 12.sp
            )
        )
    }
}

@Composable
fun PackageGridItem(item: PackageItem) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { /* TODO: */ }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ) {
            Layout(
                content = {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("TODO:")
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
                    Tag(title = item.dataType.name)
                }
            ) { measurables, constraints ->
                val imagePlaceable = measurables[0].measure(constraints)
                val tagPlaceable = measurables.first { it.layoutId == "tag" }.measure(constraints)

                layout(imagePlaceable.width, imagePlaceable.height) {
                    imagePlaceable.placeRelative(0, 0)
                    tagPlaceable.placeRelative(
                        x = imagePlaceable.width - tagPlaceable.width - 16,
                        y = 16,
                    )
                }
            }
            Text(
                text = item.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = Typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProvideTextStyle(value = Typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)) {
                    Text(text = item.region)
                    if (item.minFW.isNotEmpty()) {
                        Text(text = String.format("%.2f", item.minFW.toFloat()))
                    }
                    if (item.pkgSize.isNotEmpty()) {
                        Text(text = item.pkgSize)
                    }
                }
            }
            Row {
                if (item.pkgUrl.isEmpty() || item.pkgUrl == "MISSING") {
                    Icon(
                        painter = painterResource(id = R.drawable.no_url_icon),
                        contentDescription = "No PKG URL",
                        tint = Color(0xFFCB5A5A)
                    )
                }
                if (item.zRif.isEmpty() && item.rap.isEmpty()) {
                    Icon(
                        painter = painterResource(id = R.drawable.no_key_icon),
                        contentDescription = "No License Key",
                        tint = Color(0xFFCB5A5A)
                    )
                }
            }
        }
    }
}

@Composable
fun LayoutDialog(
    onDismiss: () -> Unit,
    prefs: SettingsPreferences,
    viewModel: SettingsViewModel
) {
    NPSAlertDialog(
        onDismiss = onDismiss,
        title = stringResource(id = R.string.layout),
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    ) {
        Column {
            NPSRadioButton(
                text = "List",
                selected = prefs.layout == ItemLayout.LIST.ordinal,
                onClick = { viewModel.setLayout(ItemLayout.LIST) },
            )
            NPSRadioButton(
                text = "Grid",
                selected = prefs.layout == ItemLayout.GRID.ordinal,
                onClick = { viewModel.setLayout(ItemLayout.GRID) },
            )
        }
    }
}
