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

public class CallCenter {

  private static final Logger logger = LoggerFactory.getLogger(CallCenter.class);

  public static void main(String[] args) throws InterruptedException {
    List<Call> calls = Call.build(10, 5, 20);
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

    executorService.awaitTermination(40, TimeUnit.SECONDS);
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
