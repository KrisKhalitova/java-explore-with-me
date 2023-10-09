package ru.practicum.ewm.compilations.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.util.EwmPageRequest;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    Page<Compilation> findAllByPinned(Boolean pinned, EwmPageRequest ewmPageRequest);

    List<Compilation> findByPinned(Boolean pinned, Pageable pageable);
}
