package com.fss.translator.service.impl;

import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fss.translator.constants.ResponseMessages;
import com.fss.translator.constants.TranslatorConstants;
import com.fss.translator.dto.ValueDTO;
import com.fss.translator.exception.ServiceException;
import com.fss.translator.service.ISO20022TranslateService;
import com.fss.translator.service.ISO8583TranslatorService;
import com.fss.translator.service.TranslatorTargetformat;
import com.fss.translator.util.Response;
import com.fss.translator.util.ResponseBuilder;
import com.fss.translator.util.Util;

import lombok.extern.log4j.Log4j2;

/**
 * Class derived the implimentaton of response format based on mimeType and
 * configuration type.
 * 
 * @author ravinaganaboyina
 *
 */
@Log4j2
@Service
public class TranslatorTargetformatImpl implements TranslatorTargetformat {

	@Autowired
	ResponseBuilder responseBuilder;

	@Autowired
	ISO20022TranslateService iso22TranslateService;
	@Autowired
	ISO8583TranslatorService translatorJposService;

	@Override
	public Object contentTypeResponse(ValueDTO valueDto) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);

		Object object = null;
		String targetResponseFormat = "";
		if (Util.isNotNull(valueDto) && Util.isNotNull(valueDto.getRequestObject())) {
			targetResponseFormat = valueDto.getRequestObject().get(TranslatorConstants.ACCEPT) + "";
			if (Util.isEmpty(targetResponseFormat)
					|| (!TranslatorConstants.CONTENT_RESPONSE_ISJSON.equals(targetResponseFormat)
							|| !TranslatorConstants.CONTENT_RESPONSE_ISXML.equals(targetResponseFormat)))
				targetResponseFormat = valueDto.getRequestObject().get(TranslatorConstants.CONTENT_TYPE) + "";
		}
		switch (targetResponseFormat) {

		case TranslatorConstants.CONTENT_RESPONSE_ISJSON:
			object = getResponseObject(valueDto);
			break;

		case TranslatorConstants.CONTENT_RESPONSE_ISXML:
			object = xmlResponseFormat(valueDto);
			break;

		default:
			log.info("Target message format has not available on system");
			break;

		}
		log.debug(TranslatorConstants.EXIT);
		return object;
	}

	private Object xmlResponseFormat(ValueDTO valueDto) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		//Response response = null;
		String responseObj = "";

		try {

			//response = getResponseObject(valueDto);
			JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(getResponseObject(valueDto)/*response*/, sw);
			responseObj = sw.toString();

		} catch (JAXBException e) {
			log.error("error while formatting data",e.getMessage());
			//String responseCode = valueDto.getRequestObject().get(TranslatorConstants.RESPONSE_CODE) + "";
			throw new ServiceException("", valueDto.getRequestObject().get(TranslatorConstants.RESPONSE_CODE) + "");
		}
		log.debug(TranslatorConstants.EXIT);
		return responseObj;
	}

	private Response getResponseObject(ValueDTO valueDto) {
		log.debug(TranslatorConstants.ENTER);
		Response response = null;
		//String responseMessage = "";
		if (Util.isNotNull(valueDto) && Util.isNotNull(valueDto.getRequestObject())) {
			if (ResponseMessages.SUCCESS.equals(valueDto.getRequestObject().get(TranslatorConstants.RESPONSE_CODE))) {
				return responseBuilder.buildSuccessResponse(valueDto.getResponseData());
				/*responseMessage = valueDto.getResponseData();
				response = responseBuilder.buildSuccessResponse(responseMessage);*/
			} else {
				return /*response = */responseBuilder.buildFailureResponse(
						valueDto.getRequestObject().get(TranslatorConstants.RESPONSE_DATA) + "",
						valueDto.getRequestObject().get(TranslatorConstants.RESPONSE_CODE) + "");
			}
		}
		log.debug(TranslatorConstants.EXIT);
		return response;
	}

	@Override
	public void targetConverstion(ValueDTO valueDto) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);
		/*String targetFormat = null;

		targetFormat = getMessageFormat(valueDto, false);
*/
		switch (getMessageFormat(valueDto, false)/*targetFormat*/) {

		case TranslatorConstants.MESSAGEFORMAT_ISO8583:
			translatorJposService.packService(valueDto);
			break;

		case TranslatorConstants.TARGET_RESPONSE_ISO20022:
			iso22TranslateService.doMarshalling(valueDto);
			break;

		case TranslatorConstants.TARGET_RESPONSE_JSON:
			break;

		default:
			valueDto.getRequestObject().put(TranslatorConstants.RESPONSE_CODE,
					ResponseMessages.CONFIGURATION_ERR_MESSAGE);
			log.info("Target conversation not set for this instiuttion");
		}
		log.debug(TranslatorConstants.EXIT);

	}

	@Override
	public void doTranslateProcess(ValueDTO valueDto) throws ServiceException {
		log.debug(TranslatorConstants.ENTER);

		/*String sourceMessageType = getMessageFormat(valueDto, true);*/

		switch (getMessageFormat(valueDto, true)/*sourceMessageType*/) {

		case TranslatorConstants.MESSAGEFORMAT_ISO020022:
			iso22TranslateService.doUnmarshalling(valueDto);
			break;

		case TranslatorConstants.MESSAGEFORMAT_ISO8583:
			translatorJposService.unpackService(valueDto);
			break;

		default:
			log.info("sourceMessageType not configure on system");
			throw new ServiceException("", ResponseMessages.CONFIGURATION_ERR_MESSAGE);
		}

		log.debug(TranslatorConstants.EXIT);

	}

	private String getMessageFormat(ValueDTO valueDto, boolean isSource) {
		String messageFormat = "";
		if (Util.isNotNull(valueDto) && !valueDto.getRequestObject().isEmpty()) {
			messageFormat = valueDto.getRequestObject().get(TranslatorConstants.SOURCEMESSAGETYPE) + "";
			String srcappid = valueDto.getRequestObject().get(TranslatorConstants.SRCAPPID) + "";
			if (!Util.isEmpty(messageFormat)) {
				Map<String, String> instMap = valueDto.getInstituation();
				if (Util.isNotNull(instMap) && instMap.containsKey(messageFormat)) {
					instMap.put(TranslatorConstants.SOURCEMESSAGETYPE, messageFormat);
					String configInstTarget = instMap.get(messageFormat);
					instMap.put(TranslatorConstants.MAPPINGPROPERTY,
							srcappid + "_" + messageFormat + "_" + configInstTarget);
					if (isSource) {
						messageFormat = getMessageFormat(messageFormat);
					} else {
						messageFormat = getMessageFormat(configInstTarget);
						if (!Util.isEmpty(messageFormat))
							instMap.put(TranslatorConstants.TARGETRESPONSEFORMATOPT, configInstTarget);

					}
					valueDto.setInstituation(instMap);
				}
			}
		}

		return messageFormat;
	}

	private String getMessageFormat(String sourceCheck) {
		String messageFormat = "";
		if (!Util.isEmpty(sourceCheck) && (sourceCheck.startsWith("pacs") || sourceCheck.startsWith("pain"))) {
			messageFormat = TranslatorConstants.MESSAGEFORMAT_ISO020022;
		} else if (!Util.isEmpty(sourceCheck)
				&& (sourceCheck.startsWith("FPS") || sourceCheck.startsWith("93") || sourceCheck.startsWith("87"))) {
			messageFormat = TranslatorConstants.MESSAGEFORMAT_ISO8583;
		}
		return messageFormat;
	}
}
