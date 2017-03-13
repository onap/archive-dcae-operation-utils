
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

import javax.servlet.http.HttpServletRequest;

import com.att.eelf.i18n.EELFResourceManager;

import org.slf4j.MDC;

import org.openecomp.entity.EcompOperation;
import org.openecomp.entity.EcompOperationEnum;
import org.openecomp.entity.EcompSubComponentInstance;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.SLF4jWrapper;

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

	public static final String THREAD_ID = "thread";
	public static final String PARTNER_NAME = "PartnerName";
	public static final String TARGET_ENTITY = "TargetEntity";
	public static final String TARGET_SERVICE_NAME = "TargetServiceName";
	public static final String STATUS_CODE = "StatusCode";
	public static final String RESPONSE_CODE = "ResponseCode";
	public static final String RESPONSE_DESCRIPTION = "ResponseDescription";
	public static final String CLASS_NAME = "ClassName";

	private static final String FATAL_LEVEL = "FATAL";
	private static final String ERROR_LEVEL = "ERROR";
	private static final String WARN_LEVEL = "WARN";
	private static final String INFO_LEVEL = "INFO";
	private static final String DEBUG_LEVEL = "DEBUG";

	private static final String CustomField1 = "CustomField1";
	private static final String CustomField2 = "CustomField2";
	private static final String CustomField3 = "CustomField3";
	private static final String CustomField4 = "CustomField4";

	private EELFLogger errorLogger, auditLogger, metricsLogger, debugLogger;

	private static final String ERROR_LOGGER_NAME = "org.openecomp.error";
	private static final String AUDIT_LOGGER_NAME = "org.openecomp.audit";
	private static final String METRICS_LOGGER_NAME = "org.openecomp.metrics";
	private static final String DEBUG_LOGGER_NAME = "org.openecomp.debug";

	protected EcompLogger() {
		this.errorLogger = new SLF4jWrapper(ERROR_LOGGER_NAME);
		this.auditLogger = new SLF4jWrapper(AUDIT_LOGGER_NAME);
		this.metricsLogger = new SLF4jWrapper(METRICS_LOGGER_NAME);
		this.debugLogger = new SLF4jWrapper(DEBUG_LOGGER_NAME);
	}

	public static synchronized EcompLogger getEcompLogger() {
		return new EcompLogger();
	}

	// region Error Logging region

	private void clearMetricFields() {
		MDC.remove(RESPONSE_CODE);
		MDC.remove(RESPONSE_DESCRIPTION);
		MDC.remove(TARGET_ENTITY);
		MDC.remove(TARGET_SERVICE_NAME);
		MDC.remove(STATUS_CODE);
		MDC.remove(CLASS_NAME);
		MDC.remove(CustomField1);
		MDC.remove(CustomField2);
		MDC.remove(CustomField3);
		MDC.remove(CustomField4);
	}

	private void clearAuditFields() {
		clearMetricFields();
		MDC.remove(SERVICE_NAME);
		MDC.remove(PARTNER_NAME);
		MDC.remove(REMOTE_HOST);
		MDC.remove(INSTANCE_UUID);
		MDC.remove(SERVICE_INSTANCE_ID);
		MDC.remove(SERVICE_NAME);
		MDC.remove(SERVICE_NAME);
	}

	private void prepareMsgEELFv1(String threadID, String partnerName, String targetEntity, String targetServiceName,
			StatusCodeEnum statusCode, String[] customFields) {
		if (!isNullOrEmpty(threadID))
			MDC.put(THREAD_ID, threadID);
		if (!isNullOrEmpty(partnerName))
			MDC.put(PARTNER_NAME, partnerName);
		if (!isNullOrEmpty(targetEntity))
			MDC.put(TARGET_ENTITY, targetEntity);
		if (!isNullOrEmpty(targetServiceName))
			MDC.put(TARGET_SERVICE_NAME, targetServiceName);
		if (statusCode != null)
			MDC.put(STATUS_CODE, statusCode.toString());

		// Backfill unknown values
		if (isNullOrEmpty(MDC.get(THREAD_ID)))
			MDC.put(THREAD_ID, "UNKNOWN");
		if (isNullOrEmpty(MDC.get(PARTNER_NAME)))
			MDC.put(PARTNER_NAME, "UNKNOWN");
		if (isNullOrEmpty(MDC.get(TARGET_ENTITY)))
			MDC.put(TARGET_ENTITY, "UNKNOWN");
		if (isNullOrEmpty(MDC.get(TARGET_SERVICE_NAME)))
			MDC.put(TARGET_SERVICE_NAME, "UNKNOWN");
		if (isNullOrEmpty(MDC.get(STATUS_CODE)))
			MDC.put(STATUS_CODE, "UNKNOWN");

		if (customFields != null) {
			for (int i = 0; i < customFields.length; i++) {
				MDC.put("CustomField" + Integer.toString(i + 1), customFields[i]);
				if (i == 3)
					break;
			}
		}
	}

	// region Error Logging region

	public void error(String threadID, String partnerName, LogCategoryEnum logCategoryEnum, String[] customFields,
			EcompMessageEnum msg, String... args) {
		prepareMsg(logCategoryEnum, msg);
		prepareMsgEELFv1(threadID, partnerName, null, null, null, customFields);
		errorLogger.error(msg, normalizeArray(args));
		clearMetricFields();
	}

	public void error(LogCategoryEnum logCategoryEnum, EcompMessageEnum msg, String... args) {
		setClassName();
		prepareMsg(logCategoryEnum, msg);
		errorLogger.error(msg, normalizeArray(args));
	}

	/**
	 * Record the Error event
	 *
	 * @param msg
	 *            The log message to put
	 */
	public void error(EcompMessageEnum msg, String... args) {
		setClassName();
		prepareMsg(ERROR_LEVEL, msg);
		errorLogger.error(msg, normalizeArray(args));
	}

	/**
	 * Record the Error event
	 *
	 * @param msg
	 *            The log message to put
	 * @param t
	 *            The exception info
	 */
	public void error(EcompMessageEnum msg, Throwable t, String... args) {
		setClassName();
		prepareMsg(ERROR_LEVEL, msg);
		errorLogger.error(msg, normalizeArray(args));
		errorLogger.error(msg, getNormalizedStackTrace(t));
		errorLogger.debug("Exception raised", t);
	}

	/**
	 * Record the Info event
	 *
	 * @param msg
	 *            The log message to put
	 */
	public void info(EcompMessageEnum msg, String... args) {
		setClassName();
		prepareMsg(INFO_LEVEL, msg);
		debugLogger.info(msg, normalizeArray(args));
	}

	/**
	 * Record the Info event
	 *
	 * @param msg
	 *            The log message to put
	 * @param t
	 *            The exception info
	 */
	public void info(EcompMessageEnum msg, Throwable t, String... args) {
		setClassName();
		prepareMsg(WARN_LEVEL, msg);
		debugLogger.info(msg, normalizeArray(args));
		debugLogger.info("Exception raised: " + getNormalizedStackTrace(t));
		debugLogger.debug("Exception raised", t);
	}

	public void info(String msg) {
		setClassName();
		prepareMsg(INFO_LEVEL);
		debugLogger.debug(msg);
	}

	/**
	 * Record the Warning event
	 *
	 * @param msg
	 *            The log message to put
	 */
	public void warn(EcompMessageEnum msg, String... args) {
		setClassName();
		prepareMsg(WARN_LEVEL, msg);
		errorLogger.warn(msg, normalizeArray(args));
	}

	/**
	 * Record the Warning event
	 *
	 * @param msg
	 *            The log message to put
	 * @param t
	 *            The exception info
	 */
	public void warn(EcompMessageEnum msg, Throwable t, String... args) {
		setClassName();
		prepareMsg(WARN_LEVEL, msg);
		errorLogger.warn(msg, normalizeArray(args));
		errorLogger.warn("Exception raised: " + getNormalizedStackTrace(t));
		errorLogger.debug("Exception raised", t);
	}

	/**
	 * Record the Debug event
	 *
	 * @param msg
	 *            The log message to put
	 */
	public void debug(String msg) {
		setClassName();
		prepareMsg(DEBUG_LEVEL);
		debugLogger.debug(msg);
	}

	/**
	 * Record the Debug event
	 *
	 * @param msg
	 *            The log message to put
	 * @param t
	 *            The exception to put
	 */
	public void debug(String msg, Throwable t) {
		setClassName();
		prepareMsg(DEBUG_LEVEL);
		debugLogger.debug(msg, t);
	}

	public void debug(EcompMessageEnum msg, String... args) {
		setClassName();
		prepareMsg(DEBUG_LEVEL, msg);
		debugLogger.info(msg, normalizeArray(args));
	}

	public void debug(Throwable t, EcompMessageEnum msg, String... args) {
		setClassName();
		prepareMsg(DEBUG_LEVEL, msg);
		debugLogger.info(msg, t, normalizeArray(args));
	}

	// endregion Error Logging region

	// region Audit Logging section

	/**
	 * Record the Audit start event
	 */
	public void recordAuditEventStart() {
		Stopwatch.clearAndStart();
		// recordAuditEventStart(GenericMessageEnum.AUDIT_BEGIN);
	}

	public void recordAuditEventStart(String targetEntity, String targetServiceName) {
		Stopwatch.clearAndStart();
		if (!isNullOrEmpty(targetEntity))
			MDC.put(TARGET_ENTITY, targetEntity);
		if (!isNullOrEmpty(targetServiceName))
			MDC.put(TARGET_SERVICE_NAME, targetServiceName);
	}

	/**
	 * Record the Audit start event
	 */
	public void recordAuditEventStart(EcompMessageEnum msg, String... args) {
		Stopwatch.clearAndStart();
		/*
		 * prepareMsg (INFO_LEVEL); setStart(); auditLogger.info (msg, args);
		 */
	}

	protected void recordAuditEventEnd(StatusCodeEnum statusCode, LogCategoryEnum logCategoryEnum,
			String[] customFields, EcompMessageEnum msg, String... args) {
		Stopwatch.stopAndPop();
		if (!Stopwatch.emptyStack()) {
			warn(GenericMessagesMessageEnum.ECOMP_LOGGER_NON_EMPTY_STACK);
		}
		setClassName();
		prepareMsg(logCategoryEnum, msg);
		prepareMsgEELFv1(null, null, null, null, statusCode, customFields);
		auditLogger.info(msg, normalizeArray(args));
		clearAuditFields();
	}

	public void setClassName() {
		StackTraceElement[] l = Thread.currentThread().getStackTrace();
		for (StackTraceElement x : l) {
			if (x.getClassName().equals("java.lang.Thread"))
				continue;
			if (x.getClassName().equals("org.openecomp.logger.EcompLogger"))
				continue;
			if (x.getClassName().equals("org.openecomp.logger.Stopwatch"))
				continue;
			MDC.put(CLASS_NAME, x.getClassName() + "@" + x.getMethodName() + ":" + x.getLineNumber());
			return;
		}
	}

	/**
	 * Record the Audit end event with log message to put
	 */
	public void recordAuditEventEnd() {
		recordAuditEventEnd(StatusCodeEnum.COMPLETE, LogCategoryEnum.INFO, null,
				GenericMessagesMessageEnum.ECOMP_REQUEST_OK);
	}

	public void recordAuditEventEnd(StatusCodeEnum code) {
		if (code == StatusCodeEnum.COMPLETE) {
			EcompMessageEnum msg = GenericMessagesMessageEnum.ECOMP_REQUEST_OK;
			recordAuditEventEnd(code, msg2cat(msg), null, msg);
		} else {
			EcompMessageEnum msg = GenericMessagesMessageEnum.ECOMP_REQUEST_ERROR;
			recordAuditEventEnd(code, msg2cat(msg), null, msg, "unknown error");
		}
	}

	/**
	 * Record the Audit end event with log message to put
	 */
	public void recordAuditEventEnd(EcompMessageEnum msg, String... args) {
		recordAuditEventEnd(StatusCodeEnum.COMPLETE, LogCategoryEnum.INFO, null, msg, args);
	}

	// endregion

	// region Metrics Logging region

	public void recordAuditEventEnd(StatusCodeEnum code, EcompMessageEnum msg, String... args) {
		recordAuditEventEnd(code, msg2cat(msg), null, msg, args);
	}

	/**
	 * Record the Metric start event without log message
	 */
	public void recordMetricEventStart() {
		Stopwatch.pushAndStart(null, null);
		// recordMetricEventStart(GenericMessageEnum.METRICS_BEGIN);
	}

	public void recordMetricEventStart(String targetEntity, String targetServiceName) {
		Stopwatch.pushAndStart(targetEntity, targetServiceName);
	}

	/**
	 * Record the Metric start event without log message
	 */
	public void recordMetricEventStart(EcompMessageEnum msg, String... args) {
		Stopwatch.pushAndStart(msg.toString(), null);
	}

	public void recordMetricEventStart(EcompOperationEnum op, String target) {
		Stopwatch.pushAndStart(op.toString(), target);
	}

	protected void recordMetricEventEnd(StatusCodeEnum statusCode, LogCategoryEnum logCategoryEnum,
			String[] customFields, EcompMessageEnum msg, String... args) {
		Stopwatch.stopAndPop();
		setClassName();
		prepareMsg(logCategoryEnum, msg);
		prepareMsgEELFv1(null, null, null, null, statusCode, customFields);
		metricsLogger.info(msg, normalizeArray(args));
		clearMetricFields();
	}

	/**
	 * Record the Metric end event with log message to put
	 */
	public void recordMetricEventEnd(EcompMessageEnum msg, String... args) {
		recordMetricEventEnd(StatusCodeEnum.COMPLETE, msg2cat(msg), null, msg, args);
	}

	/**
	 * Record the Metric end event with log message to put
	 */
	public void recordMetricEventEnd() {
		recordMetricEventEnd(GenericMessagesMessageEnum.ECOMP_REMOTE_CALL_OK);
	}

	public void recordMetricEventEnd(StatusCodeEnum code) {
		if (code == StatusCodeEnum.COMPLETE) {
			EcompMessageEnum msg = GenericMessagesMessageEnum.ECOMP_REMOTE_CALL_OK;
			recordMetricEventEnd(code, msg2cat(msg), null, msg);
		} else {
			EcompMessageEnum msg = GenericMessagesMessageEnum.ECOMP_REMOTE_CALL_ERROR;
			recordMetricEventEnd(code, msg2cat(msg), null, msg, "unknown error");
		}
	}

	public void recordMetricEventEnd(StatusCodeEnum code, EcompMessageEnum msg, String... args) {
		recordMetricEventEnd(code, msg2cat(msg), null, msg, args);
	}

	// endregion

	// region Setters Public

	public static void setLogContext(String reqId, String svcId) {
		MDC.put(REQUEST_ID, reqId);
		MDC.put(SERVICE_INSTANCE_ID, svcId);
	}

	public static void setLogContext(EcompOperationEnum op, String serviceInstanceId, String requestId,
			String partnerName) {
		MDC.put(SERVICE_NAME, op.toString());
		MDC.put(SERVICE_INSTANCE_ID, serviceInstanceId);
		MDC.put(REQUEST_ID, requestId);
		MDC.put(THREAD_ID, Long.toString(Thread.currentThread().getId()));
		MDC.put(PARTNER_NAME, partnerName);
	}

	public static void setLoggerParameters(String remoteIpp, String userp) {
		MDC.put(REMOTE_HOST, remoteIpp);
		MDC.put(USER, userp);
	}

	public static void setServiceName(String serviceNamep) {
		MDC.put(SERVICE_NAME, serviceNamep);
	}

	public void setPartnerName(String name) {
		MDC.put(PARTNER_NAME, name);
	}

	public static void setLogContext(EcompOperation operation) {
		if (operation != null) {
			MDC.put(REQUEST_ID, operation.getRequestId());
			MDC.put(SERVICE_INSTANCE_ID, operation.getServiceInstanceId());
		} else {
			MDC.put(REQUEST_ID, DUMMY_REQUEST_VALUE);
			MDC.put(SERVICE_INSTANCE_ID, DUMMY_SERVICE_INSTANCE_VALUE);
		}
	}

	public void setOperation(EcompOperationEnum op) {
		MDC.put(SERVICE_NAME, op.toString());
	}

	public void setRequestId(String requestId) {
		MDC.put(REQUEST_ID, requestId);
	}

	// endregion

	// region Setters Private

	private LogCategoryEnum msg2cat(EcompMessageEnum msg) {
		String s = EELFResourceManager.getIdentifier(msg);
		char c = 'W';
		if (s != null && s.length() > 0)
			c = s.toUpperCase().charAt(0);
		switch (c) {
		case 'I':
			return LogCategoryEnum.INFO;
		case 'W':
			return LogCategoryEnum.WARN;
		case 'E':
			return LogCategoryEnum.ERROR;
		case 'F':
			return LogCategoryEnum.FATAL;
		}
		return LogCategoryEnum.WARN;
	}

	@SuppressWarnings("unused")
	private void setStart() {
		MDC.put("startTime", Long.toString(new Date().getTime()));
	}

	@SuppressWarnings("unused")
	private void setTimer() {
		Date d = new Date(Long.parseLong(MDC.get("startTime")));
		MDC.put(TIMER, Long.toString(new Date().getTime() - d.getTime()));
	}

	// endregion

	// region Helpers

	private void prepareMsg(LogCategoryEnum logCategoryEnum, EcompMessageEnum msg) {
		prepareMsg(getSeverityLevel(logCategoryEnum), null, null, msg);
	}

	private void prepareMsg(String loggingLevel, EcompMessageEnum msg) {
		prepareMsg(loggingLevel, null, null, msg);
	}

	private void prepareMsg(String loggingLevel, String serviceNamep, String timer, EcompMessageEnum msg) {
		String reqId = MDC.get(REQUEST_ID);
		String svcId = MDC.get(SERVICE_INSTANCE_ID);

		if (reqId == null || reqId.isEmpty()) {
			MDC.put(REQUEST_ID, UUID.randomUUID().toString());
		}

		if (svcId == null || svcId.isEmpty()) {
			MDC.put(SERVICE_INSTANCE_ID, DUMMY_SERVICE_INSTANCE_VALUE);
		}

		if (timer != null) {
			MDC.put(TIMER, timer);
		} else {
			MDC.remove(TIMER);
		}

		String instance = MDC.get(INSTANCE_UUID);
		if (instance == null || instance.isEmpty()) {
			MDC.put(INSTANCE_UUID, EcompSubComponentInstance.getUuid());
		}

		MDC.put(ALERT_SEVERITY, getSeverityLevel(loggingLevel));
		MDC.put(SERVER_IP, EcompSubComponentInstance.getServerIP());
		MDC.put(FQDN, EcompSubComponentInstance.getServerName());

		if (null != msg) {
			MDC.put(RESPONSE_CODE, EELFResourceManager.getIdentifier(msg));
			MDC.put(RESPONSE_DESCRIPTION, EELFResourceManager.getDescription(msg));
		}
	}

	private void prepareMsg(String loggingLevel) {
		prepareMsg(loggingLevel, null, null, null);
	}

	private String getSeverityLevel(String loggingLevel) {
		String severity;
		// According to the Nagios alerting: 0=OK; 1=WARNING; 2=UNKOWN;
		// 3=CRITICAL
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

	private String getSeverityLevel(LogCategoryEnum logCategoryEnum) {
		String severity;
		// According to the Nagios alerting: 0=OK; 1=WARNING; 2=UNKOWN;
		// 3=CRITICAL
		switch (logCategoryEnum) {
		case FATAL:
			severity = "2";
			break;
		case ERROR:
			severity = "3";
			break;
		case WARN:
			severity = "1";
			break;
		default:
			severity = "0";
			break;
		}
		return severity;
	}

	private String normalize(String input) {
		if (input == null) {
			return null;
		}
		String result = input.replace('|', '!');
		result = result.replace("\n", " - ");
		return result;
	}

	private String[] normalizeArray(String[] args) {
		for (int i = 0; i < args.length; i++) {
			args[i] = normalize(args[i]);
		}
		return args;
	}

	private String getNormalizedStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString().replace('|', '!').replace("\n", " - ");
	}

	// endregion

	public void newRequestId() {
		String uuid = UUID.randomUUID().toString();
		MDC.put(REQUEST_ID, uuid);
	}

	public boolean isDebugEnabled() {
		return errorLogger.isDebugEnabled();
	}

	public void setRemoteHost(String remote) {
		MDC.put(REMOTE_HOST, remote);
	}

	public void setInstanceId(String instance) {
		MDC.put(INSTANCE_UUID, instance);
	}

	static boolean isNullOrEmpty(String value) {
		return (value == null || value.isEmpty()) ? true : false;
	}

	public void missingRequestId(HttpServletRequest request) {
		warn(GenericMessagesMessageEnum.ECOMP_MISSING_REQUESTID, request.getRemoteHost());
	}

	public String getRequestId() {
		return MDC.get(REQUEST_ID);
	}

	@Override
	public String toString() {
		return MDC.getCopyOfContextMap().toString();
	}
}
