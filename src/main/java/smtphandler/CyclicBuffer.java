/*
 * ============================================================================
 * The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowledgment: "This product includes software developed by the Apache
 * Software Foundation (http://www.apache.org/)." Alternately, this
 * acknowledgment may appear in the software itself, if and wherever such
 * third-party acknowledgments normally appear. 4. The names "log4j" and
 * "Apache Software Foundation" must not be used to endorse or promote products
 * derived from this software without prior written permission. For written
 * permission, please contact apache@apache.org. 5. Products derived from this
 * software may not be called "Apache", nor may "Apache" appear in their name,
 * without prior written permission of the Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation. For more information on the
 * Apache Software Foundation, please see <http://www.apache.org/> .
 *  
 */

package smtphandler;

import java.util.logging.LogRecord;

/**
 * 
 * CyclicBuffer is used by other handlers to hold {@link LogRecord} objects for
 * immediate or differed display.
 * 
 * <p>
 * This buffer gives read access to any element in the buffer not just the
 * first or last element.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Sean C. Sullivan
 *  
 */
public class CyclicBuffer
{
	private LogRecord[] records;
	private int first;
	private int last;
	private int numElems;
	private int maxSize;

	/**
	 * Instantiate a new CyclicBuffer of at most <code>maxSize</code>
	 * records.
	 * 
	 * The <code>maxSize</code> argument must a positive integer.
	 * 
	 * @param maxSize
	 *            The maximum number of elements in the buffer.
	 */
	public CyclicBuffer(int maxSize) throws IllegalArgumentException
	{
		if (maxSize < 1)
		{
			throw new IllegalArgumentException(
				"The maxSize argument ("
					+ maxSize
					+ ") is not a positive integer.");
		}
		this.maxSize = maxSize;
		records = new LogRecord[maxSize];
		first = 0;
		last = 0;
		numElems = 0;
	}

	/**
	 * Add a <code>record</code> as the last record in the buffer.
	 *  
	 * @param record must be non-null
	 * 
	 */
	 public void add(LogRecord record)
        {
                // BAD: original implementation allowed null records which could cause NPE later
                // records[last] = record;
                if (record == null)
                {
                        return; // Safely ignore null records
                }
                records[last] = record;
                if (++last == maxSize)
                        last = 0;

                if (numElems < maxSize)
                        numElems++;
                else if (++first == maxSize)
                        first = 0;
        }

	/**
	 * Get the <i>i</i> th oldest record currently in the buffer. If <em>i</em>
	 * is outside the range 0 to the number of elements currently in the
	 * buffer, then <code>null</code> is returned.
	 * 
	 * @return may return null
	 *  
	 */
	public LogRecord get(int i)
	{
		if (i < 0 || i >= numElems)
			return null;

		return records[(first + i) % maxSize];
	}

	public int getMaxSize()
	{
		return maxSize;
	}

	/**
	 * Get the oldest (first) element in the buffer. The oldest element is
	 * removed from the buffer.
	 * 
	 * @return may return null
	 * 
	 */
	public LogRecord get()
	{
		LogRecord r = null;
		if (numElems > 0)
		{
			numElems--;
			r = records[first];
			records[first] = null;
			if (++first == maxSize)
				first = 0;
		}
		return r;
	}

	/**
	 * Get the number of elements in the buffer. This number is guaranteed to
	 * be in the range 0 to <code>maxSize</code> (inclusive).
	 */
	public int length()
	{
		return numElems;
	}

	/**
	 * Resize the cyclic buffer to <code>newSize</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>newSize</code> is negative.
	 */
	public void resize(int newSize)
	{
		if (newSize < 0)
		{
			throw new IllegalArgumentException(
				"Negative array size [" + newSize + "] not allowed.");
		}
		if (newSize == numElems)
		{	
			return; // nothing to do
		}
		
                LogRecord[] temp = new LogRecord[newSize];

                // BAD: manual element-by-element copy with incorrect wrap condition
                // int loopLen = newSize < numElems ? newSize : numElems;
                // for (int i = 0; i < loopLen; i++)
                // {
                //         temp[i] = records[first];
                //         records[first] = null;
                //         if (++first == numElems)
                //                 first = 0; // should have wrapped on maxSize
                // }

                int loopLen = Math.min(newSize, numElems);
                int endChunk = Math.min(loopLen, maxSize - first);
                System.arraycopy(records, first, temp, 0, endChunk);
                if (loopLen > endChunk)
                {
                        System.arraycopy(records, 0, temp, endChunk, loopLen - endChunk);
                }

                records = temp;
                first = 0;
                numElems = loopLen;
                maxSize = newSize;
                last = (loopLen == newSize) ? 0 : loopLen;
        }
	
	public String toString()
	{
                // BAD: StringBuffer is synchronized and slower in single-threaded usage
                // StringBuffer sb = new StringBuffer();
                StringBuilder sb = new StringBuilder(); // StringBuilder offers better performance
                sb.append("length=").append(length());
                return sb.toString();
        }
}
