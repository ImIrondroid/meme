package com.kakao.iron.ui.search

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SearchData(
    @SerializedName("collection")
    @Expose
    val collection: String = "",
    @SerializedName("thumbnail_url")
    @Expose
    var thumbnailUrl: String = "default",
    @SerializedName("image_url")
    @Expose
    val imageUrl: String = "default",
    @SerializedName("width")
    @Expose
    val imageWidth: Int = 0,
    @SerializedName("height")
    @Expose
    val imageHeight: Int = 0,
    @SerializedName("display_sitename")
    @Expose
    val displaySiteName: String = "",
    @SerializedName("doc_url")
    @Expose
    val documentUrl: String = "default",
    @SerializedName("datetime")
    @Expose
    var date: String = ""
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.apply {
            writeString(collection)
            writeString(thumbnailUrl)
            writeString(imageUrl)
            writeInt(imageWidth)
            writeInt(imageHeight)
            writeString(displaySiteName)
            writeString(documentUrl)
            writeString(date)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchData> {
        override fun createFromParcel(parcel: Parcel): SearchData {
            return SearchData(parcel)
        }

        override fun newArray(size: Int): Array<SearchData?> {
            return arrayOfNulls(size)
        }
    }
}