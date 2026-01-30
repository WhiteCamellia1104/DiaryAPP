package com.example.diarytest.utils

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
    private const val ALGORITHM = "AES"
    private const val KEY = "DiaryAppSecretKey"

//    fun encryptPassword(password: String): String {
//        try {
//            val digest = MessageDigest.getInstance("SHA-256")
//            val hash = digest.digest(password.toByteArray())
//            return Base64.encodeToString(hash, Base64.DEFAULT)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return ""
//        }
//    }
fun encryptPassword(password: String): String {
    try {
        val secretKey = SecretKeySpec(KEY.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(password.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

    fun decryptPassword(encryptedPassword: String): String {
        try {
            val secretKey = SecretKeySpec(KEY.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decodedBytes = Base64.decode(encryptedPassword, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        val inputHash = encryptPassword(inputPassword)
        return inputHash == storedHash
    }
}