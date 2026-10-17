package com.taskflow.api.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.taskflow.api.exception.BadRequestException;

class PageRequestsTest {

    @Test
    @DisplayName("Tri par échéance croissante, avec tri secondaire par id")
    void validRequest_buildsSortWithSecondaryId() {
        PageRequest request = PageRequests.forTasks(0, 10, "dueDate", "asc");

        assertThat(request.getPageSize()).isEqualTo(10);
        assertThat(request.getSort().getOrderFor("dueDate").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(request.getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    @DisplayName("Seuls dueDate et createdAt sont autorisés pour le tri")
    void forbiddenSortField_isRejected() {
        assertThatThrownBy(() -> PageRequests.forTasks(0, 10, "ownerId", "asc"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("La taille de page est limitée à 1..100")
    void invalidSize_isRejected() {
        assertThatThrownBy(() -> PageRequests.forTasks(0, 0, "createdAt", "desc"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> PageRequests.forTasks(0, 101, "createdAt", "desc"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Numéro de page négatif refusé")
    void negativePage_isRejected() {
        assertThatThrownBy(() -> PageRequests.forTasks(-1, 10, "createdAt", "desc"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Direction inconnue refusée")
    void invalidDirection_isRejected() {
        assertThatThrownBy(() -> PageRequests.forTasks(0, 10, "createdAt", "up"))
                .isInstanceOf(BadRequestException.class);
    }
}
