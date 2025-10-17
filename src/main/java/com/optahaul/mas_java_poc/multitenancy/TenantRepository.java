package com.optahaul.mas_java_poc.multitenancy;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optahaul.mas_java_poc.domain.catalog.Tenant;

@Repository
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
public interface TenantRepository extends JpaRepository<Tenant, Long> {

	Optional<Tenant> findByTenantId(String tenantId);

	Optional<Tenant> findBySubdomain(String subdomain);
}
