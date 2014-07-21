package com.zozot.OEM.cloudservice.exception;


public class ParseResponseException extends XClientException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParseResponseException(String msg, Throwable e)
	{
		super(msg, e);
	}
}
