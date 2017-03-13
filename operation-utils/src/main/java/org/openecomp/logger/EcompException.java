
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

import com.att.eelf.i18n.EELFResourceManager;

public class EcompException extends RuntimeException {
	public EcompMessageEnum msgEnum;
	public String[] args;
	static final long serialVersionUID = -4904961953794640177L;

	protected EcompException(EcompMessageEnum msgEnum, String message) {
		super(message);
		this.msgEnum = msgEnum;
	}

	protected EcompException(EcompMessageEnum msgEnum, String message, Throwable t) {
		super(message, t);
		this.msgEnum = msgEnum;
	}

	public static EcompException create(EcompMessageEnum msgEnum, String... args) {
		EcompException e = new EcompException(msgEnum, EELFResourceManager.format(msgEnum, args));
		e.args = args;
		return e;
	}

	public static EcompException create(EcompMessageEnum msgEnum, Throwable t, String... args) {
		EcompException e =  new EcompException(msgEnum, EELFResourceManager.format(msgEnum, args), t);
		e.args = args;
		return e;
	}

}
