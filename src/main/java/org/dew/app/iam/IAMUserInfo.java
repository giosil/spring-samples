package org.dew.app.iam;

import java.io.Serializable;

public class IAMUserInfo implements Serializable {
  
  private static final long serialVersionUID = -1454764372198547543L;
  
  private String sub;
  private String name;
  private String given_name;
  private String family_name;
  private String email;
  private String phone_number;
  
  private String fiscal_number;
  private String parse_fiscal_number;
  
  private String iss;
  private String aud;
  private long iat;
  private long nbf;
  private long exp;
  
  public IAMUserInfo()
  {
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGiven_name() {
    return given_name;
  }

  public void setGiven_name(String given_name) {
    this.given_name = given_name;
  }

  public String getFamily_name() {
    return family_name;
  }

  public void setFamily_name(String family_name) {
    this.family_name = family_name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone_number() {
    return phone_number;
  }

  public void setPhone_number(String phone_number) {
    this.phone_number = phone_number;
  }

  public String getFiscal_number() {
    return fiscal_number;
  }

  public void setFiscal_number(String fiscal_number) {
    this.fiscal_number = fiscal_number;
  }

  public String getParse_fiscal_number() {
    return parse_fiscal_number;
  }

  public void setParse_fiscal_number(String parse_fiscal_number) {
    this.parse_fiscal_number = parse_fiscal_number;
  }

  public String getIss() {
    return iss;
  }

  public void setIss(String iss) {
    this.iss = iss;
  }

  public String getAud() {
    return aud;
  }

  public void setAud(String aud) {
    this.aud = aud;
  }

  public long getIat() {
    return iat;
  }

  public void setIat(long iat) {
    this.iat = iat;
  }

  public long getNbf() {
    return nbf;
  }

  public void setNbf(long nbf) {
    this.nbf = nbf;
  }

  public long getExp() {
    return exp;
  }

  public void setExp(long exp) {
    this.exp = exp;
  }
  
  @Override
  public boolean equals(Object object) {
    if(object instanceof IAMUserInfo) {
      String objSub = ((IAMUserInfo) object).getSub();
      if(objSub == null && sub == null) return true;
      return objSub != null && objSub.equals(sub);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if(sub == null) return 0;
    return sub.hashCode();
  }
  
  @Override
  public String toString() {
    return "IAMUserInfo(" + sub + ")";
  }
}
