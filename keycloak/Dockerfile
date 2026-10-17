# ============================================================
#  Étape 1 : pré-construction de Keycloak (options de build figées)
# ============================================================
FROM quay.io/keycloak/keycloak:26.3 AS builder

ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
# Une seule instance : cache local, pas de recherche d'autres nœuds du cluster
ENV KC_CACHE=local

RUN /opt/keycloak/bin/kc.sh build

# ============================================================
#  Étape 2 : image d'exécution
# ============================================================
FROM quay.io/keycloak/keycloak:26.3

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY taskflow-realm.json /opt/keycloak/data/import/taskflow-realm.json

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized", "--import-realm"]