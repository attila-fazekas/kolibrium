package dev.kolibrium.selenium.internal.pages

import dev.kolibrium.selenium.clickable
import dev.kolibrium.selenium.id
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

context(WebDriver)
class ButtonsPage {
    val button1 by id<WebElement>("button1") {
        it.clickable
    }

    val button2 by id<WebElement>("button2") {
        it.clickable
    }

    val button3 by id<WebElement>("button3") {
        it.clickable
    }

    val button4 by id<WebElement>("button4") {
        it.clickable
    }

    val result by id<WebElement>("result")
}
