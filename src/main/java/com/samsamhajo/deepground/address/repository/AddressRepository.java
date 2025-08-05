package com.samsamhajo.deepground.address.repository;

import com.samsamhajo.deepground.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT DISTINCT a.city FROM Address a ORDER BY a.city ASC")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT a.gu FROM Address a " +
            "WHERE a.city = :city ORDER BY a.gu ASC ")
    List<String> findDistinctGusByCity(String city);

    List<Address> findByCityAndGuOrderByDongAsc(String city, String gu);
    List<Address> findByCityLikeAndGuOrderByDongAsc(String city, String gu);
}
