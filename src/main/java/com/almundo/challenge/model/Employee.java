package com.almundo.challenge.model;

import com.almundo.challenge.enumerate.EmployeeRole;
import com.almundo.challenge.enumerate.EmployeeStatus;
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

  /** */
  private static final Logger logger = LoggerFactory.getLogger(Employee.class);

  /** */
  private ConcurrentLinkedDeque<Call> completed;

  /** */
  private String id;

  /** */
  private EmployeeRole role;

  /** */
  private EmployeeStatus status;

  /** */
  private ConcurrentLinkedDeque<Call> waiting;

  public Employee(String id, EmployeeRole role) {
    this.completed = new ConcurrentLinkedDeque<>();
    this.id = id;
    this.role = role;
    this.status = EmployeeStatus.WAIT_FOR_CALL;
    this.waiting = new ConcurrentLinkedDeque<>();
  }

  /**
   *
   */
  @Override public void run() {
    logger.info("Employee [{} - {}] starts to work", getId(), getRole());
    do {
      if (!waiting.isEmpty()) {
        Call call = waiting.poll();
        setStatus(EmployeeStatus.IN_CALL);
        logger.info("Start attempt call by employee [{} - {}]", getId(), getRole());
        try {
          TimeUnit.SECONDS.sleep(call.getDuration());
        } catch (InterruptedException e) {
          logger.error("Call {}, answered by employee [{} - {}] finished unexpectedly",
              call.getId(), getId(), getRole());
        } finally {
          setStatus(EmployeeStatus.WAIT_FOR_CALL);
        }
        completed.add(call);
        logger.info("Finished answer for call {} by employee [{} - {}]", call.getId()
            , getId(), getRole());
      }
    } while (true);
  }

  /**
   *
   * @return
   */
  public synchronized EmployeeStatus getStatus() {
    return status;
  }

  /**
   *
   * @param status
   */
  public synchronized void setStatus(EmployeeStatus status) {
    this.status = status;
  }

  /**
   *
   * @param call
   */
  public synchronized void startAnswerCall(Call call) {
    waiting.add(call);
    logger.info("Employee [{} - {}] queues a call of {} seconds", getId(), getRole(),
        call.getDuration());
  }
}