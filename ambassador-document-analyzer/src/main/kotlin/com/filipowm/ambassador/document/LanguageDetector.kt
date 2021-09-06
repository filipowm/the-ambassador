package com.filipowm.ambassador.document

import com.github.pemistahl.lingua.api.LanguageDetectorBuilder
import com.github.pemistahl.lingua.api.Language as LinguaLanguage

interface LanguageDetector {

    fun warmUp()
    fun detectLanguageOf(text: String): Language

    companion object {
        fun default(): LanguageDetector {
            // disable not popular or unlikely languages to minimize memory impact
            val lingua = LanguageDetectorBuilder
                .fromLanguages(LinguaLanguage.ENGLISH, LinguaLanguage.FRENCH, LinguaLanguage.GERMAN)
//                .fromAllLanguagesWithout(
//                    LinguaLanguage.ESPERANTO,
//                    LinguaLanguage.LATIN,
//                    LinguaLanguage.BASQUE,
//                    LinguaLanguage.GANDA,
//                    LinguaLanguage.SOTHO,
//                    LinguaLanguage.TSONGA,
//                    LinguaLanguage.TSWANA,
//                    LinguaLanguage.XHOSA,
//                    LinguaLanguage.BASQUE
//                )
                .withMinimumRelativeDistance(0.1)
                .build()
            return LinguaLanguageDetector(lingua)
        }
    }
}