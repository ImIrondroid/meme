package com.kakao.iron.ui.camera

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kakao.iron.data.local.room.SaveRepository
import com.kakao.iron.ui.base.BaseViewModel
import com.kakao.iron.util.coroutine.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CameraViewModel(
    private val saveRepository: SaveRepository,
    private val dispatchers: Dispatchers
) : BaseViewModel() {

    private var _fileList: MutableLiveData<MutableList<FileData>> = MutableLiveData(mutableListOf())
    var fileList: LiveData<MutableList<FileData>> = _fileList

    val stateWatcher: ObservableField<FileState> = ObservableField(FileState.Normal)

    fun setUpFiles(storagePath: String) {
        launch {
            val files = File(storagePath).listFiles() ?: arrayOf()
            val newFileList = mutableListOf<FileData>()
            withContext(dispatchers.io()) {
                files.filter {
                    it.name.contains("camera")
                }.forEach {
                    newFileList.add(
                        FileData(
                            filePath = storagePath + "/" + it.name,
                            _fileState = FileState.Normal
                        )
                    )
                }
                newFileList.sortByDescending { it.filePath }
            }

            _fileList.value = newFileList
        }
    }

    fun onOptionChanged(storagePath: String, state: FileState) {
        launch {
            stateWatcher.set(state)
            val files = File(storagePath).listFiles() ?: arrayOf()
            val newFileList = mutableListOf<FileData>()
            withContext(dispatchers.io()) {
                files.filter {
                    it.name.contains("camera")
                }.forEach {
                    newFileList.add(
                        FileData(
                            filePath = storagePath + "/" + it.name,
                            _fileState = FileState.Normal
                        )
                    )
                }
                newFileList.sortByDescending { it.filePath }
                newFileList.forEach {
                    it.fileState = state
                }
            }
            _fileList.value = newFileList
        }
    }

    fun onCancel() {
        stateWatcher.set(FileState.Normal)
        _fileList.value = _fileList.value?.toMutableList()?.apply {
            this.map {
                it.fileState = FileState.Normal
            }
        }
    }

    fun onDelete(position: Int) {
        launch {
            val filePath = _fileList.value?.get(position)?.filePath ?: "empty"
            deleteStorageData(filePath)
            _fileList.value = _fileList.value?.toMutableList()?.apply {
                val file = File(filePath)
                if(file.exists()) { file.delete() }
                removeAt(position)
            }
        }
    }

    private suspend fun deleteStorageData(filePath: String) {
        withContext(dispatchers.single()) {
            saveRepository.deleteStorageData(filePath = filePath)
        }
    }
}