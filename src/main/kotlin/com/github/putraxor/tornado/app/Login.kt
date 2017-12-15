package com.github.putraxor.tornado.app

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import tornadofx.*
import javax.json.JsonObject


/**
 * Screen
 */
class LoginScreen : View("Login") {

    val model = ViewModel()
    val username = model.bind { SimpleStringProperty() }
    val password = model.bind { SimpleStringProperty() }

    val controller: LoginController by inject()

    override val root = form {
        fieldset(labelPosition = Orientation.VERTICAL) {
            fieldset("Username") {
                textfield(username).required()
            }
            fieldset("Password") {
                passwordfield(password).required()
            }
            button("Login") {
                enableWhen(model.valid)
                isDefaultButton = true
                useMaxHeight = true

                action {
                    runAsyncWithProgress {
                        controller.login(username.value, password.value)
                    }
                }
            }
            label(controller.statusProperty)
        }
    }
}

/**
 * Controller
 */
class LoginController : Controller() {

    val statusProperty = SimpleStringProperty()
    var status by statusProperty
    val api: Rest by inject()
    val user: UserModel by inject()

    init {
        api.baseURI = "https://api.github.com/"
    }

    fun login(username: String, password: String) {
        runLater { status = "" }
        api.setBasicAuth(username, password)
        val response = api.get("user")
        val json = response.one()
        println(json)
        runLater {
            if (response.ok()) {
                user.item = json.toModel()
                tornadofx.find(LoginScreen::class).replaceWith(Welcome::class, sizeToScene = true, centerOnScreen = true)
            } else {
                status = json.string("message") ?: "Login Failed"
            }
        }
    }

}

/**
 * Model
 */
class User : JsonModel {
    val nameProperty = SimpleStringProperty()
    var name by nameProperty

    val avatarProperty = SimpleStringProperty()
    var avatar by avatarProperty

    val companyProperty = SimpleStringProperty()
    var company by companyProperty

    val locationProperty = SimpleStringProperty()
    var location by locationProperty

    val emailProperty = SimpleStringProperty()
    var email by emailProperty

    val bioProperty = SimpleStringProperty()
    var bio by bioProperty


    override fun updateModel(json: JsonObject) {
        with(json) {
            name = string("name")
            avatar = string("avatar_url")
            company = string("company")
            location = string("location")
            email = string("email")
            bio = string("bio")
        }
    }
}

/**
 * ViewModel
 */
class UserModel : ItemViewModel<User>() {
    val name = bind(User::nameProperty)
    val avatar = bind(User::avatarProperty)
    val company = bind(User::companyProperty)
    val location = bind(User::locationProperty)
    val email = bind(User::emailProperty)
    val bio = bind(User::bioProperty)
}


/**
 * Welcome Screen
 */
class Welcome : View("Welcome") {

    val user: UserModel by inject()

    override val root = vbox(12) {
        imageview(user.avatar)
        label(user.name)
        label(user.email)
        label(user.location)
        label(user.company)
        label(user.bio)
    }
}

/**
 * Main App
 */
class LoginApp : App(LoginScreen::class)