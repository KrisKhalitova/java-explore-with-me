package ru.practicum.ewm.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class EwmPageRequest extends PageRequest {

    public EwmPageRequest(int page, int size, Sort sort) {
        super(page / size, size, sort);
    }
}
