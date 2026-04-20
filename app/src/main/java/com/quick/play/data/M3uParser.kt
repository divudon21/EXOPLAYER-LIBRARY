package com.quick.play.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object M3uParser {
    suspend fun parse(url: String, playlistUserAgent: String = ""): List<Channel> {
        return withContext(Dispatchers.IO) {
            val channels = mutableListOf<Channel>()
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                val defaultUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                val uaToUse = if (playlistUserAgent.isNotEmpty()) playlistUserAgent else defaultUA
                connection.setRequestProperty("User-Agent", uaToUse)
                
                // Handle redirects if needed
                var redirect = false
                var status = connection.responseCode
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                        redirect = true
                    }
                }
                
                val finalConnection = if (redirect) {
                    val newUrl = connection.getHeaderField("Location")
                    val newConn = URL(newUrl).openConnection() as HttpURLConnection
                    newConn.setRequestProperty("User-Agent", uaToUse)
                    newConn
                } else {
                    connection
                }
                
                val reader = BufferedReader(InputStreamReader(finalConnection.inputStream))
                var line: String?
                var currentName = ""
                var currentLogo = ""
                var currentGroup = ""
                var currentLicenseType = ""
                var currentLicenseKey = ""
                var currentUserAgent = ""
                var currentCookie = ""
                
                while (reader.readLine().also { line = it } != null) {
                    val l = line?.trim() ?: ""
                    if (l.startsWith("#KODIPROP:inputstream.adaptive.license_type=")) {
                        currentLicenseType = l.substringAfter("=").trim()
                    } else if (l.startsWith("#KODIPROP:inputstream.adaptive.license_key=")) {
                        currentLicenseKey = l.substringAfter("=").trim()
                    } else if (l.startsWith("#EXTVLCOPT:http-user-agent=")) {
                        currentUserAgent = l.substringAfter("=").trim()
                    } else if (l.startsWith("#EXTVLCOPT:http-cookie=")) {
                        currentCookie = l.substringAfter("=").trim()
                    } else if (l.startsWith("#EXTHTTP:")) {
                        try {
                            val jsonStr = l.substringAfter(":")
                            if (jsonStr.contains("\"cookie\"")) {
                                val cookieMatch = "\"cookie\"\\s*:\\s*\"([^\"]+)\"".toRegex(RegexOption.IGNORE_CASE).find(jsonStr)
                                cookieMatch?.let { currentCookie = it.groupValues[1] }
                            }
                            if (jsonStr.contains("\"user-agent\"")) {
                                val uaMatch = "\"user-agent\"\\s*:\\s*\"([^\"]+)\"".toRegex(RegexOption.IGNORE_CASE).find(jsonStr)
                                uaMatch?.let { currentUserAgent = it.groupValues[1] }
                            }
                        } catch (e: Exception) {}
                    } else if (l.startsWith("#EXTINF:")) {
                        // Parse logo
                        val logoRegex = "tvg-logo=\"([^\"]+)\"".toRegex()
                        val logoMatch = logoRegex.find(l)
                        currentLogo = logoMatch?.groupValues?.get(1) ?: ""
                        
                        // Parse group
                        val groupRegex = "group-title=\"([^\"]+)\"".toRegex()
                        val groupMatch = groupRegex.find(l)
                        currentGroup = groupMatch?.groupValues?.get(1) ?: ""
                        
                        // Parse name
                        val nameParts = l.split(",")
                        if (nameParts.size > 1) {
                            currentName = nameParts.last().trim()
                        } else {
                            currentName = "Unknown Channel"
                        }
                    } else if (l.isNotEmpty() && !l.startsWith("#")) {
                        var streamUrl = l
                        var urlUserAgent = currentUserAgent
                        var urlCookie = currentCookie
                        
                        val delimiter = if (streamUrl.contains("|")) "|" else if (streamUrl.contains("%7C", ignoreCase = true)) "%7C" else null
                        
                        if (delimiter != null) {
                            val parts = streamUrl.split(Regex(delimiter, RegexOption.IGNORE_CASE), limit = 2)
                            streamUrl = parts[0]
                            if (parts.size > 1) {
                                val headerString = parts[1]
                                val headerParts = headerString.split("&")
                                for (hp in headerParts) {
                                    val kv = hp.split("=", limit = 2)
                                    if (kv.size == 2) {
                                        if (kv[0].equals("User-Agent", ignoreCase = true)) {
                                            urlUserAgent = kv[1]
                                        } else if (kv[0].equals("cookie", ignoreCase = true)) {
                                            urlCookie = kv[1]
                                        }
                                    }
                                }
                            }
                        }
                        
                        channels.add(Channel(
                            name = currentName, 
                            logo = currentLogo, 
                            group = currentGroup, 
                            url = streamUrl,
                            licenseType = currentLicenseType,
                            licenseKey = currentLicenseKey,
                            userAgent = urlUserAgent,
                            cookie = urlCookie
                        ))
                        
                        currentName = ""
                        currentLogo = ""
                        currentGroup = ""
                        currentLicenseType = ""
                        currentLicenseKey = ""
                        currentUserAgent = ""
                        currentCookie = ""
                    }
                }
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            channels
        }
    }
}