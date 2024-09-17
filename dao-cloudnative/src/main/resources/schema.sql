create table if not exists unit (
    unit_id bigint not null,
    symbol varchar(255) not null,
    name varchar(255),
    link varchar(255),
    constraint unit_pkey primary key (unit_id),
    constraint un_unit_symbol unique (symbol)
    );
create table if not exists format (
    format_id bigint not null,
    definition varchar(255) not null,
    constraint format_pkey primary key (format_id),
    constraint un_format_definition unique (definition)
    );
create table if not exists feature (
    feature_id bigint not null,
--     discriminator varchar(255),
    fk_format_id bigint not null,
    identifier varchar(255) not null,
    sta_identifier varchar(255) not null,
    name varchar(255),
    description varchar,
    xml varchar,
--     url varchar(255),
    geom blob,
    constraint feature_pkey primary key (feature_id),
    foreign key (fk_format_id) references format (format_id),
    constraint un_feature_identifier unique (identifier),
    constraint un_feature_staidentifier unique (sta_identifier)
--     constraint un_feature_url unique (url)
    );
create table if not exists feature_parameter (
    parameter_id bigint not null,
--     type varchar(255) not null,
    name varchar(255) not null,
--     description varchar,
--     last_update timestamp,
--     domain varchar(255),
    fk_feature_id bigint not null,
    value_boolean smallint,
--     value_category varchar(255),
--     fk_unit_id bigint,
--     value_count int,
    value_quantity numeric(19, 2),
    value_text varchar(255),
--     value_xml varchar,
--     value_json varchar,
--     value_temporal_from timestamp,
--     value_temporal_to timestamp,
--     fk_parent_parameter_id bigint,
    foreign key (fk_feature_id) references feature (feature_id),
--     foreign key (fk_unit_id) references unit (unit_id),
--     foreign key (fk_parent_parameter_id) references feature_parameter (parameter_id),
    constraint feature_parameter_pkey primary key (parameter_id)
    --     constraint feature_parameter_type_check check (cast(type as varchar) = cast(array[
--                                                                                 cast('bool' as varchar),
--     cast('category' as varchar),
--     cast('count' as varchar),
--     cast('quantity' as varchar),
--     cast('text' as varchar),
--     cast('xml' as varchar),
--     cast('json' as varchar),
--     cast('complex' as varchar),
--     cast('temporal' as varchar)
--     ] as array))
    );
create table if not exists platform (
    platform_id bigint not null,
    identifier varchar(255) not null,
    sta_identifier varchar(255) not null,
    name varchar(255),
    description varchar,
    constraint platform_pkey primary key (platform_id),
    constraint un_platform_identifier unique (identifier),
    constraint un_platform_staidentifier unique (sta_identifier)
    );
create table if not exists platform_parameter (
    parameter_id bigint not null,
--     type varchar(255) not null,
    name varchar(255) not null,
--     description varchar,
--     last_update timestamp,
--     domain varchar(255),
    fk_platform_id bigint not null,
--     value_category varchar(255),
--     fk_unit_id bigint,
    value_boolean smallint,
--     value_count int,
    value_quantity numeric(19, 2),
    value_text varchar(255),
--     value_xml varchar,
--     value_json varchar,
--     value_temporal_from timestamp,
--     value_temporal_to timestamp,
--     fk_parent_parameter_id bigint,
    foreign key (fk_platform_id) references platform (platform_id),
--     foreign key (fk_unit_id) references unit (unit_id),
--     foreign key (fk_parent_parameter_id) references platform_parameter (parameter_id),
    constraint platform_parameter_pkey primary key (parameter_id)
    --     constraint platform_parameter_type_check check (cast(type as varchar) = cast(array[
--                                                                                  cast('bool' as varchar),
--     cast('category' as varchar),
--     cast('count' as varchar),
--     cast('quantity' as varchar),
--     cast('text' as varchar),
--     cast('xml' as varchar),
--     cast('json' as varchar),
--     cast('complex' as varchar),
--     cast('temporal' as varchar)
--     ] as array))
    );
