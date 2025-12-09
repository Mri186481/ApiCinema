package com.svalero.cinemas.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    //Metodo para calcular cuantos dias han pasado entre dos fechas
    public static long getDaysBetweenDates(LocalDate startDate, LocalDate endDate) {
        return Math.abs(startDate.until(endDate, ChronoUnit.DAYS));
    }
}
