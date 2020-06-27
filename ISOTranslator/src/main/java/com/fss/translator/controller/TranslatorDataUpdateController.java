package com.fss.translator.controller;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fss.translator.constants.TranslatorConstants;
import com.fss.translator.exception.ServiceException;
import com.fss.translator.resource.TranslatorResources;
import com.fss.translator.util.Util;

import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;

/**
 * Class is used for update to cache
 * 
 * @author ravinaganaboyina
 *
 */

@Log4j2
@RestController
@Api(value = "Translator Data update")
@RequestMapping(value = "/translateData")
public class TranslatorDataUpdateController {

	@Autowired
	TranslatorResources translatorResources;

	@PutMapping(value = "/updateInst/{inst}"/*, method = RequestMethod.PUT*/)
	public ResponseEntity<Object> updateInstitutionDetails(@Required @PathVariable("inst") String inst)
			throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		if (!Util.isEmpty(inst)) {
			translatorResources.setInstitutionData(inst);
		}
		log.debug(TranslatorConstants.EXIT);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping(value = "/updateReqfield"/*, method = RequestMethod.PUT*/)
	public ResponseEntity<Object> updateupdateReqfield() throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		translatorResources.setRequestFields();
		log.debug(TranslatorConstants.EXIT);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping(value = "/updateMapClasses/{name}"/*, method = RequestMethod.PUT*/)
	public ResponseEntity<Object> updateXSDmappingCls(@Required @PathVariable("name") String name)
			throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		translatorResources.setISO20022XSDmapping(name);
		log.debug(TranslatorConstants.EXIT);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping(value = "/updateMapProperty/{name}"/*, method = RequestMethod.PUT*/)
	public ResponseEntity<Object> updateMapProperty(@Required @PathVariable("name") String name)
			throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		translatorResources.setISO20022propertymapping(name);
		log.debug(TranslatorConstants.EXIT);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/download/{root}/{name}"/*, method = RequestMethod.GET*/)
	public ResponseEntity<Object> downLoadXSDKeys(@Required @PathVariable(name = "root") String root,
			@Required @PathVariable(name = "name") String name) throws ServiceException {

		String keys = "";
		if (!Util.isEmpty(name) && !Util.isEmpty(root))
			keys = translatorResources.getXSDKeys(name, root);
		byte[] isr = keys.getBytes();
		HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentLength(isr.length);
		respHeaders.setContentType(MediaType.parseMediaType(TranslatorConstants.DOWNLOAD_TYPE));
		respHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		respHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name + ".txt");
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);

	}
}
