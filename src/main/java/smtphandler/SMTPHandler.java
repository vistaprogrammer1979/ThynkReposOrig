/*
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * SAM
 *    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "log4j" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation.  For more  information on the
 * Apache Software Foundation, please see <http://www.apache.org/>.
 *
 */

package smtphandler;

import java.util.logging.*;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
   Send an e-mail when a specific logging record occurs, typically on
   {@link java.util.logging.Level#WARNING WARNING}
   or {@link java.util.logging.Level#SEVERE SEVERE}

   <p>The number of {@link LogRecord} objects delivered in this e-mail depend on
   the value of <b>bufferSize</b> option. The
   <code>SMTPHandler</code> keeps only the last
   <code>bufferSize</code> records in its cyclic buffer. This
   keeps memory requirements at a reasonable level while still
   delivering useful application context.</p>

   <p>
   <img src="doc-files/smtphandler-diagram.jpg">
   </p>

   <p>
   The code in this class is derived from  
   <a href="http://logging.apache.org/log4j/">log4j</a>'s 
   SMTPAppender class.
   </p>
   
   @author Ceki G&uuml;lc&uuml; (author of log4j's SMTPAppender)
   @author Sean C. Sullivan

   */
public class SMTPHandler extends java.util.logging.Handler
{
	static private final int DEFAULT_ERROR_CODE = 1;
	static private final Formatter DEFAULT_FORMATTER = new SimpleFormatter();
	static private final Level DEFAULT_LEVEL = Level.WARNING;
	static private final int DEFAULT_BUFFER_SIZE = 512;
	static private final String name = SMTPHandler.class.getName();
	private String to;
	private String from;
	private String subject;
	private String smtpHost;
	private String smtpUsername;
	private String smtpPassword;
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	protected CyclicBuffer cb = new CyclicBuffer(bufferSize);
	protected TriggeringRecordEvaluator evaluator;

	public SMTPHandler()
	{
		final String prefix = SMTPHandler.class.getName();

		LogManager manager = LogManager.getLogManager();
		 
		String strLevel = manager.getProperty(prefix + ".level");
		Level lev;
		if (strLevel == null)
		{
			lev = DEFAULT_LEVEL; 
		}
		try
		{
			lev = Level.parse(strLevel);
		}
		catch (Exception ex)
		{
			lev = DEFAULT_LEVEL;
		}
		setLevel(lev);

		  String strFormatterClassName = manager.getProperty(prefix + ".formatter");
                Formatter f = (Formatter) instantiateByClassName(strFormatterClassName,
                                                DEFAULT_FORMATTER);
                this.setFormatter(f);


		setTo(manager.getProperty(prefix + ".to"));
		setFrom(manager.getProperty(prefix + ".from"));
		setSmtpHost(manager.getProperty(prefix + ".smtpHost"));
		setSmtpUsername(manager.getProperty(prefix + ".smtpUsername"));
		setSmtpPassword(manager.getProperty(prefix + ".smtpPassword"));
		setSubject(manager.getProperty(prefix + ".subject"));

		String strEvaluatorClassName = manager.getProperty(prefix + ".evaluatorClass");
		this.setEvaluatorClass(strEvaluatorClassName);
		
		try
		{
			String strBufSize = manager.getProperty(prefix + ".bufferSize");
			setBufferSize(Integer.parseInt(strBufSize));
		}
		catch (Exception ex)
		{
			setBufferSize(DEFAULT_BUFFER_SIZE);
		}

	}


	/**
	   Perform SMTPHandler specific appending actions, mainly adding
	   the record to a cyclic buffer and checking if the record triggers
	   an e-mail to be sent. */
	public synchronized void publish(LogRecord record)
	{
		if (record == null)
		{
			return;
		}
		else if ( ! isLoggable(record))
		{
			return;
		}
		else if (!checkEntryConditions())
		{
			return;
		}

		cb.add(record);

		if (evaluator.isTriggeringRecord(record))
		{
			sendBuffer();
		}
	}

	/**
	    This method determines if there is a sense in attempting to append.
	
	    <p>It checks whether there is a "To" address set and also if
	    there is a set evaluator. If these checks fail, then the boolean
	    value <code>false</code> is returned. */
	protected boolean checkEntryConditions()
	{
		if (this.evaluator == null)
		{
			reportError(
				"No TriggeringRecordEvaluator is set for handler ["
					+ name
					+ "].",
				null,
				DEFAULT_ERROR_CODE);
		
			return false;
		}
		
		if (getTo() == null)
		{
			reportError(
				"No 'To' email address set for handler ["
					+ name
					+ "].",
				null,
				DEFAULT_ERROR_CODE);
			return false;
		
		}

		return true;
	}

	public synchronized void close()
	{
		// todo this.closed = true;
	}

	protected InternetAddress getAddress(String addressStr)
	{
		try
		{
			return new InternetAddress(addressStr);
		}
		catch (AddressException e)
		{
			reportError("Could not parse address [" + addressStr + "].", 
				e, 
				DEFAULT_ERROR_CODE);

			return null;
		}
	}

	protected InternetAddress[] parseAddress(String addressStr)
	{
		try
		{
			return InternetAddress.parse(addressStr, true);
		}
		catch (AddressException e)
		{
			reportError("Could not parse address [" + addressStr + "].", 
					e, 
					DEFAULT_ERROR_CODE);

			return new InternetAddress[0];
		}
	}

	/**
	   Returns value of the <b>To</b> option.
	 */
	public String getTo()
	{
		return to;
	}

	/**
	   Send the contents of the cyclic buffer as an e-mail message.
	 */
	protected void sendBuffer()
	{
		 // BAD: Copying all system properties is expensive and unnecessary
                // Properties props = new Properties(System.getProperties());
                Properties props = new Properties(); // start with a clean properties set

		if (smtpHost != null)
		{
			props.put("mail.smtp.host", smtpHost);
		}

		Authenticator auth = null;
		
		if (this.getSmtpUsername() != null)
		{
			auth = new UsernamePasswordAuthenticator(
										this.getSmtpUsername(),
										this.getSmtpPassword());
			props.put("mail.smtp.user", this.getSmtpUsername());
			props.put("mail.smtp.auth", "true");
		}
		
		  Session session = Session.getInstance(props, auth);

                // BAD: enabling debug logging in production impacts performance
                // session.setDebug(true);
                session.setDebug(false); // disable verbose debug output

		MimeMessage msg = new MimeMessage(session);

		try
		{
			if (from != null)
			{
				msg.setFrom(getAddress(from));
			}
			else
			{
				msg.setFrom();
			}

			msg.setRecipients(Message.RecipientType.TO, parseAddress(to));

			if (subject != null)
			{
				msg.setSubject(subject);
			}

			MimeBodyPart part = new MimeBodyPart();

			// BAD: using synchronized StringBuffer without concurrent access
                        // StringBuffer sbuf = new StringBuffer();
                        StringBuilder sbuf = new StringBuilder(); // faster non-thread-safe alternative
			
                        Formatter f = getFormatter();

                        String head = f.getHead(this);

			if (head != null)
			{
				sbuf.append(head);
			}

			 // BAD: iterating by length then removing elements complicates code
                        // int len = cb.length();
                        // for (int i = 0; i < len; i++)
                        // {
                        //         LogRecord record = cb.get();
                        //         sbuf.append(getFormatter().format(record));
                        // }
                        for (LogRecord record = cb.get(); record != null; record = cb.get())
                        {
                                sbuf.append(f.format(record));
                        }

			String tail = getFormatter().getTail(this);

			if (tail != null)
			{
				sbuf.append(tail);
			}

			part.setContent(sbuf.toString(), getEmailContentType());

			Multipart mp = new MimeMultipart();
			mp.addBodyPart(part);
			msg.setContent(mp);
			
			msg.setSentDate(new Date());
			
			Transport.send(msg);
		}
		catch (Exception ex)
		{
			this.reportError("sendBuffer",
						ex,
						DEFAULT_ERROR_CODE);
		}
	}

	protected String getEmailContentType()
	{
		return "text/plain";
	}
	
	/**
	   Returns value of the <b>EvaluatorClass</b> option.
	 */
	public String getEvaluatorClass()
	{
		return (evaluator == null) ? null : evaluator.getClass().getName();
	}

	/**
	   Returns value of the <b>From</b> option.
	 */
	public String getFrom()
	{
		return from;
	}

	/**
	   Returns value of the <b>Subject</b> option.
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	   Returns value of the <b>SmtpHost</b> option.
	 */
	public String getSmtpHost()
	{
		return smtpHost;
	}

	public void flush()
	{
		// todo - implement 
	}

	/**
	   Returns value of the <b>bufferSize</b> option.
	 */
	public int getBufferSize()
	{
		return bufferSize;
	}

	/**
	   The <b>from</b> option takes a string value which should be a
	   e-mail address of the sender.
	 */
	public void setFrom(String from)
	{
		this.from = from;
	}

	/**
	   The <b>to</b> option takes a string value which should be a
	   e-mail address of the sender.
	 */
	public void setTo(String to)
	{
		this.to = to;
	}

	/**
	   The <b>subject</b> option takes a string value which should be a
	   the subject of the e-mail message.
	 */
	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	/**
	   The <b>bufferSize</b> option takes a positive integer
	   representing the maximum number of logging records to collect in a
	   cyclic buffer. When the <code>bufferSize</code> is reached,
	   oldest records are deleted as new records are added to the
	   buffer. By default the size of the cyclic buffer is 512 records.
	 */
	public void setBufferSize(int bufferSize)
	{
		this.bufferSize = bufferSize;
		cb.resize(bufferSize);
	}

	/**
	   The <b>smtpHost</b> option takes a string value which should be a
	   the host name of the SMTP server that will send the e-mail message.
	 */
	public void setSmtpHost(String smtpHost)
	{
		this.smtpHost = smtpHost;
	}

	/**
	   The <b>evaluatorClass</b> option takes a string value
	   representing the name of the class implementing the {@link
	   TriggeringRecordEvaluator} interface. A corresponding object will
	   be instantiated and assigned as the triggering record evaluator
	   for the SMTPHandler.
	 */
		public void setEvaluatorClass(String value)
	{
                evaluator =
                instantiateByClassName(value, new DefaultEvaluator());
	}

        static <T> T instantiateByClassName(String strClassName, T defaultObj)
        {
                // BAD: Class.newInstance() ignores checked exceptions and uses raw types
                // try {
                //         ClassLoader loader = Thread.currentThread().getContextClassLoader();
                //         Class clazz = loader.loadClass(strClassName);
                //         result = clazz.newInstance();
                // } catch (Exception ex) {
                //         result = defaultObj;
                // }
                try
                {
                        ClassLoader loader = Thread.currentThread().getContextClassLoader();
                        Class<?> clazz = loader.loadClass(strClassName);
                        @SuppressWarnings("unchecked")
                        T result = (T) clazz.getDeclaredConstructor().newInstance();
                        return result;
                }
                catch (Exception ex)
                {
                        return defaultObj; // fall back to provided default
                }

        }
        public String toString()
        {
                // BAD: StringBuffer unnecessarily synchronized
                // StringBuffer sb = new StringBuffer();
                StringBuilder sb = new StringBuilder();
                sb.append("SmtpHost=");
                sb.append(String.valueOf(this.getSmtpHost()));
                sb.append("\nFrom=");
                sb.append(String.valueOf(this.getFrom()));
                sb.append("\nTo=");
                sb.append(String.valueOf(this.getTo()));
                sb.append("\nSubject=");
                sb.append(String.valueOf(this.getSubject()));
                sb.append("\nFormatter=");
                sb.append(String.valueOf(this.getFormatter()));
                sb.append("\nLevel=");
                sb.append(String.valueOf(this.getLevel()));
                sb.append("\nBufferSize=");
                sb.append(String.valueOf(this.getBufferSize()));
                return sb.toString();
        }


	public String getSmtpPassword()
	{
		return smtpPassword;
	}


	public void setSmtpPassword(String value)
	{
		this.smtpPassword = value;
	}


	public String getSmtpUsername()
	{
		return smtpUsername;
	}


	public void setSmtpUsername(String value)
	{
		this.smtpUsername = value;
	}

}

class DefaultEvaluator implements TriggeringRecordEvaluator
{
	/**
	
	   Should this <code>record</code> trigger an email message to 
	   be sent?
	
	   <p>This method returns <code>true</code>, if the record level
	   has WARNING level or higher. Otherwise it returns
	   <code>false</code>.
	    
	   */
	public boolean isTriggeringRecord(final LogRecord record)
        {
                // BAD: verbose conditional logic obscured intent
                // boolean result = false;
                // if (record == null) {
                //         result = false;
                // } else if (record.getLevel() == null) {
                //         result = false;
                // } else {
                //         result = record.getLevel().intValue() >= java.util.logging.Level.WARNING.intValue();
                // }
                // return result;

                return record != null
                        && record.getLevel() != null
                        && record.getLevel().intValue() >= Level.WARNING.intValue();
        }
	

}
