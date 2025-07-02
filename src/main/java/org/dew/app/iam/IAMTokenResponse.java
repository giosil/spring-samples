package org.dew.app.iam;

import java.io.Serializable;

public class IAMTokenResponse implements Serializable {
  
  private static final long serialVersionUID = 3023931515449940606L;
  
  private String access_token;
  private String refresh_token;
  private String scope;
  private String id_token;
  private String token_type;
  private int expires_in;
  
  public IAMTokenResponse()
  {
  }

  public String getAccess_token() {
    return access_token;
  }

  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }

  public String getRefresh_token() {
    return refresh_token;
  }

  public void setRefresh_token(String refresh_token) {
    this.refresh_token = refresh_token;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getId_token() {
    return id_token;
  }

  public void setId_token(String id_token) {
    this.id_token = id_token;
  }

  public String getToken_type() {
    return token_type;
  }

  public void setToken_type(String token_type) {
    this.token_type = token_type;
  }

  public int getExpires_in() {
    return expires_in;
  }

  public void setExpires_in(int expires_in) {
    this.expires_in = expires_in;
  }
  
  @Override
  public boolean equals(Object object) {
    if(object instanceof IAMTokenResponse) {
      String objAccessToken = ((IAMTokenResponse) object).getAccess_token();
      if(objAccessToken == null && access_token == null) return true;
      return objAccessToken != null && objAccessToken.equals(access_token);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if(access_token == null) return 0;
    return access_token.hashCode();
  }
  
  @Override
  public String toString() {
    return "IAMTokenResponse(" + access_token + "," + token_type + "," + expires_in + ")";
  }
}
