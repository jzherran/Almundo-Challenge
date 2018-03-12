package com.almundo.challenge;

import com.almundo.challenge.enumerate.EmployeeRole;
import com.almundo.challenge.enumerate.EmployeeStatus;
import com.almundo.challenge.model.Call;
import com.almundo.challenge.model.Employee;
import java.util.ArrayList;
import java.util.Collections;
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
  private List<Employee> employeesList;

  /** Control for enable/disable the dispatch of calls */
  private boolean dispatcherRunning;

  /** Control for show log when there are no employees available */
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
    this.dispatcherRunning = false;
    this.employeesAvailable = true;
    this.employeesList = Collections.synchronizedList(employees);
    this.executor = Executors.newFixedThreadPool(CAPACITY_CALLS);
  }

  /**
   * Runs dispatcher only if it was start before
   */
  @Override public void run() {
    logger.info("Run dispatcher with {} employees", getEmployeesList().size());
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
   * Starts dispatcher
   */
  public synchronized void startDispatchingCalls() {
    setDispatcherRunning(true);
    getEmployeesList().forEach(employee -> getExecutor().execute(employee));
  }

  /**
   * Stops dispatcher
   */
  public synchronized void stopDispatchingCalls() {
    setDispatcherRunning(false);
    getExecutor().shutdown();
  }

  /**
   * Sets value for employees available if exist in the list any employee with status WAIT_FOR_CALL
   *
   * @param employeesAvailable the boolean value
   */
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
    Validate.notNull(getEmployeesList());

    List<Employee> employeesAvailable = getEmployeesList().stream()
        .filter(employee -> employee.getStatus()
        .equals(EmployeeStatus.WAIT_FOR_CALL)).collect(Collectors.toList());

    if (employeesAvailable.isEmpty()) {
      if (isEmployeesAvailable()) {
        logger.info("There are no employees available to answer calls");
        setEmployeesAvailable(false);
      }
      return null;
    } else {
      setEmployeesAvailable(true);
    }

    logger.info("Employees wait for a call: {}", employeesAvailable.size());
    Optional<Employee> employee = employeesAvailable.stream()
        .filter(operator -> operator.getRole().equals(EmployeeRole.OPERATOR)).findAny();

    if (!employee.isPresent()) {
      employee = employeesAvailable.stream()
          .filter(operator -> operator.getRole().equals(EmployeeRole.SUPERVISOR)).findAny();
      if (!employee.isPresent()) {
        employee = employeesAvailable.stream()
            .filter(supervisor -> supervisor.getRole().equals(EmployeeRole.DIRECTOR)).findAny();
      }
    }

    logger.info("Employee [{} - {}] selected for answer call", employee.get().getId(),
        employee.get().getRole());
    return employee.get();
  }
}
