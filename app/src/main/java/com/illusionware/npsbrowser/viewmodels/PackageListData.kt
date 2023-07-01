package com.illusionware.npsbrowser.viewmodels

import androidx.lifecycle.ViewModel
import com.github.doyaaaaaken.kotlincsv.dsl.context.ExcessFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.illusionware.npsbrowser.ConsoleType
import com.illusionware.npsbrowser.DataType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Package(
    val id: String,
    val title: String,
    val region: String,
    val minFW: String,
    val pkgUrl: String,
    val pkgSize: Int,
    val consoleType: ConsoleType,
    val dataType: DataType,
)

data class PackageListUiState(
    val packages: List<Package> = emptyList(),
    val error: String = "",
)

class PackageListViewModel(): ViewModel() {
    // TODO: actually properly think about and implement this
    private val _uiState = MutableStateFlow(PackageListUiState())
    val uiState: StateFlow<PackageListUiState> = _uiState.asStateFlow()
    private val tsvReader = csvReader {
        charset = "ISO_8859_1"
        quoteChar = '"'
        delimiter = '\t'
        excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM
        insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING
    }

    fun loadPackagesFromTsv() {
        val file = "psv_games.tsv"
        val rows = tsvReader.readAll(file)
    }
}
