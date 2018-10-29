/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi.integration;

/**
 *
 * @author malapert
 * mvn clean verify -P integration-test
 */
import static fr.cnes.doi.AbstractSpec.testTitle;
import fr.cnes.doi.InitServerForTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
@Ignore
public class ITWebsite {

    static WebDriver driver;

    @BeforeClass
    public static void setup() {
        InitServerForTest.init();
        driver = new FirefoxDriver();
    }

    @AfterClass
    public static void cleanUp() {        
        driver.quit();
        InitServerForTest.close();
    }

    @Test
    public void shouldSayHelloWorld() {
        testTitle("shouldSayHelloWorld");
        driver.get("http://localhost:8182/");
        assertEquals("Hello World!", driver.findElement(By.tagName("body")).getText());
    }
}
