package com.taskflow.api.controller;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.taskflow.api.exception.BadRequestException;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class PageRequests {

    public static final int MAX_SIZE = 100;
    private static final Set<String> TASK_SORT_FIELDS = Set.of("dueDate", "createdAt");

    public static PageRequest forTasks(int page, int size, String sort, String direction) {
        if (page < 0) {
            throw new BadRequestException("Le paramètre 'page' doit être supérieur ou égal à 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new BadRequestException("Le paramètre 'size' doit être compris entre 1 et " + MAX_SIZE);
        }
        if (!TASK_SORT_FIELDS.contains(sort)) {
            throw new BadRequestException("Tri autorisé : dueDate ou createdAt");
        }
        Sort.Direction dir = switch (direction.toLowerCase()) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new BadRequestException("Direction autorisée : asc ou desc");
        };
        return PageRequest.of(page, size, Sort.by(new Sort.Order(dir, sort), Sort.Order.asc("id")));
    }
}