create table if not exists historical_location (
    historical_location_id bigint not null,
    identifier varchar(255) not null,
    sta_identifier varchar(255) not null,
    fk_platform_id bigint not null,
    time timestamp not null,
    foreign key (fk_platform_id) references platform (platform_id),
    constraint historical_location_pkey primary key (historical_location_id),
    constraint un_historicallocation_identifier unique (identifier),
    constraint un_historicallocation_staidentifier unique (sta_identifier)
    );
create table if not exists location (
    location_id bigint not null,
    identifier varchar(255) not null,
    sta_identifier varchar(255) not null,
    name varchar(255) not null,
    description varchar not null,
    location varchar,
    geom blob,
    fk_format_id bigint not null,
    foreign key (fk_format_id) references format (format_id),
    constraint location_pkey primary key (location_id),
    constraint un_location_identifier unique (identifier),
    constraint un_location_staidentifier unique (sta_identifier)
    );
create table if not exists location_parameter (
    parameter_id bigint not null,
--     type varchar(255) not null,
    name varchar(255) not null,
--     description varchar,
--     last_update timestamp,
--     domain varchar(255),
    fk_location_id bigint not null,
    value_boolean smallint,
--     value_category varchar(255),
--     fk_unit_id bigint,
--     value_count int,
    value_quantity numeric(19, 2),
    value_text varchar(255),
--     value_xml varchar,
--     value_json varchar,
--     value_temporal_from timestamp,
--     value_temporal_to timestamp,
--     fk_parent_parameter_id bigint,
    foreign key (fk_location_id) references location (location_id),
--     foreign key (fk_unit_id) references unit (unit_id),
--     foreign key (fk_parent_parameter_id) references location_parameter (parameter_id),
    constraint location_parameter_pkey primary key (parameter_id)
    --     constraint location_parameter_type_check check (cast(type as varchar) = cast(array[
--                                                                                  cast('bool' as varchar),
--     cast('category' as varchar),
--     cast('count' as varchar),
--     cast('quantity' as varchar),
--     cast('text' as varchar),
--     cast('xml' as varchar),
--     cast('json' as varchar),
--     cast('complex' as varchar),
--     cast('temporal' as varchar)
--     ] as array))
    );
create table if not exists platform_location (
    fk_location_id bigint not null,
    fk_platform_id bigint not null,
    foreign key (fk_location_id) references location (location_id),
    foreign key (fk_platform_id) references platform (platform_id),
    constraint platform_location_pkey primary key (fk_platform_id, fk_location_id)
    );
create table if not exists location_historical_location (
    fk_location_id bigint not null,
    fk_historical_location_id bigint not null,
    foreign key (fk_location_id) references location (location_id),
    foreign key (fk_historical_location_id) references historical_location (historical_location_id),
    constraint location_historical_location_pkey primary key (fk_location_id, fk_historical_location_id)
    );
create table if not exists phenomenon (
    phenomenon_id bigint not null,
    identifier varchar(255) not null,
    sta_identifier varchar(255) not null,
    name varchar(255),
    description varchar,
    constraint phenomenon_pkey primary key (phenomenon_id),
    constraint un_phenomenon_identifier unique (identifier),
    constraint un_phenomenon_staidentifier unique (sta_identifier)
    );
