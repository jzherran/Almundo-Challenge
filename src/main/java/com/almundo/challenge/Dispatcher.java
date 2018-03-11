package com.almundo.challenge;

import com.almundo.challenge.enumerate.EmployeeRole;
import com.almundo.challenge.enumerate.EmployeeStatus;
import com.almundo.challenge.model.Call;
import com.almundo.challenge.model.Employee;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Data
public class Dispatcher implements Runnable {

  /** */
  private static final int CAPACITY_CALLS = 10;

  /** */
  private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  /** */
  private ConcurrentLinkedDeque<Call> dequeCalls;

  /** */
  private ConcurrentLinkedDeque<Employee> dequeEmployees;

  /** */
  private boolean dispatcherRunning;

  /** */
  private ExecutorService executor;

  public Dispatcher(List<Employee> employees) {
    this.dequeCalls = new ConcurrentLinkedDeque<>();
    this.dequeEmployees = new ConcurrentLinkedDeque<>(employees);
    this.dispatcherRunning = false;
    this.executor = Executors.newFixedThreadPool(CAPACITY_CALLS);
  }

  /**
   *
   */
  @Override public void run() {
    logger.info("Run dispatcher");
    do {
      if (getDequeCalls().isEmpty()) {
        continue;
      } else {
        Employee employee = findAvailableEmployeeForAttend();
        if (employee == null) {
          continue;
        }

        Call call = getDequeCalls().poll();
        try {
          employee.startAnswerCall(call);
        } catch (Exception e) {
          logger.error("Unknown error starting answer call {}", call.getId());
          dequeCalls.addFirst(call);
        }
      }
    } while (isDispatcherRunning());
  }

  /**
   *
   * @param call
   */
  public synchronized void dispatch(Call call) {
    logger.info("Dispatch a call {} with duration of {} seconds", call.getId(), call.getDuration());
    this.dequeCalls.add(call);
  }

  /**
   *
   */
  public synchronized void startDispatchingCalls() {
    setDispatcherRunning(true);
    getDequeEmployees().forEach(employee -> executor.execute(employee));
  }

  /**
   *
   */
  public synchronized void stopDispatchingCalls() {
    setDispatcherRunning(false);
    executor.shutdown();
  }

  /**
   *
   * @return
   */
  private Employee findAvailableEmployeeForAttend() {
    Validate.notNull(getDequeEmployees());

    List<Employee> employees = getDequeEmployees().stream().filter(employee -> employee.getStatus()
        .equals(EmployeeStatus.WAIT_FOR_CALL)).collect(Collectors.toList());

    if (employees.isEmpty()) {
      logger.info("Not available anyone employee");
      return null;
    }

    Optional<Employee> employee = employees.stream()
        .filter(operator -> operator.getRole().equals(EmployeeRole.OPERATOR)).findAny();

    logger.info("Employees wait for a call: {}", employees.size());

    if (!employee.isPresent()) {
      employee = employees.stream()
          .filter(operator -> operator.getRole().equals(EmployeeRole.SUPERVISOR)).findAny();
      if (!employee.isPresent()) {
        employee = employees.stream()
            .filter(supervisor -> supervisor.getRole().equals(EmployeeRole.DIRECTOR)).findAny();
      }
    }

    if (employee.isPresent()) {
      logger.info("Employee [{} - {}] selected for answer call", employee.get().getId(),
          employee.get().getRole());
      return employee.get();
    }

    return null;
  }
}
