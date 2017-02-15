
/*-
 * ============LICENSE_START==========================================
 * OPENECOMP - DCAE
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */
	

package org.openecomp.logger;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import org.slf4j.MDC;

import org.openecomp.entity.EcompOperation;
import org.openecomp.entity.EcompOperationEnum;
import org.openecomp.entity.EcompSubComponentInstance;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.configuration.SLF4jWrapper;

/**
 * Simple wrapper around the EELF Logger class for MSO usage.
 * This class supports all of the normal logging functions (debug, info, etc.),
 * prepending a string of format "[<requestId>|<serviceId]" to each message.
 *
 * MSO code should initialize with these IDs when available, so that individual
 * requests and/or services can be tracked throughout the various MSO component
 * logs (API Handler, BPEL, and Adapters).
 *
 */
public class EcompLogger {
	// MDC parameters
	public static final String REQUEST_ID = "RequestId";
	public static final String SERVICE_INSTANCE_ID = "ServiceInstanceId";
	public static final String SERVICE_NAME = "ServiceName";
	public static final String INSTANCE_UUID = "InstanceUUID";
	public static final String SERVER_IP = "ServerIPAddress";
	public static final String FQDN = "ServerFQDN";
	public static final String REMOTE_HOST = "RemoteHost";
	public static final String ALERT_SEVERITY = "AlertSeverity";
	public static final String TIMER = "Timer";
	public static final String USER = "User";
	public static final String CATALOG = "Catalog";
    public static final String DUMMY_REQUEST_VALUE = "?";
    public static final String DUMMY_SERVICE_INSTANCE_VALUE = "??";

    private static final String FATAL_LEVEL = "FATAL";
    private static final String ERROR_LEVEL = "ERROR";
    private static final String WARN_LEVEL = "WARN";
    private static final String INFO_LEVEL = "INFO";
    private static final String DEBUG_LEVEL = "DEBUG";

    private EELFLogger errorLogger, auditLogger, metricsLogger, debugLogger;
    

    // For internal logging of the initialization of MSO logs
    private static final Logger LOGGER = Logger.getLogger (EcompLogger.class.getName ());
	private static final String ERROR_LOGGER_NAME = "org.openecomp.error";
	private static final String AUDIT_LOGGER_NAME = "org.openecomp.audit";
	private static final String METRICS_LOGGER_NAME = "org.openecomp.metrics";
	private static final String DEBUG_LOGGER_NAME = "org.openecomp.debug";


    private EcompLogger () {
        this.errorLogger = new SLF4jWrapper(ERROR_LOGGER_NAME);
        this.auditLogger = new SLF4jWrapper(AUDIT_LOGGER_NAME);;
        this.metricsLogger = new SLF4jWrapper(METRICS_LOGGER_NAME);
        this.debugLogger = new SLF4jWrapper(DEBUG_LOGGER_NAME);
    }


    public static synchronized EcompLogger getEcompLogger () {
        return new EcompLogger ();
    }

    /**
     * Record the Audit start event
     *
     */
    public void recordAuditEventStart (EcompMessageEnum msg, String... args) {
    	prepareMsg (INFO_LEVEL);
    	setStart();
    	auditLogger.info (msg, args);
    }
    /**
     * Record the Audit start event
     *
     */
    public void recordAuditEventStart () {
    	recordAuditEventStart(GenericMessageEnum.AUDIT_BEGIN);
    }

    /**
     * Record the Audit end event with log message to put
     *
     * @param arg0 The log message to put
     */
    public void recordAuditEventEnd (EcompMessageEnum msg, String... args) {
     	setTimer();
    	prepareMsg (INFO_LEVEL);
    	auditLogger.info (msg, normalizeArray(args));
    }

