package com.example.data.verification

import android.graphics.Bitmap
import android.graphics.Color

object DHashCalculator {

    /**
     * Resmin 64-bit fark özetini (dHash) hesaplar.
     * Algoritma:
     * 1. Resmi 9x8 boyutuna küçült.
     * 2. Gri tonlamaya çevir.
     * 3. Her satırda soldan sağa yan yana 8 çift pikselin parlaklığını karşılaştır (piksel[x] > piksel[x+1]).
     * 4. 8 satır x 8 sütun = 64 bit Long değer üretir.
     */
    fun calculateDHash(source: Bitmap): Long {
        val scaled = Bitmap.createScaledBitmap(source, 9, 8, true)
        var hash = 0L
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val leftPixel = scaled.getPixel(x, y)
                val rightPixel = scaled.getPixel(x + 1, y)

                val leftBrightness = (Color.red(leftPixel) * 299 + Color.green(leftPixel) * 587 + Color.blue(leftPixel) * 114) / 1000
                val rightBrightness = (Color.red(rightPixel) * 299 + Color.green(rightPixel) * 587 + Color.blue(rightPixel) * 114) / 1000

                if (leftBrightness > rightBrightness) {
                    hash = hash or (1L shl (y * 8 + x))
                }
            }
        }
        if (scaled != source) {
            scaled.recycle()
        }
        return hash
    }

    /**
     * İki 64-bit özet arasındaki Hamming mesafesini döner.
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
    }

    /**
     * Daha önce kaydedilmiş özetler listesinde Hamming mesafesi < 6 olan bir eşleşme var mı kontrol eder.
     */
    fun isDuplicate(newHash: Long, existingHashes: List<Long>, threshold: Int = 6): Boolean {
        for (existing in existingHashes) {
            if (hammingDistance(newHash, existing) < threshold) {
                return true
            }
        }
        return false
    }
}
