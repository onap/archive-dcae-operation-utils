
package org.openecomp.logger;

import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.logger.EcompMessageEnum;

public enum GenericMessagesMessageEnum implements EcompMessageEnum {

  ECOMP_GENERAL_EXCEPTION,
  ECOMP_GENERAL_ERROR,
  ECOMP_GENERAL_INFO,
  ECOMP_GENERAL_WARNING,
  ECOMP_REMOTE_CALL_OK,
  ECOMP_REQUEST_OK,
  ECOMP_REMOTE_CALL_ERROR,
  ECOMP_REQUEST_ERROR,
  ECOMP_MISSING_REQUESTID,
  ECOMP_LOGGER_NON_EMPTY_STACK,
  ECOMP_LOGGER_POP_ON_EMPTY_STACK,
  ECOMP_LOGGER_TOP_ON_EMPTY_STACK;

	static {
		EELFResourceManager.loadMessageBundle("org/openecomp/logger/GenericMessages");
	}
}