    /**
     * Record the Audit end event with log message to put
     *
     * @param arg0 The log message to put
     */
    public void recordAuditEventEnd () {
    	auditLogger.info (GenericMessageEnum.AUDIT_END);
    }
    /**
     * Record the Metric start event without log message
     *
     */
    public void recordMetricEventStart (EcompMessageEnum msg, String... args) {
    	prepareMsg (INFO_LEVEL);
    	setStart();
    	metricsLogger.info (msg, args);
    }

    /**
     * Record the Metric end event with log message to put
     *
     * @param arg0 The log message to put
     */
    public void recordMetricEventEnd (EcompMessageEnum msg, String... args) {
    	prepareMsg (INFO_LEVEL);
    	setTimer();
    	metricsLogger.info (msg, normalizeArray(args));
    }
    /**
     * Record the Metric start event without log message
     *
     */
    public void recordMetricEventStart () {
    	recordMetricEventStart(GenericMessageEnum.METRICS_BEGIN);
    }

    /**
     * Record the Metric end event with log message to put
     *
     * @param arg0 The log message to put
     */
    public void recordMetricEventEnd () {
    	recordMetricEventEnd (GenericMessageEnum.METRICS_END);
    }

    // Debug methods
    /**
     * Record the Debug event
     *
     * @param msg The log message to put
     */
    public void debug (String msg) {
    	prepareMsg (DEBUG_LEVEL);
        errorLogger.debug (msg);
    }

    /**
     * Record the Debug event
     *
     * @param msg The log message to put
     * @param t The exception to put
     */
    public void debug (String msg, Throwable t) {
    	prepareMsg (DEBUG_LEVEL);
        errorLogger.debug (msg);
    }

    // Info methods
    /**
     * Record the Info event
     *
     * @param msg The log message to put
     */
    public void info (EcompMessageEnum msg, String... args) {
    	prepareMsg (INFO_LEVEL);
        errorLogger.info (msg,normalizeArray(args));
    }

	/**
     * Record the Info event
     *
     * @param msg The log message to put
     * @param t The exception info
     */
    public void info (EcompMessageEnum msg, Throwable t, String... args) {
    	prepareMsg (WARN_LEVEL);
        errorLogger.info (msg,normalizeArray(args));
        errorLogger.info ("Exception raised: " + getNormalizedStackTrace (t));
        errorLogger.debug ("Exception raised", t);
    }

    // Warning methods
    /**
     * Record the Warning event
     *
     * @param msg The log message to put
     */
    public void warn (EcompMessageEnum msg, String... args) {
    	prepareMsg (WARN_LEVEL);
        errorLogger.warn (msg,normalizeArray(args));
    }



	/**
     * Record the Warning event
     *
     * @param msg The log message to put
     * @param t The exception info
     */
    public void warn (EcompMessageEnum msg, Throwable t, String... args) {
    	prepareMsg (WARN_LEVEL);
        errorLogger.warn (msg,normalizeArray(args));
        errorLogger.warn ("Exception raised: " + getNormalizedStackTrace (t));
        errorLogger.debug ("Exception raised", t);
    }

    // Error methods
    /**
     * Record the Error event
     *
     * @param msg The log message to put
     */
    public void error (EcompMessageEnum msg, String... args) {
    	prepareMsg (ERROR_LEVEL);
        errorLogger.error (msg, normalizeArray(args));
    }

    /**
     * Record the Error event 
     *
     * @param msg The log message to put
     * @param t The exception info
     */
    public void error (EcompMessageEnum msg, Throwable t, String... args) {
    	prepareMsg (ERROR_LEVEL);
        errorLogger.error (msg, normalizeArray(args));
        errorLogger.error (GenericMessageEnum.GENERAL_EXCEPTION, getNormalizedStackTrace (t));
        errorLogger.debug ("Exception raised", t);
    }

    public boolean isDebugEnabled () {
    	return errorLogger.isDebugEnabled();
    }

    private void setStart() {
		MDC.put("startTime", Long.toString(new Date().getTime()));
	}


