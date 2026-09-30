package com.evrenhouse.trackscooter.data

import com.evrenhouse.trackscooter.data.GitHubReleaseResponse
import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * Sumber cadangan cek update: repo rilis khusus (sanixmon/track-releases).
 * Dipakai bila endpoint utama /api/app-version gagal/kedaluwarsa, agar
 * langkah manual version.json tidak lagi jadi titik tunggal kegagalan.
 * Tanpa minVersionCode/forceUpdate/changelog — hanya nama versi + APK.
 */
interface GitHubApiService {

    @Headers("Accept: application/vnd.github+json")
    @GET("repos/sanixmon/track-releases/releases/latest")
    suspend fun getLatestRelease(): GitHubReleaseResponse
}
