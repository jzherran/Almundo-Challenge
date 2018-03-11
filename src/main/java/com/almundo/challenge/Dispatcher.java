package com.almundo.challenge;

import com.almundo.challenge.enumerate.EmployeeRole;
import com.almundo.challenge.enumerate.EmployeeStatus;
import com.almundo.challenge.model.Call;
import com.almundo.challenge.model.Employee;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher class is the main class, it receive all call and manage the employees
 * for respond if is possible or put it on hold until it can be answered
 */
@Data
public class Dispatcher implements Runnable {

  /** Call capacity attended concurrently */
  private static final int CAPACITY_CALLS = 10;

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  /** Structure for manage calls when there are not employees available */
  private ConcurrentLinkedDeque<Call> dequeCalls;

  /** Structure for manage employees */
  private ConcurrentLinkedDeque<Employee> dequeEmployees;

  /** Control for enable/disable the dispatch of calls */
  private boolean dispatcherRunning;

  private boolean employeesAvailable;

  /** Executor for send a {@link Call} to an {@link Employee} */
  private ExecutorService executor;

  /**
   * Constructor with only list of {@link Employee}
   * @param employees the {@link Employee} to use in the execution
   */
  public Dispatcher(List<Employee> employees) {
    Validate.notNull(employees);
    this.dequeCalls = new ConcurrentLinkedDeque<>();
    this.dequeEmployees = new ConcurrentLinkedDeque<>(employees);
    this.dispatcherRunning = false;
    this.employeesAvailable = true;
    this.executor = new ThreadPoolExecutor(CAPACITY_CALLS, CAPACITY_CALLS,
        60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
  }

  /**
   * Runs dispatcher only if it was start before
   */
  @Override public void run() {
    logger.info("Run dispatcher with {} employees", getDequeEmployees().size());
    do {
      if (getDequeCalls().isEmpty()) {
        continue;
      } else {
        Employee employee = findAvailableEmployeeForAnswering();
        if (employee == null) {
          continue;
        }

        Call call = getDequeCalls().poll();
        try {
          employee.startRespondCall(call);
        } catch (Exception e) {
          logger.error("Unknown error starting answer call {}", call.getId());
          getDequeCalls().addFirst(call);
        }
      }
    } while (isDispatcherRunning());
  }

  /**
   * Dispatches a call, in this case put in structure for calls received
   *
   * @param call the call received
   */
  public synchronized void dispatch(Call call) {
    logger.info("Dispatch a call {} with duration of {} seconds", call.getId(), call.getDuration());
    getDequeCalls().add(call);
  }

  /**
   * Start dispatcher
   */
  public synchronized void startDispatchingCalls() {
    setDispatcherRunning(true);
    getDequeEmployees().forEach(employee -> getExecutor().execute(employee));
  }

  /**
   * Stop dispatcher
   */
  public synchronized void stopDispatchingCalls() {
    setDispatcherRunning(false);
    getExecutor().shutdown();
  }

  private synchronized void setEmployeesAvailable(boolean employeesAvailable) {
    this.employeesAvailable = employeesAvailable;
  }

  /**
   * Validate the flow predefined for the {@link Dispatcher} and return an {@link Employee}
   * if any that is available to answer the call
   *
   * @return an {@link Employee} if any that is available to answer the call
   */
  private Employee findAvailableEmployeeForAnswering() {
    List<Employee> employeeList = getDequeEmployees().stream().collect(Collectors.toList());
    Validate.notNull(employeeList);

    List<Employee> available = employeeList.stream().filter(employee -> employee.getStatus()
        .equals(EmployeeStatus.WAIT_FOR_CALL)).collect(Collectors.toList());

    if (available.isEmpty()) {
      if (isEmployeesAvailable()) {
        logger.info("There are no employees available to answer calls");
        setEmployeesAvailable(false);
      }
      return null;
    } else {
      setEmployeesAvailable(true);
    }

    logger.info("Employees wait for a call: {}", available.size());
    Optional<Employee> employee = available.stream()
        .filter(operator -> operator.getRole().equals(EmployeeRole.OPERATOR)).findAny();

    if (!employee.isPresent()) {
      employee = available.stream()
          .filter(operator -> operator.getRole().equals(EmployeeRole.SUPERVISOR)).findAny();
      if (!employee.isPresent()) {
        employee = available.stream()
            .filter(supervisor -> supervisor.getRole().equals(EmployeeRole.DIRECTOR)).findAny();
      }
    }

    logger.info("Employee [{} - {}] selected for answer call", employee.get().getId(),
        employee.get().getRole());
    return employee.get();
  }
}
