            itemsIndexed(swpResult.yearWiseData) { _, yearData ->
                SWPYearCard(yearData = yearData, currencyCode = currencyCode)
            }
        }
    }
}