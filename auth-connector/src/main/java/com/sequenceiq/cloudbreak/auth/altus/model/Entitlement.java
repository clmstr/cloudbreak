package com.sequenceiq.cloudbreak.auth.altus.model;

public enum Entitlement {

    DATAHUB_FLOW_SCALING,
    DATAHUB_STREAMING_SCALING,
    DATAHUB_DEFAULT_SCALING,
    CDP_AZURE,
    CDP_GCP,
    CDP_BASE_IMAGE,
    CDP_AUTOMATIC_USERSYNC_POLLER,
    CDP_FREEIPA_HA_REPAIR,
    CLOUDERA_INTERNAL_ACCOUNT,
    CDP_FMS_CLUSTER_PROXY,
    CDP_CLOUD_STORAGE_VALIDATION,
    CDP_RAZ,
    CDP_MEDIUM_DUTY_SDX,
    CDP_RUNTIME_UPGRADE,
    CDP_RUNTIME_UPGRADE_DATAHUB,
    LOCAL_DEV,
    CDP_AZURE_SINGLE_RESOURCE_GROUP,
    CDP_AZURE_SINGLE_RESOURCE_GROUP_DEDICATED_STORAGE_ACCOUNT,
    CDP_CLOUD_IDENTITY_MAPPING,
    CDP_ALLOW_INTERNAL_REPOSITORY_FOR_UPGRADE,
    CDP_SDX_HBASE_CLOUD_STORAGE,
    CDP_DATA_LAKE_AWS_EFS,
    CB_AUTHZ_POWER_USERS,
    CDP_ALLOW_DIFFERENT_DATAHUB_VERSION_THAN_DATALAKE,
    DATAHUB_AWS_AUTOSCALING,
    DATAHUB_AZURE_AUTOSCALING,
    CDP_CCM_V2,
    CDP_CB_DATABASE_WIRE_ENCRYPTION,
    CDP_ENABLE_DISTROX_INSTANCE_TYPES,
    CDP_SHOW_CLI,
    CDP_LIST_FILTERING,
    CDP_DATA_LAKE_LOAD_BALANCER,
    CDP_EXPERIENCE_DELETION_BY_ENVIRONMENT,
    CDP_USE_DATABUS_CNAME_ENDPOINT,
    CDP_USE_CM_SYNC_COMMAND_POLLER,
    CDP_PUBLIC_ENDPOINT_ACCESS_GATEWAY,
    CDP_DATALAKE_BACKUP_ON_UPGRADE,
    FMS_FREEIPA_BATCH_CALL,
    CDP_CB_AZURE_DISK_SSE_WITH_CMK;
}
