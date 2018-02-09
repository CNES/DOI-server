/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.cnes.doi.InitSettingsForTest;
import fr.cnes.doi.exception.DoiRuntimeException;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * @author Claire
 *
 */
public class UniqueProjectNameTest {

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    /**
     * Cache file for tests
     */
    private static final String cacheFile = "src/test/resources/projects.conf";

    /**
     * Init the settings
     */
    @BeforeClass
    public static void setUpClass() {
        InitSettingsForTest.init();
    }

    /**
     * Executed before each test
     */
    @Before
    public void setUp() {
        // Save the projects.conf file
        try {
            Files.copy(new File(UniqueProjectNameTest.cacheFile).toPath(),
                    new File(UniqueProjectNameTest.cacheFile + ".bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executed after each test
     */
    @After
    public void tearDown() {
        // restore the cache file
        try {
            Files.copy(new File(UniqueProjectNameTest.cacheFile + ".bak").toPath(),
                    new File(UniqueProjectNameTest.cacheFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.delete(new File(UniqueProjectNameTest.cacheFile + ".bak").toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for
     * {@link fr.cnes.doi.utils.UniqueProjectName#getShortName(String, int)}
     */
    @Test
    public void testGetShortName() {

        // New id
        int idSWOT = UniqueProjectName.getInstance().getShortName("SWOT", 6);
        Assert.assertTrue(UniqueProjectName.getInstance().getShortName("SWOT", 6) == idSWOT);

        // Id already in the file
        Assert.assertTrue(UniqueProjectName.getInstance().getShortName("CFOSAT", 6) == 828606);

    }

    /**
     * Test method for
     * {@link fr.cnes.doi.utils.UniqueProjectName#getShortName(String, int)}
     */
    @Test
    public void testGetShortNameWithLongName() {

        exceptions.expect(DoiRuntimeException.class);
        exceptions.expectMessage("The short name cannot be build because the length requested is too big");

        // lenght requested out of range
        UniqueProjectName.getInstance().getShortName("CFOSAT", 10);

    }

}
