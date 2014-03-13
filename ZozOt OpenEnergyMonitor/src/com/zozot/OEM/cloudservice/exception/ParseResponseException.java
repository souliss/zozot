package com.zozot.OEM.cloudservice.exception;


public class ParseResponseException extends XClientException
{
	public ParseResponseException(String msg, Throwable e)
	{
		super(msg, e);
	}
}
