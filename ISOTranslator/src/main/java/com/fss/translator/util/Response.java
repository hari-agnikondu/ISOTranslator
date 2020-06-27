package com.fss.translator.util;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Builder;
import lombok.Data;


/**
 * This class will return the Response
 * 
 * @author Harikrishna Agnikondu
 *
 */


@Builder
@SuppressWarnings("serial")
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data
public class Response implements Serializable {

	private String responseCode;
	private String responseMessage;
	private String responseData;


}
