package com.sequenceiq.redbeams.repository;

import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sequenceiq.cloudbreak.workspace.repository.EntityType;
import com.sequenceiq.cloudbreak.workspace.repository.check.CheckPermissionsByReturnValue;
import com.sequenceiq.cloudbreak.workspace.resource.ResourceAction;
import com.sequenceiq.redbeams.domain.DatabaseConfig;

@EntityType(entityClass = DatabaseConfig.class)
@Transactional(Transactional.TxType.REQUIRED)
public interface DatabaseConfigRepository extends JpaRepository<DatabaseConfig, Long> {

    // TODO check if checkPermission still works
    @CheckPermissionsByReturnValue(action = ResourceAction.READ)
    @Query("SELECT d FROM DatabaseConfig d WHERE d.environmentId = :environmentId "
        + "AND (d.name = :name OR d.resourceCrn = :name)")
    Optional<DatabaseConfig> findByEnvironmentIdAndName(String environmentId, String name);

    @CheckPermissionsByReturnValue(action = ResourceAction.READ)
    Set<DatabaseConfig> findByEnvironmentId(String environmentId);

    @CheckPermissionsByReturnValue(action = ResourceAction.READ)
    @Query("SELECT d FROM DatabaseConfig d WHERE d.environmentId = :environmentId "
        + "AND (d.name IN :names OR d.resourceCrn IN :names)")
    Set<DatabaseConfig> findAllByEnvironmentIdAndNameIn(String environmentId, Set<String> names);
}
