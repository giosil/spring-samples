package org.dew.app.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * Primary key generation examples:
 * 
 * Auto-increment:
 *  
 * @Id
 * @GeneratedValue(strategy=GenerationType.IDENTITY)
 * private Integer id;
 * 
 * Sequences:
 * 
 * @Id
 * @SequenceGenerator(name="seq",sequenceName="seq_name", allocationSize=1)
 * @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
 * private Integer id;
 */

@Entity(name="Comune")
@Table(name="ANA_COMUNI")
public class ComuneEntity {
  @Id
  @Column(name = "ID_COMUNE", nullable = false, length = 6)
  private String idComune;
  @Column(nullable = true, length = 4)
  private String fiscale;
  @Column(nullable = true, length = 3)
  private String provincia;
  @Column(nullable = true, length = 50)
  private String descrizione;
  @Column(name = "ID_REGIONE", nullable = false, length = 6)
  private String idRegione;
  
  public ComuneEntity()
  {
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
    if(object instanceof ComuneEntity) {
      String objIdComune = ((ComuneEntity) object).getIdComune();
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

