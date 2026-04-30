package com.illusionware.npsbrowser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.illusionware.npsbrowser.data.download.NPSPackageDownload
import com.illusionware.npsbrowser.ui.components.NPSIconButton
import com.illusionware.npsbrowser.ui.theme.Typography
import com.illusionware.npsbrowser.util.system.prettyByte
import com.illusionware.npsbrowser.viewmodels.PackageDetailsViewModel

@Composable
fun Downloads(navigationGoBack: () -> Unit, packageDetailsViewModel: PackageDetailsViewModel) {
    val queue = packageDetailsViewModel.downloadManager.queueState.collectAsStateWithLifecycle().value
    packageDetailsViewModel.downloadManager.statusFlow().collectAsStateWithLifecycle(initialValue = null)
    val hasActiveDownloads = queue.any {
        it.status == NPSPackageDownload.State.DOWNLOADING ||
            it.status == NPSPackageDownload.State.QUEUE
    }
    val hasResumableDownloads = queue.any {
        it.status == NPSPackageDownload.State.PAUSED ||
            it.status == NPSPackageDownload.State.ERROR
    }

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            ) {
                NPSIconButton(tooltip = "Go Back", onClick = { navigationGoBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back" )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Downloads", style = Typography.titleLarge)
                    if (queue.isNotEmpty()) {
                        Badge(containerColor = MaterialTheme.colorScheme.outlineVariant) {
                            Text(
                                text = queue.size.toString(),
                                style = Typography.titleSmall,
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (hasActiveDownloads) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Pause All") },
                    icon = { Icon(imageVector = Icons.Filled.Pause, contentDescription = "Pause")},
                    onClick = {
                        packageDetailsViewModel.downloadManager.pauseDownloads()
                    }
                )
            } else if (hasResumableDownloads) {
               ExtendedFloatingActionButton(
                    text = { Text(text = "Resume") },
                    icon = { Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Resume")},
                    onClick = {
                        packageDetailsViewModel.downloadManager.startDownloads()
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            LazyColumn(Modifier.background(MaterialTheme.colorScheme.surface)) {
                itemsIndexed(
                    queue,
                    key = { _, item -> item.url },
                ) { _, item ->
                    DownloadItem(
                        item,
                        packageDetailsViewModel::removeDownload,
                        packageDetailsViewModel::pauseDownload,
                        packageDetailsViewModel::resumeDownload
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItem(
    item: NPSPackageDownload,
    removeDownload: (String) -> Unit,
    pauseDownload: (String) -> Unit,
    resumeDownload: (String) -> Unit,
) {
    val progress = item.progressFlow.collectAsStateWithLifecycle().value
    val bytesDownloaded = item.bytesDownloadedFlow.collectAsStateWithLifecycle().value
    val status = item.statusFlow.collectAsStateWithLifecycle().value

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .clickable {
                    if (status == NPSPackageDownload.State.DOWNLOADING || status == NPSPackageDownload.State.QUEUE) {
                        pauseDownload(item.url)
                    } else if (status != NPSPackageDownload.State.DOWNLOADED) {
                        resumeDownload(item.url)
                    }
                }
                .size(48.dp)
                .aspectRatio(1f)
                .semantics {
                    role = Role.Button
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (status) {
                    NPSPackageDownload.State.DOWNLOADING, NPSPackageDownload.State.QUEUE -> Icons.Filled.Pause
                    NPSPackageDownload.State.DOWNLOADED -> Icons.Filled.DownloadDone
                    NPSPackageDownload.State.ERROR -> Icons.Filled.RestartAlt
                    else -> Icons.Filled.PlayArrow
                },
                contentDescription = when (status) {
                    NPSPackageDownload.State.DOWNLOADING, NPSPackageDownload.State.QUEUE -> "Pause"
                    NPSPackageDownload.State.DOWNLOADED -> "Downloaded"
                    NPSPackageDownload.State.ERROR -> "Retry"
                    else -> "Resume"
                },
                modifier = Modifier.size(32.dp)
            )
        }
        Column(
            modifier = Modifier
                .padding(end = 4.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.name,
                    style = Typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "${bytesDownloaded.prettyByte()} / ${item.size.prettyByte()}", style = Typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            when (status) {
                NPSPackageDownload.State.DOWNLOADING -> {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.height(2.dp),
                        strokeCap = StrokeCap.Round,
                    )
                }
                NPSPackageDownload.State.QUEUE -> {
                    Text("Pending", style = Typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                NPSPackageDownload.State.ERROR -> {
                    Text("Failed", style = Typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                NPSPackageDownload.State.DOWNLOADED -> {
                    Text("Downloaded", style = Typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                else -> {
                    Text("Paused", style = Typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .clickable { removeDownload(item.url) }
                .size(48.dp)
                .aspectRatio(1f)
                .semantics {
                    role = Role.Button
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = "Remove",
            )
        }
    }
}

@Composable
@Preview
fun DownloadPreview() {
    val packageDetailsViewModel: PackageDetailsViewModel = viewModel(
        factory = PackageDetailsViewModel.Factory
    )

    Downloads({}, packageDetailsViewModel)
}

@Composable
@Preview(showBackground = true)
fun DownloadItemPreview() {
    DownloadItem(
        item = NPSPackageDownload(
            name = "Test",
            url = "https://google.com",
            size = 385273850L,
            createdAt = 0L,
            sha256 = "",
            titleId = "BRUH00001",
            bytesDownloaded = 0L
        ),
        removeDownload = {},
        pauseDownload = {},
        resumeDownload = {},
    )
}