create table if not exists phenomenon_parameter (
    parameter_id bigint not null,
--     type varchar(255) not null,
    name varchar(255) not null,
--     description varchar,
--     last_update timestamp,
--     domain varchar(255),
    fk_phenomenon_id bigint not null,
    value_boolean smallint,
--     value_category varchar(255),
--     fk_unit_id bigint,
--     value_count int,
    value_quantity numeric(19, 2),
    value_text varchar(255),
--     value_xml varchar,
--     value_json varchar,
--     value_temporal_from timestamp,
--     value_temporal_to timestamp,
--     fk_parent_parameter_id bigint,
    foreign key (fk_phenomenon_id) references phenomenon (phenomenon_id),
--     foreign key (fk_unit_id) references unit (unit_id),
--     foreign key (fk_parent_parameter_id) references phenomenon_parameter (parameter_id),
    constraint phenomenon_parameter_pkey primary key (parameter_id)
    --     constraint phenomenon_parameter_type_check check (cast(type as varchar) = cast(array[
--                                                                                    cast('bool' as varchar),
--     cast('category' as varchar),
--     cast('count' as varchar),
--     cast('quantity' as varchar),
--     cast('text' as varchar),
--     cast('xml' as varchar),
--     cast('json' as varchar),
--     cast('complex' as varchar),
--     cast('temporal' as varchar)
--     ] as array))
    );
create table if not exists procedure (
    procedure_id bigint not null,
    identifier varchar(255) not null,
    sta_identifier varchar(255) not null,
--     fk_identifier_codespace_id bigint,
    name varchar(255),
--     fk_name_codespace_id bigint,
    description varchar,
    description_file varchar,
--     is_reference smallint default 0,
--     fk_type_of_procedure_id bigint,
--     is_aggregation smallint default 1,
    fk_format_id bigint not null,
--     foreign key (fk_type_of_procedure_id) references procedure (procedure_id),
    foreign key (fk_format_id) references format (format_id),
    constraint procedure_pkey primary key (procedure_id),
    constraint un_procedure_identifier unique (identifier),
    constraint un_procedure_staidentifier unique (sta_identifier)
    --     constraint procedure_is_reference_check check (is_reference = array[1, 0]),
--     constraint procedure_is_aggregation_check check (is_aggregation = array[1, 0])
    );
create table if not exists procedure_parameter (
    parameter_id bigint not null,
--     type varchar(255),
    name varchar(255),
--     description varchar,
--     last_update timestamp,
--     domain varchar(255),
    fk_procedure_id bigint not null,
    value_boolean smallint,
--     value_category varchar(255),
--     fk_unit_id bigint,
--     value_count int,
    value_quantity numeric(19, 2),
    value_text varchar(255),
--     value_xml varchar,
--     value_json varchar,
--     value_temporal_from timestamp,
--     value_temporal_to timestamp,
--     fk_parent_parameter_id bigint,
    foreign key (fk_procedure_id) references procedure (procedure_id),
--     foreign key (fk_unit_id) references unit (unit_id),
--     foreign key (fk_parent_parameter_id) references procedure_parameter (parameter_id),
    constraint procedure_parameter_pkey primary key (parameter_id)
    --     constraint procedure_parameter_type_check check (cast(type as varchar) = cast(array[
--                                                                                   cast('bool' as varchar),
--     cast('category' as varchar),
--     cast('count' as varchar),
--     cast('quantity' as varchar),
--     cast('text' as varchar),
--     cast('xml' as varchar),
--     cast('json' as varchar),
--     cast('complex' as varchar),
--     cast('temporal' as varchar)
--     ] as array))
    );
