package ru.practicum.ewm.locations.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.locations.dto.LocationDto;
import ru.practicum.ewm.locations.model.Location;

@UtilityClass
public class LocationMapper {
    public Location toLocation(LocationDto locationDto) {
        return new Location(locationDto.getLat(), locationDto.getLon());
    }

    public LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}
