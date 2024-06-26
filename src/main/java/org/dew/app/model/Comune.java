package org.dew.app.model;

public class Comune {
  private String idComune;
  private String fiscale;
  private String provincia;
  private String descrizione;
  private String idRegione;
  
  public Comune()
  {
  }
  
  public Comune(String idComune, String descrizione)
  {
    this.idComune    = idComune;
    this.descrizione = descrizione;
  }

  public Comune(String idComune, String descrizione, String fiscale, String provincia)
  {
    this.idComune    = idComune;
    this.descrizione = descrizione;
    this.fiscale     = fiscale;
    this.provincia   = provincia;
  }
  
  public Comune(String idComune, String descrizione, String fiscale, String provincia, String idRegione)
  {
    this.idComune    = idComune;
    this.descrizione = descrizione;
    this.fiscale     = fiscale;
    this.provincia   = provincia;
    this.idRegione   = idRegione;
  }

  public String getIdComune() {
    return idComune;
  }

  public void setIdComune(String idComune) {
    this.idComune = idComune;
  }

  public String getFiscale() {
    return fiscale;
  }

  public void setFiscale(String fiscale) {
    this.fiscale = fiscale;
  }

  public String getProvincia() {
    return provincia;
  }

  public void setProvincia(String provincia) {
    this.provincia = provincia;
  }

  public String getDescrizione() {
    return descrizione;
  }

  public void setDescrizione(String descrizione) {
    this.descrizione = descrizione;
  }

  public String getIdRegione() {
    return idRegione;
  }

  public void setIdRegione(String idRegione) {
    this.idRegione = idRegione;
  }

  @Override
  public boolean equals(Object object) {
    if(object instanceof Comune) {
      String objIdComune = ((Comune) object).getIdComune();
      if(objIdComune == null && idComune == null) {
        return true;
      }
      return objIdComune != null && objIdComune.equals(idComune);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if(idComune == null) return 0;
    return idComune.hashCode();
  }
  
  @Override
  public String toString() {
    return descrizione;
  }
}
