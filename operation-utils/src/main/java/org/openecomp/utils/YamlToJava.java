
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

	// package org.openecomp.operation.logging.usecases;
	//
	// import org.openecomp.logger.EcompMessageEnum;
	// import com.att.eelf.i18n.EELFResourceManager;
	//
	// public enum MyMessageEnum implements EcompMessageEnum {
	// // Api Handler Messages
	// FOOBAR;
	//
	// static {
	// EELFResourceManager.loadMessageBundle("foobar");
	// }
	// }

	static public void convert(String yamlFileName, String outputDir, String packageName) {
		convert(yamlFileName, outputDir, outputDir, packageName);
	}

	@SuppressWarnings("unchecked")
	static public void convert(String yamlFileName, String propertiesOutputDirectory, String enumOutputDirectory,
			String packageName) {
		try {
			System.out.println("Enterting YAML Convert)");
			if (!(new File(yamlFileName).exists())) {
				System.err.println(yamlFileName + " does not exists");
				return;
			}
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			Yaml y = new Yaml(options);
			Map<String, Object> m = (Map<String, Object>) y.load(FileUtils.filename2stream(yamlFileName, null));
			File f = new File(yamlFileName);
			String name = f.getName().replace(".yaml", "");
			m.put("name", name);
			m.put("packageName", packageName);
			String resourcePath = packageName.replace('.', '/');
			m.put("resourcePath", resourcePath);
			String ofile1 = propertiesOutputDirectory + "/" + name + ".properties";
			System.out.println("Properties file path => " + ofile1);
			OutputStreamWriter w;
			SimpleTemplateEngine engine = new SimpleTemplateEngine();
			if (m.containsKey("messages")) {
				w = FileUtils.filename2writer(ofile1);
				w.append(engine.createTemplate(getTemplate("properties_template")).make(m).toString());
				w.close();
				String ofile2 = enumOutputDirectory + "/" + name + "MessageEnum.java";
				System.out.println("Message Enum  file path => " + ofile2);
				w = FileUtils.filename2writer(ofile2);
				w.append(engine.createTemplate(getTemplate("messageEnum.java_template")).make(m).toString());
				w.close();
			}
			if (m.containsKey("operations")) {
				String ofile3 = enumOutputDirectory + "/" + name + "OperationEnum.java";
				w = FileUtils.filename2writer(ofile3);
				System.out.println("Operation Enum  file path => " + ofile3);
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

		for (int i = 0; i < args.length; i++) {
			System.out.println(" Argument " + Integer.toString(i) + " ==>  " + args[i]);
		}

		switch (args.length) {
		case 1:
			String baseDir = findBaseDir(args[0]);
			Map<String, Object> m = file2yaml(args[0]);
			String javaDest = (String) (m.containsKey("java-root") ? m.get("java-root") : "src/main/java-gen");
			String resourcesDest = (String) (m.containsKey("resources-root") ? m.get("resources-root")
					: "src/main/resources-gen");
			if (! javaDest.startsWith("/")) javaDest = baseDir + "/" + javaDest;
			if (! resourcesDest.startsWith("/")) resourcesDest = baseDir + "/" + resourcesDest;
			String packageName = (String) m.get("package-name");
			if (packageName == null) {
				System.err.println("No package-name attribute in: " + args[0]);
				System.exit(2);
			}
			String packageDir = "/" + packageName.replace(".", "/");
			convert(args[0], resourcesDest + packageDir, javaDest + packageDir, packageName);
			break;
		case 4:
			convert(args[0], args[1], args[2], args[3]);
			break;
		case 5:
			convert(args[1], args[2], args[3], args[4]);
			break;
		default:
			System.err.println("Invalid arguments, expected --> yamlFileName");
			System.exit(2);
		}
	}

	private static String findBaseDir(String filename) {
		File f = new File(filename);
		f = f.getParentFile();
		while (f != null) {
			File pom = new File(f,"pom.xml");
			if (pom.exists()) return f.getAbsolutePath();
			f = f.getParentFile();
		}
		return ".";
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> file2yaml(String yamlFileName) {
		if (!(new File(yamlFileName).exists())) {
			System.err.println(yamlFileName + " does not exists");
			System.exit(2);
		}
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml y = new Yaml(options);
		return (Map<String, Object>) y.load(FileUtils.filename2stream(yamlFileName, null));
	}
}
