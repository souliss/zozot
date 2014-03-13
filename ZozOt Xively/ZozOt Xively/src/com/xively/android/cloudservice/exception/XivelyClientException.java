package com.xively.android.cloudservice.exception;

/**
 * Top level exception for all exceptions thrown out of this library.
 * 
 * @author s0pau
 * 
 */
public class XivelyClientException extends RuntimeException
{
	public XivelyClientException(String msg)
	{
		super(msg);
	}

	public XivelyClientException(String msg, Throwable e)
	{
		super(msg, e);
	}
}
