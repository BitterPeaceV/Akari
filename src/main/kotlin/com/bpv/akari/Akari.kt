package com.bpv.akari

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerChatEvent
import cn.nukkit.plugin.PluginBase
import com.atilika.kuromoji.ipadic.Tokenizer
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

class Akari : PluginBase(), Listener {
    private val tokenizer = Tokenizer()
    private val words = mutableListOf<String>()

    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdir()
        loadWords()

        server.pluginManager.registerEvents(this, this)

        server.scheduler.scheduleRepeatingTask(
            this,
            { server.broadcastMessage(generateSentence()) },
            20 * 60 * 2
        )
    }

    override fun onDisable() {
        saveWords()
    }

    @EventHandler
    fun onPlayerChat(event: PlayerChatEvent) {
        tokenizer.tokenize(event.message)
            .filter { it.partOfSpeechLevel1 == "名詞" && !words.contains(it.surface) }
            .forEach { words.add(it.surface) }
    }

    private fun loadWords() {
        val file = File(dataFolder, "words.txt")
        if (!file.exists()) file.createNewFile()
        file.bufferedReader(StandardCharsets.UTF_8).use { reader -> reader.forEachLine { line -> words.add(line) } }
    }

    private fun saveWords() {
        File(dataFolder, "words.txt").bufferedWriter().use { writer ->
            words.forEach { word ->
                writer.write(word)
                writer.newLine()
            }
        }
    }

    private fun generateSentence(): String {
        val word = if (words.isNotEmpty()) words[Random().nextInt(words.size)] else "うすしお"
        return "<Akari-bot> わぁい$word あかり${word}大好き"
    }
}