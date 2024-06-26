package org.dew.app.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

@Repository
public interface ComunePASRepository extends PagingAndSortingRepository<ComuneEntity, String> {
  
  @Query("SELECT c FROM Comune c WHERE c.provincia = :provincia")
  List<ComuneEntity> findByProvincia(@Param("provincia") String provincia);
  
}
