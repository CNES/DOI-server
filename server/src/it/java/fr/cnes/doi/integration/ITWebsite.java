/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
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
import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.exception.ClientMdsException;
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
    public static void setup() throws ClientMdsException {
        InitServerForTest.init(InitSettingsForTest.CONFIG_IT_PROPERTIES);
        driver = new FirefoxDriver();
    }

    @AfterClass
    public static void cleanUp() {        
        driver.quit();
        InitServerForTest.close();
    }

    @Test
    public void TestIHM() {
        driver.get("https://localhost:8182/ihm");
        String text = driver.findElement(By.tagName("body")).getText();
        assertTrue("Test is the IHM is available", text.contains("Se connecter"));
    }
}
