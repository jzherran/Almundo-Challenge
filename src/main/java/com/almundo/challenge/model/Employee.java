package com.almundo.challenge.model;

import com.almundo.challenge.enumerate.EmployeeRole;
import com.almundo.challenge.enumerate.EmployeeStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model class for represent a employee
 */
@Data
public class Employee implements Runnable {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(Employee.class);

  /** Structure for calls completed by the employee */
  private List<Call> completed;

  /** Identifier for employee */
  private String id;

  /** Role associated to this employee */
  private EmployeeRole role;

  /** Status associated to this employee */
  private EmployeeStatus status;

  /** Structure for calls on hold by the employee */
  private ConcurrentLinkedDeque<Call> waiting;

  public Employee(String id, EmployeeRole role) {
    this.completed = new ArrayList<>();
    this.id = id;
    this.role = role;
    this.status = EmployeeStatus.WAIT_FOR_CALL;
    this.waiting = new ConcurrentLinkedDeque<>();
  }

  /**
   * Run method is always executed and is responsible for starting the process for answer calls
   * associated by the employee
   */
  @Override public void run() {
    logger.info("Employee [{} - {}] starts to work", getId(), getRole());
    do {
      if (!getWaiting().isEmpty()) {
        Call call = getWaiting().poll();
        setStatus(EmployeeStatus.IN_CALL);
        logger.info("Initiate answer call by employee [{} - {}]", getId(), getRole());
        try {
          TimeUnit.SECONDS.sleep(call.getDuration());
        } catch (InterruptedException e) {
          logger.error("Call {}, answered by employee [{} - {}] finished unexpectedly",
              call.getId(), getId(), getRole());
        } finally {
          setStatus(EmployeeStatus.WAIT_FOR_CALL);
        }
        getCompleted().add(call);
        logger.info("Finished answer for call {} by employee [{} - {}]", call.getId()
            , getId(), getRole());
      }
    } while (true);
  }

  /**
   * Returns status associated a this employee
   *
   * @return the {@link EmployeeStatus}
   */
  public synchronized EmployeeStatus getStatus() {
    return status;
  }

  /**
   * Sets a {@link EmployeeStatus} received
   *
   * @param status to set for employee
   */
  public synchronized void setStatus(EmployeeStatus status) {
    this.status = status;
  }

  /**
   * Starts the process for answer call
   *
   * @param call the call to respond
   */
  public synchronized void startRespondCall(Call call) {
    getWaiting().add(call);
    logger.info("Employee [{} - {}] receive a call of [{} - {} seconds]", getId(), getRole(),
        call.getId(), call.getDuration());
  }
}