create table if not exists dataset (
    dataset_id bigint not null,
    identifier varchar(255) not null,
    sta_identifier varchar(255),
    name varchar(255),
    description varchar,
    first_time timestamp,
    last_time timestamp,
    result_time_start timestamp,
    result_time_end timestamp,
    observed_area blob,
    fk_procedure_id bigint not null,
    fk_phenomenon_id bigint not null,
    fk_feature_id bigint,
    fk_platform_id bigint,
    fk_unit_id bigint,
    fk_format_id bigint,
    fk_aggregation_id bigint,
--     first_value numeric(20, 10),
--     last_value numeric(20, 10),
--     fk_first_observation_id bigint,
--     fk_last_observation_id bigint,
--     dataset_type varchar(255) not null default cast('not_initialized' as varchar),
    observation_type varchar(255) not null default cast('not_initialized' as varchar),
--     value_type varchar(255) not null default cast('not_initialized' as varchar),
--     is_deleted smallint not null default 0,
--     is_disabled smallint not null default 0,
--     is_published smallint not null default 1,
--     is_mobile smallint default 0,
--     is_insitu smallint default 1,
--     is_hidden smallint not null default 0,
--     origin_timezone varchar(40),
--     decimals int,
--     fk_value_profile_id bigint,
    foreign key (fk_procedure_id) references procedure (procedure_id),
    foreign key (fk_phenomenon_id) references phenomenon (phenomenon_id),
    foreign key (fk_feature_id) references feature (feature_id),
    foreign key (fk_platform_id) references platform (platform_id),
    foreign key (fk_unit_id) references unit (unit_id),
    foreign key (fk_format_id) references format (format_id),
    constraint dataset_pkey primary key (dataset_id),
    constraint un_dataset_identifier unique (identifier),
    constraint un_dataset_identity unique (
                                              fk_procedure_id,
                                              fk_phenomenon_id,
                                              fk_feature_id,
                                              fk_platform_id,
                                              fk_unit_id
                                          ),
    constraint un_dataset_staidentifier unique (sta_identifier)
    --     constraint dataset_dataset_type_check check (cast(dataset_type as varchar) = cast(array[
--                                                                                       cast('individualObservation' as varchar),
--     cast('sampling' as varchar),
--     cast('timeseries' as varchar),
--     cast('profile' as varchar),
--     cast('trajectory' as varchar),
--     cast('not_initialized' as varchar)
--     ] as array)),
--     constraint dataset_observation_type_check check (cast(observation_type as varchar) = cast(array[
--                                                                                               cast('simple' as varchar),
--     cast('profile' as varchar),
--     cast('timeseries' as varchar),
--     cast('trajectory' as varchar),
--     cast('not_initialized' as varchar)
--     ] as array)),
--     constraint dataset_value_type_check check (cast(value_type as varchar) = cast(array[
--                                                                                   cast('quantity' as varchar),
--     cast('count' as varchar),
--     cast('text' as varchar),
--     cast('category' as varchar),
--     cast('bool' as varchar),
--     cast('geometry' as varchar),
--     cast('blob' as varchar),
--     cast('reference' as varchar),
--     cast('complex' as varchar),
--     cast('dataarray' as varchar),
--     cast('not_initialized' as varchar)
--     ] as array)),
--     constraint dataset_is_deleted_check check (is_deleted = array[1, 0]),
--     constraint dataset_is_disabled_check check (is_disabled = array[1, 0]),
--     constraint dataset_is_published_check check (is_published = array[1, 0]),
--     constraint dataset_is_mobile_check check (is_mobile = array[1, 0]),
--     constraint dataset_is_insitu_check check (is_insitu = array[1, 0]),
--     constraint dataset_is_hidden_check check (is_hidden = array[1, 0])
    );
create table if not exists dataset_parameter (
    parameter_id bigint not null,
--     type varchar(255) not null,
    name varchar(255) not null,
--     description varchar,
--     last_update timestamp,
--     domain varchar(255),
    fk_dataset_id bigint not null,
    value_boolean smallint,
--     value_category varchar(255),
--     fk_unit_id bigint,
--     value_count int,
    value_quantity numeric(19, 2),
    value_text varchar(255),
--     value_xml varchar,
--     value_json varchar,
--     value_temporal_from timestamp,
--     value_temporal_to timestamp,
--     fk_parent_parameter_id bigint,
    foreign key (fk_dataset_id) references dataset (dataset_id),
--     foreign key (fk_unit_id) references unit (unit_id),
--     foreign key (fk_parent_parameter_id) references dataset_parameter (parameter_id),
    constraint dataset_parameter_pkey primary key (parameter_id)
    --     constraint dataset_parameter_type_check check (cast(type as varchar) = cast(array[
--                                                                                 cast('bool' as varchar),
--     cast('category' as varchar),
--     cast('count' as varchar),
--     cast('quantity' as varchar),
--     cast('text' as varchar),
--     cast('xml' as varchar),
--     cast('json' as varchar),
--     cast('complex' as varchar),
--     cast('temporal' as varchar)
--     ] as array))
    );

