import com.mobapphome.simpleencryptorlib.SimpleEncryptor
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Encryption (val key: String) {
    
    fun getKey() : ByteArray {
        return key.toByteArray().copyOf(16)
    }
    
//    fun encrypt(s: String) : String{
//        val simpleEncryptor = SimpleEncryptor.newInstance(key)
//        return simpleEncryptor!!.encodeOrReturnNull(s)
//    }
//
//    fun decrypt(s: String): String {
//        val simpleEncryptor = SimpleEncryptor.newInstance(key)
//        return simpleEncryptor.decode(s)
//    }
    
    fun encrypt(s: String) : String{
        val secretKeySpec = SecretKeySpec(getKey(), "AES")

        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)

        val encryptedValue = cipher.doFinal(s.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedValue)
    }

    fun decrypt(s: String): String {
        val secretKeySpec = SecretKeySpec(getKey(), "AES")
        
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)

        val decryptedByteValue = cipher.doFinal(Base64.getDecoder().decode(s))
        return String(decryptedByteValue)
    }

}

fun main(args: Array<String>) {
    
    println(Encryption("test").encrypt("test"))
    
    var enc = Encryption("test")
    
    println(enc.encrypt("einszwei"))
    println(enc.decrypt("h72QOIWUO+SKTmirY7Dsag=="))
    
}