	private void setTimer() {
	   	Date d = new Date(Long.parseLong(MDC.get("startTime")));
		MDC.put(TIMER, Long.toString(new Date().getTime()-d.getTime()));
	}


	private void prepareMsg (String loggingLevel) {
    	prepareMsg (loggingLevel, null, null);
    }

    private void prepareMsg (String loggingLevel, String serviceNamep, String timer) {
        String reqId = MDC.get (REQUEST_ID);
        String svcId = MDC.get (SERVICE_INSTANCE_ID);

        if (reqId == null || reqId.isEmpty()) {
            MDC.put (REQUEST_ID, DUMMY_REQUEST_VALUE);
        }

        if (svcId == null || svcId.isEmpty()) {
            MDC.put (SERVICE_INSTANCE_ID, DUMMY_SERVICE_INSTANCE_VALUE);
        }

        if (timer != null) {
            MDC.put (TIMER, timer);
        } else {
        	MDC.remove(TIMER);
        }

        MDC.put (ALERT_SEVERITY, getSeverityLevel (loggingLevel));
//        MDC.put (INSTANCE_UUID, EcompSubComponentInstance.getUuid());
        MDC.put (SERVER_IP, EcompSubComponentInstance.getServerIP());
        MDC.put (FQDN, EcompSubComponentInstance.getServerName());
    }

    private String getSeverityLevel (String loggingLevel) {
        String severity;
        // According to the Nagios alerting: 0=OK; 1=WARNING; 2=UNKOWN; 3=CRITICAL
        switch (loggingLevel) {
            case ERROR_LEVEL:
                severity = "2";
                break;
            case FATAL_LEVEL:
                severity = "3";
                break;
            case WARN_LEVEL:
                severity = "1";
                break;
            default:
                severity = "0";
                break;
        }
        return severity;
    }


    public static void setLogContext (String reqId, String svcId) {
		MDC.put (REQUEST_ID, reqId);
		MDC.put (SERVICE_INSTANCE_ID, svcId);
	}

	public static void setLoggerParameters (String remoteIpp, String userp) {
		MDC.put (REMOTE_HOST, remoteIpp);
		MDC.put (USER, userp);
	}

    public static void setServiceName (String serviceNamep) {
        MDC.put (SERVICE_NAME, serviceNamep);
    }

	public static void resetServiceName () {
		MDC.remove (SERVICE_NAME);
	}

    public static void setLogContext (EcompOperation operation) {
        if (operation != null) {
    		MDC.put (REQUEST_ID, operation.getRequestId());
    		MDC.put (SERVICE_INSTANCE_ID, operation.getServiceInstanceId());
        }
        else {
        	MDC.put (REQUEST_ID, DUMMY_REQUEST_VALUE);
    		MDC.put (SERVICE_INSTANCE_ID, DUMMY_SERVICE_INSTANCE_VALUE);
        }
    }

    private String normalize (String input) {
        if (input == null) {
            return null;
        }
        String result = input.replace ('|', '!');
        result = result.replace ("\n", " - ");
        return result;
    }
    

    private String[] normalizeArray(String[] args) {
		for (int i = 0; i< args.length; i++) {
			args[i] = normalize(args[i]);
		}
		return args;
	}

    private String getNormalizedStackTrace (Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString().replace ('|', '!').replace ("\n", " - ");
    }


	public void setOperation(EcompOperationEnum op) {
		MDC.put (SERVICE_NAME, op.toString());
	}

	public void newRequestId() {
		String uuid = UUID.randomUUID().toString();
		MDC.put (REQUEST_ID, uuid);
	}


	public void setRequestId(String requestId) {
		MDC.put (REQUEST_ID, requestId);
	}
	
	public void setRemoteHost(String remote) {
		MDC.put (REMOTE_HOST, remote);
	}
	
	public void setInstanceId(String instance) {
		MDC.put (INSTANCE_UUID, instance);
	}

}
