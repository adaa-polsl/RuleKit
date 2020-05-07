package adaa.analytics.rules.logic.representation;

import org.junit.Test;

import static org.junit.Assert.*;

public class DoubleFormatterTest {

    private double[] numbers = {
            0.2,
            0.340123,
            0.00202,
            -0.302,
            -0.00202,
            3
    };

    private String[] numbersStrings = {
            "0.20",
            "0.34",
            "0.00202",
            "-0.30",
            "-0.00202",
            "3"
    };

    @Test
    public void testFormattingDoubles() {
        DoubleFormatter.configure(3);
        for(int i = 0; i < numbers.length; i++) {
            double number = numbers[i];
            String expectedString = numbersStrings[i];
            String actual = DoubleFormatter.format(number);

            assertEquals("Strings should match", expectedString, actual);
        }
    }
}