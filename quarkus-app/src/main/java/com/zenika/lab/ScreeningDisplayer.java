package com.zenika.lab;

import com.zenika.labs.movie.Screening;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ScreeningDisplayer {

  private static final Logger LOG = Logger.getLogger(ScreeningDisplayer.class);

  @Incoming("screening-in")
  public void display(Screening screening) {
    LOG.info(screening);
  }
}
