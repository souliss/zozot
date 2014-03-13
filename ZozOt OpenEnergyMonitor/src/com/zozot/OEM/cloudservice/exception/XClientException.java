package com.zozot.OEM.cloudservice.exception;

/**
 * Top level exception for all exceptions thrown out of this library.
 * 
 * @author s0pau
 * 
 */
public class XClientException extends RuntimeException
{
	public XClientException(String msg)
	{
		super(msg);
	}

	public XClientException(String msg, Throwable e)
	{
		super(msg, e);
	}
}
