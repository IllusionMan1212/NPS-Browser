package com.illusionware.npsbrowser.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.doyaaaaaken.kotlincsv.dsl.context.ExcessFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.illusionware.npsbrowser.NPSApp
import com.illusionware.npsbrowser.data.SettingsPreferencesRepository
import com.illusionware.npsbrowser.model.ConsoleType
import com.illusionware.npsbrowser.model.PackageItem
import com.illusionware.npsbrowser.model.PackageItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.toLongOrDefault
import java.io.InputStream

data class PackageListUiState(
    val packages: ArrayList<PackageItem> = ArrayList(),
    val error: String = "",
    val isLoading: Boolean = true,
)

class PackageListViewModel(app: NPSApp, settingsRepo: SettingsPreferencesRepository): ViewModel() {
    private val _uiState = MutableStateFlow(PackageListUiState())
    val uiState: StateFlow<PackageListUiState> = _uiState.asStateFlow()

    private val tsvReader = csvReader {
        charset = "UTF-8"
        quoteChar = '"'
        delimiter = '\t'
        excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM
        insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val list = ArrayList<PackageItem>()

            settingsRepo.availableTsvs().forEach {
                list += parsePackageFromTSV(it, app)
            }

            list.sortBy { it.name }

            _uiState.emit(PackageListUiState(isLoading = false, packages = list))

            settingsRepo.tsvFiles().collect { tsv ->
                _uiState.emit(PackageListUiState())

                val alreadyExists = list.find { item ->
                    item.consoleType == tsv.second && item.dataType == tsv.third
                }

                if (alreadyExists != null) {
                    list.removeAll { item ->
                        item.consoleType == tsv.second && item.dataType == tsv.third
                    }
                }

                list.addAll(parsePackageFromTSV(tsv, app))
                list.sortBy { it.name }

                _uiState.emit(PackageListUiState(isLoading = false, packages = list))
            }
        }
    }

    private fun parsePackageFromTSV(
        it: Triple<String, ConsoleType, PackageItemType>, app: NPSApp
    ): ArrayList<PackageItem> {
        val list = ArrayList<PackageItem>()

        val fileUri = it.first
        val console = it.second
        val type = it.third

        val file: InputStream?
        try {
            file = app.contentResolver.openInputStream(Uri.parse(fileUri))
        } catch (e: Exception) {
            throw e
        }
        val tsv = tsvReader.readAllWithHeader(file!!)

        tsv.forEach { row ->
            if (row["Name"].isNullOrEmpty()) {
                return@forEach
            }
            var size = ""
            var minFW: String? = null
            var pkgUrl: String? = null
            var zRif: String? = null
            var rap: String? = null
            var contentId: String? = null
            var sha256: String? = null
            var modificationDate: String? = null

            val tempSize = row["File Size"]
            val tempFW = row["Required FW"]
            val tempUrl = row["PKG direct link"]
            val tempZRif = row["zRIF"]
            val tempRap = row["RAP"]
            val tempContentId = row["Content ID"]
            val tempsha256 = row["SHA256"]
            val tempDate = row["Last Modification Date"]
            if (!tempSize.isNullOrEmpty()) {
                size = bytesIntoHumanReadable(tempSize.toLongOrDefault(0))
            }
            if (!tempFW.isNullOrEmpty()) {
                minFW = String.format("%.2f", tempFW.toFloat())
            }
            if (!tempUrl.isNullOrEmpty() && tempUrl != "MISSING") {
                pkgUrl = tempUrl
            }
            if (!tempZRif.isNullOrEmpty() && tempZRif != "MISSING") {
                zRif = tempZRif
            }
            if (!tempRap.isNullOrEmpty() && tempRap != "MISSING") {
                rap = tempRap
            }
            if (!tempContentId.isNullOrEmpty()) {
                contentId = tempContentId
            }
            if (!tempsha256.isNullOrEmpty()) {
                sha256 = tempsha256
            }
            if (!tempDate.isNullOrEmpty()) {
                modificationDate = tempDate
            }

            list.add(
                PackageItem(
                    row["Title ID"] ?: "",
                    row["Region"] ?: "",
                    row["Name"]?.trim() ?: "",
                    pkgUrl,
                    contentId,
                    modificationDate,
                    size,
                    sha256,
                    zRif,
                    rap,
                    minFW,
                    console,
                    type,
                )
            )
        }

        file.close()

        return list
    }

    private fun bytesIntoHumanReadable(bytes: Long): String {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024

        return if (bytes in 0 until kilobyte) {
            "$bytes" + "B"
        } else if (bytes in kilobyte until megabyte) {
            String.format("%.2f", (bytes.toDouble() / kilobyte.toDouble())) + "KB"
        } else if (bytes in megabyte until gigabyte) {
            String.format("%.2f", (bytes.toDouble() / megabyte.toDouble())) + "MB"
        } else if (bytes in gigabyte until terabyte) {
            String.format("%.2f", (bytes.toDouble() / gigabyte.toDouble())) + "GB"
        } else if (bytes >= terabyte) {
            String.format("%.2f", (bytes.toDouble() / terabyte.toDouble())) + "TB"
        } else {
            "$bytes" + "B"
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NPSApp)
                PackageListViewModel(application, application.settingsPreferencesRepository)
            }
        }
    }
}
