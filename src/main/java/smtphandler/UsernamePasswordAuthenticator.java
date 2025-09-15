/*
 * 
 * 
 */
package smtphandler;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

class UsernamePasswordAuthenticator extends Authenticator
{
	 // BAD: field was mutable even though never reassigned
        // private PasswordAuthentication auth = null;
        private final PasswordAuthentication auth;

        public UsernamePasswordAuthenticator(String user, String password)
        {
                // BAD: missing "this" and null-safety documentation
                // auth = new PasswordAuthentication(user, password);
                this.auth = new PasswordAuthentication(user, password);
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication()
        {
                return auth; // simple accessor to immutable authentication
        }
	

}
