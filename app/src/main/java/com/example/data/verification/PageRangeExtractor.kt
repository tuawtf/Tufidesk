package com.example.data.verification

object PageRangeExtractor {

    /**
     * Ödev başlığı ve açıklamasından beklenen sayfa numaralarını çıkarır.
     * Bulunamazsa boş liste döner.
     * Aralık 8 sayfadan uzunsa sadece ilk ve son sayfayı döner.
     */
    fun extractPages(title: String, description: String): List<Int> {
        val combined = "$title $description"
        return parsePages(combined)
    }

    fun parsePages(text: String): List<Int> {
        if (text.isBlank()) return emptyList()

        // 1. Aralık kalıpları: "26-29", "26–29", "26 ile 29", "s. 26-29", "sayfa 26-29", "sayfa 26 ile 29"
        val rangeRegexes = listOf(
            Regex("""(?:sayfa|sayfalar|sf\.?|s\.?|syf\.?)\s*[:.]?\s*(\d+)\s*(?:-|–|—|\.\.|ile|ila|veya|ve|'dan\s*|dan\s*)\s*(\d+)""", RegexOption.IGNORE_CASE),
            Regex("""(\d+)\s*(?:-|–|—|\.\.|ile|ila)\s*(\d+)\s*(?:sayfa|sf|syf)?""", RegexOption.IGNORE_CASE)
        )

        for (regex in rangeRegexes) {
            val match = regex.find(text)
            if (match != null) {
                val start = match.groupValues[1].toIntOrNull()
                val end = match.groupValues[2].toIntOrNull()
                if (start != null && end != null && start > 0 && end > 0) {
                    val min = minOf(start, end)
                    val max = maxOf(start, end)
                    val count = max - min + 1
                    return if (count > 8) {
                        listOf(min, max)
                    } else {
                        (min..max).toList()
                    }
                }
            }
        }

        // 2. Virgüllü veya boşluklu listeler: "sayfa 26, 27, 28" veya "26, 27, 28"
        val listRegex = Regex("""(?:sayfa|sayfalar|sf\.?|s\.?|syf\.?)\s*[:.]?\s*(\d+(?:\s*,\s*\d+)+)""", RegexOption.IGNORE_CASE)
        val listMatch = listRegex.find(text)
        if (listMatch != null) {
            val pages = listMatch.groupValues[1].split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it > 0 }
                .distinct()
                .sorted()
            if (pages.isNotEmpty()) {
                return if (pages.size > 8) listOf(pages.first(), pages.last()) else pages
            }
        }

        // 3. Tekil sayfa: "sayfa 26", "s.26", "sf. 26", "syf 26"
        val singleRegex = Regex("""(?:sayfa|sf\.?|s\.?|syf\.?)\s*[:.]?\s*(\d+)""", RegexOption.IGNORE_CASE)
        val singleMatch = singleRegex.find(text)
        if (singleMatch != null) {
            val page = singleMatch.groupValues[1].toIntOrNull()
            if (page != null && page > 0) {
                return listOf(page)
            }
        }

        return emptyList()
    }
}
