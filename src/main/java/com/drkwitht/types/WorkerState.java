package com.drkwitht.types;

/**
 * WorkerState.java
 * @author Derek Tan (DrkWithT at GitHub)
 */

/**
 * @apiNote Defines states for the ServerWorker FSM. This ensures more organization in the control flow logic.
 */
public enum WorkerState {
    IDLE,
    START,              // process HTTP/1.1 heading line if valid
    REQUEST,
    RESPOND,            // process request by method and relative URL before sending response
    ERROR_REQUEST,      // handle req. syntax error (400)
    ERROR_NOT_FOUND,    // handle resource match error (404)
    ERROR_GENERIC,      // handle generic error (500)
    ERROR_UNSUPPORTED,  // handle an "unusable" method (501)
    STOP        // stop handling requests and close connection
}
