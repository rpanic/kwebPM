package main

import io.kweb.shoebox.Shoebox
import java.nio.file.Files
import java.nio.file.Path

class PasswordsState(dir: Path){
    init {
        if (Files.notExists(dir)) {
            Files.createDirectory(dir)
        }
    }
    
    data class Password(val uid: String, var name: String, val userkey: String, var username: String, var password: String, var description: String)
    
    val passwords = Shoebox<Password>(dir.resolve("password"))
    
    val passwordsByUser = passwords.view("passwordByUser", Password::userkey)
    
    fun passwordsByUser(user: String) = passwordsByUser.orderedSet(user, compareBy(Password::name))
    
}