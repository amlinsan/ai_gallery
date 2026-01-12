package com.elink.aigallery.ai

import android.content.Context
import java.util.Locale
import java.util.zip.GZIPInputStream

class ClipTokenizer(
    context: Context,
    private val contextLength: Int = DEFAULT_CONTEXT_LENGTH,
    vocabAssetName: String = DEFAULT_VOCAB_ASSET
) {

    private val encoder: Map<String, Int>
    private val bpeRanks: Map<String, Int>
    private val byteEncoder: Map<Int, String>
    private val cache = HashMap<String, String>()
    private val pattern = Regex(
        "<\\|startoftext\\|>|<\\|endoftext\\|>|'s|'t|'re|'ve|'m|'ll|'d|\\p{L}+|\\p{N}|[^\\s\\p{L}\\p{N}]+",
        RegexOption.IGNORE_CASE
    )

    init {
        val byteEncoding = bytesToUnicode()
        byteEncoder = byteEncoding.encoder
        val merges = loadMerges(context, vocabAssetName)
        bpeRanks = merges.withIndex().associate { (idx, pair) -> pair.first + " " + pair.second to idx }
        encoder = buildEncoder(byteEncoding.values, merges)
        cache["<|startoftext|>"] = "<|startoftext|>"
        cache["<|endoftext|>"] = "<|endoftext|>"
    }

    fun encode(text: String): IntArray {
        val tokens = mutableListOf<Int>()
        tokens.add(START_TOKEN)

        val cleaned = cleanText(text)
        if (cleaned.isNotEmpty()) {
            for (match in pattern.findAll(cleaned)) {
                val token = match.value
                val encodedToken = buildString {
                    val bytes = token.toByteArray(Charsets.UTF_8)
                    for (b in bytes) {
                        append(byteEncoder[b.toInt() and 0xFF])
                    }
                }
                val bpeTokens = bpe(encodedToken).split(' ')
                for (bpeToken in bpeTokens) {
                    val id = encoder[bpeToken] ?: continue
                    tokens.add(id)
                }
            }
        }

        tokens.add(END_TOKEN)
        if (tokens.size > contextLength) {
            tokens.subList(contextLength, tokens.size).clear()
            tokens[contextLength - 1] = END_TOKEN
        }

        val output = IntArray(contextLength)
        for (i in tokens.indices) {
            output[i] = tokens[i]
        }
        return output
    }

    private fun cleanText(text: String): String {
        return text.replace(Regex("\\s+"), " ").trim().lowercase(Locale.ROOT)
    }

    private fun bpe(token: String): String {
        cache[token]?.let { return it }

        if (token.isEmpty()) {
            return token
        }

        var word = buildWord(token)
        var pairs = getPairs(word)
        if (pairs.isEmpty()) {
            val fallback = token + END_OF_WORD
            cache[token] = fallback
            return fallback
        }

        while (true) {
            val minPair = minPair(pairs) ?: break
            val parts = minPair.split(' ')
            if (parts.size != 2 || !bpeRanks.containsKey(minPair)) {
                break
            }
            val first = parts[0]
            val second = parts[1]
            val newWord = mutableListOf<String>()
            var i = 0
            while (i < word.size) {
                val j = indexOfFrom(word, first, i)
                if (j == -1) {
                    newWord.addAll(word.subList(i, word.size))
                    break
                }
                newWord.addAll(word.subList(i, j))
                if (j < word.size - 1 && word[j] == first && word[j + 1] == second) {
                    newWord.add(first + second)
                    i = j + 2
                } else {
                    newWord.add(word[j])
                    i = j + 1
                }
            }
            word = newWord
            if (word.size == 1) {
                break
            }
            pairs = getPairs(word)
        }

        val result = word.joinToString(" ")
        cache[token] = result
        return result
    }

    private fun buildWord(token: String): MutableList<String> {
        val chars = token.toCharArray()
        val word = MutableList(chars.size) { "" }
        for (i in chars.indices) {
            val chunk = chars[i].toString()
            word[i] = if (i == chars.lastIndex) {
                chunk + END_OF_WORD
            } else {
                chunk
            }
        }
        return word
    }

    private fun getPairs(word: List<String>): Set<String> {
        val pairs = HashSet<String>()
        if (word.size < 2) return pairs
        for (i in 0 until word.size - 1) {
            pairs.add(word[i] + " " + word[i + 1])
        }
        return pairs
    }

    private fun minPair(pairs: Set<String>): String? {
        var minPair: String? = null
        var minRank = Int.MAX_VALUE
        for (pair in pairs) {
            val rank = bpeRanks[pair] ?: Int.MAX_VALUE
            if (rank < minRank) {
                minRank = rank
                minPair = pair
            }
        }
        return if (minRank == Int.MAX_VALUE) null else minPair
    }

    private fun indexOfFrom(list: List<String>, value: String, start: Int): Int {
        for (i in start until list.size) {
            if (list[i] == value) return i
        }
        return -1
    }

    private fun buildEncoder(
        byteEncoderValues: List<String>,
        merges: List<Pair<String, String>>
    ): Map<String, Int> {
        val vocab = mutableListOf<String>()
        vocab.addAll(byteEncoderValues)
        vocab.addAll(byteEncoderValues.map { it + END_OF_WORD })
        for (merge in merges) {
            vocab.add(merge.first + merge.second)
        }
        vocab.add("<|startoftext|>")
        vocab.add("<|endoftext|>")
        return vocab.withIndex().associate { it.value to it.index }
    }

    private fun loadMerges(context: Context, assetName: String): List<Pair<String, String>> {
        val merges = mutableListOf<Pair<String, String>>()
        val reader = openBpeReader(context, assetName)
        reader.useLines { lines ->
            var isFirst = true
            var count = 0
            for (line in lines) {
                if (isFirst) {
                    isFirst = false
                    continue
                }
                if (count >= MAX_MERGES) break
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                val parts = trimmed.split(' ')
                if (parts.size == 2) {
                    merges.add(parts[0] to parts[1])
                    count++
                }
            }
        }
        return merges
    }

    private fun openBpeReader(context: Context, assetName: String): java.io.BufferedReader {
        return try {
            openReaderForAsset(context, assetName)
        } catch (e: java.io.FileNotFoundException) {
            val fallback = if (assetName.endsWith(".gz")) {
                assetName.removeSuffix(".gz")
            } else {
                "$assetName.gz"
            }
            openReaderForAsset(context, fallback)
        }
    }

    private fun openReaderForAsset(context: Context, assetName: String): java.io.BufferedReader {
        val input = context.assets.open(assetName)
        val stream = if (assetName.endsWith(".gz")) {
            GZIPInputStream(input)
        } else {
            input
        }
        return stream.bufferedReader(Charsets.UTF_8)
    }

    private fun bytesToUnicode(): ByteEncoding {
        val bs = mutableListOf<Int>()
        for (b in 33..126) bs.add(b)
        for (b in 161..172) bs.add(b)
        for (b in 174..255) bs.add(b)
        val cs = bs.toMutableList()
        var n = 0
        for (b in 0..255) {
            if (!bs.contains(b)) {
                bs.add(b)
                cs.add(256 + n)
                n++
            }
        }
        val encoder = HashMap<Int, String>()
        val values = ArrayList<String>(bs.size)
        for (i in bs.indices) {
            val value = String(Character.toChars(cs[i]))
            encoder[bs[i]] = value
            values.add(value)
        }
        return ByteEncoding(encoder, values)
    }

    private data class ByteEncoding(
        val encoder: Map<Int, String>,
        val values: List<String>
    )

    companion object {
        private const val END_OF_WORD = "</w>"
        private const val MAX_MERGES = 49152 - 256 - 2
        private const val START_TOKEN = 49406
        private const val END_TOKEN = 49407
        private const val DEFAULT_CONTEXT_LENGTH = 77
        private const val DEFAULT_VOCAB_ASSET = "bpe_simple_vocab_16e6.txt.gz"
    }
}
