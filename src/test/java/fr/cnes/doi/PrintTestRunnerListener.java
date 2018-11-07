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
package fr.cnes.doi;


import fr.cnes.doi.InitServerForTest.ColorStatus;
import static fr.cnes.doi.InitServerForTest.mavenInfoRun;
import static fr.cnes.doi.InitServerForTest.mavenSkipMessage;
import static fr.cnes.doi.InitServerForTest.mavenTitle;
import static fr.cnes.doi.InitServerForTest.testTitle;
import org.junit.Ignore;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author malapert
 */
public class PrintTestRunnerListener extends RunListener {

    private static final Description FAILED = Description.createTestDescription("failed", "failed");
    private static final Description SKIP = Description.createTestDescription("skip", "skip");
    private int nbTotalTestCases;
    //private int nbSkips = 0;
    //private int nbErrors = 0;
    
    public PrintTestRunnerListener() {
        super();
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        this.nbTotalTestCases = description.testCount();
        mavenInfoRun(this.nbTotalTestCases);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        testTitle(failure.getDescription().getMethodName(),ColorStatus.FAILED);
        failure.getDescription().addChild(FAILED);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        Ignore ignore = description.getAnnotation(Ignore.class);
        System.out.println(ignore.value());
        testTitle(description.getDisplayName(), ColorStatus.SKIP, ignore.value());
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        int testCount = failure.getDescription().testCount();
        if (testCount == 1) {
            testTitle(failure.getDescription().getMethodName(), ColorStatus.SKIP, failure.getMessage());
            failure.getDescription().addChild(SKIP);
        } else {
            mavenTitle(failure);
            List<Description> children = failure.getDescription().getChildren();
            for (Description it : children) {
                testTitle(it.getMethodName(), ColorStatus.SKIP);
            }
            mavenSkipMessage(failure);
        }

    }

    @Override
    public void testStarted(Description description) throws Exception {
    }

    @Override
    public void testFinished(Description description) throws Exception {
        if (description.getChildren().contains(FAILED)) {
        } else if (description.getChildren().contains(SKIP)) {
        } else {
            testTitle(description.getMethodName(), ColorStatus.OK);
        }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        //super.testRunFinished(result);
//        int count = result.getFailureCount();
//        int ignoreCount = result.getIgnoreCount();
//        boolean isSuccess = result.wasSuccessful();
//        //System.out.println("count:"+count+" ignore:"+ignoreCount+" isSuc:"+isSuccess+ " "+result.getRunCount());
//        List<Failure> fails = result.getFailures();
//        for (Failure fail : fails) {
//            //System.out.println(fail.getDescription().getClassName());
//        }
    }

}
