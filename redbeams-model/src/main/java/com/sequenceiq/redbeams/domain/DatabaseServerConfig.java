package com.sequenceiq.redbeams.domain;

//import java.util.HashSet;
//import java.util.Set;

//import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
//import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.JoinTable;
//import javax.persistence.ManyToMany;
// import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Where;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DatabaseVendor;
import com.sequenceiq.cloudbreak.api.endpoint.v4.common.ResourceStatus;
import com.sequenceiq.cloudbreak.aspect.secret.SecretValue;
import com.sequenceiq.cloudbreak.domain.Secret;
import com.sequenceiq.cloudbreak.domain.SecretToString;
import com.sequenceiq.cloudbreak.workspace.resource.WorkspaceResource;
//import com.sequenceiq.cloudbreak.domain.environment.EnvironmentAwareResource;
//import com.sequenceiq.cloudbreak.domain.view.EnvironmentView;
// import com.sequenceiq.cloudbreak.domain.workspace.Workspace;
// import com.sequenceiq.cloudbreak.domain.workspace.WorkspaceAwareResource;

@Entity
@Where(clause = "archived = false")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "name"}))
public class DatabaseServerConfig implements ProvisionEntity, /* EnvironmentAwareResource, WorkspaceAwareResource, */ ArchivableResource {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "databaseserverconfig_generator")
    @SequenceGenerator(name = "databaseserverconfig_generator", sequenceName = "databaseserverconfig_id_seq", allocationSize = 1)
    private Long id;

    // @ManyToOne
    // private Workspace workspace;
    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000000, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DatabaseVendor databaseVendor;

    @Column(nullable = false)
    private String connectionDriver;

    @Convert(converter = SecretToString.class)
    @SecretValue
    @Column(nullable = false)
    private Secret connectionUserName = Secret.EMPTY;

    @Convert(converter = SecretToString.class)
    @SecretValue
    @Column(nullable = false)
    private Secret connectionPassword = Secret.EMPTY;

    private Long creationDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceStatus resourceStatus;

    @Column
    private String connectorJarUrl;

    private boolean archived;

    private Long deletionTimestamp = -1L;

    // might go away / change, since environments are not stored in redbeams
    // @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    // @JoinTable(name = "env_databaseserver", joinColumns = @JoinColumn(name = "databaseserverid"), inverseJoinColumns = @JoinColumn(name = "envid"))
    // private Set<EnvironmentView> environments = new HashSet<>();

    //@Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    //@Override
    public WorkspaceResource getResource() {
        return WorkspaceResource.DATABASE_SERVER;
    }

    // @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public DatabaseVendor getDatabaseVendor() {
        return databaseVendor;
    }

    public void setDatabaseVendor(DatabaseVendor databaseVendor) {
        this.databaseVendor = databaseVendor;
    }

    public String getConnectionDriver() {
        return connectionDriver;
    }

    public void setConnectionDriver(String connectionDriver) {
        this.connectionDriver = connectionDriver;
    }

    public String getConnectionUserName() {
        return connectionUserName.getRaw();
    }

    public String getConnectionUserNameSecret() {
        return connectionUserName.getSecret();
    }

    public void setConnectionUserName(String connectionUserName) {
        this.connectionUserName = new Secret(connectionUserName);
    }

    public String getConnectionPassword() {
        return connectionPassword.getRaw();
    }

    public String getConnectionPasswordSecret() {
        return connectionPassword.getSecret();
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = new Secret(connectionPassword);
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public ResourceStatus getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(ResourceStatus resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public String getConnectorJarUrl() {
        return connectorJarUrl;
    }

    public void setConnectorJarUrl(String connectorJarUrl) {
        this.connectorJarUrl = connectorJarUrl;
    }

    public Long getDeletionTimestamp() {
        return deletionTimestamp;
    }

    @Override
    public void setDeletionTimestamp(Long timestampMillisecs) {
        deletionTimestamp = timestampMillisecs;
    }

    public boolean isArchived() {
        return archived;
    }

    @Override
    public void setArchived(boolean archived) {
        this.archived = true;
    }

    @Override
    public void unsetRelationsToEntitiesToBeDeleted() {
    }

    // @Override
    // public Set<EnvironmentView> getEnvironments() {
    //     return environments;
    // }

    // @Override
    // public void setEnvironments(Set<EnvironmentView> environments) {
    //     this.environments = environments;
    // }

}
