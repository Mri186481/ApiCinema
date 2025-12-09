package com.svalero.cinemas;

import com.svalero.cinemas.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilTests {

    @Test
    public void testGetDaysBetweenDates() {
        //Los test se pueden hacer a metodos que no dependan de otros metodos
        //Si hay metodos por el medio , se simulan los resultados o mokean
        //Las fechas vienen con formato Localdate
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 2, 1);

        long actualDays = DateUtil.getDaysBetweenDates(startDate, endDate);
        //Con esto le digo que lo compruebe con assetEquals, y el expected
        //es el que manda, el que deberia de salir
        assertEquals(31, actualDays);

        startDate = LocalDate.of(2025, 1, 15);
        endDate = LocalDate.of(2025, 1, 20);
        actualDays = DateUtil.getDaysBetweenDates(startDate, endDate);
        assertEquals(5, actualDays);
        //se ejecuta con mvn test
    }

}
