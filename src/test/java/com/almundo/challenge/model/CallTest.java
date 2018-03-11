package com.almundo.challenge.model;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CallTest {

  @Test(expected = IllegalArgumentException.class)
  public void shouldReturnInvalidParameterExceptionWhenMaxValueInvalid() {
    Call.build(-1, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldReturnInvalidParameterExceptionWhenMinValueInvalid() {
    Call.build(1, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldReturnInvalidParameterExceptionWhenBuildCalls() {
    Call.build(5, 10);
  }

  @Test
  public void shouldCreateCallSuccessful() {
    Call call = Call.build(10, 5);

    assertNotNull(call);
    assertTrue(5 <= call.getDuration());
    assertTrue(call.getDuration() <= 10);
  }
}
