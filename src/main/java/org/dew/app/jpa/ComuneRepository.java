package org.dew.app.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComuneRepository extends JpaRepository<ComuneEntity, String> {
  
  @Query("SELECT c FROM Comune c WHERE c.provincia = :provincia")
  List<ComuneEntity> findByProvincia(@Param("provincia") String provincia);
  
}
