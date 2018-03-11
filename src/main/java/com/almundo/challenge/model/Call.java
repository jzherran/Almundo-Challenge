package com.almundo.challenge.model;

import com.almundo.challenge.Dispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.Validate;

/**
 * Model class for represent a call object
 */
@Data
@AllArgsConstructor
public class Call {

  private static final String CALL_ID = "ID_%d";

  /**
   * Duration for call represented in seconds
   */
  private int duration;

  /**
   * Identifier for recognize call
   */
  private String id;

  /**
   * Build a {@link Call} with a duration random between min value and max value
   * received by parameter
   *
   * @param max duration to set in call
   * @param min duration to set in call
   * @return a {@link Call}
   */
  public static Call build(int max, int min) {
    Validate.isTrue(min > -1 && max >= min);
    return new Call(ThreadLocalRandom.current().nextInt(min, max + 1),
        String.format(CALL_ID, ThreadLocalRandom.current().nextInt(1000, 2000)));
  }

  /**
   * Build a list of {@link Call} with a size received by parameter
   *
   * @param max duration to set for all calls in the list
   * @param min duration to set for all calls in the list
   * @param size of the list to create
   * @return a list of {@link Call}
   */
  public static List<Call> build(int max, int min, int size) {
    Validate.isTrue(size > -1);
    List<Call> calls = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      calls.add(build(max, min));
    }
    return calls;
  }
}
