package tw.moe.r20

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import java.awt.image.BufferedImage
import java.io.*


class NhentaiProvider {
    /*
    正常的連結 https://nhentai.net/g/175395/
    搜尋用的 https://nhentai.net/api/galleries/search?query=language:chinese&page=1&sort=popular
    車牌換資料 https://nhentai.net/api/gallery/175395
    車牌換資料 https://nhentai.net/api/gallery/175395
    縮圖 https://t.nhentai.net/galleries/981518/thumb.jpg
    小圖 https://t.nhentai.net/galleries/981518/1t.jpg
    感覺沒必要 先跳過
    大圖 https://i.nhentai.net/galleries/981518/1.jpg
    */
    private val searchPrefix: String = "https://nhentai.net/api/galleries/search?query="
    private val infoPrefix: String = "https://nhentai.net/api/gallery/"
    private val thumbPrefix = "https://t.nhentai.net/galleries/"
    private val imagePrefix: String = "https://i.nhentai.net/galleries/"

    private val client = OkHttpClient()

    //Headers
    private val headerName = "User-Agent"
    private val headerValue = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:82.0) Gecko/20100101 Firefox/82.0"

    fun search(keyword: String, page: Int, sortedPopular: Boolean = true): JsonObject {
        val searchUrl: String = if (sortedPopular) {
            "$searchPrefix$keyword&page=$page&sort=popular"
        } else {
            "$searchPrefix$keyword&page=$page"
        }

        val request: Request = Request.Builder()
            .url(searchUrl)
            .addHeader(headerName,headerValue)
            .build()

        client.newCall(request).execute().use { response ->
            return JsonParser.parseString(response.body!!.string()).asJsonObject
        }
    }

    fun getInfo(ID: String): JsonObject {
        val request: Request = Request.Builder()
            .url("$infoPrefix$ID")
            .addHeader(headerName,headerValue)
            .build()

        client.newCall(request).execute().use { response ->
            return JsonParser.parseString(response.body!!.string()).asJsonObject
        }
    }

    fun getThumb(media_id: String, type: Char): InputStream {
        val request: Request
        if (type == 'j') {
            request = Request.Builder()
                    .url("$thumbPrefix$media_id/thumb.jpg")
                    .addHeader(headerName,headerValue)
                    .build()
            return client.newCall(request).execute().body!!.byteStream()
        }else if (type == 'p') {
            request = Request.Builder()
                    .url("$thumbPrefix$media_id/thumb.png")
                    .addHeader(headerName,headerValue)
                    .build()
            return client.newCall(request).execute().body!!.byteStream()
        }
        return InputStream.nullInputStream()
    }

    fun getImage(media_id: String, img: Int, type: Char): InputStream {
        if (type == 'p') {
            val request: Request = Request.Builder()
                    .url("$imagePrefix$media_id/$img.png")
                    .addHeader(headerName,headerValue)
                    .build()
            return client.newCall(request).execute().body!!.byteStream()
        }else if (type == 'j') {
            val request: Request = Request.Builder()
                    .url("$imagePrefix$media_id/$img.jpg")
                    .addHeader(headerName,headerValue)
                    .build()
            return client.newCall(request).execute().body!!.byteStream()
        }
        return InputStream.nullInputStream()
    }
}

/*
fun main() {
    println(NhentaiProvider().search("language:chinese",1,true))
    println(NhentaiProvider().getInfo("175395"))
    File("owo.jpg").writeBytes(NhentaiProvider().getThumb("981518").readAllBytes())
    File("OmO.jpg").writeBytes(NhentaiProvider().getImage("981518", 1).readAllBytes())
}
*/
