package tw.moe.r20

import com.google.gson.JsonElement
import java.awt.*
import java.awt.List
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

class MainFrame: Frame() {
    private val imgShower = ImgShower("", "j")
    private val comicList = List()
    private var savePath = ".\\Downloads\\"
    private var page = 1
    private var searchBuff = NhentaiProvider().search("language:chinese", page).get("result").asJsonArray
    private val language = "english"

    init {
        createAndShowGUI()
        refreshComicList()
        checkDict(savePath)
        this.isVisible = true
    }

    private fun createAndShowGUI() {
        this.title = "nHentai Downloader (Mid-term assignment)"
        this.layout = BorderLayout()
        this.setSize(852, 480)
        this.iconImage = Toolkit.getDefaultToolkit().getImage("resources/icon.ico")
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                super.windowClosing(e)
                this@MainFrame.dispose()
            }
        })

        createMenuBar()
        buttonContainer()
        createMainContainer()
    }

    private fun createMenuBar() {
        this.menuBar = MenuBar()

        val properties = Menu("Properties")
        this.menuBar.add(properties)

        val view = Menu("View")
        this.menuBar.add(view)

        val help = Menu("Help")
        this.menuBar.add(help)

        val saveTo = MenuItem("Export to...")
        properties.add(saveTo)
        saveTo.addActionListener {
            val dialog = Dialog(this)
            dialog.title = "Set export path"
            dialog.setSize(200, 100)
            dialog.layout = GridLayout(2, 1)
            dialog.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    super.windowClosing(e)
                    dialog.dispose()
                }
            })
            val textField = TextField()
            dialog.add(textField)
            val button = Button("Ok")
            dialog.add(button)
            button.addActionListener {
                this.savePath = textField.text
                dialog.dispose()
            }

            dialog.isVisible = true
        }

        val exit = MenuItem("Exit")
        properties.add(exit)
        exit.addActionListener { this.dispose() }

        val search = MenuItem("Download by license plate.")
        view.add(search)
        search.addActionListener {
            val dialog = Dialog(this)
            dialog.title = "Download by license plate."
            dialog.setSize(400, 150)
            dialog.layout = GridLayout(2, 1)
            dialog.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    super.windowClosing(e)
                    dialog.dispose()
                }
            })

            val textField = TextField()
            dialog.add(textField)
            val button = Button("Ok")
            dialog.add(button)
            button.addActionListener {
                val info = NhentaiProvider().getInfo(textField.text)
                val resultId = info.asJsonObject.get("id").asString + "\\"
                val mediaId = info.asJsonObject.get("media_id").asString
                val mediaLength = info.asJsonObject.get("num_pages").asInt

                downloader(info, resultId, mediaId, mediaLength)
                dialog.dispose()
            }

            dialog.isVisible = true
        }

        val about = MenuItem("About")
        help.add(about)
        about.addActionListener {
            val dialog = Dialog(this)
            dialog.title = "About"
            dialog.setSize(400, 150)
            dialog.layout = BorderLayout()
            dialog.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    super.windowClosing(e)
                    dialog.dispose()
                }
            })

            val textArea = TextArea("A interest toy.\n" + "E-Mail: 108021063@live.asia.edu.tw", 2, 34, TextArea.SCROLLBARS_NONE)
            textArea.isEditable = false
            dialog.add(textArea, BorderLayout.CENTER)

            dialog.isVisible = true
        }
    }

    private fun createMainContainer() {
        val container = Container()

        container.layout = GridLayout(0,2)
        container.add(imgShower)
        container.add(comicList)
        renderingList()

        this.add(container, BorderLayout.CENTER)
    }

    private fun buttonContainer() {
        val halfContainer = Container()
        halfContainer.layout = GridLayout(0,2)

        val downloadButton = Button("Download")
        halfContainer.add(downloadButton)
        downloadButton.addActionListener {
            for (result in searchBuff) {
                if (result.asJsonObject.get("title").asJsonObject.get(language).asString == comicList.selectedItem.toString()) {
                    val resultId = result.asJsonObject.get("id").asString + "\\"
                    val mediaId = result.asJsonObject.get("media_id").asString
                    val mediaLength = result.asJsonObject.get("num_pages").asInt

                    downloader(result, resultId, mediaId, mediaLength)
                }
            }
        }

        val moreButton = Button("More...")
        halfContainer.add(moreButton)
        moreButton.addActionListener {
            ++this.page
            this.searchBuff.addAll(NhentaiProvider().search("language:chinese", page).get("result").asJsonArray)
            refreshComicList()
        }

        this.add(halfContainer, BorderLayout.PAGE_END)
    }

    private fun checkDict(path: String) {
        if (Files.notExists(Paths.get(path))) Files.createDirectories(Paths.get(path))
    }

    private fun renderingList() {
        comicList.addActionListener {
            for (result in searchBuff) {
                if (result.asJsonObject.get("title").asJsonObject.get(language).asString == comicList.selectedItem.toString()) {
                    imgShower.imgID = result.asJsonObject.get("media_id").asString
                    imgShower.type = result.asJsonObject.get("images").asJsonObject.get("thumbnail").asJsonObject.get("t").asString
                    imgShower.repaint()
                }
            }
        }
    }

    private fun downloader(result:JsonElement ,result_id: String, media_id: String, media_length: Int) {
        checkDict(savePath + result_id)

        //超速的下載
        Thread{
            for (i in 1..media_length) {
                val prefix = result.asJsonObject.get("images").asJsonObject.get("pages").asJsonArray[i - 1].asJsonObject.get("t").asString
                if (prefix == "j") File("$savePath$result_id$i.jpg").writeBytes(NhentaiProvider().getImage(media_id, i, prefix).readAllBytes())
                if (prefix == "p") File("$savePath$result_id$i.png").writeBytes(NhentaiProvider().getImage(media_id, i, prefix).readAllBytes())
            }
        }.start()
    }

    private fun refreshComicList() {
        comicList.removeAll()
        for (result in searchBuff) comicList.add(result.asJsonObject.get("title").asJsonObject.get(language).asString)
    }

    class ImgShower(var imgID: String, var type: String) : Component() {
        override fun paint(g: Graphics?) {
            super.paint(g)
            g?.drawImage(ImageIO.read(NhentaiProvider().getThumb(imgID, type)), 0, 0, null)
        }
    }
}

fun main() {
    MainFrame()
}