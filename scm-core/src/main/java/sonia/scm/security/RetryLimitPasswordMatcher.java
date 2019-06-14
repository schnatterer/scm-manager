package sonia.scm.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

class RetryLimitPasswordMatcher implements CredentialsMatcher {

  private final LoginAttemptHandler loginAttemptHandler;
  private final CredentialsMatcher credentialsMatcher;

  RetryLimitPasswordMatcher(LoginAttemptHandler loginAttemptHandler, CredentialsMatcher credentialsMatcher) {
    this.loginAttemptHandler = loginAttemptHandler;
    this.credentialsMatcher = credentialsMatcher;
  }

  @Override
  public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
    loginAttemptHandler.beforeAuthentication(token);
    boolean result = credentialsMatcher.doCredentialsMatch(token, info);
    if ( result ) {
      loginAttemptHandler.onSuccessfulAuthentication(token, info);
    } else {
      loginAttemptHandler.onUnsuccessfulAuthentication(token, info);
    }
    return result;
  }

}
