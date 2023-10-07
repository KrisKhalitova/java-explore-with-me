package ru.practicum.ewm.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.requests.dto.RequestDto;
import ru.practicum.ewm.requests.mapper.RequestMapper;
import ru.practicum.ewm.requests.model.Request;
import ru.practicum.ewm.requests.model.RequestStatus;
import ru.practicum.ewm.requests.repository.RequestRepository;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    public RequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ValidationException("Инициатор события не может создать запрос на участие в событии.");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ValidationException("Невозможно участвовать в неопубликованном событии.");
        }
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId,
                RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() <= confirmedRequests && event.getParticipantLimit() != 0) {
            throw new ValidationException(("Лимит запрос превышен."));
        }
        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(!event.getRequestModeration() || event.getParticipantLimit() == 0
                        ? RequestStatus.CONFIRMED
                        : RequestStatus.PENDING)
                .build();
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь не найден")));

        Request request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException(String.format("Запрос не найден")));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException(
                    String.format("Пользователь не является участником."));
        }
        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> getRequestsByUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь не найден")));
        return RequestMapper.toRequestDtoList(requestRepository.findAllByRequesterId(userId));
    }
}
