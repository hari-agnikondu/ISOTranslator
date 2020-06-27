package com.fss.translator.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;

import com.fss.translator.constants.TranslatorConstants;
import com.fss.translator.exception.ServiceException;
import com.fss.translator.resource.TranslatorResources;
import com.fss.translator.util.Util;

import lombok.extern.log4j.Log4j2;

/**
 * This class used for cache for each entities
 * 
 * @author ravinaganaboyina
 *
 */
@Log4j2
@Configuration
public class TranslatorCacheConfig {

	@Autowired
	CacheManager cacheManager;

	@Autowired
	TranslatorResources translatorResources;

	@Value("${tranlator.config.path}")
	String tranlatorPath;

	public static final String TRANSLATOR_DATA_CACHE = "translateDataCache";

	public static final String TRANSALATOR_CACHE_INSTITUTIONS = "translator_institutions";

	public static final String TRANSALATOR_CACHE_REQUESTFIEDS = "translator_requestfieds";

	public static final String TRANSALATOR_CACHE_ISO20022PROPERTYMAPPING = "iso20022property_mapping";

	public static final String TRANSALATOR_CACHE_ISO20022XSDMAPPING = "iso20022XSD_mapping";

	@PostConstruct
	public void loadTranlatorsInstitutionsToLocalCache() {
		log.debug(TranslatorConstants.ENTER);
		cacheManager.getCache(TranslatorConstants.TRANSLATOR_DATA_CACHE).put(TRANSALATOR_CACHE_INSTITUTIONS,
				getFileMap(false));

		log.debug(TranslatorConstants.EXIT);
	}

	@PostConstruct
	public void loadRequestValidationToLocalCache() throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		cacheManager.getCache(TranslatorConstants.TRANSLATOR_DATA_CACHE).put(TRANSALATOR_CACHE_REQUESTFIEDS,
				translatorResources.getRequestConfigElements());

		log.debug(TranslatorConstants.EXIT);
	}

	@PostConstruct
	public void loadMappingPropertiesToLocalCach() {
		log.debug(TranslatorConstants.ENTER);
		cacheManager.getCache(TranslatorConstants.TRANSLATOR_DATA_CACHE).put(TRANSALATOR_CACHE_ISO20022PROPERTYMAPPING,
				getFileMap(true));
		log.debug(TranslatorConstants.EXIT);
	}

	@PostConstruct
	public void loadMappingXSDToLocalCache() throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		cacheManager.getCache(TranslatorConstants.TRANSLATOR_DATA_CACHE).put(TRANSALATOR_CACHE_ISO20022XSDMAPPING,
				translatorResources.getXSDMap());
		log.debug(TranslatorConstants.EXIT);
	}

	private Map<String, Map<String, String>> getFileMap(boolean isRequire) {
		log.debug(TranslatorConstants.ENTER);
		Map<String, Map<String, String>> setMap = new HashMap<>();
		File folder = new File(tranlatorPath + TranslatorConstants.PROPERTIES_PATH + File.separator);
		File[] listOfFiles = folder.listFiles();
		try {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					String fileName = file.getName();
					if (!isRequire && fileName.startsWith(TranslatorConstants.INST_PREFIX)
							&& fileName.endsWith(TranslatorConstants.PEROPERTY_SUFFIX)) {
						Map<String, String> getInstMap;
						getInstMap = translatorResources.getPropertyMap(fileName);
						setMap.put(Util.getPropertKey(fileName), getInstMap);
					} else if (isRequire && !fileName.startsWith(TranslatorConstants.INST_PREFIX)
							&& fileName.endsWith(TranslatorConstants.PEROPERTY_SUFFIX) && isNot(fileName)) {
						setMap.put(Util.getPropertKey(fileName), translatorResources.getPropertyMap(fileName));
					}
				}

			}

		} catch (ServiceException e) {
			log.error("Institution configuration not loaded into property file");
		}
		log.debug(TranslatorConstants.ENTER);
		return setMap;
	}

	private boolean isNot(String fileName) {
		boolean flag = false;
		if (!TranslatorConstants.REQUEST_VALIDATION_FILE.equalsIgnoreCase(fileName)
				&& !TranslatorConstants.REQUEST_ISO22_MAP_FILE.endsWith(fileName))
			flag = true;
		return flag;
	}

}