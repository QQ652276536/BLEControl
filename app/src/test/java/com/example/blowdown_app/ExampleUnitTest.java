package com.example.bluetoothtest;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        LocalDate today = LocalDate.now();
        String sss = today.toString();
    }
}