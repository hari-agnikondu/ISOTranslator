package com.fss.translator.resource.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fss.translator.constants.ResponseMessages;
import com.fss.translator.constants.TranslatorConstants;
import com.fss.translator.exception.ServiceException;
import com.fss.translator.resource.TranslatorResources;
import com.fss.translator.service.impl.TranslatorCacheServiceImpl;
import com.fss.translator.util.Util;
import com.fss.translator.xsd.XSDElement;
import com.fss.translator.xsd.XSDParser;

import lombok.extern.log4j.Log4j2;

/**
 * This class handle all properties file
 * 
 * @author ravinaganaboyina
 *
 */
@Log4j2
@Service
public class TranslatorResourceImpl implements TranslatorResources {

	@Value("${tranlator.config.path}")
	String tranlatorPath;

	@Autowired
	TranslatorCacheServiceImpl translatorCacheServiceImpl;

	StringBuilder st = new StringBuilder();

	@Override
	public Map<String, String> getRequestConfigElements() throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Map<String, String> mapObj = null;
		mapObj = getPropertyMap(TranslatorConstants.REQUEST_VALIDATION_FILE);
		log.debug(TranslatorConstants.EXIT);
		return mapObj;
	}

	@Override
	public Map<String, String> getPropertyMap(String fileName) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);

		String file = fileName;
		if (!Util.isEmpty(fileName)) {
			file = getPropertypath(fileName);
		}

		Properties prop = getProperty(file);
		if (!Util.isNotNull(prop)) {
			throw new ServiceException("", ResponseMessages.GENERIC_ERR_MESSAGE);
		}

		log.debug(TranslatorConstants.EXIT);
		return prop.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
	}

	private Properties getProperty(String fileName) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Properties prop = null;

		try {
			prop = new Properties();
			if (new File(fileName).exists()) {
				InputStream inputStream = new FileInputStream(fileName);
				prop.load(inputStream);
			}
		} catch (Exception e) {
			throw new ServiceException("");
		}
		log.debug(TranslatorConstants.EXIT);

		return prop;
	}

	@Override
	public Map<String, Map<String, String>> getXSDMap() throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Map<String, Map<String, String>> xsdMap = new HashMap<>();
		Map<String, String> innerMap = getPropertyMap(TranslatorConstants.REQUEST_ISO22_MAP_FILE);
		for (Map.Entry<String, String> map : innerMap.entrySet()) {
			xsdMap.put(Util.getXSDKey(map.getKey()), xsdtoMap(Util.getXSDFileName(map.getKey()), map.getValue()));
		}
		log.debug(TranslatorConstants.EXIT);
		return xsdMap;
	}

	public Map<String, String> xsdtoMap(String fileName, String root) throws ServiceException {
		
		log.debug(TranslatorConstants.ENTER);
		String file = fileName;
		Map<String, String> map = null;
		try {
			if (!Util.isEmpty(fileName)) {
				file = getXSDpath(fileName);
			}
			XSDElement mainElement = XSDParser.parseXSD(file, root);
			StringBuilder str = new StringBuilder();
			map = new TreeMap<>();
			printData(mainElement, 0, map, str);
			map.put(root, root);
			st = new StringBuilder();
		} catch (Exception e) {
			log.error("xsdtoMap:", e);
		}
		log.debug(TranslatorConstants.EXIT);
		return map;
	}

	private void printData(XSDElement xsdElement, int level, Map<String, String> map, StringBuilder str) {
		log.debug(TranslatorConstants.ENTER);
		String subName = "";

		if (str.length() <= 0)
			st.append(xsdElement.getName());
		map.put(st.append(str).toString(), xsdElement.getType());

		if (!xsdElement.getChildren().isEmpty() && xsdElement.getChildren().size() > 0) {
			for (XSDElement child : xsdElement.getChildren()) {
				subName = child.getName();
				printData(child, level + 2, map, new StringBuilder("." + subName));
			}
		}
		int lastIndex = st.lastIndexOf(".");
		if (lastIndex != -1)
			st.delete(lastIndex, st.length());

		log.debug(TranslatorConstants.EXIT);

	}

	@Override
	public Map<String, String> getInstitutionData(String id) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Map<String, String> instMap = null;
		Map<String, Map<String, String>> detailsMap = translatorCacheServiceImpl.getInstitutionData(null);
		if (Util.isNotNull(detailsMap) && !detailsMap.isEmpty()) {
			instMap = detailsMap.get(TranslatorConstants.INST_PREFIX + id);
			if (Util.isNotNull(instMap) && CollectionUtils.isEmpty(instMap)) {
				String fileName = Util.getPropertInstFileName(id);
				instMap = getPropertyMap(fileName);
				detailsMap.put(Util.getPropertKey(fileName), instMap);
				translatorCacheServiceImpl.setInstitutionData(detailsMap);
			}
		}
		log.debug(TranslatorConstants.EXIT);
		return instMap;
	}

	@Override
	public void setInstitutionData(String id) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Map<String, Map<String, String>> detailsMap = translatorCacheServiceImpl.getInstitutionData(null);
		if (Util.isNotNull(detailsMap) && !detailsMap.isEmpty()) {
			String fileName = Util.getPropertInstFileName(id);
			Map<String, String> instMap = getPropertyMap(fileName);
			detailsMap.put(Util.getPropertKey(fileName), instMap);
			translatorCacheServiceImpl.setInstitutionData(detailsMap);
		}

		log.debug(TranslatorConstants.EXIT);

	}

	@Override
	public Map<String, String> getRequestFields() throws ServiceException {
		log.debug(TranslatorConstants.ENTER);

		Map<String, String> requestFielMap = translatorCacheServiceImpl.getRequestFields(null);
		if (Util.isNotNull(requestFielMap) && requestFielMap.isEmpty()) {
			requestFielMap = getRequestConfigElements();
			translatorCacheServiceImpl.setRequestFields(getRequestConfigElements());
		}
		log.debug(TranslatorConstants.EXIT);

		return requestFielMap;
	}

	@Override
	public void setRequestFields() throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		translatorCacheServiceImpl.setRequestFields(getRequestConfigElements());
		log.debug(TranslatorConstants.EXIT);

	}

	public void setISO20022XSDmapping(String name) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Map<String, Map<String, String>> iso2022XSDmap = translatorCacheServiceImpl.getISO20022XSDmapping(null);
		if (Util.isNotNull(iso2022XSDmap) && !iso2022XSDmap.isEmpty()) {
			Map<String, String> innerMap = getPropertyMap(TranslatorConstants.REQUEST_ISO22_MAP_FILE);
			String root = innerMap.get(name);
			iso2022XSDmap.put(name, xsdtoMap(Util.getXSDFileName(name), root));
			translatorCacheServiceImpl.setISO20022XSDmapping(iso2022XSDmap);
			log.debug(TranslatorConstants.EXIT);
		}

	}

	public Map<String, String> getISO20022XSDmapping(String name) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Map<String, String> painMap = null;
		Map<String, Map<String, String>> iso2022XSDmap = translatorCacheServiceImpl.getISO20022XSDmapping(null);
		if (Util.isNotNull(iso2022XSDmap) && !iso2022XSDmap.isEmpty()) {
			painMap = iso2022XSDmap.get(name);
			if (!Util.isNotNull(painMap)) {
				Map<String, String> innerMap = getPropertyMap(TranslatorConstants.REQUEST_ISO22_MAP_FILE);
				if (Util.isNotNull(innerMap) && !innerMap.isEmpty()) {
					String root = innerMap.get(name);
					iso2022XSDmap.put(name, xsdtoMap(Util.getXSDFileName(name), root));
				}
			}
		}
		log.debug(TranslatorConstants.EXIT);
		return painMap;
	}

	public void setISO20022propertymapping(String name) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Map<String, String> isoPropMap = null;
		Map<String, Map<String, String>> detailsMap = translatorCacheServiceImpl.getISO20022propertymapping(null);
		if (Util.isNotNull(detailsMap) && !detailsMap.isEmpty()) {
			String fileName = Util.getPropertFileName(name);
			isoPropMap = getPropertyMap(fileName);
			detailsMap.put(name, isoPropMap);
			translatorCacheServiceImpl.setISO20022propertymapping(detailsMap);
		}
		log.debug(TranslatorConstants.EXIT);

	}

	public Map<String, String> getISO20022propertymapping(String name) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		Map<String, String> isoPropMap = null;
		Map<String, Map<String, String>> detailsMap = translatorCacheServiceImpl.getISO20022propertymapping(null);
		if (Util.isNotNull(detailsMap) && !detailsMap.isEmpty()) {
			isoPropMap = detailsMap.get(name);
			if (!Util.isNotNull(isoPropMap)) {
				String fileName = Util.getPropertFileName(name);
				isoPropMap = getPropertyMap(fileName);
				detailsMap.put(name, isoPropMap);
				translatorCacheServiceImpl.setInstitutionData(detailsMap);
			}
		}
		Map<String, String> treeMap = new TreeMap<>();
		treeMap.putAll(isoPropMap);
		log.debug(TranslatorConstants.EXIT);
		return treeMap;
	}

	@Override
	public String getXSDKeys(String name, String root) throws ServiceException {

		Map<String, String> mapingkeys = xsdtoMap(Util.getXSDFileName(name), root);
		String mapString = "";
		if (Util.isNotNull(mapingkeys)) {
			mapString = mapingkeys.entrySet().parallelStream().map(map1 -> map1.getKey())
					.collect(Collectors.joining("\r\n"));
		}
		return mapString;
	}

	private String getPropertypath(String fileName) {

		return tranlatorPath + TranslatorConstants.PROPERTIES_PATH + File.separator + fileName;
	}

	private String getXSDpath(String fileName) {

		return tranlatorPath + TranslatorConstants.XSD_PATH + File.separator + fileName;
	}

}
