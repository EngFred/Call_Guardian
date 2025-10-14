package com.engfred.callguardian.domain.utils

import android.util.Log
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneNumberNormalizer @Inject constructor() {
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun normalize(number: String, countryIso: String): String? {
        if (number.isBlank()) return null
        val effectiveCountryIso = if (countryIso.isBlank()) "UG" else countryIso.uppercase()
        Log.d("PhoneNormalizer", "Normalizing input: $number for ISO: $effectiveCountryIso")
        return try {
            val parsed = phoneUtil.parse(number, effectiveCountryIso)
            if (phoneUtil.isValidNumber(parsed)) {
                val e164 = phoneUtil.format(parsed, PhoneNumberFormat.E164).removePrefix("+")
                Log.d("PhoneNormalizer", "Parsed to E164: $e164")
                e164
            } else {
                Log.w("PhoneNormalizer", "Invalid number after parse: $number for $effectiveCountryIso")
                fallbackNormalize(number, effectiveCountryIso)
            }
        } catch (e: NumberParseException) {
            Log.w("PhoneNormalizer", "Parse failed for $number: ${e.message}")
            fallbackNormalize(number, effectiveCountryIso)
        }
    }

    private fun fallbackNormalize(number: String, countryIso: String): String? {
        val digits = number.replace(Regex("[^0-9+]"), "")
        val cleanDigits = digits.removePrefix("+")
        val length = cleanDigits.length
        return when {
            length >= 10 -> {
                // If full international (e.g., 12+ for UG), use as-is
                if (length >= 12 && cleanDigits.startsWith("256")) {
                    Log.d("PhoneNormalizer", "Fallback: International as-is: $cleanDigits")
                    cleanDigits
                }
                // If local length (~10 for UG, starting with 0), prepend country code
                else if (length == 10 && cleanDigits.startsWith("0")) {
                    val withCountry = "256${cleanDigits.drop(1)}"
                    Log.d("PhoneNormalizer", "Fallback: Local prepended to 256: $withCountry")
                    withCountry
                }
                else {
                    Log.d("PhoneNormalizer", "Fallback: Clean digits: $cleanDigits")
                    cleanDigits
                }
            }
            else -> {
                Log.w("PhoneNormalizer", "Fallback too short: $cleanDigits")
                null
            }
        }
    }
}