package org.globsframework.view;

import java.time.*;

public class DateUtils {

    public static ZonedDateTime parse(String str) {
        final int indexOfT = str.indexOf("T");
        if (indexOfT != -1) {
            if (str.contains("+") || str.lastIndexOf("-") > indexOfT || str.endsWith("Z")) {
                return ZonedDateTime.parse(str);
            }
            else {
                final int square = str.indexOf("[");
                if (square != -1){
                    return ZonedDateTime.of(LocalDateTime.parse(str.substring(0, square)), ZoneId.of(str.substring(square + 1, str.length() - 1)));
                }
                else{
                    return ZonedDateTime.of(LocalDateTime.parse(str), ZoneId.systemDefault());
                }
            }
        }
        else {
            final int square = str.indexOf("[");
            if (square != -1) {
                LocalDate parse = LocalDate.parse(str.substring(0, square));
                return ZonedDateTime.of(parse, LocalTime.MIDNIGHT, ZoneId.of(str.substring(square +1, str.length() - 1)));
            }
            else{
                LocalDate parse = LocalDate.parse(str);
                return ZonedDateTime.of(parse, LocalTime.MIDNIGHT, ZoneId.systemDefault());
            }
        }
    }
}
