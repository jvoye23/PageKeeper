package com.jvcodingsolutions.pagekeeper.feature.reader.domain

object ReadingProgressCalculator {

    /**
     * Section + intra-section blend for FB2 / EPUB content.
     *
     * The numerator is `currentSectionIndex + scrollFractionWithinLoadedItems`,
     * the denominator is `totalSectionCount`. This produces a smooth value that
     * advances both when the user scrolls within a chapter and when the chapter
     * count grows.
     *
     * @param firstVisibleItemIndex current LazyColumn first-visible-item index
     * @param totalLoadedItemCount  number of LazyColumn items currently loaded (sum of all parsed sections)
     * @param loadedSectionCount    how many sections have been parsed so far
     * @param totalSectionCount     total number of sections in the book
     */
    fun forText(
        firstVisibleItemIndex: Int,
        totalLoadedItemCount: Int,
        loadedSectionCount: Int,
        totalSectionCount: Int,
    ): Float {
        if (totalSectionCount <= 0) return 0f
        val safeLoaded = loadedSectionCount.coerceIn(0, totalSectionCount)
        val intra = if (totalLoadedItemCount > 0 && safeLoaded > 0) {
            val itemsPerSection = totalLoadedItemCount.toFloat() / safeLoaded
            val approxCurrentSection = (firstVisibleItemIndex / itemsPerSection).coerceAtLeast(0f)
            approxCurrentSection / totalSectionCount
        } else {
            0f
        }
        return intra.coerceIn(0f, 1f)
    }

    /**
     * For PDF books — straight ratio of current page to total pages.
     */
    fun forPdf(currentPageIndex: Int, totalPageCount: Int): Float {
        if (totalPageCount <= 0) return 0f
        return (currentPageIndex.toFloat() / totalPageCount).coerceIn(0f, 1f)
    }
}
