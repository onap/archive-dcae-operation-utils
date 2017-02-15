
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
	
package org.openecomp.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import org.openecomp.ncomp.webservice.utils.FileUtils;

public class ConvertMessagePropertiesToYaml {

	public static void main(String[] args) throws IOException {

		Properties props = new Properties();
		String pname = "GenericMessages.properties";
		String fname = "src/main/resources/GenericMessages.yaml";
		props.load(ConvertMessagePropertiesToYaml.class.getClassLoader().getResourceAsStream(pname));
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml y = new Yaml(options);
		JSONObject j = new JSONObject();
		for (Object k : props.keySet()) {
			String a[] = props.getProperty((String) k).split("\\|");
			JSONObject j1 = new JSONObject();
			j.put((String) k, j1);
			j1.put("errorCode", a[0]);
			j1.put("messageFormat", a[1]);
			j1.put("resolution", a[2]);
			j1.put("description", a[3].trim());
		}

		Object data = y.load(j.toString());
		OutputStreamWriter w = FileUtils.filename2writer(fname);
		w.append(y.dump(data) + "\n");
		w.close();
	}
}
