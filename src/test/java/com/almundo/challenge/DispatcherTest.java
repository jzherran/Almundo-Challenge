package com.almundo.challenge;

import com.almundo.challenge.enumerate.EmployeeRole;
import com.almundo.challenge.enumerate.EmployeeStatus;
import com.almundo.challenge.model.Call;
import com.almundo.challenge.model.Employee;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DispatcherTest {

  private Dispatcher dispatcher;

  @Test(expected = NullPointerException.class)
  public void shouldReturnExceptionWhenReceiveEmployeesNull() {
    dispatcher = new Dispatcher(null);
  }

  @Test
  public void shouldReturnDispatcher() {
    dispatcher = new Dispatcher(new ArrayList<>());
    assertNotNull(dispatcher);
    assertTrue(dispatcher.getDequeEmployees().isEmpty());
  }

  @Test
  public void shouldExecuteDispatchOfTenCallConcurrently() throws InterruptedException {
    List<Call> calls = Call.build(10, 5, 10);
    dispatcher = new Dispatcher(getEmployees());

    startCalls(calls);

    dispatcher.stopDispatchingCalls();
    assertEquals(10, dispatcher.getDequeEmployees().stream()
        .mapToInt(value -> value.getCompleted().size()).sum());
  }

  @Test
  public void shouldThrowExceptionWhenStartCall() throws InterruptedException {
    Employee mockEmployee = mock(Employee.class);

    List<Call> calls = Call.build(10, 5, 5);
    List<Employee> employees = new ArrayList<>(Arrays.asList(mockEmployee));
    dispatcher = new Dispatcher(employees);

    when(mockEmployee.getRole()).thenReturn(EmployeeRole.OPERATOR);
    when(mockEmployee.getStatus()).thenReturn(EmployeeStatus.WAIT_FOR_CALL);
    doThrow(new RuntimeException()).when(mockEmployee).startRespondCall(any(Call.class));

    startCalls(calls, 10);

    dispatcher.stopDispatchingCalls();
    assertEquals(5, dispatcher.getDequeCalls().size());
  }

  @Test
  public void shouldContinueRunWhenNotExistEmployeeAvailable() throws InterruptedException {
    Employee mockEmployee = mock(Employee.class);

    List<Call> calls = Call.build(10, 5, 10);
    List<Employee> employees = new ArrayList<>(Arrays.asList(mockEmployee));
    dispatcher = new Dispatcher(employees);

    when(mockEmployee.getRole()).thenReturn(EmployeeRole.OPERATOR);
    when(mockEmployee.getStatus()).thenReturn(EmployeeStatus.IN_CALL);

    startCalls(calls);

    dispatcher.stopDispatchingCalls();
    assertEquals(10, dispatcher.getDequeCalls().size());
    assertEquals(1, dispatcher.getDequeEmployees().stream()
        .mapToInt(value -> (value.getStatus().equals(EmployeeStatus.IN_CALL)?1:0)).sum());
  }

  @Test
  public void shouldAnswerCallForSupervisorAndDirectorWhenNotOperatorsAreAvailable() throws InterruptedException {
    List<Call> calls = Call.build(10, 5, 10);
    List<Employee> employees = getEmployees(10, 0, 5, 5);

    dispatcher = new Dispatcher(employees);

    startCalls(calls);

    dispatcher.stopDispatchingCalls();
    assertTrue(dispatcher.getDequeCalls().isEmpty());
    assertEquals(10, dispatcher.getDequeEmployees().stream()
        .mapToInt(value -> value.getCompleted().size()).sum());
  }

  private void startCalls(List<Call> calls) throws InterruptedException {
    startCalls(calls, 20);
  }

  private void startCalls(List<Call> calls, int timeout) throws InterruptedException {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    dispatcher.startDispatchingCalls();
    TimeUnit.SECONDS.sleep(1);
    executorService.execute(dispatcher);
    TimeUnit.SECONDS.sleep(1);
    calls.stream().forEach(c -> {
      dispatcher.dispatch(c);
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (Exception e) {
        e.getStackTrace();
      }
    });
    executorService.awaitTermination(timeout, TimeUnit.SECONDS);
  }

  private List<Employee> getEmployees() {
    return getEmployees(10, 5, 3,2);
  }

  private List<Employee> getEmployees(int size, int operators, int supervisors, int directors) {
    List<Employee> employees = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      if (i < operators) {
        employees.add(new Employee(String.format("Employee %d", i), EmployeeRole.OPERATOR));
      } else if (i < operators + supervisors) {
        employees.add(new Employee(String.format("Employee %d", i), EmployeeRole.SUPERVISOR));
      } else if (i < operators + supervisors + directors) {
        employees.add(new Employee(String.format("Employee %d", i), EmployeeRole.DIRECTOR));
      }
    }
    return employees;
  }
}
