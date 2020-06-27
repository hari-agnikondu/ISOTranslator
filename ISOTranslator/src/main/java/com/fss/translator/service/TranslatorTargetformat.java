package com.fss.translator.service;

import com.fss.translator.dto.ValueDTO;
import com.fss.translator.exception.ServiceException;

/**
 * interface is define the response format
 * 
 * @author ravinaganaboyina
 * 
 *
 */

public interface TranslatorTargetformat {

	public void doTranslateProcess(ValueDTO valyueDto) throws ServiceException;

	public void targetConverstion(ValueDTO valueDto) throws ServiceException;

	public Object contentTypeResponse(ValueDTO valueDto) throws ServiceException;

}
