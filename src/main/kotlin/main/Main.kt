package main

import io.kweb.Kweb
import io.kweb.dom.BodyElement
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.events.on
import io.kweb.dom.element.events.onImmediate
import io.kweb.dom.element.new
import io.kweb.plugins.fomanticUI.fomantic
import io.kweb.plugins.fomanticUI.fomanticUIPlugin
import io.kweb.routing.route
import io.kweb.state.render.render
import io.kweb.state.render.renderEach
import java.nio.file.Paths
import kotlin.random.Random
import PasswordsState.Password
import io.kweb.WebBrowser
import io.kweb.shoebox.KeyValue
import io.kweb.shoebox.Source
import io.kweb.state.*

val state = PasswordsState(Paths.get("data"))

fun main(args: Array<String>) {
    
    state.passwords.onChange { password: Password, keyValue: KeyValue<Password>, source: Source ->
        println("Changed in DB: ${password.name} ${keyValue.value.name}")
    }
    
    Kweb(port = 80, plugins = listOf(fomanticUIPlugin)) {
        
        
        var username = KVar("")
        var password = KVar("")
        
        var search = KVar("")
        
        val map = mutableMapOf<String, UserCredentials>("test4" to UserCredentials("test4", "test"))
        
        doc.body.new {
            
            div(fomantic.ui.main.container).new {
                div(fomantic.column).new {
                    div(fomantic.ui.vertical.segment).new {
                        
                        route {
                            
                            path("/") {
                                
                                input(type = InputType.text, attributes = fomantic.ui.input).value = username
                                input(type = InputType.text, attributes = fomantic.ui.input).value = password
                                
                                button(fomantic.ui.button).text("Login").on.click {
                                    
                                    val token = randomToken();
                                    
                                    map[token] = UserCredentials(username.value, password.value)
                                    
                                    url.path.value = "/user/$token"
                                    
                                }
                            }
                            
                            path("/user/{token}") { params ->
                                
                                val token = params["token"]!!
                                
                                val userCredentials = map[token.value]
                                
                                if (userCredentials != null) {
                                    div(fomantic.ui.icon.input).new {
                                        input(type = InputType.text, placeholder = "Search..").value = search
                                        i(fomantic.search.icon).on.click {
                                            println("Search clicked")
                                        }
                                    }
    
                                    val encryption = Encryption(userCredentials.password)
                                    
                                    search.addListener { s, s2 -> println("Search Changed $s -> $s2") }
                                    render(search) { searchValue ->
                                        
                                        println("Render1")
                                        
                                        initTable {
                                            renderEach(state.passwordsByUser(userCredentials.name)) { password ->
                                                
                                                println(
                                                    "Render2 ${password.value.name} ${search.value} ${password.value.name.contains(
                                                        search.value
                                                    )}"
                                                )
                                                if(password.value.name.contains(searchValue)) {
                                                    
                                                    passwordRow(password, encryption)
                                                    
                                                }
                                            }
                                            endRow(this@Kweb, userCredentials, encryption)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ElementCreator<TbodyElement>.passwordRow(
    password: KVar<Password>,
    encryption: Encryption
){
    this.tr().new {
        td().new {
            p(attributes = fomantic.ui.text).apply {
                val x = password.map(Password::name)
                text(x)
            }
        }
        td().new {
            input(
                type = InputType.text,
                attributes = fomantic.ui.transparent.input,
                placeholder = "Loading"
            ).apply {
                val x = password.property(Password::username)
                value = x
            }
        }
        td().new {
            val input = input(
                type = InputType.password,
                attributes = fomantic.ui.input,
                placeholder = "Loading"
            ).apply {
                val x = password
                    .property(Password::password)
                    .map(object : ReversableFunction<String, String>("encryption"){
                        override fun invoke(from: String) =
                            encryption.decrypt(from)
        
                        override fun reverse(original: String, change: String) = encryption.encrypt(change)
                    })
                value = x
            }
            val i = i(fomantic.icon.search)
            i.onImmediate.mousedown {
                input.setAttribute("type", "type")
            }
            i.onImmediate.mouseup {
                input.setAttribute("type", "password")
            }
        }
        td().new {
            i(fomantic.trash.icon).on.click {
                val res = state.passwords.remove(password.value.uid)
                //                                                            state.passwords[password.value.uid] = null;
                println(res != null)
                println("OnDelete")
            }
        }
    }
}

private fun ElementCreator<TbodyElement>.endRow(
    browser: WebBrowser,
    userCredentials: UserCredentials,
    encryption: Encryption
){
    tr().new {
        
        val password = KVar(Password(randomToken(), "", userCredentials.name, "", "", ""))
        
        td().new {
            input(type = InputType.text, attributes = fomantic.ui.input).apply {
                val x = password.property(Password::name)
                value = x
            }
        }
        td().new {
            input(type = InputType.text, attributes = fomantic.ui.input).apply {
                val x = password.property(Password::username)
                value = x
            }
        }
        td().new {
            input(type = InputType.password, attributes = fomantic.ui.input).apply {
                val x = password.property(Password::password)
                value = x
            }
            
        }
        td().new {
            input(type = InputType.text, attributes = fomantic.ui.input).apply {
                val x = password.property(Password::description)
                value = x
            }
        }
        td().new {
            i(fomantic.check.icon).on.click {
                val pw = password.value
                pw.password = encryption.encrypt(pw.password);
                state.passwords[password.value.uid] = pw
                password.value =
                    Password(
                        randomToken(),
                        "",
                        userCredentials.name,
                        "",
                        "",
                        ""
                    )
            }
        }
        
    }
}

private fun ElementCreator<Element>.initTable(
    content: ElementCreator<TbodyElement>.() -> Unit
) {
    table(fomantic.ui.celled.table).new {
        thead().new {
            tr().new {
                th().text("Name")
                th().text("Username")
                th().text("Password")
                th().text("Info")
                th().text("")
            }
        }
        tbody().new {
            content(this)
        }
    }
}

val random = Random(System.currentTimeMillis())

private fun randomToken(): String {
    val size = 10
    var s = ""
    for (i in 0..size) {
        s += random.nextInt('a'.toInt(), 'z'.toInt()).toChar()
    }
    return s
}