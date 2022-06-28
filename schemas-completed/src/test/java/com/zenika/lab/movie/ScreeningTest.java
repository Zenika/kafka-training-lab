package com.zenika.lab.movie;

import com.zenika.labs.movie.Screening;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

public class ScreeningTest {

  @Test
  void canGenerateScreening() {
    Screening.newBuilder()
          .setMovieId("123")
          .setCinemaId("123")
          .setScreeningDate(LocalDate.of(2022, Month.AUGUST, 10))
          .build();
  }

}
