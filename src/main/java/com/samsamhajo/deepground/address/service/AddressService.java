package com.samsamhajo.deepground.address.service;

import com.samsamhajo.deepground.address.dto.AddressDto;
import com.samsamhajo.deepground.address.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<AddressDto> getAllCities() {
        return addressRepository.findDistinctCities().stream()
                .map(AddressDto::fromCity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getGusByCity(String city) {
        return addressRepository.findDistinctGusByCity(city).stream()
                .map(gu -> AddressDto.fromGu(city, gu))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getDongsByCityAndGu(String city, String gu) {
        return addressRepository.findByCityAndGuOrderByDongAsc(city, gu).stream()
                .map(AddressDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getDongsByCityAndGuUsingLike(String city, String gu) {
        return addressRepository.findByCityLikeAndGuOrderByDongAsc(city+"%", gu).stream()
                .map(AddressDto::from)
                .toList();
    }
}
