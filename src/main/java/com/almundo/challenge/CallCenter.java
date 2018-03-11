package com.almundo.challenge;

import com.almundo.challenge.enumerate.EmployeeRole;
import com.almundo.challenge.model.Call;
import com.almundo.challenge.model.Employee;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is only a test for not using a Unit test for validate functionality with different
 * cases or data
 */
public class CallCenter {

  private static final Logger logger = LoggerFactory.getLogger(CallCenter.class);

  private static final int MIN_TIME_CALL = 5;

  private static final int MAX_TIME_CALL = 15;

  private static final int TOTAL_CALLS = 30;

  public static void main(String[] args) throws InterruptedException {
    List<Call> calls = Call.build(MAX_TIME_CALL, MIN_TIME_CALL, TOTAL_CALLS);
    Dispatcher dispatcher = new Dispatcher(getEmployees());
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    logger.info("Total employees working {}", dispatcher.getDequeEmployees().size());
    dispatcher.startDispatchingCalls();
    TimeUnit.SECONDS.sleep(1);
    executorService.execute(dispatcher);
    TimeUnit.SECONDS.sleep(1);

    calls.stream().forEach(call -> {
      dispatcher.dispatch(call);
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.getStackTrace();
      }
    });

    executorService.awaitTermination(MAX_TIME_CALL * (TOTAL_CALLS / 10), TimeUnit.SECONDS);
    dispatcher.stopDispatchingCalls();
    System.exit(0);
  }

  private static List<Employee> getEmployees() {
    List<Employee> employees = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      if (i < 5) {
        employees.add(new Employee(String.format("Employee %d", i), EmployeeRole.OPERATOR));
      } else if (i < 8) {
        employees.add(new Employee(String.format("Employee %d", i), EmployeeRole.SUPERVISOR));
      } else {
        employees.add(new Employee(String.format("Employee %d", i), EmployeeRole.DIRECTOR));
      }
    }
    return employees;
  }
}
