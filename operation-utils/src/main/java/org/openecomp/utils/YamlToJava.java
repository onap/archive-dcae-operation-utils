
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;

import org.openecomp.ncomp.utils.PropertyUtil;
import org.openecomp.ncomp.webservice.utils.FileUtils;

public class YamlToJava {
	
//	package org.openecomp.operation.logging.usecases;
//
//	import org.openecomp.logger.EcompMessageEnum;
//	import com.att.eelf.i18n.EELFResourceManager;
//
//	public enum MyMessageEnum implements EcompMessageEnum {
//		// Api Handler Messages
//		FOOBAR;
//
//		static {
//			EELFResourceManager.loadMessageBundle("foobar");
//		}
//	}
	
	@SuppressWarnings("unchecked")
	static public void convert(String yamlFileName, String outputSourceRootDir, String packageName) {
		try {
			if (! (new File(yamlFileName).exists())) {
				System.err.println(yamlFileName + " does not exists");
				return;
			}
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			Yaml y = new Yaml(options);
			Map<String,Object> m = (Map<String, Object>) y.load(FileUtils.filename2stream(yamlFileName, null));
			File f = new File(yamlFileName);
			String name = f.getName().replace(".yaml", "");
			m.put("name", name);
			m.put("packageName", packageName);
			String ofile1 = outputSourceRootDir + "/" + name + ".properties";
			OutputStreamWriter w;
			SimpleTemplateEngine engine = new SimpleTemplateEngine();
			if (m.containsKey("messages")) {
				w = FileUtils.filename2writer(ofile1);
				w.append(engine.createTemplate(getTemplate("properties_template")).make(m).toString());
				w.close();
				String ofile2 = outputSourceRootDir + "/" + name + "MessageEnum.java";
				w = FileUtils.filename2writer(ofile2);
				w.append(engine.createTemplate(getTemplate("messageEnum.java_template")).make(m).toString());
				w.close();
			}
			if (m.containsKey("operations")) {
				String ofile3 = outputSourceRootDir + "/" + name + "OperationEnum.java";
				w = FileUtils.filename2writer(ofile3);
				w.append(engine.createTemplate(getTemplate("operationEnum.java_template")).make(m).toString());
				w.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String getTemplate(String res) throws IOException {
		InputStream in = YamlToJava.class.getClassLoader().getResourceAsStream(res);
		if (in == null) {
			throw new RuntimeException("Unable to find resource: " + res);
		}
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		FileUtils.copyStream(in, o);
		return o.toString();
}

	public static void main(String[] args) throws IOException {

		Properties props = new Properties();
		String pname = "GenericMessages.properties";
		String fname = "src/main/resources/GenericMessages.yaml";
		props.load(YamlToJava.class.getClassLoader().getResourceAsStream(pname));
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