create table if not exists observation (
    observation_id bigint not null,
    value_type varchar(255) not null,
    fk_dataset_id bigint not null,
    sampling_time_start timestamp not null,
    sampling_time_end timestamp not null,
    result_time timestamp,
    identifier varchar(255),
    sta_identifier varchar(255) not null,
    name varchar(255),
    description varchar,
--     is_deleted smallint not null default 0,
    valid_time_start timestamp,
    valid_time_end timestamp,
--     sampling_geometry blob,
--     value_identifier varchar(255),
--     value_name varchar(255),
--     value_description varchar(255),
--     vertical_from numeric(20, 10) not null default 0,
--     vertical_to numeric(20, 10) not null default 0,
    fk_parent_observation_id bigint,
    value_quantity numeric(20, 10),
    value_text varchar(255),
    value_count int,
    value_category varchar(255),
    value_boolean smallint,
--     detection_limit_flag smallint,
--     detection_limit numeric(20, 10),
--     value_reference varchar(255),
--     value_geometry blob,
--     value_array varchar,
    foreign key (fk_dataset_id) references dataset (dataset_id),
--     foreign key (fk_parent_observation_id) references observation (observation_id),
    constraint observation_pkey primary key (observation_id),
    constraint un_observation_identifier unique (identifier),
    constraint un_observation_identity unique (
                                                  value_type,
                                                  fk_dataset_id,
                                                  sampling_time_start,
                                                  sampling_time_end,
                                                  result_time
--                                                   vertical_from,
--                                                   vertical_to
                                              ),
    constraint un_observation_staidentifier unique (sta_identifier)
    );
create table if not exists observation_parameter (
    parameter_id bigint not null,
--     type varchar(255) not null,
    name varchar(255) not null,
--     description varchar,
--     last_update timestamp,
--     domain varchar(255),
    fk_observation_id bigint not null,
    value_boolean smallint,
--     value_category varchar(255),
--     fk_unit_id bigint,
--     value_count int,
    value_quantity numeric(19, 2),
    value_text varchar(255),
--     value_xml varchar,
--     value_json varchar,
--     value_temporal_from timestamp,
--     value_temporal_to timestamp,
--     fk_parent_parameter_id bigint,
--     foreign key (fk_unit_id) references unit (unit_id),
    --     foreign key (fk_observation_id) references observation (observation_id),
--     foreign key (fk_parent_parameter_id) references observation_parameter (parameter_id),
    constraint observation_parameter_pkey primary key (parameter_id)
    --     constraint observation_parameter_type_check check (cast(type as varchar) = cast(array[
--                                                                                     cast('bool' as varchar),
--     cast('category' as varchar),
--     cast('count' as varchar),
--     cast('quantity' as varchar),
--     cast('text' as varchar),
--     cast('xml' as varchar),
--     cast('json' as varchar),
--     cast('complex' as varchar),
--     cast('temporal' as varchar)
--     ] as array))
    );

-- alter table observation
--     add constraint
--         foreign key (fk_parent_observation_id)
--             references observation (observation_id);
alter table observation_parameter
    add constraint
        foreign key (fk_observation_id)
            references observation (observation_id);
-- alter table observation_parameter
--     add constraint
--         foreign key (fk_parent_parameter_id)
--             references observation_parameter (parameter_id);
alter table dataset
    add constraint
        foreign key (fk_aggregation_id)
            references dataset (dataset_id);
-- alter table dataset
--     add constraint
--         foreign key (fk_first_observation_id)
--             references observation (observation_id);
-- alter table dataset
--     add constraint
--         foreign key (fk_last_observation_id)
--             references observation (observation_id);
-- alter table dataset_parameter
--     add constraint foreign key (fk_parent_parameter_id)
--         references dataset_parameter (parameter_id);