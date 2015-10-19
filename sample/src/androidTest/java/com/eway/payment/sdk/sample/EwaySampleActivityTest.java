package com.eway.payment.sdk.sample;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.eway.payment.sdk.sample.EwaySampleActivityTest \
 * com.eway.payment.sdk.sample.tests/android.test.InstrumentationTestRunner
 */
public class EwaySampleActivityTest extends ActivityInstrumentationTestCase2<EwaySampleActivity> {

    public EwaySampleActivityTest() {
        super("com.eway.payment.sdk.sample", EwaySampleActivity.class);
    }

}
